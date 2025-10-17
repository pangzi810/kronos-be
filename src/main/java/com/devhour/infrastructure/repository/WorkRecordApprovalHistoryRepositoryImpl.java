package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.WorkRecordApprovalHistory;
import com.devhour.domain.repository.WorkRecordApprovalHistoryRepository;
import com.devhour.infrastructure.mapper.WorkRecordApprovalHistoryMapper;

/**
 * 承認履歴リポジトリ実装
 * 
 * MyBatisを使用した承認履歴の永続化実装
 */
@Repository
public class WorkRecordApprovalHistoryRepositoryImpl implements WorkRecordApprovalHistoryRepository {
    
    private final WorkRecordApprovalHistoryMapper mapper;
    
    public WorkRecordApprovalHistoryRepositoryImpl(WorkRecordApprovalHistoryMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public void save(WorkRecordApprovalHistory history) {
        // 承認履歴は追記のみ（更新なし）
        mapper.insert(history);
    }
    
    @Override
    public Optional<WorkRecordApprovalHistory> findById(String historyId) {
        List<WorkRecordApprovalHistory> histories = mapper.findByWorkRecordId(historyId.split("#")[0]);
        return histories.stream()
            .filter(h -> h.getHistoryId().equals(historyId))
            .findFirst();
    }
    
    @Override
    public List<WorkRecordApprovalHistory> findByWorkRecordId(String workRecordId) {
        return mapper.findByWorkRecordId(workRecordId);
    }
    
    @Override
    public List<WorkRecordApprovalHistory> findByApproverIdAndPeriod(String approverId,
                                                          LocalDateTime from,
                                                          LocalDateTime to) {
        return mapper.findByApproverIdAndPeriod(approverId, from, to);
    }
    
    @Override
    public List<WorkRecordApprovalHistory> findByUserIdAndPeriod(String userId,
                                                      LocalDateTime from,
                                                      LocalDateTime to) {
        return mapper.findApprovalHistoryByUserAndPeriod(userId, from, to);
    }
    
    @Override
    public List<WorkRecordApprovalHistory> findByProjectIdAndPeriod(String projectId,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        // プロジェクトIDでの検索は、全履歴から絞り込み
        // 実際の実装では専用のクエリを追加することが望ましい
        return mapper.findApprovalHistoryByUserAndPeriod(null, from, to).stream()
            .filter(h -> projectId.equals(h.getProjectId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public int countApprovalsByApproverAndPeriod(String approverId,
                                                LocalDateTime from,
                                                LocalDateTime to) {
        return mapper.findByApproverIdAndPeriod(approverId, from, to).stream()
            .filter(WorkRecordApprovalHistory::isApprovalAction)
            .collect(Collectors.toList()).size();
    }
    
    @Override
    public int countRejectionsByApproverAndPeriod(String approverId,
                                                 LocalDateTime from,
                                                 LocalDateTime to) {
        return mapper.findByApproverIdAndPeriod(approverId, from, to).stream()
            .filter(WorkRecordApprovalHistory::isRejectionAction)
            .collect(Collectors.toList()).size();
    }
    
    @Override
    public Optional<WorkRecordApprovalHistory> findLatestByWorkRecordId(String workRecordId) {
        List<WorkRecordApprovalHistory> histories = mapper.findByWorkRecordId(workRecordId);
        if (histories.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(histories.get(0)); // DESCでソート済みなので最初が最新
    }
    
    @Override
    public boolean existsById(String historyId) {
        return mapper.countByHistoryId(historyId) > 0;
    }
}