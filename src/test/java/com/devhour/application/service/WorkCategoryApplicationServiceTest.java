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
import java.util.Arrays;
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
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;
import com.devhour.domain.repository.WorkCategoryRepository;

/**
 * WorkCategoryApplicationServiceのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkCategoryApplicationService")
class WorkCategoryApplicationServiceTest {

    @Mock
    private WorkCategoryRepository workCategoryRepository;
    
    
    @InjectMocks
    private WorkCategoryApplicationService service;
    
    private User pmoUser;
    private User developerUser;
    private CategoryCode testCategoryCode;
    private CategoryName testCategoryName;
    private WorkCategory testWorkCategory;

    @BeforeEach
    void setUp() {
        // PMOユーザーの作成
        pmoUser = User.create("pmo_user", "pmo@example.com", "PMOユーザー");
        
        // 開発者ユーザーの作成（権限なし）
        developerUser = User.create("dev_user", "dev@example.com", "開発者");
        
        // テストデータ
        testCategoryCode = new CategoryCode("TEST");
        testCategoryName = new CategoryName("テストカテゴリ");
        testWorkCategory = WorkCategory.create(
            testCategoryCode,
            testCategoryName,
            "テスト用のカテゴリです",
            new DisplayOrder(1),
            "#FF0000",
            pmoUser.getId()
        );
    }

    @Test
    @DisplayName("作業カテゴリ作成 - 正常ケース")
    void createWorkCategory_Success() {
        // Arrange
        when(workCategoryRepository.existsByCode(testCategoryCode))
            .thenReturn(false);
        when(workCategoryRepository.save(any(WorkCategory.class)))
            .thenReturn(testWorkCategory);

        // Act
        WorkCategory result = service.createWorkCategory(
            testCategoryCode, testCategoryName, "テスト用のカテゴリです", "#FF0000", pmoUser.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryCode, result.getCode());
        assertEquals(testCategoryName, result.getName());
        
        verify(workCategoryRepository).existsByCode(testCategoryCode);
        verify(workCategoryRepository).save(any(WorkCategory.class));
    }



    @Test
    @DisplayName("作業カテゴリ作成 - カテゴリコード重複")
    void createWorkCategory_DuplicateCode() {
        // Arrange
        when(workCategoryRepository.existsByCode(testCategoryCode))
            .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.createWorkCategory(testCategoryCode, testCategoryName, "説明", "#FF0000", pmoUser.getId())
        );

        assertTrue(exception.getMessage().contains("カテゴリコード 'TEST' は既に使用されています"));
        verify(workCategoryRepository).existsByCode(testCategoryCode);
        verify(workCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("作業カテゴリ更新 - 正常ケース")
    void updateWorkCategory_Success() {
        // Arrange
        String categoryId = "category123";
        CategoryName newName = new CategoryName("新しいカテゴリ名");
        String newDescription = "更新された説明";
        String newColorCode = "#00FF00";
        
        when(workCategoryRepository.findById(categoryId))
            .thenReturn(Optional.of(testWorkCategory));
        when(workCategoryRepository.save(any(WorkCategory.class)))
            .thenReturn(testWorkCategory);

        // Act
        WorkCategory result = service.updateWorkCategory(categoryId, newName, newDescription, newColorCode, pmoUser.getId());

        // Assert
        assertNotNull(result);
        verify(workCategoryRepository).findById(categoryId);
        verify(workCategoryRepository).save(testWorkCategory);
    }

    @Test
    @DisplayName("作業カテゴリ更新 - カテゴリが存在しない")
    void updateWorkCategory_CategoryNotFound() {
        // Arrange
        String nonExistentCategoryId = "nonexistent";
        when(workCategoryRepository.findById(nonExistentCategoryId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.updateWorkCategory(nonExistentCategoryId, testCategoryName, "説明", "#FF0000", pmoUser.getId())
        );

        assertEquals("WorkCategory not found with identifier: " + nonExistentCategoryId, exception.getMessage());
        verify(workCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("作業カテゴリ有効化 - 正常ケース")
    void activateWorkCategory_Success() {
        // Arrange
        String categoryId = "category123";
        when(workCategoryRepository.findById(categoryId))
            .thenReturn(Optional.of(testWorkCategory));
        when(workCategoryRepository.save(any(WorkCategory.class)))
            .thenReturn(testWorkCategory);

        // Act
        WorkCategory result = service.activateWorkCategory(categoryId, pmoUser.getId());

        // Assert
        assertNotNull(result);
        verify(workCategoryRepository).findById(categoryId);
        verify(workCategoryRepository).save(testWorkCategory);
    }

    @Test
    @DisplayName("作業カテゴリ無効化 - 正常ケース")
    void deactivateWorkCategory_Success() {
        // Arrange
        String categoryId = "category123";
        when(workCategoryRepository.findById(categoryId))
            .thenReturn(Optional.of(testWorkCategory));
        when(workCategoryRepository.save(any(WorkCategory.class)))
            .thenReturn(testWorkCategory);

        // Act
        WorkCategory result = service.deactivateWorkCategory(categoryId, pmoUser.getId());

        // Assert
        assertNotNull(result);
        verify(workCategoryRepository).findById(categoryId);
        verify(workCategoryRepository).save(testWorkCategory);
    }





    @Test
    @DisplayName("IDで作業カテゴリ取得 - 正常ケース")
    void findById_Success() {
        // Arrange
        String categoryId = "category123";
        when(workCategoryRepository.findById(categoryId))
            .thenReturn(Optional.of(testWorkCategory));

        // Act
        Optional<WorkCategory> result = service.findById(categoryId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testWorkCategory, result.get());
        verify(workCategoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("IDで作業カテゴリ取得 - 存在しない")
    void findById_NotFound() {
        // Arrange
        String categoryId = "nonexistent";
        when(workCategoryRepository.findById(categoryId))
            .thenReturn(Optional.empty());

        // Act
        Optional<WorkCategory> result = service.findById(categoryId);

        // Assert
        assertFalse(result.isPresent());
        verify(workCategoryRepository).findById(categoryId);
    }


    @Test
    @DisplayName("全作業カテゴリ取得")
    void findAllWorkCategories_Success() {
        // Arrange
        WorkCategory category2 = WorkCategory.create(
            new CategoryCode("CATEGORY_TWO"), new CategoryName("カテゴリ2"),
            "説明2", new DisplayOrder(2), "#00FF00", pmoUser.getId()
        );
        List<WorkCategory> categories = Arrays.asList(testWorkCategory, category2);
        when(workCategoryRepository.findAll())
            .thenReturn(categories);

        // Act
        List<WorkCategory> result = service.findAllWorkCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testWorkCategory, result.get(0));
        assertEquals(category2, result.get(1));
        verify(workCategoryRepository).findAll();
    }

    @Test
    @DisplayName("アクティブな作業カテゴリ取得")
    void findActiveWorkCategories_Success() {
        // Arrange
        List<WorkCategory> activeCategories = Arrays.asList(testWorkCategory);
        when(workCategoryRepository.findAllActive())
            .thenReturn(activeCategories);

        // Act
        List<WorkCategory> result = service.findActiveWorkCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkCategory, result.get(0));
        verify(workCategoryRepository).findAllActive();
    }







}