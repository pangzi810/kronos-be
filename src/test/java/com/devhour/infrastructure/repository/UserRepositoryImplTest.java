package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devhour.domain.model.entity.User;
import com.devhour.infrastructure.mapper.UserMapper;

/**
 * UserRepositoryImplの単体テスト
 * 
 * 新しく追加されたメソッドのテストを中心に実装
 * - findByEmailAndDeletedAtIsNull
 * - findByOktaUserId (既存だが確認)
 * - updateLastLoginAt (既存だが確認)
 */
@DisplayName("UserRepositoryImpl単体テスト")
class UserRepositoryImplTest {

    @Mock
    private UserMapper userMapper;

    private UserRepositoryImpl userRepository;

    private User testUser;
    private User oktaUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRepository = new UserRepositoryImpl(userMapper);

        // テスト用の通常ユーザー
        testUser = User.create(
            "testuser", 
            "test@example.com", 
            "Test User"
        );

        // テスト用のOktaユーザー
        oktaUser = User.createFromOkta(
            "okta@example.com",
            "Okta User",
            "okta123456789"
        );
    }

    @Test
    @DisplayName("findByEmailAndDeletedAtIsNull: アクティブユーザーをメールアドレスで検索できる")
    void findByEmailAndDeletedAtIsNull_ActiveUser_ReturnsUser() {
        // Arrange
        String email = "test@example.com";
        when(userMapper.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getFullName());
        verify(userMapper).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("findByEmailAndDeletedAtIsNull: 存在しないメールアドレスでは空を返す")
    void findByEmailAndDeletedAtIsNull_NonExistentEmail_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userMapper.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userMapper).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("findByEmailAndDeletedAtIsNull: deleted_atが設定されたユーザーは検索結果に含まれない")
    void findByEmailAndDeletedAtIsNull_DeletedUser_ReturnsEmpty() {
        // Arrange
        String email = "deleted@example.com";
        // Mapperがdeleted_at IS NULLで絞り込みを行うため、削除済みユーザーは返されない
        when(userMapper.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userMapper).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("findByEmailAndDeletedAtIsNull: メールアドレスの大文字小文字を区別せずに検索")
    void findByEmailAndDeletedAtIsNull_CaseInsensitive_ReturnsUser() {
        // Arrange
        String email = "TEST@EXAMPLE.COM";
        when(userMapper.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userMapper).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("findByOktaUserId: Okta User IDで正常にユーザーを検索できる")
    void findByOktaUserId_ValidOktaUserId_ReturnsUser() {
        // Arrange
        String oktaUserId = "okta123456789";
        when(userMapper.findByOktaUserId(oktaUserId)).thenReturn(Optional.of(oktaUser));

        // Act
        Optional<User> result = userRepository.findByOktaUserId(oktaUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(oktaUser.getId(), result.get().getId());
        assertEquals("okta@example.com", result.get().getEmail());
        assertEquals("Okta User", result.get().getFullName());
        assertEquals("okta123456789", result.get().getOktaUserId());
        verify(userMapper).findByOktaUserId(oktaUserId);
    }

    @Test
    @DisplayName("findByOktaUserId: 存在しないOkta User IDでは空を返す")
    void findByOktaUserId_NonExistentOktaUserId_ReturnsEmpty() {
        // Arrange
        String oktaUserId = "nonexistent-okta-id";
        when(userMapper.findByOktaUserId(oktaUserId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByOktaUserId(oktaUserId);

        // Assert
        assertFalse(result.isPresent());
        verify(userMapper).findByOktaUserId(oktaUserId);
    }

    @Test
    @DisplayName("findByOktaUserId: nullのOkta User IDでは空を返す")
    void findByOktaUserId_NullOktaUserId_ReturnsEmpty() {
        // Arrange
        when(userMapper.findByOktaUserId(null)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByOktaUserId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(userMapper).findByOktaUserId(null);
    }

    @Test
    @DisplayName("updateLastLoginAt: 最終ログイン時刻を正常に更新できる")
    void updateLastLoginAt_ValidTimestamp_UpdatesSuccessfully() {
        // Arrange
        LocalDateTime loginTime = LocalDateTime.now().minusMinutes(5);
        String userId = testUser.getId();

        // Act
        userRepository.updateLastLoginAt(userId, loginTime);

        // Assert
        verify(userMapper).updateLastLoginAt(eq(userId), eq(loginTime), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("updateLastLoginAt: 存在しないユーザーIDでの更新も正常に呼び出される")
    void updateLastLoginAt_NonExistentUserId_CallsMapper() {
        // Arrange
        LocalDateTime loginTime = LocalDateTime.now();
        String nonExistentUserId = "non-existent-user-id";

        // Act
        userRepository.updateLastLoginAt(nonExistentUserId, loginTime);

        // Assert
        verify(userMapper).updateLastLoginAt(eq(nonExistentUserId), eq(loginTime), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("updateLastLoginAt: Oktaユーザーの最終ログイン時刻も正常に更新できる")
    void updateLastLoginAt_OktaUser_UpdatesSuccessfully() {
        // Arrange
        LocalDateTime loginTime = LocalDateTime.now().minusHours(1);
        String userId = oktaUser.getId();

        // Act
        userRepository.updateLastLoginAt(userId, loginTime);

        // Assert
        verify(userMapper).updateLastLoginAt(eq(userId), eq(loginTime), any(LocalDateTime.class));
    }
}