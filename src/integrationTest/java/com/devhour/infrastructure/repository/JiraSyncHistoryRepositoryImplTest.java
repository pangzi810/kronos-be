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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.infrastructure.mapper.JiraSyncHistoryDetailMapper;
import com.devhour.infrastructure.mapper.JiraSyncHistoryMapper;

/**
 * SyncHistoryRepositoryImplのテストクラス
 * 
 * 同期履歴リポジトリの実装クラスをテスト
 * MyBatisマッパーとの連携、パラメータ検証、エラーハンドリングを検証
 * 
 * テスト観点:
 * - 基本的なCRUD操作
 * - 日付範囲での検索機能
 * - ステータス・トリガータイプでのフィルタリング
 * - ページネーション機能
 * - 詳細履歴との関連付け
 * - パラメータ検証とエラーハンドリング
 * - 統計情報の取得
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncHistoryRepositoryImpl テスト")
class JiraSyncHistoryRepositoryImplTest {
    
    @Mock
    private JiraSyncHistoryMapper syncHistoryMapper;
    
    @Mock
    private JiraSyncHistoryDetailMapper syncHistoryDetailMapper;
    
    @InjectMocks
    private JiraSyncHistoryRepositoryImpl repository;
    
    private JiraSyncHistory testSyncHistory;
    private JiraSyncHistoryDetail testDetail;
    
    @BeforeEach
    void setUp() {
        testSyncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");
        testDetail = JiraSyncHistoryDetail.createSuccess(testSyncHistory.getId(), 1, "UPDATED", null);
    }
    
    // ========================================
    // findById テスト
    // ========================================
    
    @Test
    @DisplayName("findById - 正常系: 同期履歴が存在する場合")
    void findById_Success_WhenSyncHistoryExists() {
        // Given
        String syncId = "sync-123";
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.of(testSyncHistory));
        
        // When
        Optional<JiraSyncHistory> result = repository.findById(syncId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testSyncHistory, result.get());
        verify(syncHistoryMapper).selectById(syncId);
    }
    
    @Test
    @DisplayName("findById - 正常系: 同期履歴が存在しない場合")
    void findById_Success_WhenSyncHistoryNotExists() {
        // Given
        String syncId = "non-existent";
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraSyncHistory> result = repository.findById(syncId);
        
        // Then
        assertFalse(result.isPresent());
        verify(syncHistoryMapper).selectById(syncId);
    }
    
    @Test
    @DisplayName("findById - 異常系: IDがnullの場合")
    void findById_ThrowException_WhenIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findById(null)
        );
        assertEquals("同期履歴IDは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectById(any());
    }
    
    // ========================================
    // findWithDetails テスト
    // ========================================
    
    @Test
    @DisplayName("findWithDetails - 正常系: 詳細履歴付きで同期履歴を取得")
    void findWithDetails_Success_WhenSyncHistoryWithDetailsExists() {
        // Given
        String syncId = "sync-123";
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);
        
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.of(testSyncHistory));
        when(syncHistoryDetailMapper.selectBySyncHistoryId(syncId)).thenReturn(details);
        
        // When
        Optional<JiraSyncHistory> result = repository.findWithDetails(syncId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testSyncHistory, result.get());
        assertEquals(1, result.get().getDetails().size());
        verify(syncHistoryMapper).selectById(syncId);
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(syncId);
    }
    
    @Test
    @DisplayName("findWithDetails - 正常系: 同期履歴が存在しない場合")
    void findWithDetails_Success_WhenSyncHistoryNotExists() {
        // Given
        String syncId = "non-existent";
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.empty());
        
        // When
        Optional<JiraSyncHistory> result = repository.findWithDetails(syncId);
        
        // Then
        assertFalse(result.isPresent());
        verify(syncHistoryMapper).selectById(syncId);
        verify(syncHistoryDetailMapper, never()).selectBySyncHistoryId(any());
    }
    
    // ========================================
    // findRecent テスト
    // ========================================
    
    @Test
    @DisplayName("findRecent - 正常系: 最近30日間の同期履歴を取得")
    void findRecent_Success_ReturnsRecentSyncHistories() {
        // Given
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        when(syncHistoryMapper.selectByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(expectedHistories);
        
        // When
        List<JiraSyncHistory> result = repository.findRecent();
        
        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }
    
    @Test
    @DisplayName("findRecent - 正常系: 履歴が存在しない場合")
    void findRecent_Success_WhenNoHistoriesExist() {
        // Given
        when(syncHistoryMapper.selectByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(Collections.emptyList());
        
        // When
        List<JiraSyncHistory> result = repository.findRecent();
        
        // Then
        assertTrue(result.isEmpty());
        verify(syncHistoryMapper).selectByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }
    
    // ========================================
    // findByDateRange テスト
    // ========================================
    
    @Test
    @DisplayName("findByDateRange - 正常系: 指定期間の同期履歴を取得")
    void findByDateRange_Success_ReturnsHistoriesInRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);

        when(syncHistoryMapper.selectByDateRange(start, end, null)).thenReturn(expectedHistories);
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(details);

        // When
        List<JiraSyncHistory> result = repository.findByDateRange(start, end, null);

        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectByDateRange(start, end, null);
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
    }
    
    @Test
    @DisplayName("findByDateRange - 異常系: 開始日時がnullの場合")
    void findByDateRange_ThrowException_WhenStartDateIsNull() {
        // Given
        LocalDateTime end = LocalDateTime.now();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByDateRange(null, end, null)
        );
        assertEquals("開始日時は必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectByDateRange(any(), any(), any());
    }
    
    @Test
    @DisplayName("findByDateRange - 異常系: 終了日時がnullの場合")
    void findByDateRange_ThrowException_WhenEndDateIsNull() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByDateRange(start, null, null)
        );
        assertEquals("終了日時は必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectByDateRange(any(), any(), any());
    }
    
    @Test
    @DisplayName("findByDateRange - 異常系: 開始日時が終了日時より後の場合")
    void findByDateRange_ThrowException_WhenStartDateAfterEndDate() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByDateRange(start, end, null)
        );
        assertEquals("開始日時は終了日時より前である必要があります", exception.getMessage());
        verify(syncHistoryMapper, never()).selectByDateRange(any(), any(), any());
    }
    
    // ========================================
    // findByStatus テスト
    // ========================================
    
    @Test
    @DisplayName("findByStatus - 正常系: 指定ステータスの同期履歴を取得")
    void findByStatus_Success_ReturnsHistoriesWithSpecifiedStatus() {
        // Given
        JiraSyncStatus status = JiraSyncStatus.COMPLETED;
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);

        when(syncHistoryMapper.selectByStatus(status.getValue())).thenReturn(expectedHistories);
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(details);

        // When
        List<JiraSyncHistory> result = repository.findByStatus(status);

        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectByStatus(status.getValue());
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
    }
    
    @Test
    @DisplayName("findByStatus - 異常系: ステータスがnullの場合")
    void findByStatus_ThrowException_WhenStatusIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByStatus(null)
        );
        assertEquals("同期ステータスは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectByStatus(any());
    }
    
    // ========================================
    // findByTriggerType テスト
    // ========================================
    
    @Test
    @DisplayName("findByTriggerType - 正常系: 指定トリガータイプの同期履歴を取得")
    void findByTriggerType_Success_ReturnsHistoriesWithSpecifiedTriggerType() {
        // Given
        JiraSyncType triggerType = JiraSyncType.MANUAL;
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);

        when(syncHistoryMapper.selectByTriggerType(triggerType.getValue())).thenReturn(expectedHistories);
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(details);

        // When
        List<JiraSyncHistory> result = repository.findByTriggerType(triggerType);

        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectByTriggerType(triggerType.getValue());
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
    }
    
    @Test
    @DisplayName("findByTriggerType - 異常系: トリガータイプがnullの場合")
    void findByTriggerType_ThrowException_WhenTriggerTypeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByTriggerType(null)
        );
        assertEquals("トリガータイプは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectByTriggerType(any());
    }
    
    // ========================================
    // findInProgress テスト
    // ========================================
    
    @Test
    @DisplayName("findInProgress - 正常系: 実行中の同期履歴を取得")
    void findInProgress_Success_ReturnsInProgressSyncHistories() {
        // Given
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);
        when(syncHistoryMapper.selectInProgress()).thenReturn(expectedHistories);
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(details);

        // When
        List<JiraSyncHistory> result = repository.findInProgress();

        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectInProgress();
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
    }
    
    // ========================================
    // findWithPagination テスト
    // ========================================
    
    @Test
    @DisplayName("findWithPagination - 正常系: ページネーションで同期履歴を取得")
    void findWithPagination_Success_ReturnsPagedSyncHistories() {
        // Given
        int limit = 10;
        int offset = 0;
        List<JiraSyncHistory> expectedHistories = Arrays.asList(testSyncHistory);
        List<JiraSyncHistoryDetail> details = Arrays.asList(testDetail);

        when(syncHistoryMapper.selectRecentWithPagination(limit, offset, null)).thenReturn(expectedHistories);
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(details);

        // When
        List<JiraSyncHistory> result = repository.findWithPagination(limit, offset, null);

        // Then
        assertEquals(expectedHistories, result);
        verify(syncHistoryMapper).selectRecentWithPagination(limit, offset, null);
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
    }
    
    @Test
    @DisplayName("findWithPagination - 異常系: limitが負の値の場合")
    void findWithPagination_ThrowException_WhenLimitIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findWithPagination(-1, 0, null)
        );
        assertEquals("取得件数は0以上である必要があります", exception.getMessage());
        verify(syncHistoryMapper, never()).selectRecentWithPagination(anyInt(), anyInt(), any());
    }
    
    @Test
    @DisplayName("findWithPagination - 異常系: offsetが負の値の場合")
    void findWithPagination_ThrowException_WhenOffsetIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findWithPagination(10, -1, null)
        );
        assertEquals("取得開始位置は0以上である必要があります", exception.getMessage());
        verify(syncHistoryMapper, never()).selectRecentWithPagination(anyInt(), anyInt(), any());
    }
    
    // ========================================
    // save テスト
    // ========================================
    
    @Test
    @DisplayName("save - 正常系: 新規同期履歴の保存")
    void save_Success_WhenSavingNewSyncHistory() {
        // Given
        testSyncHistory.addDetail("CREATE", testDetail.getStatus(), "Project created");
        when(syncHistoryMapper.selectById(testSyncHistory.getId())).thenReturn(Optional.empty());
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(Collections.emptyList());

        // When
        JiraSyncHistory result = repository.save(testSyncHistory);

        // Then
        assertEquals(testSyncHistory, result);
        verify(syncHistoryMapper).selectById(testSyncHistory.getId());
        verify(syncHistoryMapper).insert(
            eq(testSyncHistory.getId()),
            eq(testSyncHistory.getSyncType().getValue()),
            eq(testSyncHistory.getSyncStatus().getValue()),
            eq(testSyncHistory.getStartedAt()),
            eq(testSyncHistory.getCompletedAt()),
            eq(testSyncHistory.getTotalProjectsProcessed()),
            eq(testSyncHistory.getSuccessCount()),
            eq(testSyncHistory.getErrorCount()),
            eq(testSyncHistory.getErrorDetails()),
            eq(testSyncHistory.getTriggeredBy())
        );
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
        verify(syncHistoryDetailMapper).insert(
            any(String.class),
            eq(testSyncHistory.getId()),
            any(Integer.class),
            eq("CREATE"),
            any(String.class),
            eq("Project created"),
            any(LocalDateTime.class)
        );
    }
    
    @Test
    @DisplayName("save - 正常系: 既存同期履歴の更新")
    void save_Success_WhenUpdatingExistingSyncHistory() {
        // Given
        testSyncHistory.addDetail("UPDATE", testDetail.getStatus(), "Project updated");
        when(syncHistoryMapper.selectById(testSyncHistory.getId())).thenReturn(Optional.of(testSyncHistory));
        when(syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistory.getId())).thenReturn(Collections.emptyList());

        // When
        JiraSyncHistory result = repository.save(testSyncHistory);

        // Then
        assertEquals(testSyncHistory, result);
        verify(syncHistoryMapper).selectById(testSyncHistory.getId());
        verify(syncHistoryMapper).update(
            eq(testSyncHistory.getId()),
            eq(testSyncHistory.getSyncStatus().getValue()),
            eq(testSyncHistory.getCompletedAt()),
            eq(testSyncHistory.getTotalProjectsProcessed()),
            eq(testSyncHistory.getSuccessCount()),
            eq(testSyncHistory.getErrorCount()),
            eq(testSyncHistory.getErrorDetails())
        );
        verify(syncHistoryDetailMapper).selectBySyncHistoryId(testSyncHistory.getId());
        verify(syncHistoryDetailMapper).insert(
            any(String.class),
            eq(testSyncHistory.getId()),
            any(Integer.class),
            eq("UPDATE"),
            any(String.class),
            eq("Project updated"),
            any(LocalDateTime.class)
        );
    }
    
    @Test
    @DisplayName("save - 異常系: 同期履歴がnullの場合")
    void save_ThrowException_WhenSyncHistoryIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.save(null)
        );
        assertEquals("同期履歴エンティティは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectById(any());
    }
    
    // ========================================
    // saveAll テスト
    // ========================================

    @Test
    @DisplayName("saveAll - 正常系: 複数同期履歴の一括保存")
    void saveAll_Success_WhenSavingMultipleSyncHistories() {
        // Given
        JiraSyncHistory testSyncHistory2 = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "user");
        List<JiraSyncHistory> syncHistories = Arrays.asList(testSyncHistory, testSyncHistory2);

        when(syncHistoryMapper.selectById(testSyncHistory.getId())).thenReturn(Optional.empty());
        when(syncHistoryMapper.selectById(testSyncHistory2.getId())).thenReturn(Optional.empty());
        when(syncHistoryDetailMapper.selectBySyncHistoryId(any())).thenReturn(Collections.emptyList());

        // When
        List<JiraSyncHistory> result = repository.saveAll(syncHistories);

        // Then
        assertEquals(syncHistories, result);
        verify(syncHistoryMapper).selectById(testSyncHistory.getId());
        verify(syncHistoryMapper).selectById(testSyncHistory2.getId());
        verify(syncHistoryMapper).insert(
            eq(testSyncHistory.getId()),
            eq(testSyncHistory.getSyncType().getValue()),
            eq(testSyncHistory.getSyncStatus().getValue()),
            eq(testSyncHistory.getStartedAt()),
            eq(testSyncHistory.getCompletedAt()),
            eq(testSyncHistory.getTotalProjectsProcessed()),
            eq(testSyncHistory.getSuccessCount()),
            eq(testSyncHistory.getErrorCount()),
            eq(testSyncHistory.getErrorDetails()),
            eq(testSyncHistory.getTriggeredBy())
        );
        verify(syncHistoryMapper).insert(
            eq(testSyncHistory2.getId()),
            eq(testSyncHistory2.getSyncType().getValue()),
            eq(testSyncHistory2.getSyncStatus().getValue()),
            eq(testSyncHistory2.getStartedAt()),
            eq(testSyncHistory2.getCompletedAt()),
            eq(testSyncHistory2.getTotalProjectsProcessed()),
            eq(testSyncHistory2.getSuccessCount()),
            eq(testSyncHistory2.getErrorCount()),
            eq(testSyncHistory2.getErrorDetails()),
            eq(testSyncHistory2.getTriggeredBy())
        );
    }

    @Test
    @DisplayName("saveAll - 異常系: 同期履歴リストがnullの場合")
    void saveAll_ThrowException_WhenSyncHistoriesIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.saveAll(null)
        );
        assertEquals("同期履歴リストは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectById(any());
    }

    // ========================================
    // deleteById テスト
    // ========================================
    
    @Test
    @DisplayName("deleteById - 正常系: 同期履歴の削除")
    void deleteById_Success_DeletesSyncHistory() {
        // Given
        String syncId = "sync-123";
        when(syncHistoryMapper.deleteById(syncId)).thenReturn(1);
        
        // When
        repository.deleteById(syncId);
        
        // Then
        verify(syncHistoryMapper).deleteById(syncId);
    }
    
    @Test
    @DisplayName("deleteById - 異常系: IDがnullの場合")
    void deleteById_ThrowException_WhenIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.deleteById(null)
        );
        assertEquals("同期履歴IDは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).deleteById(any());
    }
    
    // ========================================
    // 統計メソッド テスト
    // ========================================
    
    @Test
    @DisplayName("countByStatus - 正常系: 指定ステータスの同期履歴数を取得")
    void countByStatus_Success_ReturnsCountForSpecifiedStatus() {
        // Given
        JiraSyncStatus status = JiraSyncStatus.COMPLETED;
        long expectedCount = 5L;
        
        when(syncHistoryMapper.countByStatus(status.getValue())).thenReturn(expectedCount);
        
        // When
        long result = repository.countByStatus(status);
        
        // Then
        assertEquals(expectedCount, result);
        verify(syncHistoryMapper).countByStatus(status.getValue());
    }
    
    @Test
    @DisplayName("countByStatus - 異常系: ステータスがnullの場合")
    void countByStatus_ThrowException_WhenStatusIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.countByStatus(null)
        );
        assertEquals("同期ステータスは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).countByStatus(any());
    }
    
    @Test
    @DisplayName("countRecentFailures - 正常系: 最近の失敗数を取得")
    void countRecentFailures_Success_ReturnsRecentFailureCount() {
        // Given
        int hours = 24;
        long expectedCount = 3L;
        
        when(syncHistoryMapper.countRecentFailures(any(LocalDateTime.class))).thenReturn(expectedCount);
        
        // When
        long result = repository.countRecentFailures(hours);
        
        // Then
        assertEquals(expectedCount, result);
        verify(syncHistoryMapper).countRecentFailures(any(LocalDateTime.class));
    }
    
    @Test
    @DisplayName("countRecentFailures - 異常系: hoursが負の値の場合")
    void countRecentFailures_ThrowException_WhenHoursIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.countRecentFailures(-1)
        );
        assertEquals("時間は0以上である必要があります", exception.getMessage());
        verify(syncHistoryMapper, never()).countRecentFailures(any());
    }
    
    @Test
    @DisplayName("existsById - 正常系: 同期履歴が存在する場合")
    void existsById_Success_WhenSyncHistoryExists() {
        // Given
        String syncId = "sync-123";
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.of(testSyncHistory));
        
        // When
        boolean result = repository.existsById(syncId);
        
        // Then
        assertTrue(result);
        verify(syncHistoryMapper).selectById(syncId);
    }
    
    @Test
    @DisplayName("existsById - 正常系: 同期履歴が存在しない場合")
    void existsById_Success_WhenSyncHistoryNotExists() {
        // Given
        String syncId = "non-existent";
        when(syncHistoryMapper.selectById(syncId)).thenReturn(Optional.empty());
        
        // When
        boolean result = repository.existsById(syncId);
        
        // Then
        assertFalse(result);
        verify(syncHistoryMapper).selectById(syncId);
    }
    
    @Test
    @DisplayName("existsById - 異常系: IDがnullの場合")
    void existsById_ThrowException_WhenIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.existsById(null)
        );
        assertEquals("同期履歴IDは必須です", exception.getMessage());
        verify(syncHistoryMapper, never()).selectById(any());
    }
}