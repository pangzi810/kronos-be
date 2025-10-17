package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * ProjectApplicationServiceのユーザープロジェクト機能に関するユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectApplicationService - User Projects")
class ProjectApplicationServiceUserProjectsTest {

    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectApplicationService service;
    
    private User testUser;
    private Project project1;
    private Project project2;
    private Project project3;
    
    @BeforeEach
    void setUp() {
        // テストユーザーの作成
        testUser = User.create("test_user", "test@example.com", "テストユーザー");
        
        // テストプロジェクトの作成
        LocalDate now = LocalDate.now();
        
        // 進行中プロジェクト（工数記録可能）
        project1 = Project.create("プロジェクト1", "説明1", 
            now.minusDays(10), now.plusDays(20), "creator1");
        project1.start();
            
        // 進行中プロジェクト（工数記録可能）
        project2 = Project.create("プロジェクト2", "説明2",
            now.minusDays(5), now.plusDays(30), "creator2");
        project2.start();
            
        // 計画中プロジェクト（工数記録不可）
        project3 = Project.create("プロジェクト3", "説明3",
            now.plusDays(10), now.plusDays(40), "creator3");
    }

    @Test
    @DisplayName("ユーザーがアサインされている工数記録可能プロジェクト取得 - 正常ケース")
    void findWorkRecordableProjectsForUser_Success() {
        // Arrange
        String userId = testUser.getId();
        List<Project> expectedProjects = Arrays.asList(project1, project2);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        // アサイン機能廃止により、findActiveProjects()を使用
        when(projectRepository.findActiveProjects()).thenReturn(expectedProjects);
        
        // Act
        List<Project> result = service.findWorkRecordableProjectsForUser(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(project1));
        assertTrue(result.contains(project2));
        assertFalse(result.contains(project3));
        
        verify(userRepository).findById(userId);
        // アサイン関連のメソッドではなく、全体メソッドが呼ばれることを確認
        verify(projectRepository).findActiveProjects();
    }

    @Test
    @DisplayName("ユーザーがアサインされている工数記録可能プロジェクト取得 - プロジェクトなし")
    void findWorkRecordableProjectsForUser_NoProjects() {
        // Arrange
        String userId = testUser.getId();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        // アサイン機能廃止により、findActiveProjects()を使用
        when(projectRepository.findActiveProjects()).thenReturn(Collections.emptyList());
        
        // Act
        List<Project> result = service.findWorkRecordableProjectsForUser(userId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(userRepository).findById(userId);
        verify(projectRepository).findActiveProjects();
    }

    @Test
    @DisplayName("ユーザーがアサインされている工数記録可能プロジェクト取得 - ユーザーが存在しない")
    void findWorkRecordableProjectsForUser_UserNotFound() {
        // Arrange
        String nonExistentUserId = "non_existent_user";
        
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.findWorkRecordableProjectsForUser(nonExistentUserId)
        );
        
        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
        
        verify(userRepository).findById(nonExistentUserId);
        verify(projectRepository, never()).findActiveProjects();
    }

    @Test
    @DisplayName("全体の工数記録可能プロジェクト取得 - 正常ケース")
    void findWorkRecordableProjects_Success() {
        // Arrange
        List<Project> expectedProjects = Arrays.asList(project1, project2);
        
        when(projectRepository.findActiveProjects()).thenReturn(expectedProjects);
        
        // Act
        List<Project> result = service.findWorkRecordableProjects();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> 
            p.getStatus() == ProjectStatus.IN_PROGRESS
        ));
        
        verify(projectRepository).findActiveProjects();
    }
}