package com.devhour.infrastructure.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.devhour.domain.model.entity.Project;

/**
 * プロジェクトMyBatisマッパー
 * 
 * プロジェクトエンティティの永続化操作を提供
 */
@Mapper
public interface ProjectMapper {
    
    /**
     * プロジェクトIDでプロジェクトを検索
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    Optional<Project> findById(@Param("id") String id);
    
    /**
     * プロジェクト名でプロジェクトを検索
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE name = #{name} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    Optional<Project> findByName(@Param("name") String name);
    
    /**
     * 全プロジェクト取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE deleted_at IS NULL
        ORDER BY created_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findAll();
    
    /**
     * 指定状態のプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE status = #{status} AND deleted_at IS NULL
        ORDER BY start_date ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findByStatus(@Param("status") String status);
    
    /**
     * アクティブなプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE status IN ('DRAFT', 'IN_PROGRESS') AND deleted_at IS NULL
        ORDER BY start_date ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findActiveProjects();
    
    /**
     * 最近工数記録されたプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE deleted_at IS NULL
          AND id IN (
            SELECT distinct project_id
            FROM work_records
            WHERE deleted_at IS NULL
              AND work_date between (CURDATE() - INTERVAL 1 MONTH ) and CURDATE()
            ORDER BY work_date DESC
          )
        ORDER BY id
        LIMIT 10
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findRecentWorkRecordedProjects();

    /**
     * 工数記録可能なプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE status = 'IN_PROGRESS' AND deleted_at IS NULL
        ORDER BY start_date ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findWorkRecordableProjects();
    
    /**
     * @deprecated アサイン機能廃止により使用されません
     */
    @Deprecated
    default List<Project> findWorkRecordableProjectsByUser(@Param("userId") String userId) {
        throw new UnsupportedOperationException("廃止されたメソッドです。findWorkRecordableProjects()を使用してください。");
    }
    
    /**
     * 指定期間に開始されるプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE start_date BETWEEN #{startDate} AND #{endDate} AND deleted_at IS NULL
        ORDER BY start_date ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findByStartDateBetween(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * 指定期間に終了予定のプロジェクト一覧を取得
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE planned_end_date BETWEEN #{startDate} AND #{endDate} AND deleted_at IS NULL
        ORDER BY planned_end_date ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> findByPlannedEndDateBetween(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * プロジェクト名での部分一致検索
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE name LIKE CONCAT('%', #{namePattern}, '%') AND deleted_at IS NULL
        ORDER BY name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> searchByName(@Param("namePattern") String namePattern);
    

    /**
     * プロジェクト名での部分一致検索
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects
        WHERE (
            name LIKE CONCAT('%', #{query}, '%') OR
            jira_issue_key LIKE CONCAT('%', #{query}, '%')
            ) AND deleted_at IS NULL
        ORDER BY name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    List<Project> searchByNameOrJiraIssueKey(@Param("query") String query);

    /**
     * プロジェクト作成
     */
    @Insert("""
        INSERT INTO projects (id, name, description, status, start_date, planned_end_date,
                             created_by, created_at, updated_at, jira_issue_key, custom_fields)
        VALUES (#{id}, #{name}, #{description}, #{status}, #{startDate}, #{plannedEndDate},
                #{createdBy}, #{createdAt}, #{updatedAt}, #{jiraIssueKey}, #{customFields})
        """)
    void insert(@Param("id") String id,
                @Param("name") String name,
                @Param("description") String description,
                @Param("status") String status,
                @Param("startDate") LocalDate startDate,
                @Param("plannedEndDate") LocalDate plannedEndDate,
                @Param("createdBy") String createdBy,
                @Param("createdAt") LocalDateTime createdAt,
                @Param("updatedAt") LocalDateTime updatedAt,
                @Param("jiraIssueKey") String jiraIssueKey,
                @Param("customFields") String customFields);
    
    /**
     * プロジェクト更新
     */
    @Update("""
        UPDATE projects
        SET name = #{name}, description = #{description}, status = #{status}, start_date = #{startDate},
            planned_end_date = #{plannedEndDate}, jira_issue_key = #{jiraIssueKey},
            custom_fields = #{customFields}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int update(@Param("id") String id,
               @Param("name") String name,
               @Param("description") String description,
               @Param("status") String status,
               @Param("startDate") LocalDate startDate,
               @Param("plannedEndDate") LocalDate plannedEndDate,
               @Param("jiraIssueKey") String jiraIssueKey,
               @Param("customFields") String customFields,
               @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * プロジェクト状態更新
     */
    @Update("""
        UPDATE projects 
        SET status = #{status}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int updateStatus(@Param("id") String id,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * プロジェクトのJIRAイシューキー更新
     * 
     * JIRA同期機能でイシューキーを設定/更新する際に使用
     * 
     * @param id プロジェクトID
     * @param jiraIssueKey JIRAイシューキー
     * @param updatedAt 更新日時
     * @return 更新された行数
     */
    @Update("""
        UPDATE projects 
        SET jira_issue_key = #{jiraIssueKey}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int updateJiraIssueKey(@Param("id") String id,
                          @Param("jiraIssueKey") String jiraIssueKey,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    
    /**
     * プロジェクト論理削除
     */
    @Update("""
        UPDATE projects 
        SET deleted_at = #{deletedAt}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int softDelete(@Param("id") String id,
                   @Param("deletedAt") LocalDateTime deletedAt,
                   @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * プロジェクト名存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM projects 
        WHERE name = #{name} AND deleted_at IS NULL
        """)
    boolean existsByName(@Param("name") String name);
    
    /**
     * プロジェクト存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM projects 
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    boolean existsById(@Param("id") String id);
    
    /**
     * JIRAイシューキーでプロジェクトを検索
     * 
     * JIRA同期機能でイシューキーからプロジェクトを特定する際に使用
     * 
     * @param jiraIssueKey JIRAイシューキー
     * @return プロジェクト（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, name, description, status, start_date, planned_end_date,
               actual_end_date, created_by, created_at, updated_at, jira_issue_key, custom_fields
        FROM projects 
        WHERE jira_issue_key = #{jiraIssueKey} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status", 
                typeHandler = com.devhour.infrastructure.typehandler.ProjectStatusTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "plannedEndDate", column = "planned_end_date"),
        @Result(property = "actualEndDate", column = "actual_end_date"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "jiraIssueKey", column = "jira_issue_key"),
        @Result(property = "customFields", column = "custom_fields")
    })
    Optional<Project> selectByJiraIssueKey(@Param("jiraIssueKey") String jiraIssueKey);
    
    /**
     * プロジェクト総数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM projects 
        WHERE deleted_at IS NULL
        """)
    long count();
    
    /**
     * 指定状態のプロジェクト数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM projects 
        WHERE status = #{status} AND deleted_at IS NULL
        """)
    long countByStatus(@Param("status") String status);
}