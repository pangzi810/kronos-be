package com.devhour.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JIRA接続テストレスポンスDTO
 * 
 * JIRA接続テストAPI (/api/jira/connection/test) のレスポンスボディ
 * 接続テストの実行結果とその詳細を含む
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionTestResponse {
    
    /**
     * 接続テスト成功フラグ
     * true: 接続成功, false: 接続失敗
     */
    private boolean success;
    
    /**
     * テスト結果メッセージ
     * 成功時は成功メッセージ、失敗時はエラー内容を含む
     */
    private String message;
    
    /**
     * テスト実行日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime testedAt;
    
    /**
     * エラーコード（失敗時のみ）
     * オプショナルフィールド
     */
    private String errorCode;
    
    /**
     * 詳細なエラー情報（失敗時のみ）
     * オプショナルフィールド
     */
    private String errorDetails;
    
    /**
     * HTTPステータスコード（API呼び出し失敗時のみ）
     * オプショナルフィールド
     */
    private Integer httpStatusCode;
    
    /**
     * 接続成功の結果レスポンスを作成
     * 
     * @return 成功を示すConnectionTestResponse
     */
    public static ConnectionTestResponse success() {
        return ConnectionTestResponse.builder()
                .success(true)
                .message("JIRA接続テストが成功しました")
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 接続成功の結果レスポンスをカスタムメッセージで作成
     * 
     * @param message カスタム成功メッセージ
     * @return 成功を示すConnectionTestResponse
     */
    public static ConnectionTestResponse success(String message) {
        return ConnectionTestResponse.builder()
                .success(true)
                .message(message)
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 接続失敗の結果レスポンスを作成
     * 
     * @param message エラーメッセージ
     * @return 失敗を示すConnectionTestResponse
     */
    public static ConnectionTestResponse failure(String message) {
        return ConnectionTestResponse.builder()
                .success(false)
                .message(message)
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 接続失敗の結果レスポンスを詳細情報付きで作成
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     * @param errorDetails 詳細エラー情報
     * @return 失敗を示すConnectionTestResponse
     */
    public static ConnectionTestResponse failure(String message, String errorCode, String errorDetails) {
        return ConnectionTestResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * HTTP応答エラーの結果レスポンスを作成
     * 
     * @param message エラーメッセージ
     * @param httpStatusCode HTTPステータスコード
     * @return 失敗を示すConnectionTestResponse
     */
    public static ConnectionTestResponse httpError(String message, int httpStatusCode) {
        return ConnectionTestResponse.builder()
                .success(false)
                .message(message)
                .httpStatusCode(httpStatusCode)
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 認証エラーの結果レスポンスを作成
     * 
     * @param message 認証エラーメッセージ
     * @param httpStatusCode HTTPステータスコード
     * @return 認証失敗を示すConnectionTestResponse
     */
    public static ConnectionTestResponse authenticationError(String message, int httpStatusCode) {
        return ConnectionTestResponse.builder()
                .success(false)
                .message("JIRA認証に失敗しました: " + message)
                .errorCode("AUTHENTICATION_FAILED")
                .httpStatusCode(httpStatusCode)
                .testedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 接続エラーの結果レスポンスを作成
     * 
     * @param message 接続エラーメッセージ
     * @return 接続失敗を示すConnectionTestResponse
     */
    public static ConnectionTestResponse connectionError(String message) {
        return ConnectionTestResponse.builder()
                .success(false)
                .message("JIRA接続エラー: " + message)
                .errorCode("CONNECTION_FAILED")
                .testedAt(LocalDateTime.now())
                .build();
    }
}