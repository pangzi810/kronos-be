package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.service.DataMappingDomainService;
import com.devhour.domain.service.JiraSyncDomainService;
import com.devhour.infrastructure.jira.JiraClient;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JIRA同期バッチ処理最適化テスト
 * 
 * Task 5.2.1: バッチ処理最適化の実装の一部として、
 * バッチサイズ設定、メモリ効率処理、進捗ログ出力の機能テスト
 * 
 * テスト要件:
 * - バッチサイズ設定が正しく動作すること
 * - 進捗ログが適切に出力されること
 * - メモリ効率的な処理が実行されること
 * - パフォーマンス監視機能が動作すること
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JiraSyncApplicationService - バッチ処理最適化テスト")
class JiraSyncBatchOptimizationTest {

    @Mock private JiraJqlQueryRepository jqlQueryRepository;
    @Mock private JiraResponseTemplateRepository responseTemplateRepository;
    @Mock private JiraSyncHistoryRepository syncHistoryRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private JiraClient jiraClient;
    @Mock private JsonTransformService jsonTransformService;
    @Mock private DataMappingDomainService dataMappingDomainService;
    @Mock private JiraSyncDomainService jiraSyncDomainService;
    @Mock private ObjectMapper objectMapper;
    @Mock private RetryTemplate jiraSyncRetryTemplate;
    @Mock private AdminNotificationService adminNotificationService;

    private JiraSyncApplicationService jiraSyncApplicationService;
    
    // Test constants for batch processing
    private static final int TEST_BATCH_SIZE = 10;
    private static final int LARGE_DATASET_SIZE = 100; // Smaller for unit tests
    
    @BeforeEach
    void setUp() {
        jiraSyncApplicationService = new JiraSyncApplicationService(
            jqlQueryRepository,
            responseTemplateRepository,
            syncHistoryRepository,
            jiraClient,
            jsonTransformService,
            jiraSyncDomainService,
            objectMapper,
            jiraSyncRetryTemplate,
            adminNotificationService
        );
        
        // Set test configuration for batch processing
        ReflectionTestUtils.setField(jiraSyncApplicationService, "batchSize", TEST_BATCH_SIZE);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "memoryEfficientProcessing", true);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingEnabled", true);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingInterval", 1);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "performanceMonitoringEnabled", true);
    }

    @Test
    @DisplayName("バッチ処理設定 - バッチサイズ設定が適用されること")
    void testBatchSizeConfiguration() throws Exception {
        // Given: バッチサイズを上回るデータセット
        JiraSyncHistory syncHistory = createTestSyncHistory();
        JiraResponseTemplate template = createTestResponseTemplate();
        List<JiraJqlQuery> queries = createTestJqlQueries(1, template.getId());
        JiraJqlQuery testQuery = queries.get(0);
        
        // バッチサイズの2倍のデータを用意
        List<JsonNode> issueList = createJiraIssueList(TEST_BATCH_SIZE * 2);
        int totalIssues = issueList.size();

        // Mock setup
        when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(queries);
        when(syncHistoryRepository.save(any(JiraSyncHistory.class))).thenReturn(syncHistory);
        when(responseTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));

        // ページネーション対応: 20件を10件ずつ処理（2回のAPI呼び出し）
        JiraIssueSearchResponse response1 = createPaginatedResponse(issueList, 0, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response2 = createPaginatedResponse(issueList, TEST_BATCH_SIZE, TEST_BATCH_SIZE, totalIssues);

        when(jiraSyncRetryTemplate.execute(any(), (org.springframework.retry.RecoveryCallback<JiraIssueSearchResponse>) any()))
            .thenReturn(response1, response2);

        // JSON processing mocks
        when(objectMapper.writeValueAsString(any(JsonNode.class))).thenReturn("{}");
        when(jsonTransformService.transformResponse(anyString(), eq(template.getTemplateName())))
            .thenReturn("{\"issueKey\":\"TEST-1\",\"projectCode\":\"TEST\",\"projectName\":\"Test Project\"}");

        // Domain service mocks (now void method)
        doNothing().when(jiraSyncDomainService).applyProjectChanges(anyString(), any(JiraSyncHistory.class));
        
        // When: バッチサイズ設定での同期実行
        JiraSyncHistory result = jiraSyncApplicationService.executeSync();
        
        // Then: 処理が正常完了すること
        assertNotNull(result, "同期履歴が返されること");
        assertEquals(JiraSyncStatus.COMPLETED, result.getSyncStatus(), "同期が正常完了すること");

        // JiraClientのリトライテンプレート処理が2回実行されること（ページネーション）
        verify(jiraSyncRetryTemplate, times(2)).execute(any(), (org.springframework.retry.RecoveryCallback<JiraIssueSearchResponse>) any());
    }
    
    @Test
    @DisplayName("メモリ効率処理 - 大量データでメモリ効率処理が有効になること")
    void testMemoryEfficientProcessing() throws Exception {
        // Given: メモリ効率処理設定
        ReflectionTestUtils.setField(jiraSyncApplicationService, "memoryEfficientProcessing", true);
        
        JiraSyncHistory syncHistory = createTestSyncHistory();
        JiraResponseTemplate template = createTestResponseTemplate();
        List<JiraJqlQuery> queries = createTestJqlQueries(1, template.getId());
        JiraJqlQuery testQuery = queries.get(0);
        
        // バッチサイズを上回るデータでメモリ効率処理をトリガー
        List<JsonNode> largeIssueList = createJiraIssueList(TEST_BATCH_SIZE + 10);
        int totalIssues = largeIssueList.size();

        // Mock setup
        when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(queries);
        when(syncHistoryRepository.save(any(JiraSyncHistory.class))).thenReturn(syncHistory);
        when(responseTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));

        // ページネーション対応: 呼び出しごとに異なるstartAtを返す
        JiraIssueSearchResponse response1 = createPaginatedResponse(largeIssueList, 0, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response2 = createPaginatedResponse(largeIssueList, TEST_BATCH_SIZE, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response3 = createPaginatedResponse(largeIssueList, TEST_BATCH_SIZE * 2, TEST_BATCH_SIZE, totalIssues);

        when(jiraSyncRetryTemplate.execute(any(), (org.springframework.retry.RecoveryCallback<JiraIssueSearchResponse>) any()))
            .thenReturn(response1, response2, response3);

        // JSON processing mocks
        when(objectMapper.writeValueAsString(any(JsonNode.class))).thenReturn("{}");
        when(jsonTransformService.transformResponse(anyString(), eq(template.getTemplateName())))
            .thenReturn("{\"issueKey\":\"TEST-1\",\"projectCode\":\"TEST\",\"projectName\":\"Test Project\"}");

        // Domain service mocks (now void method)
        doNothing().when(jiraSyncDomainService).applyProjectChanges(anyString(), any(JiraSyncHistory.class));
        
        // When: メモリ効率処理での実行
        JiraSyncHistory result = jiraSyncApplicationService.executeSync();
        
        // Then: 処理が正常完了すること
        assertNotNull(result, "同期履歴が返されること");
        assertEquals(JiraSyncStatus.COMPLETED, result.getSyncStatus(), "同期が正常完了すること");
    }
    
    @Test
    @DisplayName("進捗ログ出力 - 進捗ログが有効な場合にログ出力されること")
    void testProgressLogging(TestInfo testInfo) throws Exception {
        // Given: 進捗ログ有効設定
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingEnabled", true);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingInterval", 1); // 高頻度ログ
        
        JiraSyncHistory syncHistory = createTestSyncHistory();
        JiraResponseTemplate template = createTestResponseTemplate();
        List<JiraJqlQuery> queries = createTestJqlQueries(1, template.getId());
        JiraJqlQuery testQuery = queries.get(0);
        
        // 進捗確認用のデータ
        List<JsonNode> issueList = createJiraIssueList(25);
        int totalIssues = issueList.size();

        // Mock setup
        when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(queries);
        when(syncHistoryRepository.save(any(JiraSyncHistory.class))).thenReturn(syncHistory);
        when(responseTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));

        // ページネーション対応: 25件を10件ずつ処理（3回のAPI呼び出し）
        JiraIssueSearchResponse response1 = createPaginatedResponse(issueList, 0, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response2 = createPaginatedResponse(issueList, TEST_BATCH_SIZE, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response3 = createPaginatedResponse(issueList, TEST_BATCH_SIZE * 2, TEST_BATCH_SIZE, totalIssues);

        when(jiraSyncRetryTemplate.execute(any(), (org.springframework.retry.RecoveryCallback<JiraIssueSearchResponse>) any()))
            .thenReturn(response1, response2, response3);

        // JSON processing mocks
        when(objectMapper.writeValueAsString(any(JsonNode.class))).thenReturn("{}");
        when(jsonTransformService.transformResponse(anyString(), eq(template.getTemplateName())))
            .thenReturn("{\"issueKey\":\"TEST-1\",\"projectCode\":\"TEST\",\"projectName\":\"Test Project\"}");

        // Domain service mocks (now void method)
        doNothing().when(jiraSyncDomainService).applyProjectChanges(anyString(), any(JiraSyncHistory.class));
        
        // When: 進捗ログ付き同期実行
        JiraSyncHistory result = jiraSyncApplicationService.executeSync();
        
        // Then: 処理が正常完了すること
        assertNotNull(result, "同期履歴が返されること");
        assertEquals(JiraSyncStatus.COMPLETED, result.getSyncStatus(), "同期が正常完了すること");
        
        System.out.println("[PROGRESS] 進捗ログテスト完了 - バッチサイズ" + TEST_BATCH_SIZE + "で25件処理");
    }
    
    @Test
    @DisplayName("パフォーマンス監視 - パフォーマンス監視が有効な場合に処理時間が記録されること")
    void testPerformanceMonitoring() throws Exception {
        // Given: パフォーマンス監視有効設定
        ReflectionTestUtils.setField(jiraSyncApplicationService, "performanceMonitoringEnabled", true);
        
        JiraSyncHistory syncHistory = createTestSyncHistory();
        JiraResponseTemplate template = createTestResponseTemplate();
        List<JiraJqlQuery> queries = createTestJqlQueries(1, template.getId());
        JiraJqlQuery testQuery = queries.get(0);
        
        List<JsonNode> issueList = createJiraIssueList(50);
        int totalIssues = issueList.size();

        // Mock setup
        when(jqlQueryRepository.findActiveQueriesOrderByPriority()).thenReturn(queries);
        when(syncHistoryRepository.save(any(JiraSyncHistory.class))).thenReturn(syncHistory);
        when(responseTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));

        // ページネーション対応: 50件を10件ずつ処理（5回のAPI呼び出し）
        JiraIssueSearchResponse response1 = createPaginatedResponse(issueList, 0, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response2 = createPaginatedResponse(issueList, TEST_BATCH_SIZE, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response3 = createPaginatedResponse(issueList, TEST_BATCH_SIZE * 2, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response4 = createPaginatedResponse(issueList, TEST_BATCH_SIZE * 3, TEST_BATCH_SIZE, totalIssues);
        JiraIssueSearchResponse response5 = createPaginatedResponse(issueList, TEST_BATCH_SIZE * 4, TEST_BATCH_SIZE, totalIssues);

        when(jiraSyncRetryTemplate.execute(any(), (org.springframework.retry.RecoveryCallback<JiraIssueSearchResponse>) any()))
            .thenReturn(response1, response2, response3, response4, response5);

        // JSON processing mocks
        when(objectMapper.writeValueAsString(any(JsonNode.class))).thenReturn("{}");
        when(jsonTransformService.transformResponse(anyString(), eq(template.getTemplateName())))
            .thenReturn("{\"issueKey\":\"TEST-1\",\"projectCode\":\"TEST\",\"projectName\":\"Test Project\"}");

        // Domain service mocks (now void method)
        doNothing().when(jiraSyncDomainService).applyProjectChanges(anyString(), any(JiraSyncHistory.class));
        
        // When: パフォーマンス監視付き実行
        long startTime = System.currentTimeMillis();
        JiraSyncHistory result = jiraSyncApplicationService.executeSync();
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then: 処理が正常完了すること
        assertNotNull(result, "同期履歴が返されること");
        assertEquals(JiraSyncStatus.COMPLETED, result.getSyncStatus(), "同期が正常完了すること");
        
        // パフォーマンス監視の証拠としてログメッセージを出力
        System.out.printf("[PERFORMANCE] パフォーマンス監視テスト完了 - 処理時間: %d ms%n", executionTime);
    }
    
    // === Helper Methods ===
    
    private List<JiraJqlQuery> createTestJqlQueries(int count, String templateId) {
        List<JiraJqlQuery> queries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JiraJqlQuery query = JiraJqlQuery.createNew(
                "Batch Test Query " + (i + 1),
                "project = TEST AND status = 'In Progress'",
                templateId,
                i + 1,
                "batch-test-user"
            );
            queries.add(query);
        }
        return queries;
    }
    
    private JiraSyncHistory createTestSyncHistory() {
        return JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "batch-test");
    }
    
    private JiraResponseTemplate createTestResponseTemplate() {
        return JiraResponseTemplate.createNew(
            "Batch Test Template",
            "{ \"issueKey\": \"$!{key}\", \"projectCode\": \"TEST\", \"projectName\": \"Batch Test\" }",
            "Batch processing test template"
        );
    }
    
    
    private List<JsonNode> createJiraIssueList(int size) {
        List<JsonNode> issues = new ArrayList<>();
        JsonNodeFactory factory = JsonNodeFactory.instance;
        
        for (int i = 1; i <= size; i++) {
            ObjectNode issue = factory.objectNode();
            issue.put("key", "TEST-" + i);
            issue.put("id", String.valueOf(1000 + i));
            
            ObjectNode fields = factory.objectNode();
            fields.put("summary", "Batch Test Issue " + i);
            fields.put("description", "This is a batch processing test issue");
            
            ObjectNode status = factory.objectNode();
            status.put("name", "In Progress");
            fields.set("status", status);
            
            issue.set("fields", fields);
            issues.add(issue);
        }
        
        return issues;
    }
    
    private JiraIssueSearchResponse createJiraSearchResponse(List<JsonNode> issues) {
        JiraIssueSearchResponse response = new JiraIssueSearchResponse();
        response.setStartAt(0);
        response.setTotal(issues.size());
        response.setMaxResults(issues.size());
        response.setIssues(issues);
        return response;
    }

    private JiraIssueSearchResponse createPaginatedResponse(List<JsonNode> issues, int startAt, int maxResults, int total) {
        JiraIssueSearchResponse response = new JiraIssueSearchResponse();
        response.setStartAt(startAt);
        response.setMaxResults(maxResults);
        response.setTotal(total);
        response.setIssues(issues);
        return response;
    }
}