package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.service.ListApproverDomainService;

/**
 * ApproverApplicationServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApproverApplicationService")
class ApproverApplicationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ListApproverDomainService listApproverService;
    
    @InjectMocks
    private ApproverApplicationService service;
    
    private User testUser;
    private User approverUser;
    
    @BeforeEach
    void setUp() {
        // テストユーザーの作成
        testUser = User.create("test_user", "test@example.com", "テストユーザー");
        
        // 承認者ユーザーの作成
        approverUser = User.create("approver_user", "approver@example.com", "承認者ユーザー");
    }
    
    @Test
    @DisplayName("承認対象者リストを取得 - 正常ケース")
    void getApprovalTargets_Success() {
        // Arrange
        String approverId = approverUser.getId();
        List<User> expectedTargets = Arrays.asList(testUser);
        
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(listApproverService.findApprovalTargetsByApprover(approverUser)).thenReturn(expectedTargets);
        
        // Act
        List<User> result = service.getApprovalTargets(approverId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        
        verify(userRepository).findById(approverId);
        verify(listApproverService).findApprovalTargetsByApprover(approverUser);
    }
    
    @Test
    @DisplayName("承認対象者リストを取得 - ユーザーが存在しない")
    void getApprovalTargets_UserNotFound() {
        // Arrange
        String nonExistentUserId = "nonexistent";
        
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.getApprovalTargets(nonExistentUserId)
        );
        
        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verifyNoInteractions(listApproverService);
    }
    
    @Test
    @DisplayName("承認対象者リストを取得 - 対象者がいない場合")
    void getApprovalTargets_EmptyList() {
        // Arrange
        String approverId = approverUser.getId();
        
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(listApproverService.findApprovalTargetsByApprover(approverUser)).thenReturn(Collections.emptyList());
        
        // Act
        List<User> result = service.getApprovalTargets(approverId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(userRepository).findById(approverId);
        verify(listApproverService).findApprovalTargetsByApprover(approverUser);
    }
    
    @Test
    @DisplayName("承認者リストを取得 - 正常ケース")
    void getApprovers_Success() {
        // Arrange
        String targetId = testUser.getId();
        List<User> expectedApprovers = Arrays.asList(approverUser);
        
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser));
        when(listApproverService.findApproversByTarget(testUser)).thenReturn(expectedApprovers);
        
        // Act
        List<User> result = service.getApprovers(targetId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(approverUser, result.get(0));
        
        verify(userRepository).findById(targetId);
        verify(listApproverService).findApproversByTarget(testUser);
    }
    
    @Test
    @DisplayName("承認者リストを取得 - ユーザーが存在しない")
    void getApprovers_UserNotFound() {
        // Arrange
        String nonExistentUserId = "nonexistent";
        
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.getApprovers(nonExistentUserId)
        );
        
        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verifyNoInteractions(listApproverService);
    }
    
    @Test
    @DisplayName("承認者リストを取得 - 承認者がいない場合")
    void getApprovers_EmptyList() {
        // Arrange
        String targetId = testUser.getId();
        
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser));
        when(listApproverService.findApproversByTarget(testUser)).thenReturn(Collections.emptyList());
        
        // Act
        List<User> result = service.getApprovers(targetId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(userRepository).findById(targetId);
        verify(listApproverService).findApproversByTarget(testUser);
    }
}