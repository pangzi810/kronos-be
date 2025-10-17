package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * ProjectApplicationServiceのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectApplicationService")
class ProjectApplicationServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectApplicationService service;
    
    private Project testProject;
    private User pmoUser;
    private User developerUser;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        // PMOユーザーの作成
        pmoUser = User.create("pmo_user", "pmo@example.com", "PMOユーザー");
        
        // 開発者ユーザーの作成
        developerUser = User.create("dev_user", "dev@example.com", "開発者");
        
        // テスト用日付
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusDays(30);
        
        // テストプロジェクトの作成
        testProject = Project.create("テストプロジェクト", "テストプロジェクトの説明", startDate, endDate, pmoUser.getId());
    }


    @Test
    @DisplayName("プロジェクト作成 - 正常ケース")
    void createProject_Success() {
        // Arrange
        when(userRepository.findById(pmoUser.getId()))
            .thenReturn(Optional.of(pmoUser));
        when(projectRepository.save(any(Project.class)))
            .thenReturn(testProject);

        // Act
        Project result = service.createProject(
            pmoUser.getId(), "新しいプロジェクト", startDate, endDate, "新しいプロジェクトの説明"
        );

        // Assert
        assertNotNull(result);
        assertEquals("テストプロジェクト", result.getName()); // モックで返されるプロジェクト名
        verify(userRepository).findById(pmoUser.getId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("プロジェクト作成 - ユーザーが存在しない")
    void createProject_UserNotFound() {
        // Arrange
        String userId = "nonexistent";
        when(userRepository.findById(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.createProject(userId, "プロジェクト", startDate, endDate, "説明")
        );

        assertEquals("User not found with identifier: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("プロジェクト作成 - 開発者でも作成可能")
    void createProject_DeveloperCanCreate() {
        // Arrange
        when(userRepository.findById(developerUser.getId()))
            .thenReturn(Optional.of(developerUser));
        when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Project result = service.createProject(developerUser.getId(), "プロジェクト", startDate, endDate, "説明");

        // Assert
        assertNotNull(result);
        assertEquals("プロジェクト", result.getName());
        verify(userRepository).findById(developerUser.getId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("プロジェクト更新 - 正常ケース")
    void updateProject_Success() {
        // Arrange
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(userRepository.findById(pmoUser.getId()))
            .thenReturn(Optional.of(pmoUser));
        when(projectRepository.save(any(Project.class)))
            .thenReturn(testProject);

        // Act
        Project result = service.updateProject(
            testProject.getId(), pmoUser.getId(), "更新されたプロジェクト", 
            startDate.plusDays(1), endDate.plusDays(5), "更新された説明"
        );

        // Assert
        assertNotNull(result);
        verify(projectRepository).findById(testProject.getId());
        verify(userRepository).findById(pmoUser.getId());
        verify(projectRepository).save(testProject);
    }

    @Test
    @DisplayName("プロジェクト更新 - プロジェクトが存在しない")
    void updateProject_ProjectNotFound() {
        // Arrange
        String projectId = "nonexistent";
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.updateProject(projectId, pmoUser.getId(), "更新", startDate, endDate, "説明")
        );

        assertEquals("Project not found with identifier: " + projectId, exception.getMessage());
        verify(projectRepository).findById(projectId);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("プロジェクト更新 - 開発者でも更新可能")
    void updateProject_DeveloperCanUpdate() {
        // Arrange
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(userRepository.findById(developerUser.getId()))
            .thenReturn(Optional.of(developerUser));
        when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Project result = service.updateProject(testProject.getId(), developerUser.getId(), "更新", startDate, endDate, "説明");

        // Assert
        assertNotNull(result);
        verify(projectRepository).findById(testProject.getId());
        verify(userRepository).findById(developerUser.getId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("プロジェクト取得 - 正常ケース")
    void findById_Success() {
        // Arrange
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));

        // Act
        Optional<Project> result = service.findById(testProject.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectRepository).findById(testProject.getId());
    }

    @Test
    @DisplayName("プロジェクト取得 - 存在しない")
    void findById_NotFound() {
        // Arrange
        String projectId = "nonexistent";
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Project> result = service.findById(projectId);

        // Assert
        assertFalse(result.isPresent());
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("全プロジェクト取得")
    void findAllProjects_Success() {
        // Arrange
        Project project1 = Project.create("プロジェクト1", "説明1", startDate, endDate, pmoUser.getId());
        Project project2 = Project.create("プロジェクト2", "説明2", startDate, endDate, pmoUser.getId());
        List<Project> projects = Arrays.asList(project1, project2);
        
        when(projectRepository.findAll())
            .thenReturn(projects);

        // Act
        List<Project> result = service.findAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("プロジェクト1", result.get(0).getName());
        assertEquals("プロジェクト2", result.get(1).getName());
        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("プロジェクト削除 - 正常ケース")
    void deleteProject_Success() {
        // Arrange
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(userRepository.findById(pmoUser.getId()))
            .thenReturn(Optional.of(pmoUser));

        // Act
        assertDoesNotThrow(() -> service.deleteProject(testProject.getId(), pmoUser.getId()));

        // Assert
        verify(projectRepository).findById(testProject.getId());
        verify(userRepository).findById(pmoUser.getId());
        verify(projectRepository).deleteById(testProject.getId());
    }

    @Test
    @DisplayName("プロジェクト削除 - プロジェクトが存在しない")
    void deleteProject_ProjectNotFound() {
        // Arrange
        String projectId = "nonexistent";
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.deleteProject(projectId, pmoUser.getId())
        );

        assertEquals("Project not found with identifier: " + projectId, exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("プロジェクト削除 - 開発者でも削除可能")
    void deleteProject_DeveloperCanDelete() {
        // Arrange
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(userRepository.findById(developerUser.getId()))
            .thenReturn(Optional.of(developerUser));
        doNothing().when(projectRepository).deleteById(testProject.getId());

        // Act
        assertDoesNotThrow(() -> service.deleteProject(testProject.getId(), developerUser.getId()));

        // Assert
        verify(projectRepository).findById(testProject.getId());
        verify(userRepository).findById(developerUser.getId());
        verify(projectRepository).deleteById(testProject.getId());
    }

    @Test
    @DisplayName("PMOアクセス権限検証 - ユーザーが存在しない")
    void validatePMOAccess_UserNotFound() {
        // Arrange
        String nonExistentUserId = "nonexistent";
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(userRepository.findById(nonExistentUserId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.updateProject(testProject.getId(), nonExistentUserId, "更新", startDate, endDate, "説明")
        );

        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
    }
    
    @Test
    @DisplayName("工数記録可能プロジェクト一覧取得 - 正常ケース")
    void findWorkRecordableProjects_Success() {
        // Arrange
        List<Project> expectedProjects = Arrays.asList(testProject);
        when(projectRepository.findActiveProjects()).thenReturn(expectedProjects);
        
        // Act
        List<Project> result = service.findWorkRecordableProjects();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject, result.get(0));
        verify(projectRepository).findActiveProjects();
    }
    
    @Test
    @DisplayName("工数記録可能プロジェクト一覧取得 - 空のリスト")
    void findWorkRecordableProjects_EmptyList() {
        // Arrange
        when(projectRepository.findActiveProjects()).thenReturn(Collections.emptyList());
        
        // Act
        List<Project> result = service.findWorkRecordableProjects();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository).findActiveProjects();
    }
}