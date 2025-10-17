package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * SyncHistoryとSyncHistoryDetailの統合テストクラス
 */
@DisplayName("SyncHistoryとSyncHistoryDetailの統合テスト")
class JiraSyncHistoryIntegrationTest {
    
    @Nested
    @DisplayName("同期履歴と詳細の関連性テスト")
    class SyncHistoryDetailRelationshipTest {
        
        @Test
        @DisplayName("同期履歴に複数の詳細を追加して完了できる")
        void testCompleteWorkflowWithMultipleDetails() {
            // 同期開始
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            assertThat(syncHistory.isInProgress()).isTrue();
            assertThat(syncHistory.getDetails()).isEmpty();
            
            // 1件目: プロジェクト新規作成成功
            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();
            
            // 2件目: プロジェクト更新成功
            syncHistory.incrementProcessed();
            syncHistory.addDetail("UPDATED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();
            
            // 3件目: エラー発生
            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.ERROR, "Validation failed");
            syncHistory.incrementError();
            
            // 4件目: スキップ
            syncHistory.incrementProcessed();
            syncHistory.addDetail("SKIPPED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();
            
            // 同期完了
            syncHistory.completeSync();
            
            // 検証
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.COMPLETED);
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(4);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(3);
            assertThat(syncHistory.getErrorCount()).isEqualTo(1);
            assertThat(syncHistory.getSuccessRate()).isEqualTo(75.0);
            assertThat(syncHistory.getDetails()).hasSize(4);
            
            // 詳細の検証
            List<JiraSyncHistoryDetail> details = syncHistory.getDetails();
            assertThat(details.get(0).getOperation()).isEqualTo("CREATED");
            assertThat(details.get(0).getStatus()).isEqualTo(DetailStatus.SUCCESS);
            // Note: hasProject() method no longer exists

            assertThat(details.get(1).getOperation()).isEqualTo("UPDATED");
            assertThat(details.get(1).getStatus()).isEqualTo(DetailStatus.SUCCESS);
            // Note: isDataModifying() method no longer exists

            assertThat(details.get(2).getOperation()).isEqualTo("CREATED");
            assertThat(details.get(2).getStatus()).isEqualTo(DetailStatus.ERROR);
            assertThat(details.get(2).getResult()).isEqualTo("Validation failed");
            // Note: hasProject() method no longer exists

            assertThat(details.get(3).getOperation()).isEqualTo("SKIPPED");
            assertThat(details.get(3).getStatus()).isEqualTo(DetailStatus.SUCCESS);
            // Note: isDataModifying() method no longer exists
        }
        
        @Test
        @DisplayName("同期失敗時の詳細管理")
        void testFailedSyncWithDetails() {
            // 同期開始
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "scheduler");
            
            // いくつかの詳細を追加
            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();

            syncHistory.incrementProcessed();
            syncHistory.addDetail("UPDATED", DetailStatus.ERROR, "Network timeout");
            syncHistory.incrementError();
            
            // 同期失敗
            syncHistory.failSync("JIRA API connection lost");
            
            // 検証
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(syncHistory.getErrorDetails()).isEqualTo("JIRA API connection lost");
            assertThat(syncHistory.getCompletedAt()).isNotNull();
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(2);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(1);
            assertThat(syncHistory.getErrorCount()).isEqualTo(1);
            assertThat(syncHistory.getSuccessRate()).isEqualTo(50.0);
            assertThat(syncHistory.getDetails()).hasSize(2);
        }
        
        @Test
        @DisplayName("詳細レコードからの処理時間計算")
        void testProcessingTimeCalculationFromDetails() {
            // 同期開始
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            LocalDateTime syncStartTime = syncHistory.getStartedAt();
            
            // 詳細レコード作成（少し後の時刻で）
            try {
                Thread.sleep(10); // 10ミリ秒待機
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);

            // 処理時間計算
            JiraSyncHistoryDetail detail = syncHistory.getDetails().get(0);
            long processingTime = detail.getProcessingTime(syncStartTime);

            assertThat(processingTime).isGreaterThanOrEqualTo(0);
        }
    }
    
    @Nested
    @DisplayName("ビジネスルールの統合テスト")
    class BusinessRulesIntegrationTest {
        
        @Test
        @DisplayName("同期タイプとアクションの組み合わせテスト")
        void testSyncTypeAndActionCombinations() {
            // 手動同期
            JiraSyncHistory manualSync = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "admin");
            manualSync.addDetail("CREATED", DetailStatus.SUCCESS, null);

            // スケジュール同期
            JiraSyncHistory scheduledSync = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "scheduler");
            scheduledSync.addDetail("UPDATED", DetailStatus.SUCCESS, null);

            // 検証
            assertThat(manualSync.getSyncType().isManual()).isTrue();
            assertThat(manualSync.getDetails().get(0).getOperation()).isEqualTo("CREATED");

            assertThat(scheduledSync.getSyncType().isScheduled()).isTrue();
            assertThat(scheduledSync.getDetails().get(0).getOperation()).isEqualTo("UPDATED");
        }
        
        @Test
        @DisplayName("エラーハンドリングの統合テスト")
        void testErrorHandlingIntegration() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            // 複数のエラーパターンをテスト
            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.ERROR, "Network timeout");
            syncHistory.incrementError();

            syncHistory.incrementProcessed();
            syncHistory.addDetail("UPDATED", DetailStatus.ERROR, "Invalid data format");
            syncHistory.incrementError();

            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.ERROR, null); // result can be null
            syncHistory.incrementError();
            
            // 同期失敗
            syncHistory.failSync("Too many errors occurred");
            
            // 検証
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(syncHistory.getErrorDetails()).isEqualTo("Too many errors occurred");
            assertThat(syncHistory.getErrorCount()).isEqualTo(3);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(0);
            assertThat(syncHistory.getSuccessRate()).isEqualTo(0.0);
            
            List<JiraSyncHistoryDetail> details = syncHistory.getDetails();
            assertThat(details).allMatch(detail -> detail.getStatus().isError());
            // Note: hasProject() method no longer exists

            // 個別エラーの検証
            assertThat(details.get(0).getResult()).isEqualTo("Network timeout");
            assertThat(details.get(1).getResult()).isEqualTo("Invalid data format");
            assertThat(details.get(2).getResult()).isNull();
            // Note: hasJiraIssueKey() method no longer exists
        }
        
        @Test
        @DisplayName("実行時間とメトリクスの統合計算")
        void testDurationAndMetricsIntegration() throws InterruptedException {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            LocalDateTime startTime = syncHistory.getStartedAt();
            
            // 複数の詳細を段階的に追加
            Thread.sleep(50); // 50ミリ秒待機
            
            syncHistory.incrementProcessed();
            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();

            Thread.sleep(50); // さらに50ミリ秒待機

            syncHistory.incrementProcessed();
            syncHistory.addDetail("UPDATED", DetailStatus.SUCCESS, null);
            syncHistory.incrementSuccess();
            
            syncHistory.completeSync();
            
            // メトリクス検証
            assertThat(syncHistory.getDurationMinutes()).isGreaterThanOrEqualTo(0);
            assertThat(syncHistory.getSuccessRate()).isEqualTo(100.0);
            
            // 各詳細の処理時間検証
            JiraSyncHistoryDetail detail1 = syncHistory.getDetails().get(0);
            JiraSyncHistoryDetail detail2 = syncHistory.getDetails().get(1);
            long processingTime1 = detail1.getProcessingTime(startTime);
            long processingTime2 = detail2.getProcessingTime(startTime);

            assertThat(processingTime1).isGreaterThanOrEqualTo(0);
            assertThat(processingTime2).isGreaterThan(processingTime1); // detail2の方が後に処理されているため
        }
    }
    
    @Nested
    @DisplayName("データ整合性の統合テスト")
    class DataIntegrityIntegrationTest {
        
        @Test
        @DisplayName("同期履歴と詳細のIDの整合性")
        void testIdConsistency() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            String syncHistoryId = syncHistory.getId();
            
            // 複数の詳細を追加
            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
            syncHistory.addDetail("UPDATED", DetailStatus.ERROR, "Error");
            
            // ID整合性の検証
            List<JiraSyncHistoryDetail> details = syncHistory.getDetails();
            assertThat(details.get(0).getSyncHistoryId()).isEqualTo(syncHistoryId);
            assertThat(details.get(1).getSyncHistoryId()).isEqualTo(syncHistoryId);

            assertThat(details).allMatch(detail ->
                detail.getSyncHistoryId().equals(syncHistoryId));
        }
        
        @Test
        @DisplayName("カウント数の整合性検証")
        void testCountConsistency() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            // 詳細レコードを追加してカウントを更新
            for (int i = 1; i <= 5; i++) {
                syncHistory.incrementProcessed();
                
                if (i <= 3) {
                    // 成功レコード
                    syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
                    syncHistory.incrementSuccess();
                } else {
                    // エラーレコード
                    syncHistory.addDetail("UPDATED", DetailStatus.ERROR, "Error " + i);
                    syncHistory.incrementError();
                }
            }
            
            syncHistory.completeSync();
            
            // 整合性検証
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(5);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(3);
            assertThat(syncHistory.getErrorCount()).isEqualTo(2);
            assertThat(syncHistory.getDetails()).hasSize(5);
            assertThat(syncHistory.getSuccessRate()).isEqualTo(60.0);
            
            // 詳細レコードの状態検証
            List<JiraSyncHistoryDetail> details = syncHistory.getDetails();
            long successCount = details.stream().filter(JiraSyncHistoryDetail::isSuccess).count();
            long errorCount = details.stream().filter(JiraSyncHistoryDetail::isError).count();
            
            assertThat(successCount).isEqualTo(3L);
            assertThat(errorCount).isEqualTo(2L);
            assertThat(successCount + errorCount).isEqualTo(syncHistory.getTotalProjectsProcessed().longValue());
        }
        
        @Test
        @DisplayName("タイムスタンプの整合性検証")
        void testTimestampConsistency() throws InterruptedException {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            LocalDateTime syncStartTime = syncHistory.getStartedAt();
            
            Thread.sleep(10); // 少し待機

            syncHistory.addDetail("CREATED", DetailStatus.SUCCESS, null);
            
            Thread.sleep(10); // 少し待機
            
            syncHistory.completeSync();
            
            // タイムスタンプ順序の検証
            JiraSyncHistoryDetail detail = syncHistory.getDetails().get(0);
            assertThat(detail.getProcessedAt()).isAfter(syncStartTime);
            assertThat(syncHistory.getCompletedAt()).isAfter(detail.getProcessedAt());

            // 処理時間の妥当性検証
            assertThat(detail.getProcessingTime(syncStartTime)).isGreaterThan(0);
            assertThat(syncHistory.getDurationMinutes()).isGreaterThanOrEqualTo(0);
        }
    }
}