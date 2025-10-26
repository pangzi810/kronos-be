package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;

/**
 * ユーザーアプリケーションサービステスト
 */
@DisplayName("ユーザーアプリケーションサービス")
class UserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    
    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserApplicationService(userRepository);
    }

    @Test
    @DisplayName("新しいユーザーを作成できる")
    void createUser_Success() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        
        User mockUser = mock(User.class);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = service.createUser(username, email, fullName);

        // Assert
        assertNotNull(result);
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("ユーザー名重複の場合はエラー")
    void createUser_DuplicateUsername_ThrowsException() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.createUser(username, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("既に使用されています"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("メールアドレス重複の場合はエラー")
    void createUser_DuplicateEmail_ThrowsException() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.createUser(username, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("既に使用されています"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("ユーザー情報を更新できる")
    void updateUser_Success() {
        // Arrange
        String userId = "user-1";
        String email = "newemail@example.com";
        String fullName = "New Full Name";

        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        User result = service.updateUser(userId, email, fullName);

        // Assert
        assertNotNull(result);
        verify(mockUser).updateUserInfo(email, fullName);
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("存在しないユーザーの更新はエラー")
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        String userId = "nonexistent";
        String email = "test@example.com";
        String fullName = "Test User";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.updateUser(userId, email, fullName)
        );
        
        assertEquals("User not found with identifier: " + userId, exception.getMessage());
    }

//     @Test
//     @DisplayName("パスワードを変更できる")
//     void changePassword_Success() {
//         // Arrange
//         String userId = "user-1";
//         String rawPassword = "newpassword123";
//         String encodedPassword = "newEncodedPassword";
// 
//         User mockUser = mock(User.class);
//         when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
//         when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
//         when(userRepository.save(mockUser)).thenReturn(mockUser);
// 
//         // Act
//         User result = service.changePassword(userId, rawPassword);
// 
//         // Assert
//         assertNotNull(result);
//         verify(passwordEncoder).encode(rawPassword);
//         verify(mockUser).updatePasswordHash(encodedPassword);
//         verify(userRepository).save(mockUser);
//     }

    @Test
    @DisplayName("ユーザーを有効化できる")
    void activateUser_Success() {
        // Arrange
        String userId = "user-1";

        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        User result = service.activateUser(userId);

        // Assert
        assertNotNull(result);
        verify(mockUser).activate();
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("ユーザーを無効化できる")
    void deactivateUser_Success() {
        // Arrange
        String userId = "user-1";

        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        User result = service.deactivateUser(userId);

        // Assert
        assertNotNull(result);
        verify(mockUser).deactivate();
        verify(userRepository).save(mockUser);
    }

//     @Test
//     @DisplayName("パスワードの検証ができる")
//     void verifyPassword_Success() {
//         // Arrange
//         String rawPassword = "password123";
//         String encodedPassword = "encodedPassword";
// 
//         when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
// 
//         // Act
//         boolean result = service.verifyPassword(rawPassword, encodedPassword);
// 
//         // Assert
//         assertTrue(result);
//         verify(passwordEncoder).matches(rawPassword, encodedPassword);
//     }

    @Test
    @DisplayName("ユーザー名の存在チェックができる")
    void isUsernameExists_Success() {
        // Arrange
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act
        boolean result = service.isUsernameExists(username);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("メールアドレスの存在チェックができる")
    void isEmailExists_Success() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = service.isEmailExists(email);

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("アクティブな開発者一覧を取得できる")
    void findActiveDevelopers_Success() {
        // Arrange
        List<User> expectedDevelopers = Arrays.asList(
            mock(User.class),
            mock(User.class),
            mock(User.class)
        );

        when(userRepository.findAllActive()).thenReturn(expectedDevelopers);

        // Act
        List<User> result = service.findActiveDevelopers();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedDevelopers, result);
        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("アクティブな開発者一覧が空の場合")
    void findActiveDevelopers_EmptyList() {
        // Arrange
        when(userRepository.findAllActive()).thenReturn(Arrays.asList());

        // Act
        List<User> result = service.findActiveDevelopers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAllActive();
    }

    @Test
    @DisplayName("アクティブな開発者取得でエラーが発生した場合")
    void findActiveDevelopers_ThrowsException() {
        // Arrange
        when(userRepository.findAllActive())
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.findActiveDevelopers();
        });

        assertEquals("Database error", exception.getMessage());
        verify(userRepository).findAllActive();
    }
    

    @Test
    @DisplayName("メールアドレス重複時の更新エラー")
    void updateUser_DuplicateEmail_ThrowsException() {
        // Arrange
        String userId = "user-1";
        String email = "existing@example.com";
        String fullName = "Test User";
        
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        
        User existingUser = mock(User.class);
        when(existingUser.getId()).thenReturn("user-2"); // Different user ID
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.updateUser(userId, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("既に使用されています"));
        verify(userRepository, never()).save(any(User.class));
    }

//     @Test
//     @DisplayName("パスワード変更時にユーザーが見つからない場合")
//     void changePassword_UserNotFound_ThrowsException() {
//         // Arrange
//         String userId = "nonexistent";
//         String rawPassword = "newpassword";
//         
//         when(userRepository.findById(userId)).thenReturn(Optional.empty());
//         
//         // Act & Assert
//         EntityNotFoundException exception = assertThrows(
//             EntityNotFoundException.class,
//             () -> service.changePassword(userId, rawPassword)
//         );
//         
//         assertEquals("User not found with identifier: " + userId, exception.getMessage());
//         verify(userRepository, never()).save(any(User.class));
//     }

    @Test
    @DisplayName("ユーザー有効化時にユーザーが見つからない場合")
    void activateUser_UserNotFound_ThrowsException() {
        // Arrange
        String userId = "nonexistent";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.activateUser(userId)
        );
        
        assertEquals("User not found with identifier: " + userId, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("ユーザー無効化時にユーザーが見つからない場合")
    void deactivateUser_UserNotFound_ThrowsException() {
        // Arrange
        String userId = "nonexistent";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.deactivateUser(userId)
        );
        
        assertEquals("User not found with identifier: " + userId, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("ユーザーを削除できる")
    void deleteUser_Success() {
        // Arrange
        String userId = "user-1";
        User mockUser = mock(User.class);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        
        // Act
        service.deleteUser(userId);
        
        // Assert
        verify(mockUser).deactivate();
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("削除時にユーザーが見つからない場合")
    void deleteUser_UserNotFound_ThrowsException() {
        // Arrange
        String userId = "nonexistent";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.deleteUser(userId)
        );
        
        assertEquals("User not found with identifier: " + userId, exception.getMessage());
    }

    @Test
    @DisplayName("IDでユーザーを取得できる")
    void findById_Success() {
        // Arrange
        String userId = "user-1";
        User mockUser = mock(User.class);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // Act
        Optional<User> result = service.findById(userId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    @DisplayName("IDでユーザーが見つからない場合")
    void findById_NotFound() {
        // Arrange
        String userId = "nonexistent";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act
        Optional<User> result = service.findById(userId);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("ユーザー名でユーザーを取得できる")
    void findByUsername_Success() {
        // Arrange
        String username = "testuser";
        User mockUser = mock(User.class);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        
        // Act
        Optional<User> result = service.findByUsername(username);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    @DisplayName("メールアドレスでユーザーを取得できる")
    void findByEmail_Success() {
        // Arrange
        String email = "test@example.com";
        User mockUser = mock(User.class);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        
        // Act
        Optional<User> result = service.findByEmail(email);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    @DisplayName("アクティブなユーザー一覧を取得できる")
    void findAllActiveUsers_Success() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(mock(User.class), mock(User.class));
        
        when(userRepository.findAllActive()).thenReturn(expectedUsers);
        
        // Act
        List<User> result = service.findAllActiveUsers();
        
        // Assert
        assertEquals(expectedUsers, result);
    }


    @Test
    @DisplayName("全ユーザー一覧を取得できる")
    void findAllUsers_Success() {
        // Arrange
        List<User> allUsers = Arrays.asList(mock(User.class), mock(User.class), mock(User.class));
        
        when(userRepository.findAll()).thenReturn(allUsers);
        
        // Act
        List<User> result = service.findAllUsers();
        
        // Assert
        assertEquals(allUsers, result);
    }


    @Test
    @DisplayName("フルネームでユーザーを検索できる")
    void searchByFullName_Success() {
        // Arrange
        String pattern = "山田";
        List<User> expectedUsers = Arrays.asList(mock(User.class));
        
        when(userRepository.searchByFullName(pattern)).thenReturn(expectedUsers);
        
        // Act
        List<User> result = service.searchByFullName(pattern);
        
        // Assert
        assertEquals(expectedUsers, result);
    }

    @Test
    @DisplayName("ユーザーの存在チェックができる")
    void isUserExists_Success() {
        // Arrange
        String userId = "user-1";
        
        when(userRepository.existsById(userId)).thenReturn(true);
        
        // Act
        boolean result = service.isUserExists(userId);
        
        // Assert
        assertTrue(result);
    }








}