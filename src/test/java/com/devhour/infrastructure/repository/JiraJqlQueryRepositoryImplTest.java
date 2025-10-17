package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.infrastructure.mapper.JiraJqlQueryMapper;

/**
 * JQLクエリリポジトリ実装クラスのテスト
 * 
 * MyBatisマッパーとの連携を通じたJQLクエリエンティティの永続化操作をテスト
 * JqlQueryMapperのモックを使用して単体テストを実行
 * 
 * テスト対象:
 * - 基本CRUD操作（save, findById, findAll, deleteById）
 * - 優先度順アクティブクエリ取得
 * - テンプレートID検索
 * - クエリ名検索と重複チェック
 * - ページネーション対応一覧取得
 * - エラーハンドリング
 */
@ExtendWith(MockitoExtension.class)
class JiraJqlQueryRepositoryImplTest {

    @Mock
    private JiraJqlQueryMapper jqlQueryMapper;

    private JiraJqlQueryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JiraJqlQueryRepositoryImpl(jqlQueryMapper);
    }

    @Test
    void testFindById_Found() {
        // Given
        String queryId = "query123";
        JiraJqlQuery expectedQuery = createTestJqlQuery(queryId, "Test Query", 1);
        
        when(jqlQueryMapper.selectById(queryId)).thenReturn(Optional.of(expectedQuery));
        
        // When
        Optional<JiraJqlQuery> result = repository.findById(queryId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedQuery, result.get());
        verify(jqlQueryMapper).selectById(queryId);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        String queryId = "nonexistent";
        
        when(jqlQueryMapper.selectById(queryId)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraJqlQuery> result = repository.findById(queryId);
        
        // Then
        assertFalse(result.isPresent());
        verify(jqlQueryMapper).selectById(queryId);
    }

    @Test
    void testFindById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
        verify(jqlQueryMapper, never()).selectById(any());
    }

    @Test
    void testFindByQueryName_Found() {
        // Given
        String queryName = "Test Query";
        JiraJqlQuery expectedQuery = createTestJqlQuery("query123", queryName, 1);
        
        when(jqlQueryMapper.selectByQueryName(queryName)).thenReturn(Optional.of(expectedQuery));
        
        // When
        Optional<JiraJqlQuery> result = repository.findByQueryName(queryName);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedQuery, result.get());
        verify(jqlQueryMapper).selectByQueryName(queryName);
    }

    @Test
    void testFindByQueryName_NotFound() {
        // Given
        String queryName = "Nonexistent Query";
        
        when(jqlQueryMapper.selectByQueryName(queryName)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraJqlQuery> result = repository.findByQueryName(queryName);
        
        // Then
        assertFalse(result.isPresent());
        verify(jqlQueryMapper).selectByQueryName(queryName);
    }

    @Test
    void testFindByQueryName_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findByQueryName(null));
        verify(jqlQueryMapper, never()).selectByQueryName(any());
    }

    @Test
    void testFindAll() {
        // Given
        List<JiraJqlQuery> expectedQueries = Arrays.asList(
            createTestJqlQuery("query1", "Query 1", 1),
            createTestJqlQuery("query2", "Query 2", 2)
        );
        
        when(jqlQueryMapper.selectAllWithPagination(Integer.MAX_VALUE, 0)).thenReturn(expectedQueries);
        
        // When
        List<JiraJqlQuery> result = repository.findAll();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedQueries, result);
        verify(jqlQueryMapper).selectAllWithPagination(Integer.MAX_VALUE, 0);
    }

    @Test
    void testFindAll_Empty() {
        // Given
        when(jqlQueryMapper.selectAllWithPagination(Integer.MAX_VALUE, 0)).thenReturn(Arrays.asList());
        
        // When
        List<JiraJqlQuery> result = repository.findAll();
        
        // Then
        assertTrue(result.isEmpty());
        verify(jqlQueryMapper).selectAllWithPagination(Integer.MAX_VALUE, 0);
    }

    @Test
    void testFindActiveQueriesOrderByPriority() {
        // Given
        List<JiraJqlQuery> expectedQueries = Arrays.asList(
            createTestJqlQuery("query1", "High Priority Query", 1),
            createTestJqlQuery("query2", "Medium Priority Query", 2),
            createTestJqlQuery("query3", "Low Priority Query", 3)
        );
        
        when(jqlQueryMapper.selectActiveQueriesOrderByPriority()).thenReturn(expectedQueries);
        
        // When
        List<JiraJqlQuery> result = repository.findActiveQueriesOrderByPriority();
        
        // Then
        assertEquals(3, result.size());
        assertEquals(expectedQueries, result);
        verify(jqlQueryMapper).selectActiveQueriesOrderByPriority();
    }

    @Test
    void testFindActiveQueriesOrderByPriority_Empty() {
        // Given
        when(jqlQueryMapper.selectActiveQueriesOrderByPriority()).thenReturn(Arrays.asList());
        
        // When
        List<JiraJqlQuery> result = repository.findActiveQueriesOrderByPriority();
        
        // Then
        assertTrue(result.isEmpty());
        verify(jqlQueryMapper).selectActiveQueriesOrderByPriority();
    }

    @Test
    void testFindByTemplateId() {
        // Given
        String templateId = "template123";
        List<JiraJqlQuery> expectedQueries = Arrays.asList(
            createTestJqlQuery("query1", "Template Query 1", 1),
            createTestJqlQuery("query2", "Template Query 2", 2)
        );
        
        when(jqlQueryMapper.selectByTemplateId(templateId)).thenReturn(expectedQueries);
        
        // When
        List<JiraJqlQuery> result = repository.findByTemplateId(templateId);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedQueries, result);
        verify(jqlQueryMapper).selectByTemplateId(templateId);
    }

    @Test
    void testFindByTemplateId_NotFound() {
        // Given
        String templateId = "nonexistent";
        
        when(jqlQueryMapper.selectByTemplateId(templateId)).thenReturn(Arrays.asList());
        
        // When
        List<JiraJqlQuery> result = repository.findByTemplateId(templateId);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jqlQueryMapper).selectByTemplateId(templateId);
    }

    @Test
    void testFindByTemplateId_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findByTemplateId(null));
        verify(jqlQueryMapper, never()).selectByTemplateId(any());
    }

    @Test
    void testExistsByQueryName_True() {
        // Given
        String queryName = "Existing Query";
        JiraJqlQuery existingQuery = createTestJqlQuery("query123", queryName, 1);
        
        when(jqlQueryMapper.selectByQueryName(queryName)).thenReturn(Optional.of(existingQuery));
        
        // When
        boolean result = repository.existsByQueryName(queryName);
        
        // Then
        assertTrue(result);
        verify(jqlQueryMapper).selectByQueryName(queryName);
    }

    @Test
    void testExistsByQueryName_False() {
        // Given
        String queryName = "Nonexistent Query";
        
        when(jqlQueryMapper.selectByQueryName(queryName)).thenReturn(Optional.empty());
        
        // When
        boolean result = repository.existsByQueryName(queryName);
        
        // Then
        assertFalse(result);
        verify(jqlQueryMapper).selectByQueryName(queryName);
    }

    @Test
    void testExistsByQueryName_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.existsByQueryName(null));
        verify(jqlQueryMapper, never()).selectByQueryName(any());
    }

    @Test
    void testExistsById_True() {
        // Given
        String queryId = "query123";
        JiraJqlQuery existingQuery = createTestJqlQuery(queryId, "Test Query", 1);
        
        when(jqlQueryMapper.selectById(queryId)).thenReturn(Optional.of(existingQuery));
        
        // When
        boolean result = repository.existsById(queryId);
        
        // Then
        assertTrue(result);
        verify(jqlQueryMapper).selectById(queryId);
    }

    @Test
    void testExistsById_False() {
        // Given
        String queryId = "nonexistent";
        
        when(jqlQueryMapper.selectById(queryId)).thenReturn(Optional.empty());
        
        // When
        boolean result = repository.existsById(queryId);
        
        // Then
        assertFalse(result);
        verify(jqlQueryMapper).selectById(queryId);
    }

    @Test
    void testExistsById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.existsById(null));
        verify(jqlQueryMapper, never()).selectById(any());
    }

    @Test
    void testSave_NewQuery() {
        // Given
        JiraJqlQuery newQuery = createTestJqlQuery("newQuery", "New Query", 1);
        
        when(jqlQueryMapper.selectById("newQuery")).thenReturn(Optional.empty());
        
        // When
        JiraJqlQuery result = repository.save(newQuery);
        
        // Then
        assertEquals(newQuery, result);
        verify(jqlQueryMapper).selectById("newQuery");
        verify(jqlQueryMapper).insert(
            eq("newQuery"),
            eq("New Query"),
            eq("project = 'TEST'"),
            eq("template123"),
            eq(true),
            eq(1),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq("user123"),
            isNull()
        );
    }

    @Test
    void testSave_ExistingQuery() {
        // Given
        JiraJqlQuery existingQuery = createTestJqlQuery("existingQuery", "Updated Query", 2);
        
        when(jqlQueryMapper.selectById("existingQuery")).thenReturn(Optional.of(existingQuery));
        
        // When
        JiraJqlQuery result = repository.save(existingQuery);
        
        // Then
        assertEquals(existingQuery, result);
        verify(jqlQueryMapper).selectById("existingQuery");
        verify(jqlQueryMapper).update(
            eq("existingQuery"),
            eq("Updated Query"),
            eq("project = 'TEST'"),
            eq("template123"),
            eq(true),
            eq(2),
            any(LocalDateTime.class),
            isNull()
        );
    }

    @Test
    void testSave_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        verify(jqlQueryMapper, never()).selectById(any());
        verify(jqlQueryMapper, never()).insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jqlQueryMapper, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll() {
        // Given
        List<JiraJqlQuery> queries = Arrays.asList(
            createTestJqlQuery("query1", "Query 1", 1),
            createTestJqlQuery("query2", "Query 2", 2)
        );
        
        when(jqlQueryMapper.selectById("query1")).thenReturn(Optional.empty());
        when(jqlQueryMapper.selectById("query2")).thenReturn(Optional.of(queries.get(1)));
        
        // When
        List<JiraJqlQuery> result = repository.saveAll(queries);
        
        // Then
        assertEquals(queries, result);
        verify(jqlQueryMapper).selectById("query1");
        verify(jqlQueryMapper).selectById("query2");
        verify(jqlQueryMapper).insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jqlQueryMapper).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll_Empty() {
        // Given
        List<JiraJqlQuery> queries = Arrays.asList();
        
        // When
        List<JiraJqlQuery> result = repository.saveAll(queries);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jqlQueryMapper, never()).selectById(any());
        verify(jqlQueryMapper, never()).insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jqlQueryMapper, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAll_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.saveAll(null));
        verify(jqlQueryMapper, never()).selectById(any());
    }

    @Test
    void testDeleteById() {
        // Given
        String queryId = "query123";
        
        when(jqlQueryMapper.deleteById(queryId)).thenReturn(1);
        
        // When
        repository.deleteById(queryId);
        
        // Then
        verify(jqlQueryMapper).deleteById(queryId);
    }

    @Test
    void testDeleteById_NotFound() {
        // Given
        String queryId = "nonexistent";
        
        when(jqlQueryMapper.deleteById(queryId)).thenReturn(0);
        
        // When
        repository.deleteById(queryId);
        
        // Then
        verify(jqlQueryMapper).deleteById(queryId);
    }

    @Test
    void testDeleteById_NullParameter() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.deleteById(null));
        verify(jqlQueryMapper, never()).deleteById(any());
    }

    @Test
    void testFindAllWithPagination() {
        // Given
        int limit = 10;
        int offset = 0;
        List<JiraJqlQuery> expectedQueries = Arrays.asList(
            createTestJqlQuery("query1", "Query 1", 1),
            createTestJqlQuery("query2", "Query 2", 2)
        );
        
        when(jqlQueryMapper.selectAllWithPagination(limit, offset)).thenReturn(expectedQueries);
        
        // When
        List<JiraJqlQuery> result = repository.findAllWithPagination(limit, offset);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedQueries, result);
        verify(jqlQueryMapper).selectAllWithPagination(limit, offset);
    }

    @Test
    void testFindAllWithPagination_InvalidLimit() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findAllWithPagination(-1, 0));
        verify(jqlQueryMapper, never()).selectAllWithPagination(anyInt(), anyInt());
    }

    @Test
    void testFindAllWithPagination_InvalidOffset() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.findAllWithPagination(10, -1));
        verify(jqlQueryMapper, never()).selectAllWithPagination(anyInt(), anyInt());
    }

    @Test
    void testCountActiveQueries() {
        // Given
        when(jqlQueryMapper.countActiveQueries()).thenReturn(5L);
        
        // When
        long result = repository.countActiveQueries();
        
        // Then
        assertEquals(5L, result);
        verify(jqlQueryMapper).countActiveQueries();
    }

    @Test
    void testCountActiveQueries_Zero() {
        // Given
        when(jqlQueryMapper.countActiveQueries()).thenReturn(0L);
        
        // When
        long result = repository.countActiveQueries();
        
        // Then
        assertEquals(0L, result);
        verify(jqlQueryMapper).countActiveQueries();
    }

    /**
     * テスト用のJQLクエリエンティティを作成
     */
    private JiraJqlQuery createTestJqlQuery(String id, String queryName, Integer priority) {
        return JiraJqlQuery.restore(
            id,
            queryName,
            "project = 'TEST'",
            "template123",
            true,
            priority,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "user123",
            null
        );
    }
}