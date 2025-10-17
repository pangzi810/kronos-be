package com.devhour.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.repository.WorkRecordApprovalRepository;
import com.devhour.infrastructure.mapper.WorkRecordApprovalMapper;
import lombok.RequiredArgsConstructor;

/**
 * 作業記録承認リポジトリ実装
 */
@Repository
@RequiredArgsConstructor
public class WorkRecordApprovalRepositoryImpl implements WorkRecordApprovalRepository {
    
    private final WorkRecordApprovalMapper mapper;
    
    @Override
    public Optional<WorkRecordApproval> findByUserIdAndDate(String userId, LocalDate workDate) {
        return mapper.findByUserIdAndDate(userId, workDate);
    }
    
    @Override
    public List<WorkRecordApproval> findByUsersAndStatuses(List<String> userIds, List<ApprovalStatus> statuses) {
        if (userIds == null || userIds.isEmpty() || statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        return mapper.findByUsersAndStatuses(userIds, statuses);
    }
    
    @Override
    public List<WorkRecordApproval> findByUserId(String userId) {
        return mapper.findByUserId(userId);
    }
    
    @Override
    public List<WorkRecordApproval> findByApproverId(String approverId) {
        return mapper.findByApproverId(approverId);
    }
    
    @Override
    public List<WorkRecordApproval> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return mapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Override
    public List<WorkRecordApproval> findByStatus(ApprovalStatus status) {
        return mapper.findByStatus(status);
    }
    
    @Override
    public WorkRecordApproval save(WorkRecordApproval approval) {
        if (approval == null) {
            throw new IllegalArgumentException("承認レコードがnullです");
        }
        
        // 更新日時を現在時刻に設定
        WorkRecordApproval toSave = new WorkRecordApproval(
            approval.getUserId(),
            approval.getWorkDate(),
            approval.getApprovalStatus(),
            approval.getApproverId(),
            approval.getApprovedAt(),
            approval.getRejectionReason(),
            approval.getCreatedAt() != null ? approval.getCreatedAt() : LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        // 存在チェックして新規作成または更新を判断
        if (exists(toSave.getUserId(), toSave.getWorkDate())) {
            mapper.update(toSave);
        } else {
            mapper.insert(toSave);
        }
        
        return toSave;
    }
    
    @Override
    public void delete(String userId, LocalDate workDate) {
        mapper.delete(userId, workDate);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        mapper.deleteByUserId(userId);
    }
    
    @Override
    public boolean exists(String userId, LocalDate workDate) {
        return mapper.exists(userId, workDate);
    }
}