package com.devhour.config;

import com.devhour.domain.exception.JiraAuthenticationException;
import com.devhour.domain.exception.JiraRateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * JIRA API通信用のRestTemplate設定クラス
 * 
 * JIRA API専用のRestTemplateを構成し、タイムアウト設定、
 * 接続プール設定、エラーハンドラーを適用する。
 */
@Configuration
@ConditionalOnClass(HttpClientBuilder.class)
@EnableConfigurationProperties(JiraConfiguration.class)
@Slf4j
public class JiraClientConfiguration {
    
    /**
     * JIRA API通信専用のRestTemplateを作成
     * 
     * @param jiraConfiguration JIRA設定
     * @return 設定済みのRestTemplate
     */
    @Bean
    @ConditionalOnProperty(value = "jira.base-url")
    @Qualifier("jiraRestTemplate")
    public RestTemplate jiraRestTemplate(JiraConfiguration jiraConfiguration) {
        log.info("JIRA RestTemplate設定開始");
        
        RestTemplate restTemplate = new RestTemplate();
        
        // HTTPクライアントの設定
        HttpComponentsClientHttpRequestFactory factory = createHttpRequestFactory(jiraConfiguration);
        restTemplate.setRequestFactory(factory);
        
        // エラーハンドラーの設定
        restTemplate.setErrorHandler(new JiraResponseErrorHandler());
        
        log.info("JIRA RestTemplate設定完了: connection={}ms, read={}ms", 
                jiraConfiguration.getTimeout().getConnection(),
                jiraConfiguration.getTimeout().getRead());
        
        return restTemplate;
    }
    
    /**
     * HTTP接続用のRequestFactoryを作成
     * 
     * @param jiraConfiguration JIRA設定
     * @return 設定済みのHttpComponentsClientHttpRequestFactory
     */
    private HttpComponentsClientHttpRequestFactory createHttpRequestFactory(
            JiraConfiguration jiraConfiguration) {
        
        // 接続プールマネージャーの設定
        PoolingHttpClientConnectionManager connectionManager = 
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20); // 最大接続数
        connectionManager.setDefaultMaxPerRoute(10); // ルートあたりの最大接続数
        
        // HTTPクライアントの構築
        var httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .disableCookieManagement() // Cookieは使用しない
                .build();
        
        // RequestFactoryの設定
        HttpComponentsClientHttpRequestFactory factory = 
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(jiraConfiguration.getTimeout().getConnection());
        
        return factory;
    }
    
    /**
     * JIRA API専用のレスポンスエラーハンドラー
     * 
     * JIRA APIのエラーレスポンスを適切に処理し、
     * わかりやすいエラーメッセージを生成する。
     * レート制限（429）エラーの特別処理を含む。
     */
    public static class JiraResponseErrorHandler implements ResponseErrorHandler {
        
        /**
         * レスポンスがエラーかどうかを判定
         * 
         * @param response HTTPレスポンス
         * @return エラーの場合true
         */
        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) 
                throws java.io.IOException {
            return response.getStatusCode().is4xxClientError() || 
                   response.getStatusCode().is5xxServerError();
        }
        
        /**
         * エラーレスポンスを処理
         * 
         * レート制限（429）エラーの場合はRetry-Afterヘッダーを解析し、
         * 適切な例外を発生させる。
         * 
         * @param response HTTPレスポンス
         * @throws java.io.IOException エラー処理中にIOエラーが発生した場合
         */
        @Override
        public void handleError(org.springframework.http.client.ClientHttpResponse response) 
                throws java.io.IOException {
            
            int statusCode = response.getStatusCode().value();
            
            String body = "";
            try {
                body = new String(response.getBody().readAllBytes(), 
                        java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("エラーレスポンス本文の読み取りに失敗", e);
            }
            
            // レート制限エラー（HTTP 429）の特別処理
            if (statusCode == 429) {
                long retryAfterSeconds = extractRetryAfterHeader(response);
                String errorMessage = String.format("JIRA API rate limit exceeded - retry after %d seconds: %s", 
                        retryAfterSeconds, body);
                
                log.warn("JIRA APIレート制限: retryAfter={}秒, body={}", retryAfterSeconds, body);
                throw new JiraRateLimitException(errorMessage, retryAfterSeconds);
            }
            
            // 認証・権限エラー（HTTP 401, 403）の特別処理
            if (statusCode == 401 || statusCode == 403) {
                String errorMessage = String.format("JIRA API authentication/authorization error: %s - %s", 
                        statusCode, body);
                
                log.error("JIRA API認証/権限エラー: status={}, body={}", statusCode, body);
                throw new JiraAuthenticationException(errorMessage, statusCode);
            }
            
            // その他のエラー
            String errorMessage = String.format("JIRA API error: %s - %s", statusCode, body);
            
            log.error("JIRA APIエラー: status={}, body={}", statusCode, body);
            
            throw new JiraApiException(errorMessage, statusCode);
        }
        
        /**
         * Retry-Afterヘッダーから待機時間を抽出
         * 
         * @param response HTTPレスポンス
         * @return 待機時間（秒）、ヘッダーが存在しない場合は60秒をデフォルト
         */
        private long extractRetryAfterHeader(org.springframework.http.client.ClientHttpResponse response) {
            try {
                String retryAfter = response.getHeaders().getFirst("Retry-After");
                if (retryAfter != null && !retryAfter.trim().isEmpty()) {
                    return Long.parseLong(retryAfter.trim());
                }
            } catch (NumberFormatException e) {
                log.warn("Retry-Afterヘッダーの解析に失敗: {}", e.getMessage());
            }
            
            // デフォルト値として60秒を返す
            return 60L;
        }
    }
    
    /**
     * JIRA API例外クラス
     * 
     * JIRA APIからのエラーレスポンスを表現する専用例外
     */
    public static class JiraApiException extends RuntimeException {
        private final int statusCode;
        
        public JiraApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}