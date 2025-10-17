package com.devhour.infrastructure.mapper;

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
import com.devhour.domain.model.entity.JiraJqlQuery;

/**
 * JQLクエリMyBatisマッパー
 * 
 * JQLクエリエンティティの永続化操作を提供
 * アノテーションベースのMyBatisマッピングでjql_queriesテーブルにアクセス
 * 
 * 責務:
 * - JQLクエリの基本CRUD操作
 * - アクティブクエリの検索と優先度順ソート
 * - テンプレートIDによるクエリ検索
 * - ページネーション対応のクエリ取得
 * - アクティブクエリ数のカウント
 */
@Mapper
public interface JiraJqlQueryMapper {
    
    /**
     * JQLクエリを挿入
     * 
     * @param id クエリID
     * @param queryName クエリ名
     * @param jqlExpression JQL式
     * @param templateId テンプレートID
     * @param isActive アクティブフラグ
     * @param priority 優先度
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @param createdBy 作成者
     * @param updatedBy 更新者
     */
    @Insert("""
        INSERT INTO jira_jql_queries (id, query_name, jql_expression, template_id, is_active,
                                 priority, created_at, updated_at, created_by, updated_by)
        VALUES (#{id}, #{queryName}, #{jqlExpression}, #{templateId}, #{isActive},
                #{priority}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})
        """)
    void insert(@Param("id") String id,
               @Param("queryName") String queryName,
               @Param("jqlExpression") String jqlExpression,
               @Param("templateId") String templateId,
               @Param("isActive") Boolean isActive,
               @Param("priority") Integer priority,
               @Param("createdAt") LocalDateTime createdAt,
               @Param("updatedAt") LocalDateTime updatedAt,
               @Param("createdBy") String createdBy,
               @Param("updatedBy") String updatedBy);

    /**
     * IDでJQLクエリを検索
     * 
     * @param id クエリID
     * @return JQLクエリ（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    Optional<JiraJqlQuery> selectById(@Param("id") String id);

    /**
     * クエリ名でJQLクエリを検索
     * 
     * @param queryName クエリ名
     * @return JQLクエリ（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        WHERE query_name = #{queryName}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    Optional<JiraJqlQuery> selectByQueryName(@Param("queryName") String queryName);

    /**
     * JQLクエリを更新
     * 
     * @param id クエリID
     * @param queryName クエリ名
     * @param jqlExpression JQL式
     * @param templateId テンプレートID
     * @param isActive アクティブフラグ
     * @param priority 優先度
     * @param updatedAt 更新日時
     * @param updatedBy 更新者
     * @return 更新された行数
     */
    @Update("""
        UPDATE jira_jql_queries 
        SET query_name = #{queryName}, jql_expression = #{jqlExpression}, 
            template_id = #{templateId}, is_active = #{isActive}, 
            priority = #{priority}, updated_at = #{updatedAt}, updated_by = #{updatedBy}
        WHERE id = #{id}
        """)
    int update(@Param("id") String id,
              @Param("queryName") String queryName,
              @Param("jqlExpression") String jqlExpression,
              @Param("templateId") String templateId,
              @Param("isActive") Boolean isActive,
              @Param("priority") Integer priority,
              @Param("updatedAt") LocalDateTime updatedAt,
              @Param("updatedBy") String updatedBy);

    /**
     * 全JQLクエリをページネーションで取得
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return JQLクエリリスト
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        ORDER BY priority ASC, created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    List<JiraJqlQuery> selectAllWithPagination(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 全JQLクエリをページネーションで取得
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return JQLクエリリスト
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        ORDER BY priority ASC, created_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    List<JiraJqlQuery> selectAll();

    /**
     * アクティブJQLクエリを優先度順で取得
     * 
     * @return アクティブなJQLクエリリスト（優先度降順）
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        WHERE is_active = true
        ORDER BY priority ASC, created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    List<JiraJqlQuery> selectActiveQueriesOrderByPriority();

    /**
     * テンプレートIDでJQLクエリを検索
     * 
     * @param templateId テンプレートID
     * @return 指定テンプレートのJQLクエリリスト
     */
    @Select("""
        SELECT id, query_name, jql_expression, template_id, is_active,
               priority, created_at, updated_at, created_by, updated_by
        FROM jira_jql_queries 
        WHERE template_id = #{templateId}
        ORDER BY priority ASC, created_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "queryName", column = "query_name"),
        @Result(property = "jqlExpression", column = "jql_expression"),
        @Result(property = "templateId", column = "template_id"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "updatedBy", column = "updated_by")
    })
    List<JiraJqlQuery> selectByTemplateId(@Param("templateId") String templateId);

    /**
     * JQLクエリを削除
     * 
     * @param id 削除対象のクエリID
     * @return 削除された行数
     */
    @Delete("DELETE FROM jira_jql_queries WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * アクティブなJQLクエリ数をカウント
     * 
     * @return アクティブなJQLクエリ数
     */
    @Select("SELECT COUNT(*) FROM jira_jql_queries WHERE is_active = true")
    long countActiveQueries();

    /**
     * テスト用: レスポンステンプレートを挿入
     * 
     * @param id テンプレートID
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    @Insert("""
        INSERT INTO jira_response_template (id, template_name, velocity_template, template_description, created_at, updated_at)
        VALUES (#{id}, #{templateName}, #{velocityTemplate}, #{templateDescription}, #{createdAt}, #{updatedAt})
        """)
    void insertTestTemplate(@Param("id") String id,
                           @Param("templateName") String templateName,
                           @Param("velocityTemplate") String velocityTemplate,
                           @Param("templateDescription") String templateDescription,
                           @Param("createdAt") LocalDateTime createdAt,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * テスト用: アクティブステータスを更新
     * 
     * @param id クエリID
     * @param isActive アクティブフラグ
     * @param updatedAt 更新日時
     * @param updatedBy 更新者
     * @return 更新された行数
     */
    @Update("""
        UPDATE jira_jql_queries 
        SET is_active = #{isActive}, updated_at = #{updatedAt}, updated_by = #{updatedBy}
        WHERE id = #{id}
        """)
    int updateActiveStatus(@Param("id") String id,
                          @Param("isActive") boolean isActive,
                          @Param("updatedAt") LocalDateTime updatedAt,
                          @Param("updatedBy") String updatedBy);
}