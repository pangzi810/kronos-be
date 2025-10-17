package com.devhour.infrastructure.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import com.devhour.domain.model.entity.WorkRecordApprovalHistory;

@Mapper
public interface WorkRecordApprovalHistoryMapper {
    
    @Insert("""
        INSERT INTO work_record_approval_histories (
            history_id, work_record_id, user_id, project_id, work_date,
            category_hours, total_hours, description, action,
            previous_status, current_status, approver_id, rejection_reason,
            work_record_snapshot, occurred_at, created_at
        ) VALUES (
            #{historyId}, #{workRecordId}, #{userId}, #{projectId}, #{workDate},
            #{categoryHours}, #{totalHours}, #{description}, #{action},
            #{previousStatus}, #{currentStatus}, #{approverId}, #{rejectionReason},
            #{workRecordSnapshot}, #{occurredAt}, CURRENT_TIMESTAMP
        )
        """)
    void insert(WorkRecordApprovalHistory history);
    
    @Select("""
        SELECT history_id, work_record_id, user_id, project_id, work_date,
               category_hours, total_hours, description, action,
               previous_status, current_status, approver_id, rejection_reason,
               work_record_snapshot, occurred_at, created_at
        FROM work_record_approval_histories
        WHERE work_record_id = #{workRecordId}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "historyId", column = "history_id"),
        @Result(property = "workRecordId", column = "work_record_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours"),
        @Result(property = "totalHours", column = "total_hours"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "rejectionReason", column = "rejection_reason"),
        @Result(property = "workRecordSnapshot", column = "work_record_snapshot"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<WorkRecordApprovalHistory> findByWorkRecordId(@Param("workRecordId") String workRecordId);
    
    @Select("""
        SELECT history_id, work_record_id, user_id, project_id, work_date,
               category_hours, total_hours, description, action,
               previous_status, current_status, approver_id, rejection_reason,
               work_record_snapshot, occurred_at, created_at
        FROM work_record_approval_histories
        WHERE approver_id = #{approverId}
          AND occurred_at >= #{from}
          AND occurred_at <= #{to}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "historyId", column = "history_id"),
        @Result(property = "workRecordId", column = "work_record_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours"),
        @Result(property = "totalHours", column = "total_hours"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "rejectionReason", column = "rejection_reason"),
        @Result(property = "workRecordSnapshot", column = "work_record_snapshot"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<WorkRecordApprovalHistory> findByApproverIdAndPeriod(@Param("approverId") String approverId,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);
    
    @Select("""
        SELECT history_id, work_record_id, user_id, project_id, work_date,
               category_hours, total_hours, description, action,
               previous_status, current_status, approver_id, rejection_reason,
               work_record_snapshot, occurred_at, created_at
        FROM work_record_approval_histories
        WHERE user_id = #{userId}
          AND action IN ('APPROVE', 'REJECT')
          AND occurred_at >= #{from}
          AND occurred_at <= #{to}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "historyId", column = "history_id"),
        @Result(property = "workRecordId", column = "work_record_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours"),
        @Result(property = "totalHours", column = "total_hours"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "rejectionReason", column = "rejection_reason"),
        @Result(property = "workRecordSnapshot", column = "work_record_snapshot"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<WorkRecordApprovalHistory> findApprovalHistoryByUserAndPeriod(@Param("userId") String userId,
                                                             @Param("from") LocalDateTime from,
                                                             @Param("to") LocalDateTime to);
    
    @Select("""
        SELECT COUNT(*)
        FROM work_record_approval_histories
        WHERE history_id = #{historyId}
        """)
    int countByHistoryId(@Param("historyId") String historyId);
}