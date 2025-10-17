package com.devhour.infrastructure.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.devhour.domain.model.entity.Approver;
import com.devhour.infrastructure.dto.ApproverGrouping;
import com.devhour.presentation.dto.SubordinateInfo;

@Mapper
public interface ApproverMapper {
    
    @Insert("""
        INSERT INTO approvers (
            id, target_email, approver_email, effective_from, effective_to,
            created_at, updated_at
        ) VALUES (
            #{id}, #{targetEmail}, #{approverEmail}, #{effectiveFrom}, #{effectiveTo},
            #{createdAt}, #{updatedAt}
        )
        """)
    void insert(Approver approver);
    
    @Update("""
        UPDATE approvers
        SET effective_to = #{effectiveTo},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void update(Approver approver);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<Approver> findById(@Param("id") String id);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{userId}
          AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
        ORDER BY effective_from DESC
        LIMIT 1
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<Approver> findCurrentByUserId(@Param("userId") String userId);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{userId}
          AND DATE(effective_from) <= #{date}
          AND (effective_to IS NULL OR DATE(effective_to) >= #{date})
        ORDER BY effective_from DESC
        LIMIT 1
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<Approver> findByUserIdAndDate(@Param("userId") String userId,
                                                         @Param("date") LocalDate date);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE approver_email = #{approverId}
        ORDER BY effective_from DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findByApproverId(@Param("approverId") String approverId);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE approver_email = #{approverId}
          AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
        ORDER BY target_email ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findCurrentByApproverId(@Param("approverId") String approverId);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{userId}
        ORDER BY effective_from DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findHistoryByUserId(@Param("userId") String userId);
    
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{userId}
          AND DATE(effective_from) <= #{endDate}
          AND (effective_to IS NULL OR DATE(effective_to) >= #{startDate})
        ORDER BY effective_from ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findByUserIdAndPeriod(@Param("userId") String userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    @Select("""
        SELECT COUNT(*)
        FROM approvers
        WHERE target_email = #{userId}
          AND approver_email = #{approverId}
          AND DATE(effective_from) <= #{date}
          AND (effective_to IS NULL OR DATE(effective_to) >= #{date})
        """)
    int countByUserAndApproverAndDate(@Param("userId") String userId,
                                        @Param("approverId") String approverId,
                                        @Param("date") LocalDate date);
    
    @Select("""
        SELECT target_email
        FROM approvers
        WHERE approver_email = #{approverId}
          AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
        """)
    List<String> findSubordinateIds(@Param("approverId") String approverId);
    
    @Update("""
        DELETE FROM approvers
        WHERE target_email = #{userId}
        """)
    void deleteByUserId(@Param("userId") String userId);
    
    @Update("""
        UPDATE approvers
        SET effective_to = #{effectiveTo},
            updated_at = CURRENT_TIMESTAMP
        WHERE target_email = #{userId}
          AND effective_to IS NULL
        """)
    void endCurrentRelationship(@Param("userId") String userId, 
                               @Param("effectiveTo") LocalDate effectiveTo);
    
    @Select("""
        SELECT COUNT(*)
        FROM approvers
        WHERE target_email = #{userId}
          AND approver_email = #{approverId}
          AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
        """)
    int countActiveRelationship(@Param("userId") String userId, 
                               @Param("approverId") String approverId);
    
    /**
     * 指定承認者の有効な部下を取得（部下プロジェクトアサイン機能用）
     * approversとusersテーブルをJOINして、
     * user_status='ACTIVE'かつ有効期間内かつユーザーが有効な部下のみを取得
     */
    @Select("""
        SELECT a.id, a.target_email, a.approver_email, a.effective_from, a.effective_to,
               a.created_at, a.updated_at
        FROM approvers a
        INNER JOIN users u ON a.target_email = u.email
        WHERE a.approver_email = #{approverId}
          AND (a.effective_to IS NULL OR a.effective_to >= CURRENT_DATE)
          AND u.user_status = 'ACTIVE'
          AND (u.deleted_at IS NULL)
        ORDER BY u.full_name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findActiveSubordinatesByApproverId(@Param("approverId") String approverId);
    
    /**
     * 指定した承認者の配下にいるアクティブな部下一覧をページネーション対応で取得
     * アサイン機能廃止により、プロジェクト数とキャパシティは固定値を返す
     */
    @Select("""
        SELECT u.id, u.username, u.email,
               0 as assigned_project_count,
               false as is_at_max_capacity
        FROM users u
        INNER JOIN approvers a ON u.email = a.target_email
        WHERE a.approver_email = #{approverId}
          AND (a.effective_to IS NULL OR a.effective_to >= CURRENT_DATE)
          AND u.user_status = 'ACTIVE'
          AND (u.deleted_at IS NULL)
        ORDER BY ${sortBy}
        LIMIT #{size} OFFSET #{offset}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "currentProjectCount", column = "assigned_project_count"),
        @Result(property = "isMaxCapacity", column = "is_at_max_capacity")
    })
    List<SubordinateInfo> findActiveSubordinatesByApproverIdPaged(
        @Param("approverId") String approverId,
        @Param("offset") int offset,
        @Param("size") int size,
        @Param("sortBy") String sortBy
    );

    /**
     * 指定した承認者の配下にいるアクティブな部下の総数を取得
     */
    @Select("""
        SELECT COUNT(DISTINCT u.id)
        FROM users u
        INNER JOIN approvers a ON u.email = a.target_email
        WHERE a.approver_email = #{approverId}
          AND (a.effective_to IS NULL OR a.effective_to >= CURRENT_DATE)
          AND u.user_status = 'ACTIVE'
          AND (u.deleted_at IS NULL)
        """)
    long countActiveSubordinatesByApproverId(@Param("approverId") String approverId);
    
    /**
     * 承認者と部下の関係が有効かどうかをチェック（バッチアサイン用）
     */
    @Select("""
        SELECT COUNT(*)
        FROM approvers a
        INNER JOIN users u ON a.target_email = u.email
        WHERE a.approver_email = #{approverId}
          AND a.target_email = #{subordinateId}
          AND (a.effective_to IS NULL OR a.effective_to >= CURRENT_DATE)
          AND u.user_status = #{userStatus}
          AND (u.deleted_at IS NULL)
        """)
    int countByApproverIdAndSubordinateIdAndUserStatus(
        @Param("approverId") String approverId,
        @Param("subordinateId") String subordinateId, 
        @Param("userStatus") String userStatus
    );
    
    // ===== V44 Migration: New Email-based Methods =====
    
    /**
     * 対象者メールアドレスで承認者関係を検索（V44対応）
     * 
     * @param targetEmail 対象者メールアドレス
     * @return 承認者関係のリスト
     */
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{targetEmail}
        ORDER BY effective_from DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findByTargetEmail(@Param("targetEmail") String targetEmail);

    /**
     * 承認者メールアドレスで承認者関係を検索（V44対応）
     * 
     * @param approverEmail 承認者メールアドレス
     * @return 承認者関係のリスト
     */
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE approver_email = #{approverEmail}
        ORDER BY effective_from DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findByApproverEmail(@Param("approverEmail") String approverEmail);

    /**
     * 指定日において有効な承認者関係を取得（V44対応）
     * 
     * @param targetEmail 対象者メールアドレス
     * @param date 対象日
     * @return 有効な承認者関係のリスト
     */
    @Select("""
        SELECT id, target_email, approver_email, effective_from, effective_to,
               created_at, updated_at
        FROM approvers
        WHERE target_email = #{targetEmail}
          AND DATE(effective_from) <= #{date}
          AND (effective_to IS NULL OR DATE(effective_to) >= #{date})
        ORDER BY effective_from DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmail", column = "approver_email"),
        @Result(property = "effectiveFrom", column = "effective_from"),
        @Result(property = "effectiveTo", column = "effective_to"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Approver> findValidApproversForDate(@Param("targetEmail") String targetEmail, 
                                           @Param("date") LocalDate date);

    /**
     * 有効な承認者関係の存在チェック（V44対応）
     * 
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     * @param date 対象日
     * @return 有効な関係が存在する場合true
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM approvers
        WHERE target_email = #{targetEmail}
          AND approver_email = #{approverEmail}
          AND DATE(effective_from) <= #{date}
          AND (effective_to IS NULL OR DATE(effective_to) >= #{date})
        """)
    boolean isValidApprover(@Param("targetEmail") String targetEmail,
                           @Param("approverEmail") String approverEmail,
                           @Param("date") LocalDate date);

    /**
     * 対象者と承認者の組み合わせで削除（V44対応）
     * 
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     */
    @Update("""
        DELETE FROM approvers
        WHERE target_email = #{targetEmail}
          AND approver_email = #{approverEmail}
        """)
    void deleteByTargetAndApprover(@Param("targetEmail") String targetEmail,
                                  @Param("approverEmail") String approverEmail);

    /**
     * 全承認者関係を対象者でグループ化して取得（バッチ処理用）
     * 
     * @return ApproverGroupingのリスト
     */
    @Select("""
        SELECT target_email, GROUP_CONCAT(approver_email) as approver_emails
        FROM approvers
        GROUP BY target_email
        ORDER BY target_email
        """)
    @Results({
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmails", column = "approver_emails")
    })
    List<ApproverGrouping> findAllGroupedByTarget();

    /**
     * 現在有効な承認者関係を対象者でグループ化して取得（バッチ処理用）
     * 
     * @return ApproverGroupingのリスト
     */
    @Select("""
        SELECT target_email, GROUP_CONCAT(approver_email) as approver_emails
        FROM approvers
        WHERE effective_from <= NOW()
          AND (effective_to IS NULL OR effective_to >= NOW())
        GROUP BY target_email
        ORDER BY target_email
        """)
    @Results({
        @Result(property = "targetEmail", column = "target_email"),
        @Result(property = "approverEmails", column = "approver_emails")
    })
    List<ApproverGrouping> findActiveGroupedByTarget();

    // ===== Test Helper Methods =====
    
    /**
     * テスト用：全レコードの削除
     * 本番環境では使用しない
     */
    @Update("DELETE FROM approvers")
    void deleteAllForTesting();
    
    /**
     * テスト用：レコード数の取得
     * 本番環境では使用しない
     */
    @Select("SELECT COUNT(*) FROM approvers")
    int countAll();
}