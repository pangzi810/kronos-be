package com.devhour.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import com.devhour.application.service.JiraSyncApplicationService;
import com.devhour.domain.exception.JiraAuthenticationException;
import com.devhour.domain.exception.JiraSyncException;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * JiraSyncScheduler のユニットテスト
 * 
 * テストカバレッジ要件: 80%以上
 * 
 * テストシナリオ:
 * - 正常な同期実行
 * - スケジューラー無効時の動作確認
 * - JiraAuthenticationException発生時の処理
 * - JiraSyncException発生時の処理
 * - 予期しないException発生時の処理
 * - ShedLockによる重複実行防止
 * - 適切なログ出力の確認
 */
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@DisplayName("JiraSyncScheduler Tests")
class JiraSyncSchedulerTest {
    
    @Mock
    private JiraSyncApplicationService jiraSyncApplicationService;
    
    @InjectMocks
    private JiraSyncScheduler jiraSyncScheduler;
    
    @BeforeEach
    void setUp() {
        // テスト前の初期化処理
    }
    
    @Test
    @DisplayName("正常な同期実行が成功する")
    void executeSync_ShouldSucceed_WhenNormalOperation(CapturedOutput output) {
        // given
        JiraSyncHistory mockSyncHistory = createMockSyncHistory(JiraSyncStatus.COMPLETED);
        when(jiraSyncApplicationService.executeSync()).thenReturn(mockSyncHistory);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期スケジューラー実行開始"));
        assert(logOutput.contains("JIRA同期スケジューラー実行完了"));
        assert(logOutput.contains("同期ステータス: COMPLETED"));
    }
    
    @Test
    @DisplayName("JiraAuthenticationException発生時は認証エラーログを出力する")
    void executeSync_ShouldLogAuthenticationError_WhenAuthenticationFails(CapturedOutput output) {
        // given
        JiraAuthenticationException exception = new JiraAuthenticationException("認証に失敗しました", 401);
        when(jiraSyncApplicationService.executeSync()).thenThrow(exception);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA認証エラーが発生"));
        assert(logOutput.contains("認証に失敗しました"));
        assert(logOutput.contains("ステータスコード: 401"));
        assert(!logOutput.contains("JIRA同期スケジューラー実行完了")); // 正常完了ログは出力されない
    }
    
    @Test
    @DisplayName("JiraSyncException発生時は同期エラーログを出力する")
    void executeSync_ShouldLogSyncError_WhenSyncFails(CapturedOutput output) {
        // given
        JiraSyncException exception = new JiraSyncException("同期処理でエラーが発生しました");
        when(jiraSyncApplicationService.executeSync()).thenThrow(exception);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期処理でエラーが発生"));
        assert(logOutput.contains("同期処理でエラーが発生しました"));
        assert(!logOutput.contains("JIRA同期スケジューラー実行完了")); // 正常完了ログは出力されない
    }
    
    @Test
    @DisplayName("予期しないException発生時は予期しないエラーログを出力する")
    void executeSync_ShouldLogUnexpectedError_WhenUnexpectedExceptionOccurs(CapturedOutput output) {
        // given
        RuntimeException exception = new RuntimeException("予期しないエラーが発生しました");
        when(jiraSyncApplicationService.executeSync()).thenThrow(exception);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期スケジューラーで予期しないエラーが発生"));
        assert(logOutput.contains("予期しないエラーが発生しました"));
        assert(!logOutput.contains("JIRA同期スケジューラー実行完了")); // 正常完了ログは出力されない
    }
    
    @Test
    @DisplayName("同期処理が失敗した場合でもエラーログを出力する")
    void executeSync_ShouldLogError_WhenSyncHistoryIndicatesFailure(CapturedOutput output) {
        // given
        JiraSyncHistory mockSyncHistory = createMockSyncHistory(JiraSyncStatus.FAILED);
        when(jiraSyncApplicationService.executeSync()).thenReturn(mockSyncHistory);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期スケジューラー実行開始"));
        assert(logOutput.contains("同期処理が失敗で完了"));
        assert(logOutput.contains("同期ステータス: FAILED"));
        assert(!logOutput.contains("JIRA同期スケジューラー実行完了")); // 正常完了ログは出力されない
    }
    
    @Test
    @DisplayName("同期処理が進行中の場合は進行中ログを出力する")
    void executeSync_ShouldLogInProgress_WhenSyncHistoryIndicatesInProgress(CapturedOutput output) {
        // given
        JiraSyncHistory mockSyncHistory = createMockSyncHistory(JiraSyncStatus.IN_PROGRESS);
        when(jiraSyncApplicationService.executeSync()).thenReturn(mockSyncHistory);
        
        // when
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期スケジューラー実行開始"));
        assert(logOutput.contains("同期処理が進行中で完了"));
        assert(logOutput.contains("同期ステータス: IN_PROGRESS"));
    }
    
    @Test
    @DisplayName("ShedLockが適切に設定されていることを確認")
    void executeSync_ShouldHaveScheduledLockAnnotation() throws NoSuchMethodException {
        // given & when
        var method = JiraSyncScheduler.class.getMethod("executeSync");
        
        // then
        assert(method.isAnnotationPresent(net.javacrumbs.shedlock.spring.annotation.SchedulerLock.class));
        
        var lockAnnotation = method.getAnnotation(net.javacrumbs.shedlock.spring.annotation.SchedulerLock.class);
        assert(lockAnnotation.name().equals("JiraSyncScheduler.executeSync"));
    }
    
    @Test
    @DisplayName("Scheduledアノテーションが適切に設定されていることを確認")
    void executeSync_ShouldHaveScheduledAnnotation() throws NoSuchMethodException {
        // given & when
        var method = JiraSyncScheduler.class.getMethod("executeSync");
        
        // then
        assert(method.isAnnotationPresent(org.springframework.scheduling.annotation.Scheduled.class));
        
        var scheduledAnnotation = method.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assert(scheduledAnnotation.cron().equals("${jira.sync.scheduler.cron}"));
    }
    
    @Test
    @DisplayName("同期履歴がnullの場合でも例外が発生しない")
    void executeSync_ShouldNotThrow_WhenSyncHistoryIsNull(CapturedOutput output) {
        // given
        when(jiraSyncApplicationService.executeSync()).thenReturn(null);
        
        // when & then (例外が発生しないことを確認)
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(1)).executeSync();
        
        // ログメッセージの確認
        String logOutput = output.getOut();
        assert(logOutput.contains("JIRA同期スケジューラー実行開始"));
        assert(logOutput.contains("同期履歴がnullです"));
    }
    
    @Test
    @DisplayName("複数回連続実行しても正常動作する")
    void executeSync_ShouldWorkProperly_WhenExecutedMultipleTimes() {
        // given
        JiraSyncHistory mockSyncHistory = createMockSyncHistory(JiraSyncStatus.COMPLETED);
        when(jiraSyncApplicationService.executeSync()).thenReturn(mockSyncHistory);
        
        // when
        jiraSyncScheduler.executeSync();
        jiraSyncScheduler.executeSync();
        jiraSyncScheduler.executeSync();
        
        // then
        verify(jiraSyncApplicationService, times(3)).executeSync();
    }
    
    /**
     * モックのSyncHistoryオブジェクトを作成
     * 
     * @param status 同期ステータス
     * @return モックのSyncHistory
     */
    private JiraSyncHistory createMockSyncHistory(JiraSyncStatus status) {
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "scheduler");
        
        // 処理カウントをシミュレート（10件処理）- 完了前に実行する必要がある
        for (int i = 0; i < 10; i++) {
            syncHistory.incrementProcessed();
            if (status == JiraSyncStatus.COMPLETED || i < 7) {
                syncHistory.incrementSuccess();
            } else {
                syncHistory.incrementError();
            }
        }
        
        // カウント更新後にステータスを変更
        if (status == JiraSyncStatus.COMPLETED) {
            syncHistory.completeSync();
        } else if (status == JiraSyncStatus.FAILED) {
            syncHistory.failSync("テストエラー");
        }
        
        return syncHistory;
    }
}