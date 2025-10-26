package com.devhour.config;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA API統合設定クラス
 * 
 * JIRA APIへの接続設定、認証設定、タイムアウト設定、リトライ設定を管理する。
 * 環境変数を通じて認証情報を安全に取得し、設定値の検証を行う。
 */
@Data
@ConfigurationProperties("jira")
@Slf4j
public class JiraConfiguration {

    /**
     * JIRA統合機能の有効/無効を制御するマスタースイッチ
     */
    private IntegrationConfig integration = new IntegrationConfig();

    /**
     * JIRA サーバーのベースURL
     * 例: "https://company.atlassian.net"
     */
    @NotBlank(message = "JIRA Base URLは必須です")
    private String baseUrl;
    
    /**
     * JIRA API のバージョン
     * デフォルト: "2" (v2 API)
     */
    private String apiVersion = "2";
    
    /**
     * タイムアウト設定
     */
    @Valid
    private TimeoutConfig timeout = new TimeoutConfig();
    
    /**
     * リトライ設定
     */
    @Valid
    private RetryConfig retry = new RetryConfig();
    
    /**
     * 認証設定
     */
    @Valid
    private AuthConfig auth = new AuthConfig();

    /**
     * JIRA統合機能設定の内部クラス
     */
    @Data
    public static class IntegrationConfig {
        /**
         * JIRA統合機能の有効/無効
         * デフォルト: false (無効)
         */
        private boolean enabled = false;
    }

    /**
     * タイムアウト設定の内部クラス
     */
    @Data
    public static class TimeoutConfig {
        /**
         * 接続タイムアウト（ミリ秒）
         */
        @Min(value = 1, message = "接続タイムアウトは正の値である必要があります")
        private int connection = 30000; // 30秒
        
        /**
         * 読み取りタイムアウト（ミリ秒）
         */
        @Min(value = 1, message = "読み取りタイムアウトは正の値である必要があります")
        private int read = 60000; // 60秒
    }
    
    /**
     * リトライ設定の内部クラス
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大リトライ試行回数
         */
        @Min(value = 1, message = "最大リトライ試行回数は1以上である必要があります")
        private int maxAttempts = 3;
        
        /**
         * バックオフ倍率
         */
        @DecimalMin(value = "0.1", message = "バックオフ倍率は正の値である必要があります")
        private double backoffMultiplier = 2.0;
    }
    
    /**
     * 認証設定の内部クラス
     *
     * Spring Bootのプロパティバインディングにより、
     * jira.auth.token=${JIRA_API_TOKEN} として環境変数から自動取得
     */
    @Data
    public static class AuthConfig {
        /**
         * JIRA認証用APIトークン（必須）
         * 環境変数 JIRA_API_TOKEN から自動マッピング
         */
        @NotBlank(message = "APIトークンは必須です")
        private String token;
    }
    
    /**
     * JIRA設定が完全に構成されているかをチェック
     * 
     * @return 必須設定が全て設定されている場合true
     */
    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.trim().isEmpty() 
               && isAuthenticationConfigured();
    }
    
    
    /**
     * 完全なAPI URLを構築
     * 
     * @param endpoint APIエンドポイント（例: "/rest/api/2/project"）
     * @return 完全なAPI URL
     */
    public String getFullApiUrl(String endpoint) {
        if (baseUrl == null) {
            throw new IllegalStateException("Base URLが設定されていません");
        }
        
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        
        return cleanBaseUrl + cleanEndpoint;
    }
    
    /**
     * 設定値の包括的な検証を実行
     * 
     * @throws IllegalArgumentException 設定値が無効な場合
     */
    public void validateConfiguration() {
        // Base URL検証
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("JIRA Base URLが設定されていません");
        }
        
        try {
            URL url = new URL(baseUrl);
            if (!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) {
                throw new IllegalArgumentException("無効なJIRA Base URL: HTTPまたはHTTPS URLである必要があります");
            }
            
            // 本番環境でHTTPSを推奨（警告レベル）
            if ("http".equals(url.getProtocol())) {
                log.warn("セキュリティのためHTTPS URLの使用を推奨します: {}", baseUrl);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("無効なJIRA Base URL: " + baseUrl, e);
        }
        
        // 認証設定検証（APIトークンのみ）
        if (!isAuthenticationConfigured()) {
            throw new IllegalArgumentException("JIRA APIトークンが設定されていません。"
                + "環境変数 JIRA_API_TOKEN を設定するか、"
                + "application.properties で jira.auth.token を設定してください");
        }
    }
    
    /**
     * 認証設定が構成されているかをチェック
     *
     * @return APIトークンが設定されている場合true
     */
    public boolean isAuthenticationConfigured() {
        String token = auth.getToken();

        // APIトークンのみで認証可能
        return token != null && !token.trim().isEmpty();
    }
    
    /**
     * toString実装（認証情報を含まない安全な文字列表現）
     * 
     * @return 認証情報を隠した文字列表現
     */
    @Override
    public String toString() {
        return "JiraConfiguration{" +
                "baseUrl='" + baseUrl + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", timeout=" + timeout +
                ", retry=" + retry +
                ", auth=***" +
                '}';
    }
}