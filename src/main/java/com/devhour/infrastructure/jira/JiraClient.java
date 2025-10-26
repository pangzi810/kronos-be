package com.devhour.infrastructure.jira;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.devhour.config.JiraConfiguration;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA REST API通信クライアント
 *
 * JIRA API v2/v3との通信を担当し、認証、エラーハンドリング、
 * リトライ機能を提供する。
 *
 * jira.integration.enabled=true の場合のみ有効化される
 */
@Component
@ConditionalOnProperty(name = "jira.integration.enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(name = "org.apache.hc.client5.http.impl.classic.HttpClientBuilder")
@Slf4j
public class JiraClient {
    
    private final RestTemplate jiraRestTemplate;
    private final JiraConfiguration jiraConfiguration;
    private final ObjectMapper objectMapper;
    
    /**
     * JiraClientのコンストラクタ
     * 
     * @param jiraRestTemplate JIRA API通信用のRestTemplate
     * @param jiraConfiguration JIRA設定
     */
    public JiraClient(@Qualifier("jiraRestTemplate") RestTemplate jiraRestTemplate, 
                     JiraConfiguration jiraConfiguration) {
        this.jiraRestTemplate = jiraRestTemplate;
        this.jiraConfiguration = jiraConfiguration;
        this.objectMapper = new ObjectMapper();
        
        validateConfiguration();
    }
    
    /**
     * JQLクエリを実行してプロジェクト情報を取得
     * 
     * @param jqlQuery JQLクエリ文字列
     * @param maxResults 最大取得件数（デフォルト: 50）
     * @param startAt 取得開始位置（デフォルト: 0）
     * @return JIRA検索結果
     * @throws JiraClientException JIRA通信エラーの場合
     */
    public JiraIssueSearchResponse searchIssues(String jqlQuery, Integer maxResults, Integer startAt) {
        log.info("JQLクエリ実行開始: query={}, maxResults={}, startAt={}", 
                jqlQuery, maxResults, startAt);
        
        if (!jiraConfiguration.isConfigured()) {
            throw new JiraClientException("JIRA APIトークンが設定されていません。JIRA統合機能を利用できません。");
        }
        validateSearchParameters(jqlQuery, maxResults, startAt);
        
        try {
            String url = buildSearchUrl(jqlQuery, maxResults, startAt);
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            
            ResponseEntity<String> response = jiraRestTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            JiraIssueSearchResponse searchResponse = objectMapper.readValue(
                response.getBody(), JiraIssueSearchResponse.class);
            
            log.info("JQLクエリ実行完了: total={}, returned={}", 
                    searchResponse.getIssues().size(), searchResponse.getIssues().size());
            
            return searchResponse;
            
        } catch (RestClientException e) {
            log.error("JIRA API通信エラー: query={}", jqlQuery, e);
            throw new JiraClientException("Failed to search issues: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            log.error("JIRAレスポンスのパースエラー: query={}", jqlQuery, e);
            throw new JiraClientException("Failed to parse JIRA response", e);
        }
    }

    /**
     * 特定のイシューの詳細情報を取得
     *
     * @param issueKey イシューキー（例: PROJ-123）
     * @return イシュー詳細情報
     * @throws JiraClientException JIRA通信エラーの場合
     */
    public JsonNode getIssueDetails(String issueKey) {
        log.info("イシュー詳細取得開始: issueKey={}", issueKey);
        
        if (issueKey == null || issueKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue key is required");
        }
        
        if (!jiraConfiguration.isConfigured()) {
            throw new JiraClientException("JIRA APIトークンが設定されていません。JIRA統合機能を利用できません。");
        }
        
        try {
            String url = buildIssueUrl(issueKey);
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            
            ResponseEntity<String> response = jiraRestTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            JsonNode issueDetails = objectMapper.readTree(response.getBody());
            
            log.info("イシュー詳細取得完了: issueKey={}", issueKey);
            
            return issueDetails;
            
        } catch (RestClientException e) {
            log.error("JIRA API通信エラー: issueKey={}", issueKey, e);
            throw new JiraClientException("Failed to get issue details: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            log.error("JIRAレスポンスのパースエラー: issueKey={}", issueKey, e);
            throw new JiraClientException("Failed to parse JIRA response", e);
        }
    }
    
    /**
     * JIRA接続状態を確認
     * 
     * @return 接続可能な場合true
     */
    public boolean testConnection() {
        log.info("JIRA接続テスト開始");
        
        try {
            if (!jiraConfiguration.isConfigured()) {
            throw new JiraClientException("JIRA APIトークンが設定されていません。JIRA統合機能を利用できません。");
        }
            
            String url = buildServerInfoUrl();
            HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders());
            
            ResponseEntity<String> response = jiraRestTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            boolean isSuccess = response.getStatusCode().is2xxSuccessful();
            log.info("JIRA接続テスト完了: success={}", isSuccess);
            
            return isSuccess;
            
        } catch (Exception e) {
            log.warn("JIRA接続テスト失敗", e);
            return false;
        }
    }
    
    /**
     * Bearer Token認証用のHTTPヘッダーを作成
     *
     * @return 認証ヘッダーを含むHttpHeaders
     */
    private HttpHeaders createAuthHeaders() {
        String token = jiraConfiguration.getAuth().getToken();

        // Bearer token authentication (for JIRA Cloud API Token)
        String authHeader = "Bearer " + token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authHeader);
        headers.set("User-Agent", "Java Application/1.0");

        return headers;
    }
    
    /**
     * 検索用URLを構築
     * 
     * @param jqlQuery JQLクエリ
     * @param maxResults 最大取得件数
     * @param startAt 開始位置
     * @return 構築されたURL
     */
    private String buildSearchUrl(String jqlQuery, Integer maxResults, Integer startAt) {
        log.info("元のJQLクエリ: '{}'", jqlQuery);
        
        // UriComponentsBuilderを使用して適切なURLエンコーディングを行う（エンコードなし）
        String url = UriComponentsBuilder
            .fromUriString(jiraConfiguration.getFullApiUrl("/rest/api/2/search"))
            .queryParam("jql", jqlQuery)
            .queryParam("maxResults", maxResults != null ? maxResults : 50)
            .queryParam("startAt", startAt != null ? startAt : 0)
            .queryParam("expand", "renderedFields,names,schema,operations,editmeta,changelog")
            .queryParam("fields", "*all,-comment,-attachment,-worklog")
            .build(false)  // エンコードを無効にする
            .toUriString();
        
        log.info("構築された最終URL: '{}'", url);
        return url;
    }
    
    /**
     * イシュー詳細用URLを構築
     * 
     * @param issueKey イシューキー
     * @return 構築されたURL
     */
    private String buildIssueUrl(String issueKey) {
        return jiraConfiguration.getFullApiUrl("/rest/api/2/issue/" + issueKey);
    }
    
    /**
     * サーバー情報用URLを構築
     * 
     * @return 構築されたURL
     */
    private String buildServerInfoUrl() {
        return jiraConfiguration.getFullApiUrl("/rest/api/2/serverInfo");
    }
    
    /**
     * 検索パラメータの検証
     * 
     * @param jqlQuery JQLクエリ
     * @param maxResults 最大取得件数
     * @param startAt 開始位置
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    private void validateSearchParameters(String jqlQuery, Integer maxResults, Integer startAt) {
        if (jqlQuery == null || jqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("JQL query is required");
        }
        
        if (maxResults != null && maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be greater than 0");
        }
    }
    
    /**
     * JIRA設定の検証
     * 
     * @throws JiraClientException 設定が不正な場合
     */
    private void validateConfiguration() {
        if (!jiraConfiguration.isConfigured()) {
            log.warn("JIRA APIトークンが設定されていません。JIRA統合機能は利用できません。");
            log.info("JIRA統合機能を有効にするには、環境変数 JIRA_API_TOKEN を設定してください。");
        }
    }
    
    /**
     * JiraClient専用の例外クラス
     * 
     * JIRA API通信エラーやレスポンスパースエラーを表現する
     */
    public static class JiraClientException extends RuntimeException {
        
        /**
         * メッセージを指定してJiraClientExceptionを作成
         * 
         * @param message エラーメッセージ
         */
        public JiraClientException(String message) {
            super(message);
        }
        
        /**
         * メッセージと原因を指定してJiraClientExceptionを作成
         * 
         * @param message エラーメッセージ
         * @param cause 例外の原因
         */
        public JiraClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}