package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ユーザーエンティティテスト
 */
@DisplayName("ユーザーエンティティ")
class UserTest {

    @Test
    @DisplayName("ユーザー作成 - 正常ケース")
    void create_Success() {
        // Act
        User user = User.create(
            "test_user",
            "test@example.com",
            "山田太郎"
        );

        // Assert
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("test_user");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFullName()).isEqualTo("山田太郎");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ユーザー作成 - emailがnullの場合は例外")
    void create_NullEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            null,
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは必須です");
    }

    @Test
    @DisplayName("ユーザー作成 - emailが空文字の場合は例外")
    void create_EmptyEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは必須です");
    }

    @Test
    @DisplayName("ユーザー作成 - emailが不正形式の場合は例外")
    void create_InvalidEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "invalid-email",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスの形式が正しくありません");
    }

    @Test
    @DisplayName("ユーザー作成 - ユーザー名が短すぎる場合は例外")
    void create_TooShortUsername_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "ab",  // 2文字（3文字未満）
            "user@example.com",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザー名は3-50文字で入力してください");
    }

    @Test
    @DisplayName("ユーザー作成 - ユーザー名が不正文字を含む場合は例外")
    void create_InvalidUsername_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "user@name",  // @ を含む
            "user@example.com",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザー名は英数字、ハイフン、アンダースコアのみ使用可能です");
    }

    @Test
    @DisplayName("ユーザー作成 - フルネームがnullの場合は例外")
    void create_NullFullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "user@example.com",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("フルネームは必須です");
    }

    @Test
    @DisplayName("ユーザー作成 - フルネームが空文字の場合は例外")
    void create_EmptyFullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "user@example.com",
            ""
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("フルネームは必須です");
    }

    @Test
    @DisplayName("ユーザー作成 - フルネームが長すぎる場合は例外")
    void create_TooLongFullName_ThrowsException() {
        // 256文字の名前
        String longName = "a".repeat(256);
        
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "user@example.com",
            longName
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("フルネームは255文字以内で入力してください");
    }

    @Test
    @DisplayName("ユーザー情報更新 - 正常ケース")
    void updateUserInfo_Success() {
        // Arrange
        User user = User.create(
            "test_user",
            "old@example.com",
            "旧名前"
        );

        // Act
        user.updateUserInfo("new@example.com", "新名前");

        // Assert
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getFullName()).isEqualTo("新名前");
    }

    @Test
    @DisplayName("ユーザー非活性化")
    void deactivate_Success() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "山田太郎"
        );

        // Act
        user.deactivate();

        // Assert
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("ユーザー再活性化")
    void activate_Success() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "山田太郎"
        );
        user.deactivate();

        // Act
        user.activate();

        // Assert
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("等価性判定 - 同じIDの場合")
    void equals_SameId_ReturnsTrue() {
        // Arrange
        User user1 = User.restore(
            "same-id",
            "username1",
            "user1@example.com",
            "ユーザー1",
            true,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );

        User user2 = User.restore(
            "same-id",
            "username2",
            "user2@example.com", // 他の項目が違っても
            "ユーザー2",
            false,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なるIDの場合")
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        User user1 = User.restore(
            "id-1",
            "username",
            "user@example.com",
            "ユーザー",
            true,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );

        User user2 = User.restore(
            "id-2",
            "username",
            "user@example.com", // 内容が全く同じでも
            "ユーザー",
            true,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("ユーザー作成 - ユーザー名が51文字の場合は例外")
    void create_UsernameTooLong_ThrowsException() {
        // 51文字のユーザー名
        String longUsername = "a".repeat(51);
        
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            longUsername,
            "user@example.com",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザー名は3-50文字で入力してください");
    }

    @Test
    @DisplayName("ユーザー作成 - ユーザー名が2文字の場合は例外")
    void create_UsernameTooShort_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "ab",  // 2文字
            "user@example.com",
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザー名は3-50文字で入力してください");
    }

    @Test
    @DisplayName("ユーザー作成 - メールアドレスが256文字の場合は例外")
    void create_EmailTooLong_ThrowsException() {
        // 256文字のメールアドレス（@より前を長くする）
        String longEmail = "a".repeat(244) + "@example.com";  // 244 + 12 = 256文字
        
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            longEmail,
            "山田太郎"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは255文字以内で入力してください");
    }

    @Test
    @DisplayName("ユーザー情報更新 - 非活性ユーザーは更新不可")
    void updateUserInfo_InactiveUser_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "old@example.com",
            "旧名前"
        );
        user.deactivate();

        // Act & Assert
        assertThatThrownBy(() -> user.updateUserInfo(
            "new@example.com",
            "新名前"
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("無効化されたユーザーは更新できません");
    }

    @Test
    @DisplayName("ユーザー情報更新 - メールアドレスが256文字の場合は例外")
    void updateUserInfo_EmailTooLong_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "old@example.com",
            "旧名前"
        );

        // 256文字のメールアドレス
        String longEmail = "a".repeat(244) + "@example.com";  // 244 + 12 = 256文字

        // Act & Assert
        assertThatThrownBy(() -> user.updateUserInfo(
            longEmail,
            "新名前"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは255文字以内で入力してください");
    }

    @Test
    @DisplayName("ユーザー情報更新 - フルネームが256文字の場合は例外")
    void updateUserInfo_FullNameTooLong_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "old@example.com",
            "旧名前"
        );

        // 256文字のフルネーム
        String longName = "a".repeat(256);

        // Act & Assert
        assertThatThrownBy(() -> user.updateUserInfo(
            "new@example.com",
            longName
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("フルネームは255文字以内で入力してください");
    }

    @Test
    @DisplayName("ユーザー名境界値テスト - 3文字は作成可能")
    void create_ExactlyThreeCharacterUsername_Success() {
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> User.create(
            "abc",  // ちょうど3文字
            "user@example.com",
            "山田太郎"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ユーザー名境界値テスト - 50文字は作成可能")
    void create_ExactlyFiftyCharacterUsername_Success() {
        // 50文字のユーザー名
        String username = "a".repeat(50);
        
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> User.create(
            username,  // ちょうど50文字
            "user@example.com",
            "山田太郎"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("メールアドレス境界値テスト - 255文字は作成可能")
    void create_ExactlyTwoHundredFiftyFiveCharacterEmail_Success() {
        // 255文字のメールアドレス
        String email = "a".repeat(239) + "@example.com";  // 合計255文字
        
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> User.create(
            "test_user",
            email,  // ちょうど255文字
            "山田太郎"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("フルネーム境界値テスト - 255文字は作成可能")
    void create_ExactlyTwoHundredFiftyFiveCharacterFullName_Success() {
        // 255文字のフルネーム
        String fullName = "a".repeat(255);
        
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> User.create(
            "test_user",
            "user@example.com",
            fullName  // ちょうど255文字
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("toString() メソッドのテスト")
    void toString_ReturnsFormattedString() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act
        String result = user.toString();

        // Assert
        assertThat(result).contains("User{");
        assertThat(result).contains("id=");
        assertThat(result).contains("username='test_user'");
    }

    @Test
    @DisplayName("ユーザー作成 - ユーザー名がnullの場合は例外")
    void create_NullUsername_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            null,
            "user@example.com",
            "テストユーザー"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザー名は必須です");
    }

    @Test
    @DisplayName("ユーザー作成 - メールアドレス形式不正の場合は例外")
    void create_InvalidEmailFormat_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> User.create(
            "test_user",
            "invalid_email",  // @がない不正な形式
            "テストユーザー"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスの形式が正しくありません");
    }

    @Test
    @DisplayName("ユーザー情報更新 - メールアドレスがnullの場合は例外")
    void updateUserInfo_NullEmail_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act & Assert
        assertThatThrownBy(() -> user.updateUserInfo(null, "新しい名前"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("メールアドレスは必須です");
    }

    @Test
    @DisplayName("ユーザー情報更新 - フルネームがnullの場合は例外")
    void updateUserInfo_NullFullName_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act & Assert
        assertThatThrownBy(() -> user.updateUserInfo("new@example.com", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("フルネームは必須です");
    }

    @Test
    @DisplayName("deactivate - 既に無効化されている場合は何もしない")
    void deactivate_AlreadyInactive_DoesNothing() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );
        user.deactivate();
        java.time.LocalDateTime updateTimeAfterFirstDeactivate = user.getUpdatedAt();

        // Act - 2回目の無効化を試みる
        user.deactivate();

        // Assert - 更新時刻が変わらない
        assertThat(user.isActive()).isFalse();
        assertThat(user.getUpdatedAt()).isEqualTo(updateTimeAfterFirstDeactivate);
    }

    @Test
    @DisplayName("lastLoginAtフィールドのget/set")
    void getLastLoginAt_SetAndGet_Success() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );
        java.time.LocalDateTime loginTime = java.time.LocalDateTime.now();

        // Act
        user.updateLastLoginAt(loginTime);

        // Assert
        assertThat(user.getLastLoginAt()).isEqualTo(loginTime);
    }

    @Test
    @DisplayName("UserStatus - 初期状態はACTIVE")
    void userStatus_InitialState_IsActive() {
        // Arrange & Act
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Assert
        assertThat(user.getUserStatus()).isEqualTo(User.UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("UserStatus - activateUser()によりACTIVEに遷移")
    void activateUser_FromInactive_TransitionsToActive() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );
        user.deactivateUser(); // まずINACTIVEにする

        // Act
        user.activateUser();

        // Assert
        assertThat(user.getUserStatus()).isEqualTo(User.UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("UserStatus - deactivateUser()によりINACTIVEに遷移")
    void deactivateUser_FromActive_TransitionsToInactive() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act
        user.deactivateUser();

        // Assert
        assertThat(user.getUserStatus()).isEqualTo(User.UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("UserStatus - suspendUser()によりSUSPENDEDに遷移")
    void suspendUser_FromActive_TransitionsToSuspended() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act
        user.suspendUser();

        // Assert
        assertThat(user.getUserStatus()).isEqualTo(User.UserStatus.SUSPENDED);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("UserStatus - SUSPENDEDからactivateUser()でACTIVEに遷移")
    void activateUser_FromSuspended_TransitionsToActive() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );
        user.suspendUser(); // まずSUSPENDEDにする

        // Act
        user.activateUser();

        // Assert
        assertThat(user.getUserStatus()).isEqualTo(User.UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("updateLastLoginAt - 正常ケース")
    void updateLastLoginAt_Success() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );
        java.time.LocalDateTime loginTime = java.time.LocalDateTime.now();

        // Act
        user.updateLastLoginAt(loginTime);

        // Assert
        assertThat(user.getLastLoginAt()).isEqualTo(loginTime);
    }

    @Test
    @DisplayName("updateLastLoginAt - nullの場合は例外")
    void updateLastLoginAt_NullTime_ThrowsException() {
        // Arrange
        User user = User.create(
            "test_user",
            "user@example.com",
            "テストユーザー"
        );

        // Act & Assert
        assertThatThrownBy(() -> user.updateLastLoginAt(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ログイン時刻は必須です");
    }
}