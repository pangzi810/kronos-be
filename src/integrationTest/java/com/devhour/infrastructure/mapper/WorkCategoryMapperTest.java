package com.devhour.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.devhour.domain.model.entity.WorkCategory;

/**
 * WorkCategoryMapper integration test
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
class WorkCategoryMapperTest extends AbstractMapperTest {

    @Autowired
    private WorkCategoryMapper workCategoryMapper;

    @Test
    void testMapperIsNotNull() {
        assertNotNull(workCategoryMapper);
    }

    @Test
    void testFindById_ExistingCategory() {
        Optional<WorkCategory> result = workCategoryMapper.findById("category-uuid-brd-000000000001");
        
        assertTrue(result.isPresent());
        WorkCategory category = result.get();
        assertEquals("category-uuid-brd-000000000001", category.getId());
        assertEquals("BRD", category.getCode().value());
        assertEquals("BRD", category.getName().getValue());
        assertTrue(category.isActive());
    }

    @Test
    void testFindById_NonExistentCategory() {
        Optional<WorkCategory> result = workCategoryMapper.findById("non-existent");
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByCode_ExistingCategory() {
        Optional<WorkCategory> result = workCategoryMapper.findByCode("MEETING");
        
        assertTrue(result.isPresent());
        WorkCategory category = result.get();
        assertEquals("MEETING", category.getCode().value());
        assertEquals("Meeting", category.getName().getValue());
    }

    @Test
    void testFindByCode_NonExistentCode() {
        Optional<WorkCategory> result = workCategoryMapper.findByCode("NONEXISTENT");
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        List<WorkCategory> categories = workCategoryMapper.findAll();
        
        assertNotNull(categories);
        assertTrue(categories.size() >= 3); // We expect at least 3 test categories
        
        // Verify categories are ordered by display_order
        for (int i = 1; i < categories.size(); i++) {
            assertTrue(categories.get(i-1).getDisplayOrder().value() <= 
                      categories.get(i).getDisplayOrder().value());
        }
    }

    @Test
    void testFindActiveCategories() {
        List<WorkCategory> activeCategories = workCategoryMapper.findActiveCategories();
        
        assertNotNull(activeCategories);
        assertFalse(activeCategories.isEmpty());
        
        // All returned categories should be active
        for (WorkCategory category : activeCategories) {
            assertTrue(category.isActive());
        }
    }

    @Test
    void testExistsByCode_ExistingCode() {
        boolean exists = workCategoryMapper.existsByCode("DEV");
        assertTrue(exists);
    }

    @Test
    void testExistsByCode_NonExistentCode() {
        boolean exists = workCategoryMapper.existsByCode("NONEXISTENT");
        assertFalse(exists);
    }

    @Test
    void testExistsByName_ExistingName() {
        boolean exists = workCategoryMapper.existsByName("Dev");
        assertTrue(exists);
    }

    @Test
    void testExistsByName_NonExistentName() {
        boolean exists = workCategoryMapper.existsByName("Non-existent Category");
        assertFalse(exists);
    }

    @Test
    void testGetMaxDisplayOrder() {
        int maxOrder = workCategoryMapper.getMaxDisplayOrder();
        assertTrue(maxOrder > 0);
    }

    @Test
    void testCount() {
        long count = workCategoryMapper.count();
        assertTrue(count > 0);
    }

    @Test
    void testCountActiveCategories() {
        long activeCount = workCategoryMapper.countActiveCategories();
        assertTrue(activeCount > 0);
    }

    @Test
    void testInsert() {
        String categoryId = "new-test-category-" + System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        
        workCategoryMapper.insert(
            categoryId,
            "NEWCAT",
            "New Test Category",
            "Test description for new category",
            true,
            99,
            "testUser",
            now,
            "testUser",
            now
        );
        
        // Verify the category was inserted
        Optional<WorkCategory> inserted = workCategoryMapper.findById(categoryId);
        assertTrue(inserted.isPresent());
        assertEquals("NEWCAT", inserted.get().getCode().value());
        assertEquals("New Test Category", inserted.get().getName().getValue());
    }

    @Test
    void testUpdate() {
        String categoryId = "category-uuid-brd-000000000001";
        LocalDateTime now = LocalDateTime.now();
        
        int updateCount = workCategoryMapper.update(
            categoryId,
            "Updated Category Name",
            "Updated description",
            50,
            "testUpdater",
            now
        );
        
        assertEquals(1, updateCount);
        
        // Verify the update
        Optional<WorkCategory> updated = workCategoryMapper.findById(categoryId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Category Name", updated.get().getName().getValue());
    }

    @Test
    void testUpdateActiveStatus() {
        String categoryId = "category-uuid-brd-000000000001";
        LocalDateTime now = LocalDateTime.now();
        
        int updateCount = workCategoryMapper.updateActiveStatus(
            categoryId,
            false,
            "testUpdater",
            now
        );
        
        assertEquals(1, updateCount);
        
        // Verify the update
        Optional<WorkCategory> updated = workCategoryMapper.findById(categoryId);
        assertTrue(updated.isPresent());
        assertFalse(updated.get().isActive());
    }

    @Test
    void testSoftDelete() {
        String categoryId = "test-category-3";
        LocalDateTime now = LocalDateTime.now();
        
        workCategoryMapper.softDelete(categoryId, now, "testDeleter", now);
        
        // Verify the category is no longer found (soft deleted)
        Optional<WorkCategory> deleted = workCategoryMapper.findById(categoryId);
        assertFalse(deleted.isPresent());
    }
}