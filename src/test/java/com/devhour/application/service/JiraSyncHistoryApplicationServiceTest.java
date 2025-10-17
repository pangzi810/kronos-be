package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
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
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.presentation.dto.response.JiraSyncHistoryDetailResponse;
import com.devhour.presentation.dto.response.JiraSyncHistoryResponse;

/**
 * SyncHistoryApplicationServiceのユニットテスト
 * 
 * 同期履歴アプリケーションサービスのテストクラス
 * モックベースのテスト手法でビジネスロジック検証を実行
 * 80%以上のテストカバレッジを確保
 * 
 * 要件:
 * - REQ-6.1: 管理者が過去30日間の同期履歴を参照可能
 * - REQ-6.2: 管理者が特定同期の詳細情報を参照可能
 * - REQ-6.3: 管理者が期間フィルタで同期履歴を絞り込み可能
 * - REQ-6.4: 管理者が同期履歴をCSV形式でエクスポート可能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncHistoryApplicationService")
class JiraSyncHistoryApplicationServiceTest {
    
    @Mock
    private JiraSyncHistoryRepository syncHistoryRepository;
    
    @InjectMocks
    private JiraSyncHistoryApplicationService service;
    
    private JiraSyncHistory testSyncHistory;
    private List<JiraSyncHistory> testSyncHistoryList;
    
    @BeforeEach
    void setUp() {
        // テスト用同期履歴
        testSyncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");
        testSyncHistory = JiraSyncHistory.restore(
            "test-sync-id",
            JiraSyncType.SCHEDULED,
            JiraSyncStatus.COMPLETED,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now(),
            5,
            4,
            1,
            null,
            "system"
        );

        // テスト用同期履歴リスト
        JiraSyncHistory syncHistory2 = JiraSyncHistory.restore(
            "test-sync-id-2",
            JiraSyncType.MANUAL,
            JiraSyncStatus.FAILED,
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
            3,
            2,
            1,
            "API error",
            "admin"
        );
        
        testSyncHistoryList = Arrays.asList(testSyncHistory, syncHistory2);
    }
    
    
    // ========== 同期履歴取得関連のテスト ==========
    
    @Test
    @DisplayName("getSyncHistory - 正常ケース: ページネーション付き履歴取得 (REQ-6.1)")
    void getSyncHistory_Success_WithPagination() {
        // Arrange
        int page = 0;
        int size = 10;
        LocalDate startDate = null; // No date filter to use pagination path
        LocalDate endDate = null;
        
        when(syncHistoryRepository.findWithPagination(size, page * size, null)).thenReturn(testSyncHistoryList);
        when(syncHistoryRepository.findInProgress()).thenReturn(Collections.emptyList());
        when(syncHistoryRepository.countByStatus(any())).thenReturn(10L);
        
        // Act
        JiraSyncHistoryResponse response = service.getSyncHistory(page, size, startDate, endDate, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSyncHistories().size());
        assertEquals(page, response.getCurrentPage());
        assertEquals(size, response.getPageSize());
        verify(syncHistoryRepository).findWithPagination(size, 0, null);
    }
    
    @Test
    @DisplayName("getSyncHistory - 正常ケース: 日付範囲フィルタ付き履歴取得 (REQ-6.3)")
    void getSyncHistory_Success_WithDateRangeFilter() {
        // Arrange
        int page = 0;
        int size = 10;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(syncHistoryRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
            .thenReturn(testSyncHistoryList);
        when(syncHistoryRepository.findInProgress()).thenReturn(Collections.emptyList());
        when(syncHistoryRepository.countByStatus(any())).thenReturn(2L);
        
        // Act
        JiraSyncHistoryResponse response = service.getSyncHistory(page, size, startDate, endDate, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSyncHistories().size());
        verify(syncHistoryRepository).findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), eq(null));
    }
    
    @Test
    @DisplayName("getSyncHistory - 不正なページ番号")
    void getSyncHistory_InvalidPageNumber() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistory(-1, 10, LocalDate.now().minusDays(30), LocalDate.now(), null)
        );
        assertEquals("ページ番号は0以上である必要があります", exception.getMessage());
        verify(syncHistoryRepository, never()).findWithPagination(anyInt(), anyInt(), any());
    }
    
    @Test
    @DisplayName("getSyncHistory - 不正なページサイズ")
    void getSyncHistory_InvalidPageSize() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistory(0, 0, LocalDate.now().minusDays(30), LocalDate.now(), null)
        );
        assertEquals("ページサイズは1以上である必要があります", exception.getMessage());
        verify(syncHistoryRepository, never()).findWithPagination(anyInt(), anyInt(), any());
    }
    
    @Test
    @DisplayName("getSyncHistory - 不正な日付範囲")
    void getSyncHistory_InvalidDateRange() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistory(0, 10, LocalDate.now(), LocalDate.now().minusDays(1), null)
        );
        assertEquals("開始日は終了日以前である必要があります", exception.getMessage());
        verify(syncHistoryRepository, never()).findWithPagination(anyInt(), anyInt(), any());
    }
    
    @Test
    @DisplayName("getSyncHistoryDetails - 正常ケース: 詳細情報取得 (REQ-6.2)")
    void getSyncHistoryDetails_Success() {
        // Arrange
        String syncId = "550e8400-e29b-41d4-a716-446655440001";
        testSyncHistory.addDetail("CREATE_FROM_JIRA", com.devhour.domain.model.valueobject.DetailStatus.SUCCESS, null);
        
        when(syncHistoryRepository.findWithDetails(syncId)).thenReturn(Optional.of(testSyncHistory));
        
        // Act
        JiraSyncHistoryDetailResponse response = service.getSyncHistoryDetails(syncId);
        
        // Assert
        assertNotNull(response);
        assertEquals(testSyncHistory.getId(), response.getSyncHistoryId());
        assertEquals(testSyncHistory.getSyncType(), response.getSyncType());
        assertEquals(testSyncHistory.getSyncStatus(), response.getSyncStatus());
        assertEquals(1, response.getDetails().size());
        verify(syncHistoryRepository).findWithDetails(syncId.toString());
    }
    
    @Test
    @DisplayName("getSyncHistoryDetails - 同期履歴が見つからない場合")
    void getSyncHistoryDetails_NotFound() {
        // Arrange
        String syncId = "550e8400-e29b-41d4-a716-446655440999";
        when(syncHistoryRepository.findWithDetails(syncId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistoryDetails(syncId)
        );
        assertEquals("同期履歴が見つかりません: " + syncId, exception.getMessage());
        verify(syncHistoryRepository).findWithDetails(syncId);
    }
    
    @Test
    @DisplayName("getSyncHistoryDetails - パラメータがnullの場合")
    void getSyncHistoryDetails_NullSyncId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistoryDetails(null)
        );
        assertEquals("同期履歴IDは必須です", exception.getMessage());
        verify(syncHistoryRepository, never()).findWithDetails(anyString());
    }
    
    @Test
    @DisplayName("getSyncHistoryDetails - パラメータが空文字列の場合")
    void getSyncHistoryDetails_EmptySyncId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.getSyncHistoryDetails("")
        );
        assertEquals("同期履歴IDは必須です", exception.getMessage());
        verify(syncHistoryRepository, never()).findWithDetails(anyString());
    }
    
    // ========== 最近の同期ステータス取得関連のテスト ==========
    
    @Test
    @DisplayName("getRecentSyncStatus - 正常ケース: 最近の同期ステータス取得")
    void getRecentSyncStatus_Success() {
        // Arrange
        when(syncHistoryRepository.findRecent()).thenReturn(testSyncHistoryList);
        when(syncHistoryRepository.findInProgress()).thenReturn(Collections.emptyList());
        
        // Act
        JiraSyncHistoryResponse response = service.getRecentSyncStatus();
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSyncHistories().size());
        assertFalse(response.isHasInProgress());
        verify(syncHistoryRepository).findRecent();
        verify(syncHistoryRepository).findInProgress();
    }
    
    @Test
    @DisplayName("getRecentSyncStatus - 実行中の同期がある場合")
    void getRecentSyncStatus_WithInProgress() {
        // Arrange
        JiraSyncHistory inProgressSync = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");
        when(syncHistoryRepository.findRecent()).thenReturn(testSyncHistoryList);
        when(syncHistoryRepository.findInProgress()).thenReturn(Arrays.asList(inProgressSync));
        
        // Act
        JiraSyncHistoryResponse response = service.getRecentSyncStatus();
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isHasInProgress());
        verify(syncHistoryRepository).findRecent();
        verify(syncHistoryRepository).findInProgress();
    }
    
    // ========== エラーハンドリングのテスト ==========
    
    
    @Test
    @DisplayName("getSyncHistory - リポジトリエラー")
    void getSyncHistory_RepositoryError() {
        // Arrange
        when(syncHistoryRepository.findWithPagination(anyInt(), anyInt(), any()))
            .thenThrow(new RuntimeException("Database query failed"));
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> service.getSyncHistory(0, 10, null, null, null) // Use null dates to trigger pagination path
        );
        assertEquals("Database query failed", exception.getMessage());
        verify(syncHistoryRepository).findWithPagination(10, 0, null);
    }
}