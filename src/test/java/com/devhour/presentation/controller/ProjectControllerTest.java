package com.devhour.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devhour.application.service.ProjectApplicationService;
import com.devhour.domain.model.entity.Project;
import com.devhour.presentation.dto.request.ProjectCreateRequest;
import com.devhour.presentation.dto.request.ProjectUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * プロジェクトコントローラー統合テスト
 */
@WebMvcTest(ProjectController.class)
@Import(com.devhour.config.TestSecurityConfiguration.class)
@DisplayName("プロジェクトコントローラー")
class ProjectControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ProjectApplicationService projectApplicationService;
    
    
    
    
    @Test
    @DisplayName("プロジェクト作成 - 正常ケース")
    void createProject_Success() throws Exception {
        // Arrange
        String userId = "user123";
        ProjectCreateRequest request = new ProjectCreateRequest(
            "Test Project",
            "Test Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        Project expectedProject = Project.create(
            request.getName(),
            request.getDescription(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            userId
        );
        
        when(projectApplicationService.createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        )).thenReturn(expectedProject);
        
        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        verify(projectApplicationService).createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        );
    }
    
    @Test
    @DisplayName("プロジェクト作成 - バリデーションエラー（名前が空）")
    void createProject_ValidationError_EmptyName() throws Exception {
        // Arrange
        String userId = "user123";
        ProjectCreateRequest request = new ProjectCreateRequest(
            "", // 空の名前
            "Test Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(projectApplicationService, never()).createProject(any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("プロジェクト作成 - 重複エラー")
    void createProject_ConflictError() throws Exception {
        // Arrange
        String userId = "user123";
        ProjectCreateRequest request = new ProjectCreateRequest(
            "Test Project",
            "Test Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        when(projectApplicationService.createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        )).thenThrow(new IllegalStateException("Project already exists"));
        
        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        
        verify(projectApplicationService).createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        );
    }
    
    @Test
    @DisplayName("プロジェクト作成 - 引数エラー")
    void createProject_BadRequest() throws Exception {
        // Arrange
        String userId = "user123";
        ProjectCreateRequest request = new ProjectCreateRequest(
            "Test Project",
            "Test Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        when(projectApplicationService.createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        )).thenThrow(new IllegalArgumentException("Invalid project data"));
        
        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(projectApplicationService).createProject(
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        );
    }
    
    @Test
    @DisplayName("プロジェクト更新 - 正常ケース")
    void updateProject_Success() throws Exception {
        // Arrange
        String projectId = "project123";
        String userId = "user123";
        ProjectUpdateRequest request = new ProjectUpdateRequest(
            "Updated Project",
            "Updated Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        Project expectedProject = Project.create(
            request.getName(),
            request.getDescription(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            userId
        );
        
        when(projectApplicationService.updateProject(
            projectId,
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        )).thenReturn(expectedProject);
        
        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Updated Project"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
        
        verify(projectApplicationService).updateProject(
            projectId,
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        );
    }
    
    @Test
    @DisplayName("プロジェクト更新 - プロジェクトが見つからない")
    void updateProject_NotFound() throws Exception {
        // Arrange
        String projectId = "nonexistent";
        String userId = "user123";
        ProjectUpdateRequest request = new ProjectUpdateRequest(
            "Updated Project",
            "Updated Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        when(projectApplicationService.updateProject(
            projectId,
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        )).thenThrow(new IllegalArgumentException("Project not found"));
        
        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(projectApplicationService).updateProject(
            projectId,
            userId,
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription()
        );
    }
    
    @Test
    @DisplayName("プロジェクト詳細取得 - 正常ケース")
    void getProject_Success() throws Exception {
        // Arrange
        String projectId = "project123";
        Project expectedProject = Project.create(
            "Test Project",
            "Test Description",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            "user123"
        );
        
        when(projectApplicationService.findById(projectId))
                .thenReturn(Optional.of(expectedProject));
        
        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        verify(projectApplicationService).findById(projectId);
    }
    
    @Test
    @DisplayName("プロジェクト詳細取得 - プロジェクトが見つからない")
    void getProject_NotFound() throws Exception {
        // Arrange
        String projectId = "nonexistent";
        
        when(projectApplicationService.findById(projectId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isNotFound());
        
        verify(projectApplicationService).findById(projectId);
    }
    
    @Test
    @DisplayName("プロジェクト一覧取得 - 正常ケース")
    void getAllProjects_Success() throws Exception {
        // Arrange
        Project project1 = Project.create(
            "Project 1",
            "Description 1",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 30),
            "user123"
        );
        
        Project project2 = Project.create(
            "Project 2", 
            "Description 2",
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 12, 31),
            "user456"
        );
        
        List<Project> expectedProjects = Arrays.asList(project1, project2);
        
        when(projectApplicationService.findAllProjects())
                .thenReturn(expectedProjects);
        
        // Act & Assert
        mockMvc.perform(get("/api/projects")
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Project 1"))
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].name").value("Project 2"));
        
        verify(projectApplicationService).findAllProjects();
    }
    
    @Test
    @DisplayName("プロジェクト削除 - 正常ケース")
    void deleteProject_Success() throws Exception {
        // Arrange
        String projectId = "project123";
        String userId = "user123";
        
        doNothing().when(projectApplicationService).deleteProject(projectId, userId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());
        
        verify(projectApplicationService).deleteProject(projectId, userId);
    }
    
    @Test
    @DisplayName("プロジェクト削除 - プロジェクトが見つからない")
    void deleteProject_NotFound() throws Exception {
        // Arrange
        String projectId = "nonexistent";
        String userId = "user123";
        
        doThrow(new IllegalArgumentException("Project not found"))
                .when(projectApplicationService).deleteProject(projectId, userId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(projectApplicationService).deleteProject(projectId, userId);
    }
    
    @Test
    @DisplayName("プロジェクト削除 - 権限なし")
    void deleteProject_Forbidden() throws Exception {
        // Arrange
        String projectId = "project123";
        String userId = "unauthorized_user";
        
        doThrow(new IllegalStateException("Insufficient privileges"))
                .when(projectApplicationService).deleteProject(projectId, userId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isConflict());
        
        verify(projectApplicationService).deleteProject(projectId, userId);
    }
}