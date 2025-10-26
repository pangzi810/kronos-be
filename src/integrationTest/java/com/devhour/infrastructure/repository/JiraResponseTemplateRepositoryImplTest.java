package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.infrastructure.mapper.JiraResponseTemplateMapper;

/**
 * レスポンステンプレートリポジトリ実装クラスのテスト
 * 
 * MyBatisマッパーとの連携を通じたレスポンステンプレートエンティティの永続化操作をテスト
 * ResponseTemplateMapperのモックを使用して単体テストを実行
 * 
 * テスト対象:
 * - 基本CRUD操作（save, findById, findAll, deleteById）
 * - テンプレート名による検索と重複チェック
 * - テンプレート名パターン検索
 * - ページネーション対応一覧取得
 * - エラーハンドリングと入力検証
 * 
 * 品質要件:
 * - 80%以上のテストカバレッジ
 * - エッジケース処理（null値、空文字、重複名等）
 * - 期待される例外の検証
 */
@ExtendWith(MockitoExtension.class)
class JiraResponseTemplateRepositoryImplTest {

    @Mock
    private JiraResponseTemplateMapper responseTemplateMapper;

    private JiraResponseTemplateRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JiraResponseTemplateRepositoryImpl(responseTemplateMapper);
    }

    @Test
    void testFindById_Found() {
        // Given
        String templateId = "template123";
        JiraResponseTemplate expectedTemplate = createTestResponseTemplate(templateId, "Test Template");
        
        when(responseTemplateMapper.selectById(templateId)).thenReturn(Optional.of(expectedTemplate));
        
        // When
        Optional<JiraResponseTemplate> result = repository.findById(templateId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTemplate, result.get());
        verify(responseTemplateMapper).selectById(templateId);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        String templateId = "nonexistent";
        
        when(responseTemplateMapper.selectById(templateId)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraResponseTemplate> result = repository.findById(templateId);
        
        // Then
        assertFalse(result.isPresent());
        verify(responseTemplateMapper).selectById(templateId);
    }

    @Test
    void testFindById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
        verify(responseTemplateMapper, never()).selectById(any());
    }

    @Test
    void testFindByTemplateName_Found() {
        // Given
        String templateName = "Test Template";
        JiraResponseTemplate expectedTemplate = createTestResponseTemplate("template123", templateName);
        
        when(responseTemplateMapper.selectByTemplateName(templateName)).thenReturn(Optional.of(expectedTemplate));
        
        // When
        Optional<JiraResponseTemplate> result = repository.findByTemplateName(templateName);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedTemplate, result.get());
        verify(responseTemplateMapper).selectByTemplateName(templateName);
    }

    @Test
    void testFindByTemplateName_NotFound() {
        // Given
        String templateName = "Nonexistent Template";
        
        when(responseTemplateMapper.selectByTemplateName(templateName)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraResponseTemplate> result = repository.findByTemplateName(templateName);
        
        // Then
        assertFalse(result.isPresent());
        verify(responseTemplateMapper).selectByTemplateName(templateName);
    }

    @Test
    void testFindByTemplateName_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findByTemplateName(null));
        verify(responseTemplateMapper, never()).selectByTemplateName(any());
    }

    @Test
    void testFindAll() {
        // Given
        List<JiraResponseTemplate> expectedTemplates = Arrays.asList(
            createTestResponseTemplate("template1", "Template 1"),
            createTestResponseTemplate("template2", "Template 2")
        );
        
        when(responseTemplateMapper.selectAll()).thenReturn(expectedTemplates);
        
        // When
        List<JiraResponseTemplate> result = repository.findAll();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedTemplates, result);
        verify(responseTemplateMapper).selectAll();
    }

    @Test
    void testFindAll_Empty() {
        // Given
        when(responseTemplateMapper.selectAll()).thenReturn(Arrays.asList());
        
        // When
        List<JiraResponseTemplate> result = repository.findAll();
        
        // Then
        assertTrue(result.isEmpty());
        verify(responseTemplateMapper).selectAll();
    }

    @Test
    void testFindAvailableTemplates() {
        // Given
        List<JiraResponseTemplate> expectedTemplates = Arrays.asList(
            createTestResponseTemplate("template1", "Available Template 1"),
            createTestResponseTemplate("template2", "Available Template 2")
        );
        
        when(responseTemplateMapper.selectAll()).thenReturn(expectedTemplates);
        
        // When
        List<JiraResponseTemplate> result = repository.findAvailableTemplates();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedTemplates, result);
        verify(responseTemplateMapper).selectAll();
    }

    @Test
    void testSearchByNamePattern() {
        // Given
        String pattern = "Test";
        List<JiraResponseTemplate> expectedTemplates = Arrays.asList(
            createTestResponseTemplate("template1", "Test Template 1"),
            createTestResponseTemplate("template2", "Test Template 2")
        );
        
        when(responseTemplateMapper.searchByNamePattern(pattern)).thenReturn(expectedTemplates);
        
        // When
        List<JiraResponseTemplate> result = repository.searchByNamePattern(pattern);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedTemplates, result);
        verify(responseTemplateMapper).searchByNamePattern(pattern);
    }

    @Test
    void testSearchByNamePattern_EmptyResult() {
        // Given
        String pattern = "Nonexistent";
        
        when(responseTemplateMapper.searchByNamePattern(pattern)).thenReturn(Arrays.asList());
        
        // When
        List<JiraResponseTemplate> result = repository.searchByNamePattern(pattern);
        
        // Then
        assertTrue(result.isEmpty());
        verify(responseTemplateMapper).searchByNamePattern(pattern);
    }

    @Test
    void testSearchByNamePattern_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.searchByNamePattern(null));
        verify(responseTemplateMapper, never()).searchByNamePattern(any());
    }

    @Test
    void testExistsByTemplateName_True() {
        // Given
        String templateName = "Existing Template";
        
        when(responseTemplateMapper.existsByTemplateName(templateName)).thenReturn(true);
        
        // When
        boolean result = repository.existsByTemplateName(templateName);
        
        // Then
        assertTrue(result);
        verify(responseTemplateMapper).existsByTemplateName(templateName);
    }

    @Test
    void testExistsByTemplateName_False() {
        // Given
        String templateName = "Nonexistent Template";
        
        when(responseTemplateMapper.existsByTemplateName(templateName)).thenReturn(false);
        
        // When
        boolean result = repository.existsByTemplateName(templateName);
        
        // Then
        assertFalse(result);
        verify(responseTemplateMapper).existsByTemplateName(templateName);
    }

    @Test
    void testExistsByTemplateName_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.existsByTemplateName(null));
        verify(responseTemplateMapper, never()).existsByTemplateName(any());
    }

    @Test
    void testExistsByTemplateNameExcludingId_True() {
        // Given
        String templateName = "Template Name";
        String excludeId = "template123";
        
        when(responseTemplateMapper.existsByTemplateNameExcludingId(templateName, excludeId)).thenReturn(true);
        
        // When
        boolean result = repository.existsByTemplateNameExcludingId(templateName, excludeId);
        
        // Then
        assertTrue(result);
        verify(responseTemplateMapper).existsByTemplateNameExcludingId(templateName, excludeId);
    }

    @Test
    void testExistsByTemplateNameExcludingId_False() {
        // Given
        String templateName = "Unique Template Name";
        String excludeId = "template123";
        
        when(responseTemplateMapper.existsByTemplateNameExcludingId(templateName, excludeId)).thenReturn(false);
        
        // When
        boolean result = repository.existsByTemplateNameExcludingId(templateName, excludeId);
        
        // Then
        assertFalse(result);
        verify(responseTemplateMapper).existsByTemplateNameExcludingId(templateName, excludeId);
    }

    @Test
    void testExistsByTemplateNameExcludingId_NullTemplateName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> repository.existsByTemplateNameExcludingId(null, "template123"));
        verify(responseTemplateMapper, never()).existsByTemplateNameExcludingId(any(), any());
    }

    @Test
    void testExistsByTemplateNameExcludingId_NullExcludeId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> repository.existsByTemplateNameExcludingId("Template Name", null));
        verify(responseTemplateMapper, never()).existsByTemplateNameExcludingId(any(), any());
    }

    @Test
    void testExistsById_True() {
        // Given
        String templateId = "template123";
        JiraResponseTemplate existingTemplate = createTestResponseTemplate(templateId, "Test Template");
        
        when(responseTemplateMapper.selectById(templateId)).thenReturn(Optional.of(existingTemplate));
        
        // When
        boolean result = repository.existsById(templateId);
        
        // Then
        assertTrue(result);
        verify(responseTemplateMapper).selectById(templateId);
    }

    @Test
    void testExistsById_False() {
        // Given
        String templateId = "nonexistent";
        
        when(responseTemplateMapper.selectById(templateId)).thenReturn(Optional.empty());
        
        // When
        boolean result = repository.existsById(templateId);
        
        // Then
        assertFalse(result);
        verify(responseTemplateMapper).selectById(templateId);
    }

    @Test
    void testExistsById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.existsById(null));
        verify(responseTemplateMapper, never()).selectById(any());
    }

    @Test
    void testSave_NewTemplate() {
        // Given
        JiraResponseTemplate newTemplate = createTestResponseTemplate("newTemplate", "New Template");
        
        when(responseTemplateMapper.selectById("newTemplate")).thenReturn(Optional.empty());
        
        // When
        JiraResponseTemplate result = repository.save(newTemplate);
        
        // Then
        assertEquals(newTemplate, result);
        verify(responseTemplateMapper).selectById("newTemplate");
        verify(responseTemplateMapper).insert(
            eq("newTemplate"),
            eq("New Template"),
            eq("{ \"test\": \"value\" }"),
            eq("Test template description"),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testSave_ExistingTemplate() {
        // Given
        JiraResponseTemplate existingTemplate = createTestResponseTemplate("existingTemplate", "Updated Template");
        
        when(responseTemplateMapper.selectById("existingTemplate")).thenReturn(Optional.of(existingTemplate));
        
        // When
        JiraResponseTemplate result = repository.save(existingTemplate);
        
        // Then
        assertEquals(existingTemplate, result);
        verify(responseTemplateMapper).selectById("existingTemplate");
        verify(responseTemplateMapper).update(
            eq("existingTemplate"),
            eq("Updated Template"),
            eq("{ \"test\": \"value\" }"),
            eq("Test template description"),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testSave_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        verify(responseTemplateMapper, never()).selectById(any());
        verify(responseTemplateMapper, never()).insert(any(), any(), any(), any(), any(), any());
        verify(responseTemplateMapper, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll() {
        // Given
        List<JiraResponseTemplate> templates = Arrays.asList(
            createTestResponseTemplate("template1", "Template 1"),
            createTestResponseTemplate("template2", "Template 2")
        );
        
        when(responseTemplateMapper.selectById("template1")).thenReturn(Optional.empty());
        when(responseTemplateMapper.selectById("template2")).thenReturn(Optional.of(templates.get(1)));
        
        // When
        List<JiraResponseTemplate> result = repository.saveAll(templates);
        
        // Then
        assertEquals(templates, result);
        verify(responseTemplateMapper).selectById("template1");
        verify(responseTemplateMapper).selectById("template2");
        verify(responseTemplateMapper).insert(any(), any(), any(), any(), any(), any());
        verify(responseTemplateMapper).update(any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll_Empty() {
        // Given
        List<JiraResponseTemplate> templates = Arrays.asList();
        
        // When
        List<JiraResponseTemplate> result = repository.saveAll(templates);
        
        // Then
        assertTrue(result.isEmpty());
        verify(responseTemplateMapper, never()).selectById(any());
        verify(responseTemplateMapper, never()).insert(any(), any(), any(), any(), any(), any());
        verify(responseTemplateMapper, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.saveAll(null));
        verify(responseTemplateMapper, never()).selectById(any());
    }

    @Test
    void testDeleteById() {
        // Given
        String templateId = "template123";
        
        when(responseTemplateMapper.deleteById(templateId)).thenReturn(1);
        
        // When
        repository.deleteById(templateId);
        
        // Then
        verify(responseTemplateMapper).deleteById(templateId);
    }

    @Test
    void testDeleteById_NotFound() {
        // Given
        String templateId = "nonexistent";
        
        when(responseTemplateMapper.deleteById(templateId)).thenReturn(0);
        
        // When
        repository.deleteById(templateId);
        
        // Then
        verify(responseTemplateMapper).deleteById(templateId);
    }

    @Test
    void testDeleteById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.deleteById(null));
        verify(responseTemplateMapper, never()).deleteById(any());
    }

    @Test
    void testFindAllWithPagination() {
        // Given
        int limit = 10;
        int offset = 0;
        List<JiraResponseTemplate> expectedTemplates = Arrays.asList(
            createTestResponseTemplate("template1", "Template 1"),
            createTestResponseTemplate("template2", "Template 2")
        );
        
        when(responseTemplateMapper.selectAllWithPagination(limit, offset)).thenReturn(expectedTemplates);
        
        // When
        List<JiraResponseTemplate> result = repository.findAllWithPagination(limit, offset);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedTemplates, result);
        verify(responseTemplateMapper).selectAllWithPagination(limit, offset);
    }

    @Test
    void testFindAllWithPagination_InvalidLimit() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findAllWithPagination(-1, 0));
        verify(responseTemplateMapper, never()).selectAllWithPagination(anyInt(), anyInt());
    }

    @Test
    void testFindAllWithPagination_InvalidOffset() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findAllWithPagination(10, -1));
        verify(responseTemplateMapper, never()).selectAllWithPagination(anyInt(), anyInt());
    }

    @Test
    void testCountAll() {
        // Given
        when(responseTemplateMapper.countAll()).thenReturn(5);
        
        // When
        long result = repository.countAll();
        
        // Then
        assertEquals(5L, result);
        verify(responseTemplateMapper).countAll();
    }

    @Test
    void testCountAll_Zero() {
        // Given
        when(responseTemplateMapper.countAll()).thenReturn(0);
        
        // When
        long result = repository.countAll();
        
        // Then
        assertEquals(0L, result);
        verify(responseTemplateMapper).countAll();
    }

    /**
     * テスト用のレスポンステンプレートエンティティを作成
     */
    private JiraResponseTemplate createTestResponseTemplate(String id, String templateName) {
        return JiraResponseTemplate.restore(
            id,
            templateName,
            "{ \"test\": \"value\" }",
            "Test template description",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}