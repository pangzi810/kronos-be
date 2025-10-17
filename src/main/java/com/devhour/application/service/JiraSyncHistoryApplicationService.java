package com.devhour.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.presentation.dto.response.JiraSyncHistoryDetailResponse;
import com.devhour.presentation.dto.response.JiraSyncHistoryResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 同期履歴アプリケーションサービス
 * 
 * JIRA同期機能における同期履歴の管理と監視を担当するアプリケーションサービス
 * 管理者向けの同期履歴参照、統計情報提供、エクスポート機能を実装
 * 
 * 責務:
 * - ページネーション対応の同期履歴検索 (getSyncHistory)
 * - 詳細情報取得による同期実行状況分析 (getSyncHistoryDetails)
 * - 日付範囲フィルタによる期間別分析 (REQ-6.3)
 * - 実行中同期の監視機能 (getRecentSyncStatus)
 * 
 * アーキテクチャパターン:
 * - アプリケーションサービスパターン
 * - トランザクション境界の管理
 * - DTO変換による表現層分離
 * - ページネーション対応による大量データ処理
 * - エラー境界での適切な例外処理
 * 
 * 要件対応:
 * - REQ-6.1: 管理者が過去30日間の同期履歴を参照可能
 * - REQ-6.2: 管理者が特定同期の詳細情報を参照可能
 * - REQ-6.3: 管理者が期間フィルタで同期履歴を絞り込み可能
 */
@Service
@Transactional
@Slf4j
public class JiraSyncHistoryApplicationService {
    
    private final JiraSyncHistoryRepository syncHistoryRepository;
    
    public JiraSyncHistoryApplicationService(JiraSyncHistoryRepository syncHistoryRepository) {
        this.syncHistoryRepository = syncHistoryRepository;
    }
    
    
    
    /**
     * ページネーション対応の同期履歴取得 (REQ-6.1, REQ-6.3)
     * 
     * 管理者向けの同期履歴一覧表示に使用。
     * 日付範囲フィルタとページネーションによる効率的な履歴検索を提供。
     * 
     * @param page ページ番号 (0から開始)
     * @param size ページサイズ
     * @param startDate 検索開始日付
     * @param endDate 検索終了日付
     * @return 同期履歴レスポンス（ページネーション情報含む）
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    @Transactional(readOnly = true)
    public JiraSyncHistoryResponse getSyncHistory(int page, int size, LocalDate startDate, LocalDate endDate, String status) {
        log.info("同期履歴を取得: page={}, size={}, startDate={}, endDate={}, status={}", page, size, startDate, endDate, status);
        
        // パラメータ検証
        validatePaginationParameters(page, size);
        validateDateRange(startDate, endDate);
        validateStatus(status);

        try {
            List<JiraSyncHistory> syncHistories;
            
            // 日付範囲が指定されている場合はフィルタリング
            if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                syncHistories = syncHistoryRepository.findByDateRange(startDateTime, endDateTime, status);
                
                // ページネーション処理（メモリ上で実行）
                int offset = page * size;
                if (offset >= syncHistories.size()) {
                    syncHistories = List.of();
                } else {
                    int endIndex = Math.min(offset + size, syncHistories.size());
                    syncHistories = syncHistories.subList(offset, endIndex);
                }
            } else {
                // 日付範囲が指定されていない場合はリポジトリでページネーション
                syncHistories = syncHistoryRepository.findWithPagination(size, page * size, status);
            }
            
            // 実行中同期の確認
            List<JiraSyncHistory> inProgressSyncs = syncHistoryRepository.findInProgress();
            boolean hasInProgress = !inProgressSyncs.isEmpty();
            
            // DTOに変換
            List<JiraSyncHistoryResponse.SyncHistorySummary> summaries = syncHistories.stream()
                .map(JiraSyncHistoryResponse.SyncHistorySummary::new)
                .collect(Collectors.toList());
            
            // 総件数を計算（簡易実装）
            long totalRecords = syncHistoryRepository.countByStatus(JiraSyncStatus.COMPLETED) 
                              + syncHistoryRepository.countByStatus(JiraSyncStatus.FAILED)
                              + syncHistoryRepository.countByStatus(JiraSyncStatus.IN_PROGRESS);
            
            JiraSyncHistoryResponse response = new JiraSyncHistoryResponse(
                summaries, page, size, totalRecords, hasInProgress, startDate, endDate
            );
            
            log.info("同期履歴取得完了: recordsCount={}, hasInProgress={}", summaries.size(), hasInProgress);
            return response;
            
        } catch (Exception e) {
            log.error("同期履歴取得でエラーが発生: page={}, size={}", page, size, e);
            throw e;
        }
    }
    
    /**
     * 特定同期の詳細情報取得 (REQ-6.2)
     * 
     * 管理者が特定の同期実行の詳細状況を確認するために使用。
     * 処理されたプロジェクト詳細、エラー情報、実行JQLクエリ情報を提供。
     * 
     * @param syncId 同期履歴ID（UUID文字列）
     * @return 同期履歴詳細レスポンス
     * @throws IllegalArgumentException syncIdに対応する同期履歴が見つからない場合
     */
    @Transactional(readOnly = true)
    public JiraSyncHistoryDetailResponse getSyncHistoryDetails(String syncId) {
        log.info("同期履歴詳細を取得: syncId={}", syncId);
        
        if (syncId == null || syncId.trim().isEmpty()) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
        
        Optional<JiraSyncHistory> syncHistoryOpt = syncHistoryRepository.findWithDetails(syncId);
        
        if (syncHistoryOpt.isEmpty()) {
            throw new IllegalArgumentException("同期履歴が見つかりません: " + syncId);
        }
        
        try {
            JiraSyncHistory syncHistory = syncHistoryOpt.get();
            JiraSyncHistoryDetailResponse response = new JiraSyncHistoryDetailResponse(syncHistory);
            
            log.info("同期履歴詳細取得完了: syncId={}, detailsCount={}", syncId, response.getDetails().size());
            return response;
            
        } catch (Exception e) {
            log.error("同期履歴詳細取得でエラーが発生: syncId={}", syncId, e);
            throw e;
        }
    }
    
    /**
     * 最近の同期ステータス取得
     * 
     * 監視ダッシュボードでの最近の同期実行状況確認に使用。
     * 実行中の同期がある場合の検知機能も提供。
     * 
     * @return 最近の同期履歴レスポンス
     */
    @Transactional(readOnly = true)
    public JiraSyncHistoryResponse getRecentSyncStatus() {
        log.info("最近の同期ステータスを取得");
        
        try {
            List<JiraSyncHistory> recentSyncs = syncHistoryRepository.findRecent();
            List<JiraSyncHistory> inProgressSyncs = syncHistoryRepository.findInProgress();
            
            boolean hasInProgress = !inProgressSyncs.isEmpty();
            
            // DTOに変換
            List<JiraSyncHistoryResponse.SyncHistorySummary> summaries = recentSyncs.stream()
                .map(JiraSyncHistoryResponse.SyncHistorySummary::new)
                .collect(Collectors.toList());
            
            JiraSyncHistoryResponse response = new JiraSyncHistoryResponse(
                summaries, 0, summaries.size(), summaries.size(), hasInProgress, 
                LocalDate.now().minusDays(30), LocalDate.now()
            );
            
            log.info("最近の同期ステータス取得完了: recordsCount={}, hasInProgress={}", summaries.size(), hasInProgress);
            return response;
            
        } catch (Exception e) {
            log.error("最近の同期ステータス取得でエラーが発生", e);
            throw e;
        }
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * ページネーションパラメータの検証
     */
    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("ページ番号は0以上である必要があります");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("ページサイズは1以上である必要があります");
        }
    }
    
    /**
     * 日付範囲の検証
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日は終了日以前である必要があります");
        }
    }

    private void validateStatus(String status) {
        if (status != null && !status.trim().isEmpty()) {
            try {
                JiraSyncStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("無効なステータス値です: " + status);
            }
        }
    }
}