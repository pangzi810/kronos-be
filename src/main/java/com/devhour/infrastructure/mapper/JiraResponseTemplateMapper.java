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
import com.devhour.domain.model.entity.JiraResponseTemplate;

/**
 * レスポンステンプレートMyBatisマッパー
 * 
 * JIRA同期機能で使用するVelocityテンプレートの永続化操作を提供
 * アノテーションベースのMyBatisマッピングでjira_response_templateテーブルにアクセス
 * 
 * 責務:
 * - レスポンステンプレートの基本CRUD操作
 * - テンプレート名による検索・重複チェック
 * - 名前パターンによるテンプレート検索
 * - ページネーション対応の一覧取得
 * - 存在チェック機能の提供
 */
@Mapper
public interface JiraResponseTemplateMapper {
    
    /**
     * レスポンステンプレートを挿入
     * 
     * @param id テンプレートID
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    @Insert("""
        INSERT INTO jira_response_template (id, template_name, velocity_template, 
                                      template_description, created_at, updated_at)
        VALUES (#{id}, #{templateName}, #{velocityTemplate}, 
                #{templateDescription}, #{createdAt}, #{updatedAt})
        """)
    void insert(@Param("id") String id,
               @Param("templateName") String templateName,
               @Param("velocityTemplate") String velocityTemplate,
               @Param("templateDescription") String templateDescription,
               @Param("createdAt") LocalDateTime createdAt,
               @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * IDでレスポンステンプレートを検索
     * 
     * @param id テンプレートID
     * @return レスポンステンプレート（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, template_name, velocity_template, template_description,
               created_at, updated_at
        FROM jira_response_template
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "templateName", column = "template_name"),
        @Result(property = "velocityTemplate", column = "velocity_template"),
        @Result(property = "templateDescription", column = "template_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<JiraResponseTemplate> selectById(@Param("id") String id);

    /**
     * レスポンステンプレートを更新
     * 
     * @param id テンプレートID
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明
     * @param updatedAt 更新日時
     * @return 更新された行数
     */
    @Update("""
        UPDATE jira_response_template 
        SET template_name = #{templateName}, 
            velocity_template = #{velocityTemplate},
            template_description = #{templateDescription}, 
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(@Param("id") String id,
              @Param("templateName") String templateName,
              @Param("velocityTemplate") String velocityTemplate,
              @Param("templateDescription") String templateDescription,
              @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * IDでレスポンステンプレートを削除
     * 
     * @param id 削除するテンプレートID
     * @return 削除された行数
     */
    @Delete("DELETE FROM jira_response_template WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * テンプレート名でレスポンステンプレートを検索
     * 
     * テンプレート名の重複チェックや特定テンプレートの検索に使用
     * 
     * @param templateName テンプレート名
     * @return レスポンステンプレート（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, template_name, velocity_template, template_description,
               created_at, updated_at
        FROM jira_response_template 
        WHERE template_name = #{templateName}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "templateName", column = "template_name"),
        @Result(property = "velocityTemplate", column = "velocity_template"),
        @Result(property = "templateDescription", column = "template_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<JiraResponseTemplate> selectByTemplateName(@Param("templateName") String templateName);

    /**
     * 全レスポンステンプレートを取得
     * 
     * 管理画面での全テンプレート一覧表示に使用
     * テンプレート名昇順でソート
     * 
     * @return レスポンステンプレートリスト
     */
    @Select("""
        SELECT id, template_name, velocity_template, template_description,
               created_at, updated_at
        FROM jira_response_template 
        ORDER BY template_name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "templateName", column = "template_name"),
        @Result(property = "velocityTemplate", column = "velocity_template"),
        @Result(property = "templateDescription", column = "template_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<JiraResponseTemplate> selectAll();

    /**
     * レスポンステンプレートをページネーションで取得
     * 
     * 管理画面でのテンプレート一覧表示に使用
     * 作成日時降順でソート（新しいテンプレートが先頭）
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return レスポンステンプレートリスト
     */
    @Select("""
        SELECT id, template_name, velocity_template, template_description,
               created_at, updated_at
        FROM jira_response_template 
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "templateName", column = "template_name"),
        @Result(property = "velocityTemplate", column = "velocity_template"),
        @Result(property = "templateDescription", column = "template_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<JiraResponseTemplate> selectAllWithPagination(@Param("limit") int limit, 
                                                 @Param("offset") int offset);

    /**
     * 全レスポンステンプレート数をカウント
     * 
     * ページネーション計算や統計情報取得に使用
     * 
     * @return レスポンステンプレート総数
     */
    @Select("SELECT COUNT(*) FROM jira_response_template")
    int countAll();

    /**
     * テンプレート名パターンでレスポンステンプレートを検索
     * 
     * 管理画面でのテンプレート検索機能に使用
     * 部分一致検索（大文字小文字区別あり）
     * 
     * @param pattern 検索パターン（部分一致）
     * @return 検索条件に一致するレスポンステンプレートリスト（テンプレート名昇順）
     */
    @Select("""
        SELECT id, template_name, velocity_template, template_description,
               created_at, updated_at
        FROM jira_response_template 
        WHERE template_name LIKE CONCAT('%', #{pattern}, '%')
        ORDER BY template_name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "templateName", column = "template_name"),
        @Result(property = "velocityTemplate", column = "velocity_template"),
        @Result(property = "templateDescription", column = "template_description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<JiraResponseTemplate> searchByNamePattern(@Param("pattern") String pattern);

    /**
     * テンプレート名の存在チェック
     * 
     * テンプレート作成時の重複チェックに使用
     * 
     * @param templateName チェックするテンプレート名
     * @return 存在する場合true
     */
    @Select("SELECT COUNT(*) > 0 FROM jira_response_template WHERE template_name = #{templateName}")
    boolean existsByTemplateName(@Param("templateName") String templateName);

    /**
     * テンプレート名の存在チェック（指定IDを除外）
     * 
     * テンプレート更新時の重複チェックに使用
     * 自分自身を除外してテンプレート名の重複をチェック
     * 
     * @param templateName チェックするテンプレート名
     * @param excludeId 除外するテンプレートID
     * @return 存在する場合true
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM jira_response_template 
        WHERE template_name = #{templateName} AND id != #{excludeId}
        """)
    boolean existsByTemplateNameExcludingId(@Param("templateName") String templateName,
                                           @Param("excludeId") String excludeId);
}