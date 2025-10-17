package com.devhour.infrastructure.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.infrastructure.typehandler.CategoryHoursValueObjectTypeHandler;

/**
 * 工数記録MyBatisマッパー
 * 
 * 工数記録エンティティの永続化操作を提供
 * CategoryHours用のJSON TypeHandlerを使用
 * 承認関連カラムは work_record_approval テーブルで管理
 */
@Mapper
public interface WorkRecordMapper {
    
    /**
     * 工数記録IDで工数記録を検索
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WorkRecord> findById(@Param("id") String id);
    
    /**
     * ユーザーの指定日の全工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE user_id = #{userId} AND work_date = #{workDate} AND deleted_at IS NULL
        ORDER BY created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findByUserIdAndDate(@Param("userId") String userId,
                                       @Param("workDate") LocalDate workDate);
    
    /**
     * ユーザーの指定日の特定プロジェクトの工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE user_id = #{userId} AND work_date = #{workDate} AND project_id = #{projectId} AND deleted_at IS NULL
        ORDER BY created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WorkRecord> findByUserIdAndDateAndProjectId(String userId, LocalDate workDate, String projectId);
    
    /**
     * ユーザーの指定期間の工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE user_id = #{userId} AND work_date >= #{startDate} AND work_date <= #{endDate}
          AND deleted_at IS NULL
        ORDER BY work_date DESC, created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findByUserIdAndDateRange(@Param("userId") String userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    /**
     * ユーザーの全工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE user_id = #{userId} AND deleted_at IS NULL
        ORDER BY work_date DESC, created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findByUser(@Param("userId") String userId);
    
    /**
     * プロジェクトの全工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE project_id = #{projectId} AND deleted_at IS NULL
        ORDER BY work_date DESC, created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findByProject(@Param("projectId") String projectId);
    
    /**
     * 指定期間の全工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE work_date >= #{startDate} AND work_date <= #{endDate} AND deleted_at IS NULL
        ORDER BY work_date DESC, created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * ユーザーの最新工数記録を取得
     */
    @Select("""
        SELECT id, user_id, project_id, work_date, category_hours, 
               description, created_by, created_at, updated_by, updated_at
        FROM work_records 
        WHERE user_id = #{userId} AND deleted_at IS NULL
        ORDER BY work_date DESC, created_at DESC
        LIMIT #{limit}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "workDate", column = "work_date"),
        @Result(property = "categoryHours", column = "category_hours", 
                typeHandler = CategoryHoursValueObjectTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkRecord> findLatestByUser(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * 工数記録を挿入
     */
    @Insert("""
        INSERT INTO work_records (
            id, user_id, project_id, work_date, category_hours, description,
            created_by, created_at, updated_by, updated_at
        ) VALUES (
            #{id}, #{userId}, #{projectId}, #{workDate}, #{categoryHours,typeHandler=com.devhour.infrastructure.typehandler.CategoryHoursValueObjectTypeHandler}, #{description},
            #{createdBy}, #{createdAt}, #{updatedBy}, #{updatedAt}
        )
        """)
    void insert(@Param("id") String id,
               @Param("userId") String userId,
               @Param("projectId") String projectId,
               @Param("workDate") LocalDate workDate,
               @Param("categoryHours") CategoryHours categoryHours,
               @Param("description") String description,
               @Param("createdBy") String createdBy,
               @Param("createdAt") LocalDateTime createdAt,
               @Param("updatedBy") String updatedBy,
               @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 工数記録を更新
     */
    @Update("""
        UPDATE work_records SET
            category_hours = #{categoryHours,typeHandler=com.devhour.infrastructure.typehandler.CategoryHoursValueObjectTypeHandler},
            description = #{description},
            updated_by = #{updatedBy},
            updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int update(@Param("id") String id,
              @Param("categoryHours") CategoryHours categoryHours,
              @Param("description") String description,
              @Param("updatedBy") String updatedBy,
              @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 説明文のみ更新
     */
    @Update("""
        UPDATE work_records SET
            description = #{description},
            updated_by = #{updatedBy},
            updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int updateDescription(@Param("id") String id,
                         @Param("description") String description,
                         @Param("updatedBy") String updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 論理削除
     */
    @Update("""
        UPDATE work_records SET
            deleted_at = #{deletedAt},
            updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int softDelete(@Param("id") String id,
                  @Param("deletedAt") LocalDateTime deletedAt,
                  @Param("updatedAt") LocalDateTime updatedAt);

    /**     
     * 
     * 指定日のユーザーの工数記録を全て削除
     * @param userId
     * @param workDate
     */
    @Delete("""
        DELETE FROM work_records
        WHERE user_id = #{userId} AND work_date = #{workDate}
            """)
    void deleteByUserIdAndDate(String userId, LocalDate workDate);


    @Delete({
        "<script>",
        "DELETE FROM work_records",
        "WHERE user_id = #{userId} AND work_date = #{workDate}",
        "AND id IN",
        "<foreach item='recordId' collection='recordIds' open='(' separator=',' close=')'>",
        "#{recordId}",
        "</foreach>",
        "</script>"
    })
    void deleteByUserIdAndDateAndRecordIds(@Param("userId") String userId, @Param("workDate") LocalDate workDate, @Param("recordIds") List<String> recordIds);
}