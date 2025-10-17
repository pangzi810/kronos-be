package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.infrastructure.jira.JiraClient;
import com.devhour.infrastructure.jira.JiraClient.JiraClientException;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JQLクエリアプリケーションサービステストクラス
 * 
 * JqlQueryApplicationServiceの全機能をテストし、80%以上のコードカバレッジを確保する
 * 
 * テスト範囲:
 * - JQLクエリのCRUD操作
 * - JQL構文検証
 * - 優先度管理
 * - 論理削除処理
 * - エラーハンドリング
 * - パラメータ検証
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JQLクエリアプリケーションサービス")
class JiraJqlQueryApplicationServiceTest {
    
    @Mock
    private JiraJqlQueryRepository jqlQueryRepository;
    
    @Mock
    private JiraResponseTemplateRepository responseTemplateRepository;
    
    @Mock
    private JiraClient jiraClient;
    
    private JiraJqlQueryApplicationService applicationService;
    private ObjectMapper objectMapper;
    
    // テスト用データ
    private static final String TEST_QUERY_ID = "test-query-id";
    private static final String TEST_QUERY_NAME = "テストJQLクエリ";
    private static final String TEST_JQL_STRING = "project = TEST AND status = Open";
    private static final String TEST_TEMPLATE_ID = "template-001";
    private static final Integer TEST_PRIORITY = 1;
    private static final String TEST_USER_ID = "admin";
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        applicationService = new JiraJqlQueryApplicationService(
            jqlQueryRepository, 
            responseTemplateRepository, 
            jiraClient
        );
    }
    
    @Nested
    @DisplayName("JQLクエリ作成")
    class CreateJqlQueryTest {
        
        @Test
        @DisplayName("正常なJQLクエリ作成が成功する")
        void createJqlQuery_WithValidParameters_ShouldSucceed() {
            // Given
            JiraResponseTemplate mockTemplate = createMockResponseTemplate();
            JiraJqlQuery expectedQuery = JiraJqlQuery.createNew(
                TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
            );
            
            when(responseTemplateRepository.existsById(TEST_TEMPLATE_ID)).thenReturn(true);
            when(jqlQueryRepository.existsByQueryName(TEST_QUERY_NAME)).thenReturn(false);
            when(jqlQueryRepository.save(any(JiraJqlQuery.class))).thenReturn(expectedQuery);
            
            // When
            JiraJqlQuery result = applicationService.createJqlQuery(
                TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
            );
            
            // Then
            assertNotNull(result);
            assertEquals(TEST_QUERY_NAME, result.getQueryName());
            assertEquals(TEST_JQL_STRING, result.getJqlExpression());
            assertEquals(TEST_TEMPLATE_ID, result.getTemplateId());
            assertEquals(TEST_PRIORITY, result.getPriority());
            assertTrue(result.isActive());
            
            verify(responseTemplateRepository).existsById(TEST_TEMPLATE_ID);
            verify(jqlQueryRepository).existsByQueryName(TEST_QUERY_NAME);
            verify(jqlQueryRepository).save(any(JiraJqlQuery.class));
        }
        
        @Test
        @DisplayName("存在しないテンプレートIDで作成時に例外が発生する")
        void createJqlQuery_WithNonExistentTemplate_ShouldThrowException() {
            // Given
            when(responseTemplateRepository.existsById(TEST_TEMPLATE_ID)).thenReturn(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createJqlQuery(
                    TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("指定されたテンプレートIDが存在しません: " + TEST_TEMPLATE_ID, exception.getMessage());
            
            verify(responseTemplateRepository).existsById(TEST_TEMPLATE_ID);
            verifyNoInteractions(jqlQueryRepository);
        }
        
        @Test
        @DisplayName("重複するクエリ名で作成時に例外が発生する")
        void createJqlQuery_WithDuplicateQueryName_ShouldThrowException() {
            // Given
            when(responseTemplateRepository.existsById(TEST_TEMPLATE_ID)).thenReturn(true);
            when(jqlQueryRepository.existsByQueryName(TEST_QUERY_NAME)).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createJqlQuery(
                    TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("同名のJQLクエリが既に存在します: " + TEST_QUERY_NAME, exception.getMessage());
            
            verify(responseTemplateRepository).existsById(TEST_TEMPLATE_ID);
            verify(jqlQueryRepository).existsByQueryName(TEST_QUERY_NAME);
            verify(jqlQueryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("null/空のパラメータで作成時に例外が発生する")
        void createJqlQuery_WithInvalidParameters_ShouldThrowException() {
            // クエリ名がnullの場合
            IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createJqlQuery(
                    null, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("クエリ名は必須です", exception1.getMessage());
            
            // JQL式が空の場合
            IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createJqlQuery(
                    TEST_QUERY_NAME, "", TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("JQL式は必須です", exception2.getMessage());
            
            // 優先度が負数の場合
            IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createJqlQuery(
                    TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, -1, TEST_USER_ID
                )
            );
            assertEquals("優先度は0以上である必要があります", exception3.getMessage());
        }
    }
    
    @Nested
    @DisplayName("JQLクエリ更新")
    class UpdateJqlQueryTest {
        
        @Test
        @DisplayName("正常なJQLクエリ更新が成功する")
        void updateJqlQuery_WithValidParameters_ShouldSucceed() {
            // Given
            JiraJqlQuery existingQuery = createMockJqlQuery();
            String updatedName = "更新されたクエリ名";
            String updatedJql = "project = UPDATED";
            String updatedTemplateId = "template-002";
            Integer updatedPriority = 2;
            
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(existingQuery));
            when(responseTemplateRepository.existsById(updatedTemplateId)).thenReturn(true);
            when(jqlQueryRepository.findByQueryName(updatedName)).thenReturn(Optional.empty());
            when(jqlQueryRepository.save(any(JiraJqlQuery.class))).thenReturn(existingQuery);
            
            // When
            JiraJqlQuery result = applicationService.updateJqlQuery(
                TEST_QUERY_ID, updatedName, updatedJql, updatedTemplateId, updatedPriority, TEST_USER_ID
            );
            
            // Then
            assertNotNull(result);
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
            verify(responseTemplateRepository).existsById(updatedTemplateId);
            verify(jqlQueryRepository).findByQueryName(updatedName);
            verify(jqlQueryRepository).save(any(JiraJqlQuery.class));
        }
        
        @Test
        @DisplayName("存在しないJQLクエリIDで更新時に例外が発生する")
        void updateJqlQuery_WithNonExistentId_ShouldThrowException() {
            // Given
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.updateJqlQuery(
                    TEST_QUERY_ID, TEST_QUERY_NAME, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("JQLクエリが見つかりません: " + TEST_QUERY_ID, exception.getMessage());
        }
        
        @Test
        @DisplayName("同名クエリが存在する場合の更新で例外が発生する")
        void updateJqlQuery_WithDuplicateNameDifferentId_ShouldThrowException() {
            // Given
            JiraJqlQuery existingQuery = createMockJqlQuery();
            String duplicateName = "重複するクエリ名";
            
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(existingQuery));
            when(responseTemplateRepository.existsById(TEST_TEMPLATE_ID)).thenReturn(true);
            when(jqlQueryRepository.findByQueryName(duplicateName)).thenReturn(
                Optional.of(JiraJqlQuery.restore("different-id", duplicateName, TEST_JQL_STRING, TEST_TEMPLATE_ID,
                    true, TEST_PRIORITY, LocalDateTime.now(), LocalDateTime.now(), TEST_USER_ID, null))
            );
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.updateJqlQuery(
                    TEST_QUERY_ID, duplicateName, TEST_JQL_STRING, TEST_TEMPLATE_ID, TEST_PRIORITY, TEST_USER_ID
                )
            );
            assertEquals("同名のJQLクエリが既に存在します: " + duplicateName, exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("JQLクエリ削除")
    class DeleteJqlQueryTest {
        
        @Test
        @DisplayName("正常なJQLクエリ論理削除が成功する")
        void deleteJqlQuery_WithValidId_ShouldSucceed() {
            // Given
            JiraJqlQuery existingQuery = createMockJqlQuery();
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(existingQuery));
            when(jqlQueryRepository.save(any(JiraJqlQuery.class))).thenReturn(existingQuery);
            
            // When
            applicationService.deleteJqlQuery(TEST_QUERY_ID, TEST_USER_ID);
            
            // Then
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
            verify(jqlQueryRepository).save(argThat(query -> !query.isActive()));
        }
        
        @Test
        @DisplayName("存在しないIDで削除時に例外が発生する")
        void deleteJqlQuery_WithNonExistentId_ShouldThrowException() {
            // Given
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.deleteJqlQuery(TEST_QUERY_ID, TEST_USER_ID)
            );
            assertEquals("JQLクエリが見つかりません: " + TEST_QUERY_ID, exception.getMessage());
        }
        
        @Test
        @DisplayName("nullパラメータで削除時に例外が発生する")
        void deleteJqlQuery_WithNullParameters_ShouldThrowException() {
            // IDがnullの場合
            IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.deleteJqlQuery(null, TEST_USER_ID)
            );
            assertEquals("JQLクエリIDは必須です", exception1.getMessage());
            
            // 削除者がnullの場合
            IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.deleteJqlQuery(TEST_QUERY_ID, null)
            );
            assertEquals("削除実行者は必須です", exception2.getMessage());
        }
    }
    
    @Nested
    @DisplayName("JQL構文検証")
    class ValidateJqlTest {
        
        @Test
        @DisplayName("有効なJQLクエリの検証が成功する")
        void validateJql_WithValidQuery_ShouldReturnValidationResult() throws Exception {
            // Given
            JiraIssueSearchResponse mockResponse = createMockSearchResponse(10);
            when(jiraClient.searchIssues(eq(TEST_JQL_STRING), eq(1), eq(""))).thenReturn(mockResponse);
            
            // When
            JiraJqlQueryApplicationService.JqlValidationResult result = 
                applicationService.validateJql(TEST_JQL_STRING);
            
            // Then
            assertTrue(result.isValid());
            assertEquals(10, result.getMatchingProjectCount());
            assertNull(result.getErrorMessage());
            
            verify(jiraClient).searchIssues(TEST_JQL_STRING, 1, "");
        }
        
        @Test
        @DisplayName("無効なJQLクエリの検証でエラー結果が返される")
        void validateJql_WithInvalidQuery_ShouldReturnErrorResult() {
            // Given
            String invalidJql = "invalid jql syntax";
            JiraClientException clientException = new JiraClientException("JQL構文エラー");
            when(jiraClient.searchIssues(eq(invalidJql), eq(1), eq(""))).thenThrow(clientException);
            
            // When
            JiraJqlQueryApplicationService.JqlValidationResult result = 
                applicationService.validateJql(invalidJql);
            
            // Then
            assertFalse(result.isValid());
            assertEquals(0, result.getMatchingProjectCount());
            assertEquals("JQL構文エラー", result.getErrorMessage());
        }
        
        @Test
        @DisplayName("nullのJQLクエリで検証時に例外が発生する")
        void validateJql_WithNullQuery_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.validateJql(null)
            );
            assertEquals("JQL式は必須です", exception.getMessage());
        }
        
        @Test
        @DisplayName("空のJQLクエリで検証時に例外が発生する")
        void validateJql_WithEmptyQuery_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.validateJql("")
            );
            assertEquals("JQL式は必須です", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("優先度順クエリ取得")
    class GetQueriesByPriorityTest {
        
        @Test
        @DisplayName("優先度順でアクティブクエリ一覧が取得される")
        void getQueriesByPriority_ShouldReturnQueriesInPriorityOrder() {
            // Given
            JiraJqlQuery query1 = createMockJqlQuery("query1", 1);
            JiraJqlQuery query2 = createMockJqlQuery("query2", 2);
            JiraJqlQuery query3 = createMockJqlQuery("query3", 3);
            List<JiraJqlQuery> expectedQueries = Arrays.asList(query1, query2, query3);
            
            when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(expectedQueries);
            
            // When
            List<JiraJqlQuery> result = applicationService.getQueriesByPriority();
            
            // Then
            assertEquals(3, result.size());
            assertEquals(query1.getId(), result.get(0).getId());
            assertEquals(query2.getId(), result.get(1).getId());
            assertEquals(query3.getId(), result.get(2).getId());
            
            verify(jqlQueryRepository).findActiveQueriesOrderByPriority();
        }
        
        @Test
        @DisplayName("アクティブクエリが存在しない場合は空リストが返される")
        void getQueriesByPriority_WithNoActiveQueries_ShouldReturnEmptyList() {
            // Given
            when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(Collections.emptyList());
            
            // When
            List<JiraJqlQuery> result = applicationService.getQueriesByPriority();
            
            // Then
            assertTrue(result.isEmpty());
            
            verify(jqlQueryRepository).findActiveQueriesOrderByPriority();
        }
    }
    
    @Nested
    @DisplayName("JQLクエリ検索")
    class FindQueryTest {
        
        @Test
        @DisplayName("IDでJQLクエリが正常に取得される")
        void findById_WithValidId_ShouldReturnQuery() {
            // Given
            JiraJqlQuery expectedQuery = createMockJqlQuery();
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(expectedQuery));
            
            // When
            Optional<JiraJqlQuery> result = applicationService.findById(TEST_QUERY_ID);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(expectedQuery.getId(), result.get().getId());
            
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
        }
        
        @Test
        @DisplayName("存在しないIDで検索時は空のOptionalが返される")
        void findById_WithNonExistentId_ShouldReturnEmptyOptional() {
            // Given
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.empty());
            
            // When
            Optional<JiraJqlQuery> result = applicationService.findById(TEST_QUERY_ID);
            
            // Then
            assertFalse(result.isPresent());
            
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
        }
        
        @Test
        @DisplayName("全JQLクエリ一覧が正常に取得される")
        void findAll_ShouldReturnAllQueries() {
            // Given
            List<JiraJqlQuery> expectedQueries = Arrays.asList(
                createMockJqlQuery("query1", 1),
                createMockJqlQuery("query2", 2)
            );
            when(jqlQueryRepository.findAll()).thenReturn(expectedQueries);
            
            // When
            List<JiraJqlQuery> result = applicationService.findAll();
            
            // Then
            assertEquals(2, result.size());
            
            verify(jqlQueryRepository).findAll();
        }
    }
    
    
    @Nested
    @DisplayName("クエリ状態管理")
    class QueryStatusTest {
        
        @Test
        @DisplayName("JQLクエリのアクティブ化が成功する")
        void activateQuery_WithValidId_ShouldSucceed() {
            // Given
            JiraJqlQuery existingQuery = createMockJqlQuery();
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(existingQuery));
            when(jqlQueryRepository.save(any(JiraJqlQuery.class))).thenReturn(existingQuery);
            
            // When
            JiraJqlQuery result = applicationService.activateQuery(TEST_QUERY_ID);
            
            // Then
            assertNotNull(result);
            
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
            verify(jqlQueryRepository).save(any(JiraJqlQuery.class));
        }
        
        @Test
        @DisplayName("JQLクエリの非アクティブ化が成功する")
        void deactivateQuery_WithValidId_ShouldSucceed() {
            // Given
            JiraJqlQuery existingQuery = createMockJqlQuery();
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.of(existingQuery));
            when(jqlQueryRepository.save(any(JiraJqlQuery.class))).thenReturn(existingQuery);
            
            // When
            JiraJqlQuery result = applicationService.deactivateQuery(TEST_QUERY_ID);
            
            // Then
            assertNotNull(result);
            
            verify(jqlQueryRepository).findById(TEST_QUERY_ID);
            verify(jqlQueryRepository).save(any(JiraJqlQuery.class));
        }
        
        @Test
        @DisplayName("存在しないIDでアクティブ化時に例外が発生する")
        void activateQuery_WithNonExistentId_ShouldThrowException() {
            // Given
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.activateQuery(TEST_QUERY_ID)
            );
            assertEquals("JQLクエリが見つかりません: " + TEST_QUERY_ID, exception.getMessage());
        }
        
        @Test
        @DisplayName("存在しないIDで非アクティブ化時に例外が発生する")
        void deactivateQuery_WithNonExistentId_ShouldThrowException() {
            // Given
            when(jqlQueryRepository.findById(TEST_QUERY_ID)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.deactivateQuery(TEST_QUERY_ID)
            );
            assertEquals("JQLクエリが見つかりません: " + TEST_QUERY_ID, exception.getMessage());
        }
    }
    
    // ========== テストヘルパーメソッド ==========
    
    private JiraJqlQuery createMockJqlQuery() {
        return createMockJqlQuery(TEST_QUERY_ID, TEST_PRIORITY);
    }
    
    private JiraJqlQuery createMockJqlQuery(String id, Integer priority) {
        return JiraJqlQuery.restore(
            id, 
            TEST_QUERY_NAME, 
            TEST_JQL_STRING, 
            TEST_TEMPLATE_ID, 
            true, 
            priority, 
            LocalDateTime.now(), 
            LocalDateTime.now(), 
            TEST_USER_ID, 
            null
        );
    }
    
    private JiraResponseTemplate createMockResponseTemplate() {
        return JiraResponseTemplate.restore(
            TEST_TEMPLATE_ID,
            "テストテンプレート",
            "テスト用Velocityテンプレート",
            "テスト説明",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    private JiraIssueSearchResponse createMockSearchResponse(int total) throws Exception {
        String jsonResponse = String.format("""
            {
                "total": %d,
                "issues": []
            }
            """, total);
        return objectMapper.readValue(jsonResponse, JiraIssueSearchResponse.class);
    }
}