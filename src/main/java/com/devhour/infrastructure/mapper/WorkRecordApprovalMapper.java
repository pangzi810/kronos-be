package com.devhour.infrastructure.mapper;

import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 作業記録承認マッパー
 */
@Mapper
public interface WorkRecordApprovalMapper {
    
    @Select("""
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE user_id = #{userId} AND work_date = #{workDate}
    """)
    @Results(id = "workRecordApprovalResultMap", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "approvalStatus", column = "approval_status", typeHandler = com.devhour.infrastructure.typehandler.ApprovalStatusTypeHandler.class),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "approvedAt", column = "approved_at"),
        @Result(property = "rejectionReason", column = "rejection_reason"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WorkRecordApproval> findByUserIdAndDate(@Param("userId") String userId, @Param("workDate") LocalDate workDate);
    
    @Select("""
        <script>
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE user_id IN
        <foreach item='userId' collection='userIds' open='(' separator=',' close=')'>
            #{userId}
        </foreach>
        AND approval_status IN
        <foreach item='status' collection='statuses' open='(' separator=',' close=')'>
            #{status}
        </foreach>
        ORDER BY work_date DESC, user_id
        </script>
    """)
    @ResultMap("workRecordApprovalResultMap")
    List<WorkRecordApproval> findByUsersAndStatuses(
        @Param("userIds") List<String> userIds, 
        @Param("statuses") List<ApprovalStatus> statuses
    );
    
    @Select("""
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE user_id = #{userId}
        ORDER BY work_date DESC
    """)
    @ResultMap("workRecordApprovalResultMap")
    List<WorkRecordApproval> findByUserId(@Param("userId") String userId);
    
    @Select("""
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE approver_id = #{approverId}
        ORDER BY work_date DESC, user_id
    """)
    @ResultMap("workRecordApprovalResultMap")
    List<WorkRecordApproval> findByApproverId(@Param("approverId") String approverId);
    
    @Select("""
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE user_id = #{userId}
        AND work_date >= #{startDate}
        AND work_date <= #{endDate}
        ORDER BY work_date DESC
    """)
    @ResultMap("workRecordApprovalResultMap")
    List<WorkRecordApproval> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Select("""
        SELECT 
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        FROM work_record_approval
        WHERE approval_status = #{status}
        ORDER BY work_date DESC, user_id
    """)
    @ResultMap("workRecordApprovalResultMap")
    List<WorkRecordApproval> findByStatus(@Param("status") ApprovalStatus status);
    
    @Insert("""
        INSERT INTO work_record_approval (
            user_id,
            work_date,
            approval_status,
            approver_id,
            approved_at,
            rejection_reason,
            created_at,
            updated_at
        ) VALUES (
            #{userId},
            #{workDate},
            #{approvalStatus,typeHandler=com.devhour.infrastructure.typehandler.ApprovalStatusTypeHandler},
            #{approverId},
            #{approvedAt},
            #{rejectionReason},
            #{createdAt},
            #{updatedAt}
        )
    """)
    void insert(WorkRecordApproval approval);
    
    @Update("""
        UPDATE work_record_approval
        SET approval_status = #{approvalStatus,typeHandler=com.devhour.infrastructure.typehandler.ApprovalStatusTypeHandler},
            approver_id = #{approverId},
            approved_at = #{approvedAt},
            rejection_reason = #{rejectionReason},
            updated_at = #{updatedAt}
        WHERE user_id = #{userId} AND work_date = #{workDate}
    """)
    void update(WorkRecordApproval approval);
    
    @Delete("""
        DELETE FROM work_record_approval
        WHERE user_id = #{userId} AND work_date = #{workDate}
    """)
    void delete(@Param("userId") String userId, @Param("workDate") LocalDate workDate);
    
    @Delete("""
        DELETE FROM work_record_approval
        WHERE user_id = #{userId}
    """)
    void deleteByUserId(@Param("userId") String userId);
    
    @Select("""
        SELECT COUNT(*) > 0
        FROM work_record_approval
        WHERE user_id = #{userId} AND work_date = #{workDate}
    """)
    boolean exists(@Param("userId") String userId, @Param("workDate") LocalDate workDate);
}