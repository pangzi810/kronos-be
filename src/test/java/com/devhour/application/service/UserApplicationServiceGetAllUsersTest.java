package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;

/**
 * UserApplicationService.getAllUsers()のテストクラス
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceGetAllUsersTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserApplicationService userApplicationService;

    private User activeUser1;
    private User activeUser2;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // テストデータの準備
        activeUser1 = User.restore(
            "user-001",
            "john_doe",
            "john@example.com",
            "John Doe",
            true,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1)
        );

        activeUser2 = User.restore(
            "user-002",
            "jane_smith",
            "jane@example.com",
            "Jane Smith",
            true,
            LocalDateTime.now().minusDays(20),
            LocalDateTime.now().minusDays(2)
        );

        inactiveUser = User.restore(
            "user-003",
            "bob_jones",
            "bob@example.com",
            "Bob Jones",
            false,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(10)
        );
    }

    @Test
    @DisplayName("全ユーザー一覧を正常に取得できること")
    void getAllUsers_Success() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(activeUser1, activeUser2, inactiveUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userApplicationService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(activeUser1));
        assertTrue(result.contains(activeUser2));
        assertTrue(result.contains(inactiveUser));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("ユーザーが存在しない場合、空のリストを返すこと")
    void getAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userApplicationService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("アクティブ・非アクティブ両方のユーザーが含まれること")
    void getAllUsers_IncludesBothActiveAndInactive() {
        // Arrange
        List<User> mixedUsers = Arrays.asList(activeUser1, inactiveUser);
        when(userRepository.findAll()).thenReturn(mixedUsers);

        // Act
        List<User> result = userApplicationService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // アクティブユーザーが含まれることを確認
        assertTrue(result.stream().anyMatch(User::isActive));
        
        // 非アクティブユーザーが含まれることを確認
        assertTrue(result.stream().anyMatch(u -> !u.isActive()));
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("異なるユーザーが全て含まれること")
    void getAllUsers_IncludesAllUsers() {
        // Arrange
        User adminUser = User.restore(
            "user-004",
            "admin_user",
            "admin@example.com",
            "Admin User",
            true,
            LocalDateTime.now().minusDays(90),
            LocalDateTime.now()
        );

        List<User> allUsers = Arrays.asList(activeUser1, activeUser2, adminUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<User> result = userApplicationService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // 全てのユーザーが含まれることを確認
        assertTrue(result.contains(activeUser1));
        assertTrue(result.contains(activeUser2));
        assertTrue(result.contains(adminUser));
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("リポジトリで例外が発生した場合、RuntimeExceptionがスローされること")
    void getAllUsers_RepositoryException() {
        // Arrange
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userApplicationService.getAllUsers());
        
        assertEquals("ユーザー一覧の取得に失敗しました", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Database error", exception.getCause().getMessage());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("大量のユーザーデータも正常に取得できること")
    void getAllUsers_LargeDataSet() {
        // Arrange
        // 1001件のユーザーデータを作成（警告ログのテスト用）
        User[] largeUserArray = new User[1001];
        for (int i = 0; i < 1001; i++) {
            largeUserArray[i] = User.restore(
                "user-" + String.format("%04d", i),
                "user" + i,
                "user" + i + "@example.com",
                "User " + i,
                true,
                LocalDateTime.now().minusDays(i % 365),
                LocalDateTime.now().minusDays(i % 30)
            );
        }
        List<User> largeUserList = Arrays.asList(largeUserArray);
        when(userRepository.findAll()).thenReturn(largeUserList);

        // Act
        List<User> result = userApplicationService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1001, result.size());
        verify(userRepository, times(1)).findAll();
    }
}