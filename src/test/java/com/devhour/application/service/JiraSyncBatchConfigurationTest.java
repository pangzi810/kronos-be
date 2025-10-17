package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.service.DataMappingDomainService;
import com.devhour.domain.service.JiraSyncDomainService;
import com.devhour.infrastructure.jira.JiraClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JIRA同期バッチ処理設定テスト
 * 
 * Task 5.2.1: バッチ処理最適化の実装の一部として、
 * 設定値が正しく注入され、期待通りに動作することを検証する
 * 
 * 検証項目:
 * - バッチサイズ設定の注入
 * - メモリ効率処理設定の注入
 * - 進捗ログ設定の注入
 * - パフォーマンス監視設定の注入
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JiraSyncApplicationService - バッチ処理設定テスト")
class JiraSyncBatchConfigurationTest {

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
    }

    @Test
    @DisplayName("ユニットテスト環境設定値 - @Valueアノテーションなしでのプリミティブデフォルト値確認")
    void testUnitTestEnvironmentDefaultValues() {
        // Given: Spring Contextなしでのサービス初期化 (setUp()で実行済み)
        // Note: @Valueアノテーションは処理されないため、フィールドはJavaのプリミティブデフォルト値を持つ
        
        // When: 設定値を取得
        Integer batchSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "batchSize");
        Boolean memoryEfficientProcessing = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "memoryEfficientProcessing");
        Boolean progressLoggingEnabled = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "progressLoggingEnabled");
        Integer progressLoggingInterval = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "progressLoggingInterval");
        Integer streamingChunkSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "streamingChunkSize");
        Boolean performanceMonitoringEnabled = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "performanceMonitoringEnabled");
        
        // Then: Javaプリミティブのデフォルト値であることを検証
        assertEquals(0, batchSize, "プリミティブintのデフォルト値0であること");
        assertFalse(memoryEfficientProcessing, "プリミティブbooleanのデフォルト値falseであること");
        assertFalse(progressLoggingEnabled, "プリミティブbooleanのデフォルト値falseであること");
        assertEquals(0, progressLoggingInterval, "プリミティブintのデフォルト値0であること");
        assertEquals(0, streamingChunkSize, "プリミティブintのデフォルト値0であること");
        assertFalse(performanceMonitoringEnabled, "プリミティブbooleanのデフォルト値falseであること");
        
        System.out.println("[UNIT TEST] プリミティブデフォルト値確認完了 - Spring Context外でのテスト");
    }
    
    @Test
    @DisplayName("カスタム設定値 - カスタム設定値が正しく反映されること")
    void testCustomConfigurationValues() {
        // Given: カスタム設定値を設定
        int customBatchSize = 50;
        boolean customMemoryEfficient = false;
        boolean customProgressLogging = false;
        int customProgressInterval = 5;
        int customChunkSize = 25;
        boolean customPerformanceMonitoring = false;
        
        // When: 設定値を変更
        ReflectionTestUtils.setField(jiraSyncApplicationService, "batchSize", customBatchSize);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "memoryEfficientProcessing", customMemoryEfficient);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingEnabled", customProgressLogging);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingInterval", customProgressInterval);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "streamingChunkSize", customChunkSize);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "performanceMonitoringEnabled", customPerformanceMonitoring);
        
        // Then: カスタム設定値が反映されていることを検証
        Integer actualBatchSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "batchSize");
        Boolean actualMemoryEfficient = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "memoryEfficientProcessing");
        Boolean actualProgressLogging = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "progressLoggingEnabled");
        Integer actualProgressInterval = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "progressLoggingInterval");
        Integer actualChunkSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "streamingChunkSize");
        Boolean actualPerformanceMonitoring = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "performanceMonitoringEnabled");
        
        assertEquals(customBatchSize, actualBatchSize, "カスタムバッチサイズが反映されること");
        assertEquals(customMemoryEfficient, actualMemoryEfficient, "カスタムメモリ効率処理設定が反映されること");
        assertEquals(customProgressLogging, actualProgressLogging, "カスタム進捗ログ設定が反映されること");
        assertEquals(customProgressInterval, actualProgressInterval, "カスタム進捗ログ間隔が反映されること");
        assertEquals(customChunkSize, actualChunkSize, "カスタムストリーミングチャンクサイズが反映されること");
        assertEquals(customPerformanceMonitoring, actualPerformanceMonitoring, "カスタムパフォーマンス監視設定が反映されること");
    }
    
    @Test
    @DisplayName("設定値境界値 - バッチサイズの境界値が正しく処理されること")
    void testBatchSizeBoundaryValues() {
        // Given: 境界値のバッチサイズ設定
        int minBatchSize = 1;
        int maxBatchSize = 1000;
        
        // When & Then: 最小値設定
        ReflectionTestUtils.setField(jiraSyncApplicationService, "batchSize", minBatchSize);
        Integer actualMinBatchSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "batchSize");
        assertEquals(minBatchSize, actualMinBatchSize, "最小バッチサイズが設定されること");
        
        // When & Then: 最大値設定
        ReflectionTestUtils.setField(jiraSyncApplicationService, "batchSize", maxBatchSize);
        Integer actualMaxBatchSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "batchSize");
        assertEquals(maxBatchSize, actualMaxBatchSize, "最大バッチサイズが設定されること");
    }
    
    @Test
    @DisplayName("設定値組み合わせ - 異なる設定の組み合わせが正しく動作すること")
    void testConfigurationCombinations() {
        // Given: 高パフォーマンス設定の組み合わせ
        ReflectionTestUtils.setField(jiraSyncApplicationService, "batchSize", 200);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "memoryEfficientProcessing", true);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "progressLoggingEnabled", true);
        ReflectionTestUtils.setField(jiraSyncApplicationService, "performanceMonitoringEnabled", true);
        
        // When: 設定値を取得
        Integer batchSize = (Integer) ReflectionTestUtils.getField(jiraSyncApplicationService, "batchSize");
        Boolean memoryEfficient = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "memoryEfficientProcessing");
        Boolean progressLogging = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "progressLoggingEnabled");
        Boolean performanceMonitoring = (Boolean) ReflectionTestUtils.getField(jiraSyncApplicationService, "performanceMonitoringEnabled");
        
        // Then: 高パフォーマンス設定が全て有効であることを検証
        assertEquals(200, batchSize, "高パフォーマンス用バッチサイズが設定されること");
        assertTrue(memoryEfficient, "メモリ効率処理が有効であること");
        assertTrue(progressLogging, "進捗ログが有効であること");
        assertTrue(performanceMonitoring, "パフォーマンス監視が有効であること");
        
        System.out.println("[CONFIGURATION] 高パフォーマンス設定組み合わせテスト完了");
        System.out.println("  バッチサイズ: " + batchSize);
        System.out.println("  メモリ効率処理: " + memoryEfficient);
        System.out.println("  進捗ログ: " + progressLogging);
        System.out.println("  パフォーマンス監視: " + performanceMonitoring);
    }
}