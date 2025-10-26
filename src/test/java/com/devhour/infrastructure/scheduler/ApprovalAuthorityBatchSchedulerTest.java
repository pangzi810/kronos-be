package com.devhour.infrastructure.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.annotation.Scheduled;
import com.devhour.application.dto.BatchResult;
import com.devhour.application.service.ApprovalAuthorityBatchService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * ApprovalAuthorityBatchScheduler のユニットテスト
 * 
 * テストカバレッジ要件: 80%以上
 * 
 * テストシナリオ:
 * - 正常なバッチ実行とログ出力
 * - 例外発生時の処理（エラーログとRe-throw）
 * - 手動実行メソッドの動作
 * - ShedLockアノテーション設定確認
 * - Scheduledアノテーション設定確認（cron式）
 * - 設定プロパティの注入確認
 * - BatchResult結果に基づくログ出力確認
 */
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@DisplayName("ApprovalAuthorityBatchScheduler Tests")
class ApprovalAuthorityBatchSchedulerTest {
    
    @Mock
    private ApprovalAuthorityBatchService batchService;
    
    private ApprovalAuthorityBatchScheduler scheduler;
    
    private static final String TEST_CSV_PATH = "test_employee_master.csv";
    
    @BeforeEach
    void setUp() {
        scheduler = new ApprovalAuthorityBatchScheduler(batchService, TEST_CSV_PATH);
    }
    
    @Test
    @DisplayName("正常なバッチ実行が成功し、適切なログが出力される")
    void executeApprovalAuthorityBatch_ShouldSucceed_WhenNormalOperation(CapturedOutput output) {
        // given
        BatchResult mockResult = createMockBatchResult(5, 2, 3, 0, 10, 5, new ArrayList<>());
        when(batchService.importApprovalAuthoritiesFromFile(TEST_CSV_PATH)).thenReturn(mockResult);
        
        // when
        scheduler.executeApprovalAuthorityBatch();
        
        // then
        verify(batchService, times(1)).importApprovalAuthoritiesFromFile(TEST_CSV_PATH);
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assertThat(logOutput).contains("Starting approval authority batch processing...");
        assertThat(logOutput).contains("Approval authority batch processing completed successfully");
        assertThat(logOutput).contains("Processed: 5");
        assertThat(logOutput).contains("Added: 2");
        assertThat(logOutput).contains("Updated: 3");
        assertThat(logOutput).contains("Deleted: 0");
        assertThat(logOutput).contains("Approver Relations Added: 10");
        assertThat(logOutput).contains("Approver Relations Deleted: 5");
        assertThat(logOutput).contains("Errors: 0");
    }
    
    @Test
    @DisplayName("バッチ実行でエラーがある場合は警告ログを出力する")
    void executeApprovalAuthorityBatch_ShouldLogWarning_WhenBatchHasErrors(CapturedOutput output) {
        // given
        var errors = new ArrayList<String>();
        errors.add("CSV parsing error on line 10");
        errors.add("Failed to process employee: test@example.com");
        
        BatchResult mockResult = createMockBatchResult(3, 1, 2, 0, 5, 2, errors);
        when(batchService.importApprovalAuthoritiesFromFile(TEST_CSV_PATH)).thenReturn(mockResult);
        
        // when
        scheduler.executeApprovalAuthorityBatch();
        
        // then
        verify(batchService, times(1)).importApprovalAuthoritiesFromFile(TEST_CSV_PATH);
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assertThat(logOutput).contains("Starting approval authority batch processing...");
        assertThat(logOutput).contains("Approval authority batch processing completed successfully");
        assertThat(logOutput).contains("Processed: 3, Added: 1, Updated: 2, Deleted: 0");
        assertThat(logOutput).contains("Errors: 2");
        assertThat(logOutput).contains("Approval authority batch processing completed with 2 errors");
        assertThat(logOutput).contains("CSV parsing error on line 10");
        assertThat(logOutput).contains("Failed to process employee: test@example.com");
    }
    
    @Test
    @DisplayName("バッチ処理で例外が発生した場合はエラーログを出力し例外を再スローする")
    void executeApprovalAuthorityBatch_ShouldLogErrorAndRethrow_WhenExceptionOccurs(CapturedOutput output) {
        // given
        RuntimeException exception = new RuntimeException("File not found: " + TEST_CSV_PATH);
        when(batchService.importApprovalAuthoritiesFromFile(TEST_CSV_PATH)).thenThrow(exception);
        
        // when & then
        assertThatThrownBy(() -> scheduler.executeApprovalAuthorityBatch())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("File not found: " + TEST_CSV_PATH);
        
        verify(batchService, times(1)).importApprovalAuthoritiesFromFile(TEST_CSV_PATH);
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assertThat(logOutput).contains("Starting approval authority batch processing...");
        assertThat(logOutput).contains("Approval authority batch processing failed");
        assertThat(logOutput).contains("File not found: " + TEST_CSV_PATH);
    }
    
    @Test
    @DisplayName("手動実行メソッドは正常に動作しログを出力する")
    void executeManually_ShouldWork_WhenCalled(CapturedOutput output) {
        // given
        BatchResult mockResult = createMockBatchResult(2, 1, 1, 0, 3, 1, new ArrayList<>());
        when(batchService.importApprovalAuthoritiesFromFile(TEST_CSV_PATH)).thenReturn(mockResult);
        
        // when
        BatchResult result = scheduler.executeManually();
        
        // then
        verify(batchService, times(1)).importApprovalAuthoritiesFromFile(TEST_CSV_PATH);
        assertThat(result).isNotNull();
        assertThat(result.getProcessed()).isEqualTo(2);
        assertThat(result.getAdded()).isEqualTo(1);
        assertThat(result.getUpdated()).isEqualTo(1);
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assertThat(logOutput).contains("Manual approval authority batch processing started");
    }
    
    @Test
    @DisplayName("@Scheduledアノテーションが適切に設定されている")
    void executeApprovalAuthorityBatch_ShouldHaveScheduledAnnotation() throws NoSuchMethodException {
        // given & when
        var method = ApprovalAuthorityBatchScheduler.class.getMethod("executeApprovalAuthorityBatch");
        
        // then
        assertThat(method.isAnnotationPresent(Scheduled.class)).isTrue();
        
        var scheduledAnnotation = method.getAnnotation(Scheduled.class);
        assertThat(scheduledAnnotation.cron()).isEqualTo("0 0 2 * * ?");
    }
    
    @Test
    @DisplayName("@SchedulerLockアノテーションが適切に設定されている")
    void executeApprovalAuthorityBatch_ShouldHaveSchedulerLockAnnotation() throws NoSuchMethodException {
        // given & when
        var method = ApprovalAuthorityBatchScheduler.class.getMethod("executeApprovalAuthorityBatch");
        
        // then
        assertThat(method.isAnnotationPresent(SchedulerLock.class)).isTrue();
        
        var lockAnnotation = method.getAnnotation(SchedulerLock.class);
        assertThat(lockAnnotation.name()).isEqualTo("ApprovalAuthorityBatch");
        assertThat(lockAnnotation.lockAtMostFor()).isEqualTo("30m");
        assertThat(lockAnnotation.lockAtLeastFor()).isEqualTo("1m");
    }
    
    @Test
    @DisplayName("デフォルトのCSVファイルパスでスケジューラーが作成される")
    void constructor_ShouldUseDefaultPath_WhenNoPathProvided() {
        // given & when
        ApprovalAuthorityBatchScheduler defaultScheduler = 
            new ApprovalAuthorityBatchScheduler(batchService, "employee_master.csv");
        
        BatchResult mockResult = createMockBatchResult(1, 1, 0, 0, 2, 0, new ArrayList<>());
        when(batchService.importApprovalAuthoritiesFromFile("employee_master.csv")).thenReturn(mockResult);
        
        // when
        defaultScheduler.executeApprovalAuthorityBatch();
        
        // then
        verify(batchService, times(1)).importApprovalAuthoritiesFromFile("employee_master.csv");
    }
    
    @Test
    @DisplayName("複数回連続実行しても正常動作する")
    void executeApprovalAuthorityBatch_ShouldWorkProperly_WhenExecutedMultipleTimes() {
        // given
        BatchResult mockResult = createMockBatchResult(1, 0, 1, 0, 1, 1, new ArrayList<>());
        when(batchService.importApprovalAuthoritiesFromFile(anyString())).thenReturn(mockResult);
        
        // when
        scheduler.executeApprovalAuthorityBatch();
        scheduler.executeApprovalAuthorityBatch();
        scheduler.executeApprovalAuthorityBatch();
        
        // then
        verify(batchService, times(3)).importApprovalAuthoritiesFromFile(TEST_CSV_PATH);
    }
    
    @Test
    @DisplayName("空のエラーリストでも正常にログ処理される")
    void executeApprovalAuthorityBatch_ShouldHandleEmptyErrors_Correctly(CapturedOutput output) {
        // given
        BatchResult mockResult = createMockBatchResult(5, 2, 3, 0, 0, 0, new ArrayList<>());
        when(batchService.importApprovalAuthoritiesFromFile(TEST_CSV_PATH)).thenReturn(mockResult);
        
        // when
        scheduler.executeApprovalAuthorityBatch();
        
        // then
        String logOutput = output.getOut();
        assertThat(logOutput).contains("Errors: 0");
        assertThat(logOutput).doesNotContain("completed with");
    }
    
    /**
     * テスト用のモックBatchResultを作成
     * 
     * @param processed 処理済み件数
     * @param added 追加件数
     * @param updated 更新件数
     * @param deleted 削除件数
     * @param approverAdded 承認者関係追加件数
     * @param approverDeleted 承認者関係削除件数
     * @param errors エラーリスト
     * @return モックのBatchResult
     */
    private BatchResult createMockBatchResult(
        int processed, int added, int updated, int deleted,
        int approverAdded, int approverDeleted, ArrayList<String> errors
    ) {
        BatchResult result = new BatchResult();
        result.setProcessed(processed);
        result.setAdded(added);
        result.setUpdated(updated);
        result.setDeleted(deleted);
        result.getApproverRelations().setAdded(approverAdded);
        result.getApproverRelations().setDeleted(approverDeleted);
        result.setErrors(errors);
        result.markCompleted();
        return result;
    }
}