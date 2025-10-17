package com.devhour.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * UserProvisioningExceptionのテストクラス
 */
@DisplayName("UserProvisioningException")
class UserProvisioningExceptionTest {

    @Test
    @DisplayName("基本コンストラクタ - メッセージのみ")
    void constructor_MessageOnly_CreatesExceptionWithMessage() {
        // Given
        String message = "User provisioning failed";
        
        // When
        UserProvisioningException exception = new UserProvisioningException(message);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getOktaUserId()).isNull();
        assertThat(exception.getErrorContext()).isNull();
    }

    @Test
    @DisplayName("コンストラクタ - メッセージと原因")
    void constructor_MessageAndCause_CreatesExceptionWithMessageAndCause() {
        // Given
        String message = "User provisioning failed";
        RuntimeException cause = new RuntimeException("Database error");
        
        // When
        UserProvisioningException exception = new UserProvisioningException(message, cause);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getOktaUserId()).isNull();
        assertThat(exception.getErrorContext()).isNull();
    }

    @Test
    @DisplayName("詳細情報付きコンストラクタ")
    void constructor_WithDetails_CreatesExceptionWithAllDetails() {
        // Given
        String message = "User provisioning failed";
        String oktaUserId = "okta-user-123";
        String errorContext = "USER_CREATION";
        
        // When
        UserProvisioningException exception = new UserProvisioningException(message, oktaUserId, errorContext);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo(errorContext);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("詳細情報・原因付きコンストラクタ")
    void constructor_WithDetailsAndCause_CreatesFullException() {
        // Given
        String message = "User provisioning failed";
        RuntimeException cause = new RuntimeException("Database error");
        String oktaUserId = "okta-user-123";
        String errorContext = "USER_CREATION";
        
        // When
        UserProvisioningException exception = new UserProvisioningException(message, cause, oktaUserId, errorContext);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo(errorContext);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("ユーザー同期失敗ファクトリメソッド")
    void userSyncFailed_CreatesExceptionWithSyncContext() {
        // Given
        String oktaUserId = "okta-user-123";
        RuntimeException cause = new RuntimeException("Network error");
        
        // When
        UserProvisioningException exception = UserProvisioningException.userSyncFailed(oktaUserId, cause);
        
        // Then
        assertThat(exception.getMessage()).contains("Failed to sync user from Okta");
        assertThat(exception.getMessage()).contains(oktaUserId);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("USER_SYNC");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("ユーザー作成失敗ファクトリメソッド")
    void userCreationFailed_CreatesExceptionWithCreationContext() {
        // Given
        String oktaUserId = "okta-user-123";
        String email = "test@example.com";
        RuntimeException cause = new RuntimeException("Database constraint violation");
        
        // When
        UserProvisioningException exception = UserProvisioningException.userCreationFailed(oktaUserId, email, cause);
        
        // Then
        assertThat(exception.getMessage()).contains("Failed to create user");
        assertThat(exception.getMessage()).contains(oktaUserId);
        assertThat(exception.getMessage()).contains(email);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("USER_CREATION");
    }

    @Test
    @DisplayName("ユーザー更新失敗ファクトリメソッド")
    void userUpdateFailed_CreatesExceptionWithUpdateContext() {
        // Given
        String oktaUserId = "okta-user-123";
        String userId = "user-456";
        RuntimeException cause = new RuntimeException("Optimistic locking failure");
        
        // When
        UserProvisioningException exception = UserProvisioningException.userUpdateFailed(oktaUserId, userId, cause);
        
        // Then
        assertThat(exception.getMessage()).contains("Failed to update user");
        assertThat(exception.getMessage()).contains(oktaUserId);
        assertThat(exception.getMessage()).contains(userId);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("USER_UPDATE");
    }

    @Test
    @DisplayName("重複ユーザーファクトリメソッド")
    void duplicateUser_CreatesExceptionWithDuplicateContext() {
        // Given
        String email = "duplicate@example.com";
        String existingOktaUserId = "existing-user-123";
        String newOktaUserId = "new-user-456";
        
        // When
        UserProvisioningException exception = UserProvisioningException.duplicateUser(
            email, existingOktaUserId, newOktaUserId);
        
        // Then
        assertThat(exception.getMessage()).contains("Duplicate user detected");
        assertThat(exception.getMessage()).contains(email);
        assertThat(exception.getMessage()).contains(existingOktaUserId);
        assertThat(exception.getMessage()).contains(newOktaUserId);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getOktaUserId()).isEqualTo(newOktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("DUPLICATE_USER");
    }

    @Test
    @DisplayName("Oktaユーザー情報取得失敗ファクトリメソッド")
    void oktaUserFetchFailed_CreatesExceptionWithFetchContext() {
        // Given
        String oktaUserId = "okta-user-123";
        RuntimeException cause = new RuntimeException("HTTP 500 Internal Server Error");
        
        // When
        UserProvisioningException exception = UserProvisioningException.oktaUserFetchFailed(oktaUserId, cause);
        
        // Then
        assertThat(exception.getMessage()).contains("Failed to fetch user from Okta");
        assertThat(exception.getMessage()).contains(oktaUserId);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("OKTA_USER_FETCH");
    }

    @Test
    @DisplayName("プロビジョニング権限不足ファクトリメソッド")
    void insufficientProvisioningRights_CreatesExceptionWithRightsContext() {
        // Given
        String oktaUserId = "okta-user-123";
        
        // When
        UserProvisioningException exception = UserProvisioningException.insufficientProvisioningRights(oktaUserId);
        
        // Then
        assertThat(exception.getMessage()).contains("Insufficient provisioning rights");
        assertThat(exception.getMessage()).contains(oktaUserId);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getOktaUserId()).isEqualTo(oktaUserId);
        assertThat(exception.getErrorContext()).isEqualTo("INSUFFICIENT_RIGHTS");
    }

    @Test
    @DisplayName("HTTPステータスは常に500")
    void getHttpStatus_AlwaysReturns500() {
        // Given/When/Then - 各種ファクトリメソッドでも500が返される
        assertThat(new UserProvisioningException("test").getHttpStatus())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        assertThat(UserProvisioningException.userSyncFailed("user", new RuntimeException()).getHttpStatus())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        assertThat(UserProvisioningException.duplicateUser("email", "old", "new").getHttpStatus())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        assertThat(UserProvisioningException.insufficientProvisioningRights("user").getHttpStatus())
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}