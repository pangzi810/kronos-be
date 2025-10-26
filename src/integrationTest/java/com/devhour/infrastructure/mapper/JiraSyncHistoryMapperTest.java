package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * JiraSyncHistoryMapperの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
@DisplayName("JiraSyncHistoryMapper統合テスト")
class JiraSyncHistoryMapperTest extends AbstractMapperTest {

    @Autowired
    private JiraSyncHistoryMapper syncHistoryMapper;


    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("同期履歴挿入 - 正常ケース（手動同期）")
    void insert_ManualSync_Success() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "test-user");

        // Act
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // Assert
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());
        assertThat(result).isPresent();
        JiraSyncHistory retrieved = result.get();
        assertThat(retrieved.getId()).isEqualTo(syncHistory.getId());
        assertThat(retrieved.getSyncType()).isEqualTo(JiraSyncType.MANUAL);
        assertThat(retrieved.getSyncStatus()).isEqualTo(JiraSyncStatus.IN_PROGRESS);
        assertThat(retrieved.getStartedAt()).isNotNull();
        assertThat(retrieved.getCompletedAt()).isNull();
        assertThat(retrieved.getTotalProjectsProcessed()).isEqualTo(0);
        assertThat(retrieved.getSuccessCount()).isEqualTo(0);
        assertThat(retrieved.getErrorCount()).isEqualTo(0);
        assertThat(retrieved.getErrorDetails()).isNull();
        assertThat(retrieved.getTriggeredBy()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("同期履歴挿入 - 正常ケース（スケジュール同期）")
    void insert_ScheduledSync_Success() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "scheduler");

        // Act
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // Assert
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());
        assertThat(result).isPresent();
        JiraSyncHistory retrieved = result.get();
        assertThat(retrieved.getSyncType()).isEqualTo(JiraSyncType.SCHEDULED);
        assertThat(retrieved.getTriggeredBy()).isEqualTo("scheduler");
    }

    @Test
    @DisplayName("同期履歴挿入 - triggeredByがnullの場合")
    void insert_NullTriggeredBy_Success() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, null);

        // Act
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // Assert
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTriggeredBy()).isNull();
    }

    @Test
    @DisplayName("ID検索 - 存在する同期履歴")
    void selectById_ExistingHistory_ReturnsHistory() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "find-test-user");
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // Act
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(syncHistory.getId());
        assertThat(result.get().getTriggeredBy()).isEqualTo("find-test-user");
    }

    @Test
    @DisplayName("ID検索 - 存在しない同期履歴")
    void selectById_NonExistingHistory_ReturnsEmpty() {
        // Act
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("同期履歴更新 - 正常完了")
    void update_CompleteSync_Success() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "update-test-user");
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // 同期を完了状態にする
        syncHistory.incrementProcessed();
        syncHistory.incrementSuccess();
        syncHistory.completeSync();

        // Act
        int updateCount = syncHistoryMapper.update(
            syncHistory.getId(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails()
        );

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());
        assertThat(result).isPresent();
        JiraSyncHistory updated = result.get();
        assertThat(updated.getSyncStatus()).isEqualTo(JiraSyncStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(updated.getTotalProjectsProcessed()).isEqualTo(1);
        assertThat(updated.getSuccessCount()).isEqualTo(1);
        assertThat(updated.getErrorCount()).isEqualTo(0);
        assertThat(updated.getErrorDetails()).isNull();
    }

    @Test
    @DisplayName("同期履歴更新 - エラー発生")
    void update_FailSync_Success() {
        // Arrange
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "fail-test-user");
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );

        // 同期を失敗状態にする
        syncHistory.incrementProcessed();
        syncHistory.incrementError();
        syncHistory.failSync("Test error occurred");

        // Act
        int updateCount = syncHistoryMapper.update(
            syncHistory.getId(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails()
        );

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<JiraSyncHistory> result = syncHistoryMapper.selectById(syncHistory.getId());
        assertThat(result).isPresent();
        JiraSyncHistory updated = result.get();
        assertThat(updated.getSyncStatus()).isEqualTo(JiraSyncStatus.FAILED);
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(updated.getTotalProjectsProcessed()).isEqualTo(1);
        assertThat(updated.getSuccessCount()).isEqualTo(0);
        assertThat(updated.getErrorCount()).isEqualTo(1);
        assertThat(updated.getErrorDetails()).isEqualTo("Test error occurred");
    }

    @Test
    @DisplayName("最近の同期履歴取得 - ページネーション")
    void selectRecentWithPagination_MultipleHistories_ReturnsSortedByStartTime() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        
        // 異なる時刻の同期履歴を作成（時系列順）
        JiraSyncHistory history1 = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user1");
        JiraSyncHistory history2 = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "user2"); 
        JiraSyncHistory history3 = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user3");

        // 時間差を作るため各履歴の開始時刻を調整
        insertSyncHistoryWithTime(history1, baseTime.minusHours(2));
        insertSyncHistoryWithTime(history2, baseTime.minusHours(1));
        insertSyncHistoryWithTime(history3, baseTime);

        // Act
        List<JiraSyncHistory> firstPage = syncHistoryMapper.selectRecentWithPagination(2, 0, null);
        List<JiraSyncHistory> secondPage = syncHistoryMapper.selectRecentWithPagination(2, 2, null);

        // Assert
        assertThat(firstPage).hasSize(2);
        // 降順（最新から順）で並んでいることを確認
        assertThat(firstPage.get(0).getTriggeredBy()).isEqualTo("user3"); // 最新
        assertThat(firstPage.get(1).getTriggeredBy()).isEqualTo("user2");
        
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTriggeredBy()).isEqualTo("user1"); // 最古
    }

    @Test
    @DisplayName("実行中同期取得")
    void selectInProgress_MultipleStatuses_ReturnsOnlyInProgress() {
        // Arrange
        JiraSyncHistory inProgressHistory1 = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "progress-user1");
        JiraSyncHistory inProgressHistory2 = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "progress-user2");
        JiraSyncHistory completedHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "completed-user");
        completedHistory.completeSync();

        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        insertSyncHistoryWithTime(inProgressHistory1, baseTime.minusMinutes(30));
        insertSyncHistoryWithTime(inProgressHistory2, baseTime.minusMinutes(10));
        
        syncHistoryMapper.insert(
            completedHistory.getId(),
            completedHistory.getSyncType().getValue(),
            completedHistory.getSyncStatus().getValue(),
            completedHistory.getStartedAt(),
            completedHistory.getCompletedAt(),
            completedHistory.getTotalProjectsProcessed(),
            completedHistory.getSuccessCount(),
            completedHistory.getErrorCount(),
            completedHistory.getErrorDetails(),
            completedHistory.getTriggeredBy()
        );

        // Act
        List<JiraSyncHistory> result = syncHistoryMapper.selectInProgress();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(history -> history.getSyncStatus() == JiraSyncStatus.IN_PROGRESS);
        // 昇順（古い実行から順）で並んでいることを確認
        assertThat(result.get(0).getTriggeredBy()).isEqualTo("progress-user1");
        assertThat(result.get(1).getTriggeredBy()).isEqualTo("progress-user2");
    }

    @Test
    @DisplayName("日付範囲での同期履歴検索")
    void selectByDateRange_WithinRange_ReturnsMatchingHistories() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        LocalDateTime startRange = baseTime.minusDays(1);
        LocalDateTime endRange = baseTime.plusDays(1);
        
        JiraSyncHistory historyInRange1 = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "in-range-1");
        JiraSyncHistory historyInRange2 = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "in-range-2");
        JiraSyncHistory historyOutOfRange = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "out-of-range");

        insertSyncHistoryWithTime(historyInRange1, baseTime.minusHours(12));
        insertSyncHistoryWithTime(historyInRange2, baseTime.minusHours(6));
        insertSyncHistoryWithTime(historyOutOfRange, baseTime.minusDays(2)); // 範囲外

        // Act
        List<JiraSyncHistory> result = syncHistoryMapper.selectByDateRange(startRange, endRange, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(JiraSyncHistory::getTriggeredBy))
            .containsExactly("in-range-2", "in-range-1"); // 降順
    }

    @Test
    @DisplayName("存在しないIDの更新")
    void update_NonExistingId_ReturnsZero() {
        // Act
        int updateCount = syncHistoryMapper.update(
            "non-existing-id",
            JiraSyncStatus.COMPLETED.getValue(),
            LocalDateTime.now(),
            1,
            1,
            0,
            null
        );

        // Assert
        assertThat(updateCount).isEqualTo(0);
    }

    @Test
    @DisplayName("空の日付範囲検索")
    void selectByDateRange_NoMatchingData_ReturnsEmpty() {
        // Arrange
        LocalDateTime futureStart = LocalDateTime.now().plusDays(30);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(31);

        // Act
        List<JiraSyncHistory> result = syncHistoryMapper.selectByDateRange(futureStart, futureEnd, null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("実行中同期なしの場合")
    void selectInProgress_NoInProgressSync_ReturnsEmpty() {
        // Arrange - 完了した同期のみ作成
        JiraSyncHistory completedHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "completed-only");
        completedHistory.completeSync();
        syncHistoryMapper.insert(
            completedHistory.getId(),
            completedHistory.getSyncType().getValue(),
            completedHistory.getSyncStatus().getValue(),
            completedHistory.getStartedAt(),
            completedHistory.getCompletedAt(),
            completedHistory.getTotalProjectsProcessed(),
            completedHistory.getSuccessCount(),
            completedHistory.getErrorCount(),
            completedHistory.getErrorDetails(),
            completedHistory.getTriggeredBy()
        );

        // Act
        List<JiraSyncHistory> result = syncHistoryMapper.selectInProgress();

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * ヘルパーメソッド: 指定時刻で同期履歴を挿入
     */
    private void insertSyncHistoryWithTime(JiraSyncHistory syncHistory, LocalDateTime startedAt) {
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            startedAt,
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );
    }
}