package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * SyncHistoryエンティティのテストクラス
 */
@DisplayName("SyncHistoryエンティティ")
class JiraSyncHistoryTest {
    
    @Nested
    @DisplayName("startSync()ファクトリーメソッドのテスト")
    class StartSyncTest {
        
        @Test
        @DisplayName("手動同期で正しく初期化される")
        void testStartSyncManual() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            assertThat(syncHistory.getId()).isNotNull();
            assertThat(syncHistory.getSyncType()).isEqualTo(JiraSyncType.MANUAL);
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.IN_PROGRESS);
            assertThat(syncHistory.getStartedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(syncHistory.getCompletedAt()).isNull();
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(0);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(0);
            assertThat(syncHistory.getErrorCount()).isEqualTo(0);
            assertThat(syncHistory.getErrorDetails()).isNull();
            assertThat(syncHistory.getTriggeredBy()).isEqualTo("user123");
            assertThat(syncHistory.getDetails()).isEmpty();
        }
        
        @Test
        @DisplayName("スケジュール同期で正しく初期化される")
        void testStartSyncScheduled() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "scheduler");
            
            assertThat(syncHistory.getSyncType()).isEqualTo(JiraSyncType.SCHEDULED);
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.IN_PROGRESS);
            assertThat(syncHistory.getTriggeredBy()).isEqualTo("scheduler");
        }
        
        @Test
        @DisplayName("triggeredByがnullで正しく初期化される")
        void testStartSyncWithNullTriggeredBy() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, null);
            
            assertThat(syncHistory.getTriggeredBy()).isNull();
        }
        
        @Test
        @DisplayName("triggeredByが空文字でnullとして初期化される")
        void testStartSyncWithEmptyTriggeredBy() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "  ");
            
            assertThat(syncHistory.getTriggeredBy()).isNull();
        }
        
        @Test
        @DisplayName("syncTypeがnullでIllegalArgumentExceptionがスローされる")
        void testStartSyncWithNullSyncType() {
            assertThatThrownBy(() -> JiraSyncHistory.startSync(null, "user123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("同期タイプは必須です");
        }
    }
    
    @Nested
    @DisplayName("restore()ファクトリーメソッドのテスト")
    class RestoreTest {
        
        @Test
        @DisplayName("完了した同期履歴を正しく復元できる")
        void testRestore() {
            LocalDateTime startTime = LocalDateTime.of(2023, 12, 1, 10, 0);
            LocalDateTime completedTime = LocalDateTime.of(2023, 12, 1, 10, 30);
            
            JiraSyncHistory syncHistory = JiraSyncHistory.restore(
                "test-id",
                JiraSyncType.MANUAL,
                JiraSyncStatus.COMPLETED,
                startTime,
                completedTime,
                100,
                95,
                5,
                null,
                "user123"
            );
            
            assertThat(syncHistory.getId()).isEqualTo("test-id");
            assertThat(syncHistory.getSyncType()).isEqualTo(JiraSyncType.MANUAL);
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.COMPLETED);
            assertThat(syncHistory.getStartedAt()).isEqualTo(startTime);
            assertThat(syncHistory.getCompletedAt()).isEqualTo(completedTime);
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(100);
            assertThat(syncHistory.getSuccessCount()).isEqualTo(95);
            assertThat(syncHistory.getErrorCount()).isEqualTo(5);
            assertThat(syncHistory.getErrorDetails()).isNull();
            assertThat(syncHistory.getTriggeredBy()).isEqualTo("user123");
        }
    }
    
    @Nested
    @DisplayName("同期完了のテスト")
    class CompleteSyncTest {
        
        @Test
        @DisplayName("実行中の同期を完了状態にできる")
        void testCompleteSync() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            syncHistory.completeSync();
            
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.COMPLETED);
            assertThat(syncHistory.getCompletedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }
        
        @Test
        @DisplayName("実行中以外の同期を完了しようとすると例外がスローされる")
        void testCompleteSyncWhenNotInProgress() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.completeSync();
            
            assertThatThrownBy(syncHistory::completeSync)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("実行中ではない同期を完了することはできません。現在のステータス: 完了");
        }
    }
    
    @Nested
    @DisplayName("同期失敗のテスト")
    class FailSyncTest {
        
        @Test
        @DisplayName("実行中の同期を失敗状態にできる")
        void testFailSync() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            String errorDetails = "Connection timeout";
            
            syncHistory.failSync(errorDetails);
            
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(syncHistory.getCompletedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(syncHistory.getErrorDetails()).isEqualTo("Connection timeout");
        }
        
        @Test
        @DisplayName("エラー詳細がnullで失敗状態にできる")
        void testFailSyncWithNullErrorDetails() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            syncHistory.failSync(null);
            
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(syncHistory.getErrorDetails()).isNull();
        }
        
        @Test
        @DisplayName("エラー詳細が空文字でnullとして保存される")
        void testFailSyncWithEmptyErrorDetails() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            syncHistory.failSync("  ");
            
            assertThat(syncHistory.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(syncHistory.getErrorDetails()).isNull();
        }
        
        @Test
        @DisplayName("実行中以外の同期を失敗させようとすると例外がスローされる")
        void testFailSyncWhenNotInProgress() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.completeSync();
            
            assertThatThrownBy(() -> syncHistory.failSync("error"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("実行中ではない同期を失敗させることはできません。現在のステータス: 完了");
        }
    }
    
    @Nested
    @DisplayName("カウント管理のテスト")
    class CountManagementTest {
        
        @Test
        @DisplayName("処理数を正しく増加できる")
        void testIncrementProcessed() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            syncHistory.incrementProcessed();
            syncHistory.incrementProcessed();
            
            assertThat(syncHistory.getTotalProjectsProcessed()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("成功数を正しく増加できる")
        void testIncrementSuccess() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.incrementProcessed();
            
            syncHistory.incrementSuccess();
            
            assertThat(syncHistory.getSuccessCount()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("エラー数を正しく増加できる")
        void testIncrementError() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.incrementProcessed();
            
            syncHistory.incrementError();
            
            assertThat(syncHistory.getErrorCount()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("成功数とエラー数の合計が処理数を超えると例外がスローされる")
        void testInvalidCounts() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.incrementProcessed(); // 処理数 = 1
            syncHistory.incrementSuccess(); // 成功数 = 1
            
            assertThatThrownBy(syncHistory::incrementError)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("成功数とエラー数の合計が処理総数を超えています。総数: 1, 成功: 1, エラー: 1");
        }
        
        @Test
        @DisplayName("実行中以外でカウント変更しようとすると例外がスローされる")
        void testIncrementWhenNotInProgress() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            syncHistory.completeSync();
            
            assertThatThrownBy(syncHistory::incrementProcessed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("実行中ではない同期のカウントを変更することはできません");
            
            assertThatThrownBy(syncHistory::incrementSuccess)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("実行中ではない同期のカウントを変更することはできません");
            
            assertThatThrownBy(syncHistory::incrementError)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("実行中ではない同期のカウントを変更することはできません");
        }
    }
    
    @Nested
    @DisplayName("詳細履歴管理のテスト")
    class DetailManagementTest {
        
        @Test
        @DisplayName("詳細履歴を正しく追加できる")
        void testAddDetail() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");

            syncHistory.addDetail("CREATED", com.devhour.domain.model.valueobject.DetailStatus.SUCCESS, null);

            assertThat(syncHistory.getDetails()).hasSize(1);
            assertThat(syncHistory.getDetails().get(0).getOperation()).isEqualTo("CREATED");
            assertThat(syncHistory.getDetails().get(0).getStatus()).isEqualTo(com.devhour.domain.model.valueobject.DetailStatus.SUCCESS);
        }
        
        @Test
        @DisplayName("複数の詳細履歴を追加できる")
        void testAddMultipleDetails() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");

            syncHistory.addDetail("CREATED", com.devhour.domain.model.valueobject.DetailStatus.SUCCESS, null);
            syncHistory.addDetail("UPDATED", com.devhour.domain.model.valueobject.DetailStatus.ERROR, "Error occurred");

            assertThat(syncHistory.getDetails()).hasSize(2);
        }
        
    }
    
    @Nested
    @DisplayName("状態判定のテスト")
    class StatePredicateTest {
        
        @Test
        @DisplayName("実行中かを正しく判定できる")
        void testIsInProgress() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            assertThat(syncHistory.isInProgress()).isTrue();
            
            syncHistory.completeSync();
            assertThat(syncHistory.isInProgress()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("メトリクス計算のテスト")
    class MetricsCalculationTest {
        
        @Test
        @DisplayName("実行時間を正しく計算できる")
        void testGetDurationMinutes() throws InterruptedException {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            // 短い待機（テスト用）
            Thread.sleep(100);
            syncHistory.completeSync();
            
            long duration = syncHistory.getDurationMinutes();
            assertThat(duration).isGreaterThanOrEqualTo(0);
        }
        
        @Test
        @DisplayName("実行中の同期の実行時間を計算できる")
        void testGetDurationMinutesInProgress() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            long duration = syncHistory.getDurationMinutes();
            assertThat(duration).isGreaterThanOrEqualTo(0);
        }
        
        @Test
        @DisplayName("成功率を正しく計算できる")
        void testGetSuccessRate() {
            JiraSyncHistory syncHistory = JiraSyncHistory.restore(
                "test-id", JiraSyncType.MANUAL, JiraSyncStatus.COMPLETED,
                LocalDateTime.now(), LocalDateTime.now(),
                100, 80, 20, null, "user123"
            );
            
            assertThat(syncHistory.getSuccessRate()).isEqualTo(80.0);
        }
        
        @Test
        @DisplayName("処理数が0の場合の成功率は100%")
        void testGetSuccessRateWhenNoProcessed() {
            JiraSyncHistory syncHistory = JiraSyncHistory.restore(
                "test-id", JiraSyncType.MANUAL, JiraSyncStatus.COMPLETED,
                LocalDateTime.now(), LocalDateTime.now(),
                0, 0, 0, null, "user123"
            );
            
            assertThat(syncHistory.getSuccessRate()).isEqualTo(100.0);
        }
        
        @Test
        @DisplayName("処理数がnullの場合の成功率は100%")
        void testGetSuccessRateWhenProcessedIsNull() {
            JiraSyncHistory syncHistory = JiraSyncHistory.restore(
                "test-id", JiraSyncType.MANUAL, JiraSyncStatus.COMPLETED,
                LocalDateTime.now(), LocalDateTime.now(),
                null, 0, 0, null, "user123"
            );
            
            assertThat(syncHistory.getSuccessRate()).isEqualTo(100.0);
        }
    }
    
    @Nested
    @DisplayName("等価性とハッシュコードのテスト")
    class EqualsAndHashCodeTest {
        
        @Test
        @DisplayName("同じIDのエンティティは等価")
        void testEqualsWithSameId() {
            JiraSyncHistory syncHistory1 = JiraSyncHistory.restore(
                "test-id", JiraSyncType.MANUAL, JiraSyncStatus.IN_PROGRESS,
                LocalDateTime.now(), null, 0, 0, 0, null, "user123"
            );
            JiraSyncHistory syncHistory2 = JiraSyncHistory.restore(
                "test-id", JiraSyncType.SCHEDULED, JiraSyncStatus.COMPLETED,
                LocalDateTime.now(), LocalDateTime.now(), 100, 50, 50, null, "user456"
            );
            
            assertThat(syncHistory1).isEqualTo(syncHistory2);
            assertThat(syncHistory1.hashCode()).isEqualTo(syncHistory2.hashCode());
        }
        
        @Test
        @DisplayName("異なるIDのエンティティは非等価")
        void testEqualsWithDifferentId() {
            JiraSyncHistory syncHistory1 = JiraSyncHistory.restore(
                "test-id-1", JiraSyncType.MANUAL, JiraSyncStatus.IN_PROGRESS,
                LocalDateTime.now(), null, 0, 0, 0, null, "user123"
            );
            JiraSyncHistory syncHistory2 = JiraSyncHistory.restore(
                "test-id-2", JiraSyncType.MANUAL, JiraSyncStatus.IN_PROGRESS,
                LocalDateTime.now(), null, 0, 0, 0, null, "user123"
            );
            
            assertThat(syncHistory1).isNotEqualTo(syncHistory2);
        }
        
        @Test
        @DisplayName("nullとの比較")
        void testEqualsWithNull() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            assertThat(syncHistory).isNotEqualTo(null);
        }
    }
    
    @Nested
    @DisplayName("toString()のテスト")
    class ToStringTest {
        
        @Test
        @DisplayName("toString()が適切な文字列を返す")
        void testToString() {
            JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user123");
            
            String result = syncHistory.toString();
            
            assertThat(result).contains("SyncHistory");
            assertThat(result).contains("MANUAL");
            assertThat(result).contains("IN_PROGRESS");
        }
    }
}