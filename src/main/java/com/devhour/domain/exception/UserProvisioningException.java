package com.devhour.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * ユーザープロビジョニング例外
 * 
 * Oktaからのユーザープロビジョニング処理中にエラーが発生した場合にスローされる例外
 * システムエラーとして扱われ、HTTP 500 Internal Server Errorステータスにマップされる
 * 
 * 使用例:
 * - Oktaユーザー情報の取得エラー
 * - ユーザー同期処理の失敗
 * - データベースへのユーザー保存エラー
 * - Oktaユーザー重複エラー
 * - プロビジョニング権限エラー
 */
public class UserProvisioningException extends RuntimeException {
    
    private final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private final String oktaUserId;
    private final String errorContext;
    
    /**
     * メッセージ付きユーザープロビジョニング例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public UserProvisioningException(String message) {
        super(message);
        this.oktaUserId = null;
        this.errorContext = null;
    }
    
    /**
     * メッセージと原因付きユーザープロビジョニング例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public UserProvisioningException(String message, Throwable cause) {
        super(message, cause);
        this.oktaUserId = null;
        this.errorContext = null;
    }
    
    /**
     * 詳細情報付きユーザープロビジョニング例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param oktaUserId 関連するOkta User ID
     * @param errorContext エラーコンテキスト
     */
    public UserProvisioningException(String message, String oktaUserId, String errorContext) {
        super(message);
        this.oktaUserId = oktaUserId;
        this.errorContext = errorContext;
    }
    
    /**
     * 詳細情報付きユーザープロビジョニング例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     * @param oktaUserId 関連するOkta User ID
     * @param errorContext エラーコンテキスト
     */
    public UserProvisioningException(String message, Throwable cause, String oktaUserId, String errorContext) {
        super(message, cause);
        this.oktaUserId = oktaUserId;
        this.errorContext = errorContext;
    }
    
    /**
     * このエラーに対応するHTTPステータスコードを取得
     * 
     * @return HTTP 500 Internal Server Error
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * 関連するOkta User IDを取得
     * 
     * @return Okta User ID（nullable）
     */
    public String getOktaUserId() {
        return oktaUserId;
    }
    
    /**
     * エラーコンテキストを取得
     * 
     * @return エラーコンテキスト（nullable）
     */
    public String getErrorContext() {
        return errorContext;
    }
    
    /**
     * ユーザー同期失敗用のファクトリメソッド
     * 
     * @param oktaUserId Okta User ID
     * @param cause 原因となった例外
     * @return UserProvisioningException
     */
    public static UserProvisioningException userSyncFailed(String oktaUserId, Throwable cause) {
        String message = String.format("Failed to sync user from Okta - oktaUserId: %s", oktaUserId);
        return new UserProvisioningException(message, cause, oktaUserId, "USER_SYNC");
    }
    
    /**
     * ユーザー作成失敗用のファクトリメソッド
     * 
     * @param oktaUserId Okta User ID
     * @param email ユーザーメールアドレス
     * @param cause 原因となった例外
     * @return UserProvisioningException
     */
    public static UserProvisioningException userCreationFailed(String oktaUserId, String email, Throwable cause) {
        String message = String.format("Failed to create user - oktaUserId: %s, email: %s", oktaUserId, email);
        return new UserProvisioningException(message, cause, oktaUserId, "USER_CREATION");
    }
    
    /**
     * ユーザー更新失敗用のファクトリメソッド
     * 
     * @param oktaUserId Okta User ID
     * @param userId システムユーザーID
     * @param cause 原因となった例外
     * @return UserProvisioningException
     */
    public static UserProvisioningException userUpdateFailed(String oktaUserId, String userId, Throwable cause) {
        String message = String.format("Failed to update user - oktaUserId: %s, userId: %s", oktaUserId, userId);
        return new UserProvisioningException(message, cause, oktaUserId, "USER_UPDATE");
    }
    
    /**
     * 重複ユーザーエラー用のファクトリメソッド
     * 
     * @param email 重複するメールアドレス
     * @param existingOktaUserId 既存のOkta User ID
     * @param newOktaUserId 新しいOkta User ID
     * @return UserProvisioningException
     */
    public static UserProvisioningException duplicateUser(String email, String existingOktaUserId, String newOktaUserId) {
        String message = String.format(
            "Duplicate user detected - email: %s, existing oktaUserId: %s, new oktaUserId: %s", 
            email, existingOktaUserId, newOktaUserId);
        return new UserProvisioningException(message, newOktaUserId, "DUPLICATE_USER");
    }
    
    /**
     * Oktaユーザー情報取得失敗用のファクトリメソッド
     * 
     * @param oktaUserId Okta User ID
     * @param cause 原因となった例外
     * @return UserProvisioningException
     */
    public static UserProvisioningException oktaUserFetchFailed(String oktaUserId, Throwable cause) {
        String message = String.format("Failed to fetch user from Okta - oktaUserId: %s", oktaUserId);
        return new UserProvisioningException(message, cause, oktaUserId, "OKTA_USER_FETCH");
    }
    
    /**
     * プロビジョニング権限不足用のファクトリメソッド
     * 
     * @param oktaUserId Okta User ID
     * @return UserProvisioningException
     */
    public static UserProvisioningException insufficientProvisioningRights(String oktaUserId) {
        String message = String.format("Insufficient provisioning rights - oktaUserId: %s", oktaUserId);
        return new UserProvisioningException(message, oktaUserId, "INSUFFICIENT_RIGHTS");
    }
}