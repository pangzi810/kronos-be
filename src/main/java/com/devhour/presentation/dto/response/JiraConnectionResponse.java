package com.devhour.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JIRA接続設定レスポンスDTO
 * 
 * JIRA接続設定取得API (/api/jira/connection) のレスポンスボディ
 * 接続設定情報と設定完了状態を含む
 * 
 * セキュリティ考慮:
 * - 実際の認証情報（APIトークン、パスワード）は含めない
 * - 環境変数キー名のみを返す
 * - 設定完了状態のみを通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JiraConnectionResponse {
    
    /**
     * JIRA Base URL
     * 例: "https://company.atlassian.net"
     */
    private String jiraUrl;
    
    /**
     * APIトークン環境変数キー名
     * 例: "JIRA_API_TOKEN"
     * 実際のトークン値は含まれない
     */
    private String tokenEnvKey;
    
    /**
     * ユーザー名環境変数キー名
     * 例: "JIRA_USERNAME"
     * 実際のユーザー名は含まれない
     */
    private String usernameEnvKey;
    
    /**
     * 設定完了フラグ
     * 必要な環境変数がすべて設定されているかを示す
     */
    @JsonProperty("isConfigured")
    private boolean isConfigured;
    
    /**
     * 最後の接続テスト結果
     * オプショナルフィールド
     */
    private String lastTestResult;
    
    /**
     * 最後の接続テスト実行日時
     * オプショナルフィールド
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastTestedAt;
    
    /**
     * 設定完了のJIRA接続設定レスポンスを作成
     * 
     * @param jiraUrl JIRA Base URL
     * @param tokenEnvKey APIトークン環境変数キー名
     * @param usernameEnvKey ユーザー名環境変数キー名
     * @param isConfigured 設定完了フラグ
     * @return JiraConnectionResponse インスタンス
     */
    public static JiraConnectionResponse of(String jiraUrl, String tokenEnvKey, 
                                          String usernameEnvKey, boolean isConfigured) {
        return JiraConnectionResponse.builder()
                .jiraUrl(jiraUrl)
                .tokenEnvKey(tokenEnvKey)
                .usernameEnvKey(usernameEnvKey)
                .isConfigured(isConfigured)
                .build();
    }
    
    /**
     * テスト結果を含むJIRA接続設定レスポンスを作成
     * 
     * @param jiraUrl JIRA Base URL
     * @param tokenEnvKey APIトークン環境変数キー名
     * @param usernameEnvKey ユーザー名環境変数キー名
     * @param isConfigured 設定完了フラグ
     * @param lastTestResult 最後のテスト結果
     * @param lastTestedAt 最後のテスト実行日時
     * @return JiraConnectionResponse インスタンス
     */
    public static JiraConnectionResponse withTestResult(String jiraUrl, String tokenEnvKey, 
                                                       String usernameEnvKey, boolean isConfigured,
                                                       String lastTestResult, LocalDateTime lastTestedAt) {
        return JiraConnectionResponse.builder()
                .jiraUrl(jiraUrl)
                .tokenEnvKey(tokenEnvKey)
                .usernameEnvKey(usernameEnvKey)
                .isConfigured(isConfigured)
                .lastTestResult(lastTestResult)
                .lastTestedAt(lastTestedAt)
                .build();
    }
}