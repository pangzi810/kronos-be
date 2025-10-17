package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.infrastructure.mapper.JiraSyncHistoryDetailMapper;
import com.devhour.infrastructure.mapper.JiraSyncHistoryMapper;

/**
 * 同期履歴リポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してSyncHistoryRepositoryインターフェースを実装
 * Spring管理のトランザクション下で同期履歴エンティティの永続化操作を提供
 * 
 * 責務:
 * - SyncHistoryMapper・SyncHistoryDetailMapperへの委譲によるデータアクセス
 * - ドメインレイヤとインフラストラクチャレイヤの境界管理
 * - パラメータ検証とエラーハンドリング
 * - トランザクション管理の適用
 * - 詳細履歴との関連付け管理
 * 
 * アーキテクチャ:
 * - Domain Repository Pattern の実装
 * - MyBatis annotation-based mapping
 * - Spring Transaction Management
 * - Aggregate Root と Detail の関連管理
 */
@Repository
@Transactional(readOnly = true)
public class JiraSyncHistoryRepositoryImpl implements JiraSyncHistoryRepository {
    
    private final JiraSyncHistoryMapper syncHistoryMapper;
    private final JiraSyncHistoryDetailMapper syncHistoryDetailMapper;
    
    public JiraSyncHistoryRepositoryImpl(JiraSyncHistoryMapper syncHistoryMapper, 
                                   JiraSyncHistoryDetailMapper syncHistoryDetailMapper) {
        this.syncHistoryMapper = syncHistoryMapper;
        this.syncHistoryDetailMapper = syncHistoryDetailMapper;
    }
    
    @Override
    public Optional<JiraSyncHistory> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
        return syncHistoryMapper.selectById(id);
    }
    
    @Override
    public Optional<JiraSyncHistory> findWithDetails(String id) {
        if (id == null) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
        
        Optional<JiraSyncHistory> syncHistory = syncHistoryMapper.selectById(id);
        if (syncHistory.isPresent()) {
            // 詳細履歴を取得して設定
            List<JiraSyncHistoryDetail> details = syncHistoryDetailMapper.selectBySyncHistoryId(id);
            syncHistory.get().setDetails(details);
        }
        
        return syncHistory;
    }
    
    @Override
    public List<JiraSyncHistory> findRecent() {
        // 30日前から現在まで
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        return syncHistoryMapper.selectByDateRange(thirtyDaysAgo, now, null);
    }
    
    @Override
    public List<JiraSyncHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, String status) {
        if (startDate == null) {
            throw new IllegalArgumentException("開始日時は必須です");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("終了日時は必須です");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日時は終了日時より前である必要があります");
        }
        
        List<JiraSyncHistory> histories = syncHistoryMapper.selectByDateRange(startDate, endDate, status);
        setSyncHistoryDetails(histories);
        return histories;
    }

    @Override
    public List<JiraSyncHistory> findByStatus(JiraSyncStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("同期ステータスは必須です");
        }

        List<JiraSyncHistory> histories = syncHistoryMapper.selectByStatus(status.getValue());
        setSyncHistoryDetails(histories);
        return histories;
    }
    
    @Override
    public List<JiraSyncHistory> findByTriggerType(JiraSyncType triggerType) {
        if (triggerType == null) {
            throw new IllegalArgumentException("トリガータイプは必須です");
        }

        List<JiraSyncHistory> histories = syncHistoryMapper.selectByTriggerType(triggerType.getValue());
        setSyncHistoryDetails(histories);
        return histories;
    }
    
    @Override
    public List<JiraSyncHistory> findInProgress() {
        List<JiraSyncHistory> histories = syncHistoryMapper.selectInProgress();
        setSyncHistoryDetails(histories);
        return histories;
    }
    
    @Override
    public List<JiraSyncHistory> findWithPagination(int limit, int offset, String status) {
        if (limit < 0) {
            throw new IllegalArgumentException("取得件数は0以上である必要があります");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("取得開始位置は0以上である必要があります");
        }
        
        List<JiraSyncHistory> histories = syncHistoryMapper.selectRecentWithPagination(limit, offset, status);
        setSyncHistoryDetails(histories);
        return histories;
    }
    
    @Override
    @Transactional
    public JiraSyncHistory save(JiraSyncHistory syncHistory) {
        if (syncHistory == null) {
            throw new IllegalArgumentException("同期履歴エンティティは必須です");
        }
        
        if (!syncHistoryMapper.selectById(syncHistory.getId()).isPresent()) {
            // 新規作成
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
        } else {
            // 更新
            syncHistoryMapper.update(
                syncHistory.getId(),
                syncHistory.getSyncStatus().getValue(),
                syncHistory.getCompletedAt(),
                syncHistory.getTotalProjectsProcessed(),
                syncHistory.getSuccessCount(),
                syncHistory.getErrorCount(),
                syncHistory.getErrorDetails()
            );
        }
        
        List<JiraSyncHistoryDetail> records = syncHistoryDetailMapper.selectBySyncHistoryId(syncHistory.getId());
        for (JiraSyncHistoryDetail detail : syncHistory.getDetails()) {
            if (!records.stream().anyMatch(r -> r.getId().equals(detail.getId()))) {
                // 新しい詳細履歴を挿入
                syncHistoryDetailMapper.insert(
                    detail.getId(),
                    syncHistory.getId(),
                    detail.getSeq(),
                    detail.getOperation(),
                    detail.getStatus().getValue(),
                    detail.getResult(),
                    detail.getProcessedAt()
                );
            } else {
                // 既存の詳細履歴を更新
                syncHistoryDetailMapper.update(
                    detail.getId(),
                    syncHistory.getId(),
                    detail.getSeq(),
                    detail.getOperation(),
                    detail.getStatus().getValue(),
                    detail.getResult(),
                    detail.getProcessedAt()
                );
            }
        }
        
        return syncHistory;
    }
    
    @Override
    @Transactional
    public List<JiraSyncHistory> saveAll(List<JiraSyncHistory> syncHistories) {
        if (syncHistories == null) {
            throw new IllegalArgumentException("同期履歴リストは必須です");
        }
        
        syncHistories.forEach(this::save);
        return syncHistories;
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
        syncHistoryMapper.deleteById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
        return syncHistoryMapper.selectById(id).isPresent();
    }
    
    // ========================================
    // 統計情報取得メソッド
    // ========================================
    
    @Override
    public long countByStatus(JiraSyncStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("同期ステータスは必須です");
        }
        return syncHistoryMapper.countByStatus(status.getValue());
    }
    
    @Override
    public long countRecentFailures(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("時間は0以上である必要があります");
        }
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return syncHistoryMapper.countRecentFailures(since);
    }

    /**
     * 同期履歴リストに対応する詳細履歴を設定
     * 
     * @param histories 同期履歴リスト
     */    
    private void setSyncHistoryDetails(List<JiraSyncHistory> histories) {
        for (JiraSyncHistory history : histories) {
            List<JiraSyncHistoryDetail> details = syncHistoryDetailMapper.selectBySyncHistoryId(history.getId());
            history.setDetails(details);
        }
    }
}