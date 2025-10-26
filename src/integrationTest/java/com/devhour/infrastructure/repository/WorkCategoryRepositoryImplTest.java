package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;
import com.devhour.infrastructure.mapper.WorkCategoryMapper;

@ExtendWith(MockitoExtension.class)
class WorkCategoryRepositoryImplTest {

    @Mock
    private WorkCategoryMapper workCategoryMapper;

    private WorkCategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new WorkCategoryRepositoryImpl(workCategoryMapper);
    }

    @Test
    void testFindById_Found() {
        String categoryId = "category123";
        WorkCategory expectedCategory = createTestCategory(categoryId, "DEV", "Development");
        
        when(workCategoryMapper.findById(categoryId)).thenReturn(Optional.of(expectedCategory));
        
        Optional<WorkCategory> result = repository.findById(categoryId);
        
        assertTrue(result.isPresent());
        assertEquals(expectedCategory, result.get());
        verify(workCategoryMapper).findById(categoryId);
    }

    @Test
    void testFindById_NotFound() {
        String categoryId = "nonexistent";
        
        when(workCategoryMapper.findById(categoryId)).thenReturn(Optional.empty());
        
        Optional<WorkCategory> result = repository.findById(categoryId);
        
        assertFalse(result.isPresent());
        verify(workCategoryMapper).findById(categoryId);
    }

    @Test
    void testFindByCode_Found() {
        CategoryCode code = CategoryCode.of("MEETING");
        WorkCategory expectedCategory = createTestCategory("category456", "MEETING", "Meeting");
        
        when(workCategoryMapper.findByCode("MEETING")).thenReturn(Optional.of(expectedCategory));
        
        Optional<WorkCategory> result = repository.findByCode(code);
        
        assertTrue(result.isPresent());
        assertEquals(expectedCategory, result.get());
        verify(workCategoryMapper).findByCode("MEETING");
    }

    @Test
    void testFindAll() {
        List<WorkCategory> expectedCategories = Arrays.asList(
            createTestCategory("cat1", "DEV", "Development"),
            createTestCategory("cat2", "MEETING", "Meeting")
        );
        
        when(workCategoryMapper.findAll()).thenReturn(expectedCategories);
        
        List<WorkCategory> result = repository.findAll();
        
        assertEquals(2, result.size());
        assertEquals(expectedCategories, result);
        verify(workCategoryMapper).findAll();
    }

    @Test
    void testFindAllActive() {
        List<WorkCategory> expectedCategories = Arrays.asList(
            createTestCategory("cat1", "DEV", "Development"),
            createTestCategory("cat2", "MEETING", "Meeting")
        );
        
        when(workCategoryMapper.findActiveCategories()).thenReturn(expectedCategories);
        
        List<WorkCategory> result = repository.findAllActive();
        
        assertEquals(2, result.size());
        assertEquals(expectedCategories, result);
        verify(workCategoryMapper).findActiveCategories();
    }

    @Test
    void testExistsById_True() {
        String categoryId = "category123";
        
        when(workCategoryMapper.findById(categoryId))
            .thenReturn(Optional.of(createTestCategory(categoryId, "DEV", "Development")));
        
        boolean result = repository.existsById(categoryId);
        
        assertTrue(result);
        verify(workCategoryMapper).findById(categoryId);
    }

    @Test
    void testExistsById_False() {
        String categoryId = "nonexistent";
        
        when(workCategoryMapper.findById(categoryId)).thenReturn(Optional.empty());
        
        boolean result = repository.existsById(categoryId);
        
        assertFalse(result);
        verify(workCategoryMapper).findById(categoryId);
    }

    @Test
    void testSave_NewCategory() {
        WorkCategory category = createTestCategory("newCategory", "NEW", "New Category");
        
        when(workCategoryMapper.findById("newCategory")).thenReturn(Optional.empty());
        
        WorkCategory result = repository.save(category);
        
        assertEquals(category, result);
        verify(workCategoryMapper).findById("newCategory");
        verify(workCategoryMapper).insert(
            eq("newCategory"), eq("NEW"), eq("New Category"), any(),
            eq(true), eq(1), eq("system"), any(LocalDateTime.class),
            eq("system"), any(LocalDateTime.class)
        );
    }

    @Test
    void testSave_ExistingCategory() {
        WorkCategory category = createTestCategory("existingCategory", "EXISTING", "Existing Category");
        
        when(workCategoryMapper.findById("existingCategory"))
            .thenReturn(Optional.of(category));
        
        WorkCategory result = repository.save(category);
        
        assertEquals(category, result);
        verify(workCategoryMapper).findById("existingCategory");
        verify(workCategoryMapper).update(
            eq("existingCategory"), eq("Existing Category"), any(),
            eq(1), eq("system"), any(LocalDateTime.class)
        );
    }

    @Test
    void testDeleteById() {
        String categoryId = "category123";
        
        repository.deleteById(categoryId);
        
        verify(workCategoryMapper).softDelete(
            eq(categoryId), any(LocalDateTime.class), eq("system"), any(LocalDateTime.class)
        );
    }


    @Test
    void testExistsByCode_True() {
        CategoryCode code = CategoryCode.of("DEV");
        
        when(workCategoryMapper.existsByCode("DEV")).thenReturn(true);
        
        boolean result = repository.existsByCode(code);
        
        assertTrue(result);
        verify(workCategoryMapper).existsByCode("DEV");
    }

    @Test
    void testExistsByCode_False() {
        CategoryCode code = CategoryCode.of("NONEXISTENT");
        
        when(workCategoryMapper.existsByCode("NONEXISTENT")).thenReturn(false);
        
        boolean result = repository.existsByCode(code);
        
        assertFalse(result);
        verify(workCategoryMapper).existsByCode("NONEXISTENT");
    }

    @Test
    void testSaveAll_MultipleCategories() {
        List<WorkCategory> categories = Arrays.asList(
            createTestCategory("cat1", "DEV", "Development"),
            createTestCategory("cat2", "MEET", "Meeting"),
            createTestCategory("cat3", "DOC", "Documentation")
        );
        
        // Mock existing checks - assuming all are new categories
        when(workCategoryMapper.findById("cat1")).thenReturn(Optional.empty());
        when(workCategoryMapper.findById("cat2")).thenReturn(Optional.empty());
        when(workCategoryMapper.findById("cat3")).thenReturn(Optional.empty());
        
        List<WorkCategory> result = repository.saveAll(categories);
        
        assertEquals(3, result.size());
        assertEquals(categories, result);
        
        // Verify all categories were inserted
        verify(workCategoryMapper).insert(
            eq("cat1"), eq("DEV"), eq("Development"), any(),
            eq(true), eq(1), eq("system"), any(LocalDateTime.class),
            eq("system"), any(LocalDateTime.class)
        );
        verify(workCategoryMapper).insert(
            eq("cat2"), eq("MEET"), eq("Meeting"), any(),
            eq(true), eq(1), eq("system"), any(LocalDateTime.class),
            eq("system"), any(LocalDateTime.class)
        );
        verify(workCategoryMapper).insert(
            eq("cat3"), eq("DOC"), eq("Documentation"), any(),
            eq(true), eq(1), eq("system"), any(LocalDateTime.class),
            eq("system"), any(LocalDateTime.class)
        );
    }

    @Test
    void testSaveAll_EmptyList() {
        List<WorkCategory> emptyCategories = Arrays.asList();
        
        List<WorkCategory> result = repository.saveAll(emptyCategories);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveAll_MixedNewAndExisting() {
        WorkCategory newCategory = createTestCategory("newCat", "NEW", "New Category");
        WorkCategory existingCategory = createTestCategory("existingCat", "EXIST", "Existing Category");
        List<WorkCategory> categories = Arrays.asList(newCategory, existingCategory);
        
        // Mock existing checks
        when(workCategoryMapper.findById("newCat")).thenReturn(Optional.empty());
        when(workCategoryMapper.findById("existingCat")).thenReturn(Optional.of(existingCategory));
        
        List<WorkCategory> result = repository.saveAll(categories);
        
        assertEquals(2, result.size());
        assertEquals(categories, result);
        
        // Verify new category was inserted
        verify(workCategoryMapper).insert(
            eq("newCat"), eq("NEW"), eq("New Category"), any(),
            eq(true), eq(1), eq("system"), any(LocalDateTime.class),
            eq("system"), any(LocalDateTime.class)
        );
        
        // Verify existing category was updated
        verify(workCategoryMapper).update(
            eq("existingCat"), eq("Existing Category"), any(),
            eq(1), eq("system"), any(LocalDateTime.class)
        );
    }


    private WorkCategory createTestCategory(String id, String code, String name) {
        return WorkCategory.restore(
            id,
            CategoryCode.of(code),
            CategoryName.of(name),
            "Description for " + name,
            DisplayOrder.of(1),
            "#FF5722",
            true,
            "system",
            LocalDateTime.now(),
            "system",
            LocalDateTime.now()
        );
    }
}