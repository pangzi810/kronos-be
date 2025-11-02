package com.devhour.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devhour.application.service.WorkCategoryApplicationService;
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;
import com.devhour.presentation.dto.request.WorkCategoryCreateRequest;
import com.devhour.presentation.dto.request.WorkCategoryUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 作業カテゴリコントローラー統合テスト
 */
@WebMvcTest(WorkCategoryController.class)
@Import(com.devhour.config.TestSecurityConfiguration.class)
@ActiveProfiles("test")
@DisplayName("作業カテゴリコントローラー")
class WorkCategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private WorkCategoryApplicationService workCategoryApplicationService;
    
    
    
    
    @Test
    @DisplayName("作業カテゴリ作成 - 正常ケース")
    void createWorkCategory_Success() throws Exception {
        // Arrange
        String userId = "user123";
        WorkCategoryCreateRequest request = new WorkCategoryCreateRequest(
            "DEV_TASK",
            "開発タスク",
            "システム開発に関するタスク",
            "#FF5733",
            1,
            false
        );
        
        WorkCategory expectedWorkCategory = WorkCategory.create(
            CategoryCode.of(request.getCode()),
            CategoryName.of(request.getName()),
            request.getDescription(),
            DisplayOrder.of(request.getDisplayOrder()),
            "#000000",
            "system" // 作成者
        );
        
        when(workCategoryApplicationService.createWorkCategory(
            CategoryCode.of(request.getCode()),
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        )).thenReturn(expectedWorkCategory);
        
        // Act & Assert
        mockMvc.perform(post("/api/work-categories")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code.value").value("DEV_TASK"))
                .andExpect(jsonPath("$.name.value").value("開発タスク"));
        
        verify(workCategoryApplicationService).createWorkCategory(
            CategoryCode.of(request.getCode()),
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        );
    }
    
    @Test
    @DisplayName("作業カテゴリ作成 - バリデーションエラー（コードが空）")
    void createWorkCategory_ValidationError_EmptyCode() throws Exception {
        // Arrange
        String userId = "user123";
        WorkCategoryCreateRequest request = new WorkCategoryCreateRequest(
            "", // 空のコード
            "開発タスク",
            "システム開発に関するタスク",
            "#FF5733",
            1,
            false
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/work-categories")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(workCategoryApplicationService, never()).createWorkCategory(any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("作業カテゴリ作成 - バリデーションエラー（不正なコード形式）")
    void createWorkCategory_ValidationError_InvalidCodeFormat() throws Exception {
        // Arrange
        String userId = "user123";
        WorkCategoryCreateRequest request = new WorkCategoryCreateRequest(
            "dev-task", // 小文字とハイフンは不正
            "開発タスク",
            "システム開発に関するタスク",
            "#FF5733",
            1,
            false
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/work-categories")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(workCategoryApplicationService, never()).createWorkCategory(any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("作業カテゴリ作成 - 重複エラー")
    void createWorkCategory_ConflictError() throws Exception {
        // Arrange
        String userId = "user123";
        WorkCategoryCreateRequest request = new WorkCategoryCreateRequest(
            "DEV_TASK",
            "開発タスク",
            "システム開発に関するタスク",
            "#FF5733",
            1,
            false
        );
        
        when(workCategoryApplicationService.createWorkCategory(
            CategoryCode.of(request.getCode()),
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        )).thenThrow(new IllegalStateException("Category code already exists"));
        
        // Act & Assert
        mockMvc.perform(post("/api/work-categories")
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        
        verify(workCategoryApplicationService).createWorkCategory(
            CategoryCode.of(request.getCode()),
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        );
    }
    
    @Test
    @DisplayName("作業カテゴリ更新 - 正常ケース")
    void updateWorkCategory_Success() throws Exception {
        // Arrange
        String categoryId = "category123";
        String userId = "user123";
        WorkCategoryUpdateRequest request = new WorkCategoryUpdateRequest(
            "更新された開発タスク",
            "更新されたシステム開発に関するタスク",
            "#33FF57",
            2
        );
        
        WorkCategory expectedWorkCategory = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of(request.getName()),
            request.getDescription(),
            DisplayOrder.of(request.getDisplayOrder()),
            "#000000",
            "system" // 作成者
        );
        
        when(workCategoryApplicationService.updateWorkCategory(
            categoryId,
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        )).thenReturn(expectedWorkCategory);
        
        // Act & Assert
        mockMvc.perform(put("/api/work-categories/{categoryId}", categoryId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name.value").value("更新された開発タスク"));
        
        verify(workCategoryApplicationService).updateWorkCategory(
            categoryId,
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        );
    }
    
    @Test
    @DisplayName("作業カテゴリ更新 - カテゴリが見つからない")
    void updateWorkCategory_NotFound() throws Exception {
        // Arrange
        String categoryId = "nonexistent";
        String userId = "user123";
        WorkCategoryUpdateRequest request = new WorkCategoryUpdateRequest(
            "更新された開発タスク",
            "更新されたシステム開発に関するタスク",
            "#33FF57",
            2
        );
        
        when(workCategoryApplicationService.updateWorkCategory(
            categoryId,
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        )).thenThrow(new IllegalArgumentException("Category not found"));
        
        // Act & Assert
        mockMvc.perform(put("/api/work-categories/{categoryId}", categoryId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(workCategoryApplicationService).updateWorkCategory(
            categoryId,
            CategoryName.of(request.getName()),
            request.getDescription(),
            request.getColorCode(),
            userId
        );
    }
    
    @Test
    @DisplayName("作業カテゴリ詳細取得 - 正常ケース")
    void getWorkCategory_Success() throws Exception {
        // Arrange
        String categoryId = "category123";
        WorkCategory expectedWorkCategory = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of("開発タスク"),
            "システム開発に関するタスク",
            DisplayOrder.of(1),
            "#000000",
            "system" // 作成者
        );
        
        when(workCategoryApplicationService.findById(categoryId))
                .thenReturn(Optional.of(expectedWorkCategory));
        
        // Act & Assert
        mockMvc.perform(get("/api/work-categories/{categoryId}", categoryId)
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code.value").value("DEV_TASK"))
                .andExpect(jsonPath("$.name.value").value("開発タスク"));
        
        verify(workCategoryApplicationService).findById(categoryId);
    }
    
    @Test
    @DisplayName("作業カテゴリ詳細取得 - カテゴリが見つからない")
    void getWorkCategory_NotFound() throws Exception {
        // Arrange
        String categoryId = "nonexistent";
        
        when(workCategoryApplicationService.findById(categoryId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/work-categories/{categoryId}", categoryId)
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isNotFound());
        
        verify(workCategoryApplicationService).findById(categoryId);
    }
    
    @Test
    @DisplayName("全作業カテゴリ一覧取得 - 正常ケース")
    void getAllWorkCategories_Success() throws Exception {
        // Arrange
        WorkCategory category1 = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of("開発タスク"),
            "システム開発に関するタスク",
            DisplayOrder.of(1),
            "#000000",
            "system"
        );
        
        WorkCategory category2 = WorkCategory.create(
            CategoryCode.of("REVIEW"),
            CategoryName.of("レビュー"),
            "コードレビューに関するタスク",
            DisplayOrder.of(2),
            "#000000",
            "system"
        );
        
        List<WorkCategory> expectedCategories = Arrays.asList(category1, category2);
        
        when(workCategoryApplicationService.findAllWorkCategories())
                .thenReturn(expectedCategories);
        
        // Act & Assert
        mockMvc.perform(get("/api/work-categories")
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].code.value").value("DEV_TASK"))
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].code.value").value("REVIEW"));
        
        verify(workCategoryApplicationService).findAllWorkCategories();
    }
    
    @Test
    @DisplayName("アクティブな作業カテゴリ一覧取得 - 正常ケース")
    void getActiveWorkCategories_Success() throws Exception {
        // Arrange
        WorkCategory activeCategory = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of("開発タスク"),
            "システム開発に関するタスク",
            DisplayOrder.of(1),
            "#000000",
            "system"
        );
        
        List<WorkCategory> expectedCategories = Arrays.asList(activeCategory);
        
        when(workCategoryApplicationService.findActiveWorkCategories())
                .thenReturn(expectedCategories);
        
        // Act & Assert
        mockMvc.perform(get("/api/work-categories/active")
                .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].code.value").value("DEV_TASK"));
        
        verify(workCategoryApplicationService).findActiveWorkCategories();
    }
    
    
    
    @Test
    @DisplayName("作業カテゴリ有効化 - 正常ケース")
    void activateWorkCategory_Success() throws Exception {
        // Arrange
        String categoryId = "category123";
        String userId = "user123";
        
        WorkCategory expectedWorkCategory = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of("開発タスク"),
            "システム開発に関するタスク",
            DisplayOrder.of(1),
            "#000000",
            "system"
        );
        
        when(workCategoryApplicationService.activateWorkCategory(categoryId, userId))
                .thenReturn(expectedWorkCategory);
        
        // Act & Assert
        mockMvc.perform(patch("/api/work-categories/{categoryId}/activate", categoryId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code.value").value("DEV_TASK"));
        
        verify(workCategoryApplicationService).activateWorkCategory(categoryId, userId);
    }
    
    @Test
    @DisplayName("作業カテゴリ無効化 - 正常ケース")
    void deactivateWorkCategory_Success() throws Exception {
        // Arrange
        String categoryId = "category123";
        String userId = "user123";
        
        WorkCategory expectedWorkCategory = WorkCategory.create(
            CategoryCode.of("DEV_TASK"),
            CategoryName.of("開発タスク"),
            "システム開発に関するタスク",
            DisplayOrder.of(1),
            "#000000",
            "system" // 作成者
        );
        
        when(workCategoryApplicationService.deactivateWorkCategory(categoryId, userId))
                .thenReturn(expectedWorkCategory);
        
        // Act & Assert
        mockMvc.perform(patch("/api/work-categories/{categoryId}/deactivate", categoryId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code.value").value("DEV_TASK"));
        
        verify(workCategoryApplicationService).deactivateWorkCategory(categoryId, userId);
    }
    
    @Test
    @DisplayName("作業カテゴリ有効化 - 権限なし")
    void activateWorkCategory_Forbidden() throws Exception {
        // Arrange
        String categoryId = "category123";
        String userId = "unauthorized_user";
        
        when(workCategoryApplicationService.activateWorkCategory(categoryId, userId))
                .thenThrow(new IllegalStateException("Insufficient privileges"));
        
        // Act & Assert
        mockMvc.perform(patch("/api/work-categories/{categoryId}/activate", categoryId)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isConflict());
        
        verify(workCategoryApplicationService).activateWorkCategory(categoryId, userId);
    }
}