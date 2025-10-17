package com.devhour.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.repository.WorkRecordRepository;
import com.devhour.infrastructure.mapper.WorkRecordMapper;

/**
 * 工数記録リポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してWorkRecordRepositoryインターフェースを実装
 */
@Repository
public class WorkRecordRepositoryImpl implements WorkRecordRepository {
    
    private final WorkRecordMapper workRecordMapper;
    
    public WorkRecordRepositoryImpl(WorkRecordMapper workRecordMapper) {
        this.workRecordMapper = workRecordMapper;
    }
    
    @Override
    public Optional<WorkRecord> findById(String workRecordId) {
        return workRecordMapper.findById(workRecordId);
    }
    
    @Override
    public List<WorkRecord> findByUserIdAndDate(String userId, LocalDate workDate) {
        return workRecordMapper.findByUserIdAndDate(userId, workDate);
    }
    
    @Override
    public List<WorkRecord> findByUser(String userId) {
        return workRecordMapper.findByUser(userId);
    }
    
    @Override
    public List<WorkRecord> findByProject(String projectId) {
        return workRecordMapper.findByProject(projectId);
    }
    
    @Override
    public List<WorkRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return workRecordMapper.findByDateRange(startDate, endDate);
    }
    
    @Override
    public List<WorkRecord> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return workRecordMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    
    @Override
    public List<WorkRecord> findLatestByUser(String userId, int limit) {
        return workRecordMapper.findLatestByUser(userId, limit);
    }
    
    
    
    @Override
    public WorkRecord save(WorkRecord workRecord) {
        Optional<WorkRecord> existing = findById(workRecord.getId());
        if (existing.isEmpty()) {
            // 新規作成
            workRecordMapper.insert(
                workRecord.getId(),
                workRecord.getUserId(),
                workRecord.getProjectId(),
                workRecord.getWorkDate(),
                workRecord.getCategoryHours(),
                workRecord.getDescription(),
                workRecord.getCreatedBy(),
                workRecord.getCreatedAt(),
                workRecord.getUpdatedBy(),
                workRecord.getUpdatedAt()
            );
        } else {
            // 更新
            workRecordMapper.update(
                workRecord.getId(),
                workRecord.getCategoryHours(),
                workRecord.getDescription(),
                workRecord.getUpdatedBy(),
                workRecord.getUpdatedAt()
            );
        }
        return workRecord;
    }
    
    @Override
    public List<WorkRecord> saveAll(List<WorkRecord> workRecords) {
        workRecords.forEach(this::save);
        return workRecords;
    }
    
    @Override
    public Optional<WorkRecord> findByUserIdAndDateAndProjectId(String userId, LocalDate workDate, String projectId) {
        return workRecordMapper.findByUserIdAndDateAndProjectId(userId, workDate, projectId);
    }
    
    @Override
    public void deleteById(String workRecordId) {
        workRecordMapper.softDelete(workRecordId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public void deleteByUserIdAndDate(String userId, LocalDate workDate) {
        workRecordMapper.deleteByUserIdAndDate(userId, workDate);
    }

    @Override
    public void deleteByUserIdAndDateAndRecordIds(String userId, LocalDate workDate, List<String> recordIds) {
        workRecordMapper.deleteByUserIdAndDateAndRecordIds(userId, workDate, recordIds);
    }
}