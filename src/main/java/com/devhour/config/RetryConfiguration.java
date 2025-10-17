package com.devhour.config;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Retryの設定クラス
 * 
 * JIRA同期処理における一時的なエラーに対する
 * リトライ機能を設定する。
 * 
 * REQ-8.1, REQ-8.2, REQ-8.3に対応:
 * - タイムアウトエラーのリトライ（30秒間隔、最大3回）
 * - ネットワークエラーのエクスポネンシャルバックオフ
 * - レート制限に対する適応的待機
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfiguration {
    
    // REQ-8.1: 最大リトライ回数（タイムアウトとネットワークエラー用）
    public static final int MAX_ATTEMPTS = 3;
    
    // REQ-8.1, REQ-8.2: リトライ間隔設定（ミリ秒）
    public static final long INITIAL_INTERVAL = 30_000L;     // 30秒
    public static final long MAX_INTERVAL = 120_000L;        // 2分
    public static final double MULTIPLIER = 2.0;             // エクスポネンシャル倍率
    
    /**
     * JIRA同期用のRetryTemplate
     * 
     * 一般的なネットワークエラーとタイムアウトエラーに対する
     * エクスポネンシャルバックオフを設定したRetryTemplateを提供する。
     * 
     * @return 設定済みのRetryTemplate
     */
    @Bean(name = "jiraSyncRetryTemplate")
    public RetryTemplate jiraSyncRetryTemplate() {
        log.info("JIRA同期用RetryTemplateを設定");
        
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // リトライポリシーの設定
        java.util.Map<Class<? extends Throwable>, Boolean> retryableExceptions = 
            java.util.Map.of(
                // ネットワーク関連エラー（REQ-8.1, REQ-8.2）
                ResourceAccessException.class, true,
                ConnectException.class, true,
                SocketTimeoutException.class, true,
                TimeoutException.class, true,
                
                // HTTP 5xxサーバーエラー（一時的な問題と判断）
                HttpServerErrorException.class, true
            );
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(MAX_ATTEMPTS, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // エクスポネンシャルバックオフポリシーの設定（REQ-8.2）
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_INTERVAL);
        backOffPolicy.setMaxInterval(MAX_INTERVAL);
        backOffPolicy.setMultiplier(MULTIPLIER);
        
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        log.info("RetryTemplate設定完了: maxAttempts={}, initialInterval={}ms, maxInterval={}ms", 
                MAX_ATTEMPTS, INITIAL_INTERVAL, MAX_INTERVAL);
        
        return retryTemplate;
    }
    
    /**
     * リトライ可能な例外かどうかを判定
     * 
     * 認証エラーなど、リトライしても解決しない問題は
     * リトライ対象外として扱う（REQ-8.4）。
     * 
     * @param exception 判定対象の例外
     * @return リトライ可能な場合true
     */
    public static boolean isRetryableException(Throwable exception) {
        // 認証エラー（401）は即座に失敗とする（REQ-8.4）
        if (exception instanceof JiraClientConfiguration.JiraApiException) {
            JiraClientConfiguration.JiraApiException jiraException = 
                (JiraClientConfiguration.JiraApiException) exception;
            int statusCode = jiraException.getStatusCode();
            
            // 401認証エラー、403権限エラーはリトライしない
            if (statusCode == 401 || statusCode == 403) {
                log.warn("認証/権限エラーのためリトライしません: statusCode={}", statusCode);
                return false;
            }
            
            // 429レート制限エラーは特別処理（REQ-8.3）
            if (statusCode == 429) {
                log.info("レート制限エラー検出: statusCode=429");
                return true; // カスタムハンドリングで対処
            }
            
            // その他の4xxクライアントエラーはリトライしない
            if (statusCode >= 400 && statusCode < 500) {
                log.warn("クライアントエラーのためリトライしません: statusCode={}", statusCode);
                return false;
            }
        }
        
        // ネットワーク関連エラーはリトライ対象
        return exception instanceof ResourceAccessException ||
               exception instanceof ConnectException ||
               exception instanceof SocketTimeoutException ||
               exception instanceof TimeoutException ||
               exception instanceof HttpServerErrorException;
    }
}