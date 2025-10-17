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
import com.devhour.domain.model.entity.JiraSyncHistory;

/**
 * 同期履歴MyBatisマッパー
 * 
 * JIRA同期履歴エンティティの永続化操作を提供
 * アノテーションベースのMyBatisマッピングでjira_sync_historiesテーブルにアクセス
 * 
 * 責務:
 * - 同期履歴の基本CRUD操作
 * - 実行中同期の検索
 * - 日付範囲での同期履歴取得
 * - ページネーション対応の履歴取得
 * - 同期ステータス・カウント更新
 */
@Mapper
public interface JiraSyncHistoryMapper {
    
    /**
     * 同期履歴を挿入
     * 
     * @param id 同期履歴ID
     * @param syncType 同期タイプ
     * @param syncStatus 同期ステータス
     * @param startedAt 開始日時
     * @param completedAt 完了日時
     * @param totalProjectsProcessed 処理プロジェクト総数
     * @param successCount 成功数
     * @param errorCount エラー数
     * @param errorDetails エラー詳細
     * @param triggeredBy 実行者/トリガー
     */
    @Insert("""
        INSERT INTO jira_sync_histories (id, sync_type, sync_status, started_at, completed_at,
                                   total_projects_processed, success_count, error_count,
                                   error_details, triggered_by)
        VALUES (#{id}, #{syncType}, #{syncStatus}, #{startedAt}, #{completedAt},
                #{totalProjectsProcessed}, #{successCount}, #{errorCount},
                #{errorDetails}, #{triggeredBy})
        """)
    void insert(@Param("id") String id,
               @Param("syncType") String syncType,
               @Param("syncStatus") String syncStatus,
               @Param("startedAt") LocalDateTime startedAt,
               @Param("completedAt") LocalDateTime completedAt,
               @Param("totalProjectsProcessed") Integer totalProjectsProcessed,
               @Param("successCount") Integer successCount,
               @Param("errorCount") Integer errorCount,
               @Param("errorDetails") String errorDetails,
               @Param("triggeredBy") String triggeredBy);

    /**
     * IDで同期履歴を検索
     * 
     * @param id 同期履歴ID
     * @return 同期履歴（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type", 
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    Optional<JiraSyncHistory> selectById(@Param("id") String id);

    /**
     * 同期履歴を更新
     * 
     * @param id 同期履歴ID
     * @param syncStatus 同期ステータス
     * @param completedAt 完了日時
     * @param totalProjectsProcessed 処理プロジェクト総数
     * @param successCount 成功数
     * @param errorCount エラー数
     * @param errorDetails エラー詳細
     * @return 更新された行数
     */
    @Update("""
        UPDATE jira_sync_histories 
        SET sync_status = #{syncStatus}, completed_at = #{completedAt},
            total_projects_processed = #{totalProjectsProcessed}, 
            success_count = #{successCount}, error_count = #{errorCount},
            error_details = #{errorDetails}
        WHERE id = #{id}
        """)
    int update(@Param("id") String id,
              @Param("syncStatus") String syncStatus,
              @Param("completedAt") LocalDateTime completedAt,
              @Param("totalProjectsProcessed") Integer totalProjectsProcessed,
              @Param("successCount") Integer successCount,
              @Param("errorCount") Integer errorCount,
              @Param("errorDetails") String errorDetails);

    /**
     * 最近の同期履歴をページネーションで取得
     * 
     * 管理画面での同期履歴一覧表示に使用
     * 開始日時の降順（最新から順）でソート
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return 同期履歴リスト（開始日時降順）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE (#{status} IS NULL OR sync_status = #{status})
        ORDER BY started_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    List<JiraSyncHistory> selectRecentWithPagination(@Param("limit") int limit, @Param("offset") int offset, @Param("status") String status);

    /**
     * 実行中の同期履歴を検索
     * 
     * 同期の重複実行防止や進行状況確認に使用
     * 開始日時の昇順でソート（古い実行から順）
     * 
     * @return 実行中の同期履歴リスト（開始日時昇順）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE sync_status = 'IN_PROGRESS'
        ORDER BY started_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    List<JiraSyncHistory> selectInProgress();

    /**
     * 指定日付範囲の同期履歴を検索
     * 
     * 特定期間の同期実行状況分析に使用
     * 開始日時の降順でソート
     * 
     * @param startDate 検索開始日時（この日時以降）
     * @param endDate 検索終了日時（この日時以前）
     * @return 期間内の同期履歴リスト（開始日時降順）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE started_at >= #{startDate} AND started_at <= #{endDate}
            AND (#{status} IS NULL OR sync_status = #{status})
        ORDER BY started_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    List<JiraSyncHistory> selectByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("status") String status);

    /**
     * 指定ステータスの同期履歴を検索
     * 
     * 同期実行状況の分析や失敗したジョブの確認に使用
     * 開始日時の降順でソート
     * 
     * @param status 同期ステータス
     * @return 指定ステータスの同期履歴リスト（開始日時降順）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE sync_status = #{status}
        ORDER BY started_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    List<JiraSyncHistory> selectByStatus(@Param("status") String status);

    /**
     * 指定トリガータイプの同期履歴を検索
     * 
     * 手動実行・自動実行の実行状況分析に使用
     * 開始日時の降順でソート
     * 
     * @param triggerType トリガータイプ（MANUAL, SCHEDULED）
     * @return 指定トリガータイプの同期履歴リスト（開始日時降順）
     */
    @Select("""
        SELECT id, sync_type, sync_status, started_at, completed_at,
               total_projects_processed, success_count, error_count,
               error_details, triggered_by
        FROM jira_sync_histories 
        WHERE sync_type = #{triggerType}
        ORDER BY started_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncType", column = "sync_type",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncTypeTypeHandler.class),
        @Result(property = "syncStatus", column = "sync_status",
                typeHandler = com.devhour.infrastructure.typehandler.JiraSyncStatusTypeHandler.class),
        @Result(property = "startedAt", column = "started_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalProjectsProcessed", column = "total_projects_processed"),
        @Result(property = "successCount", column = "success_count"),
        @Result(property = "errorCount", column = "error_count"),
        @Result(property = "errorDetails", column = "error_details"),
        @Result(property = "triggeredBy", column = "triggered_by")
    })
    List<JiraSyncHistory> selectByTriggerType(@Param("triggerType") String triggerType);

    /**
     * 同期履歴を削除
     * 
     * 物理削除を実行（古い履歴のクリーンアップに使用）
     * 
     * @param id 削除対象の同期履歴ID
     * @return 削除された行数
     */
    @Delete("DELETE FROM jira_sync_histories WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * 指定ステータスの同期履歴数をカウント
     * 
     * 同期実行成功率や失敗率の統計情報取得に使用
     * 
     * @param status 同期ステータス
     * @return 指定ステータスの同期履歴数
     */
    @Select("SELECT COUNT(*) FROM jira_sync_histories WHERE sync_status = #{status}")
    long countByStatus(@Param("status") String status);

    /**
     * 最近N時間以内の失敗数をカウント
     * 
     * 同期実行の安定性監視や障害検知に使用
     * 
     * @param since 検索開始日時（この日時以降）
     * @return 最近N時間以内の失敗数
     */
    @Select("""
        SELECT COUNT(*) FROM jira_sync_histories 
        WHERE sync_status = 'FAILED' AND started_at >= #{since}
        """)
    long countRecentFailures(@Param("since") LocalDateTime since);
}