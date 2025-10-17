package com.devhour.infrastructure.mapper;

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
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;

/**
 * 同期履歴詳細MyBatisマッパー
 * 
 * JIRA同期履歴詳細エンティティの永続化操作を提供
 * アノテーションベースのMyBatisマッピングでsync_history_detailsテーブルにアクセス
 * 
 * 責務:
 * - 同期履歴詳細の基本CRUD操作
 * - 同期履歴IDでの詳細一覧取得
 * - JIRAイシューキー別詳細検索
 * - エラー・成功ステータス別詳細取得
 */
@Mapper
public interface JiraSyncHistoryDetailMapper {
    
    /**
     * 同期履歴詳細を挿入
     *
     * @param id 詳細ID
     * @param syncHistoryId 同期履歴ID
     * @param seq 同期履歴内でのシーケンス番号
     * @param operation 操作
     * @param status ステータス
     * @param result 結果メッセージ
     * @param processedAt 処理日時
     */
    @Insert("""
        INSERT INTO jira_sync_history_details (id, sync_history_id, seq, operation,
                                         status, result, processed_at)
        VALUES (#{id}, #{syncHistoryId}, #{seq}, #{operation},
                #{status}, #{result}, #{processedAt})
        """)
    void insert(@Param("id") String id,
               @Param("syncHistoryId") String syncHistoryId,
               @Param("seq") Integer seq,
               @Param("operation") String operation,
               @Param("status") String status,
               @Param("result") String result,
               @Param("processedAt") LocalDateTime processedAt);

    /**
     * 同期履歴詳細を更新
     *
     * @param id 詳細ID
     * @param syncHistoryId 同期履歴ID
     * @param seq 同期履歴内でのシーケンス番号
     * @param operation 操作
     * @param status ステータス
     * @param result 結果メッセージ
     * @param processedAt 処理日時
     */
    @Update("""
        UPDATE jira_sync_history_details
        SET sync_history_id = #{syncHistoryId},
            seq = #{seq},
            operation = #{operation},
            status = #{status},
            result = #{result},
            processed_at = #{processedAt}
        WHERE id = #{id}
        """)
    void update(@Param("id") String id,
               @Param("syncHistoryId") String syncHistoryId,
               @Param("seq") Integer seq,
               @Param("operation") String operation,
               @Param("status") String status,
               @Param("result") String result,
               @Param("processedAt") LocalDateTime processedAt);
    /**
     * IDで同期履歴詳細を検索
     * 
     * @param id 詳細ID
     * @return 同期履歴詳細（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, sync_history_id, seq, operation,
               status, result, processed_at
        FROM jira_sync_history_details
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncHistoryId", column = "sync_history_id"),
        @Result(property = "seq", column = "seq"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.DetailStatusTypeHandler.class),
        @Result(property = "result", column = "result"),
        @Result(property = "processedAt", column = "processed_at")
    })
    Optional<JiraSyncHistoryDetail> selectById(@Param("id") String id);

    /**
     * 同期履歴IDで詳細一覧を取得
     *
     * 特定の同期実行の詳細結果を取得する際に使用
     * シーケンス番号の昇順でソート（処理順序を再現）
     *
     * @param syncHistoryId 同期履歴ID
     * @return 同期履歴詳細リスト（シーケンス番号昇順）
     */
    @Select("""
        SELECT id, sync_history_id, seq, operation,
               status, result, processed_at
        FROM jira_sync_history_details
        WHERE sync_history_id = #{syncHistoryId}
        ORDER BY seq ASC, processed_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncHistoryId", column = "sync_history_id"),
        @Result(property = "seq", column = "seq"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.DetailStatusTypeHandler.class),
        @Result(property = "result", column = "result"),
        @Result(property = "processedAt", column = "processed_at")
    })
    List<JiraSyncHistoryDetail> selectBySyncHistoryId(@Param("syncHistoryId") String syncHistoryId);

    /**
     * 操作タイプで同期履歴詳細を検索
     *
     * 特定操作の同期履歴を追跡する際に使用
     * 処理日時の降順でソート（最新の同期から順）
     *
     * @param operation 操作タイプ
     * @return 操作関連の同期履歴詳細リスト（処理日時降順）
     */
    @Select("""
        SELECT id, sync_history_id, seq, operation,
               status, result, processed_at
        FROM jira_sync_history_details
        WHERE operation = #{operation}
        ORDER BY processed_at DESC, seq ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncHistoryId", column = "sync_history_id"),
        @Result(property = "seq", column = "seq"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.DetailStatusTypeHandler.class),
        @Result(property = "result", column = "result"),
        @Result(property = "processedAt", column = "processed_at")
    })
    List<JiraSyncHistoryDetail> selectByOperation(@Param("operation") String operation);

    /**
     * エラー状態の同期履歴詳細を検索
     * 
     * 同期エラーの分析やトラブルシューティングに使用
     * 処理日時の降順でソート（最新のエラーから順）
     * 
     * @param syncHistoryId 同期履歴ID（オプション、nullの場合は全同期対象）
     * @return エラー状態の同期履歴詳細リスト（処理日時降順）
     */
    @Select("""
        <script>
        SELECT id, sync_history_id, seq, operation,
               status, result, processed_at
        FROM jira_sync_history_details
        WHERE status = 'ERROR'
        <if test="syncHistoryId != null">
        AND sync_history_id = #{syncHistoryId}
        </if>
        ORDER BY processed_at DESC, seq ASC
        </script>
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncHistoryId", column = "sync_history_id"),
        @Result(property = "seq", column = "seq"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.DetailStatusTypeHandler.class),
        @Result(property = "result", column = "result"),
        @Result(property = "processedAt", column = "processed_at")
    })
    List<JiraSyncHistoryDetail> selectErrorDetails(@Param("syncHistoryId") String syncHistoryId);

    /**
     * 成功状態の同期履歴詳細を検索
     * 
     * 正常な同期処理の確認や統計に使用
     * 処理日時の降順でソート
     * 
     * @param syncHistoryId 同期履歴ID（オプション、nullの場合は全同期対象）
     * @return 成功状態の同期履歴詳細リスト（処理日時降順）
     */
    @Select("""
        <script>
        SELECT id, sync_history_id, seq, operation,
               status, result, processed_at
        FROM jira_sync_history_details
        WHERE status = 'SUCCESS'
        <if test="syncHistoryId != null">
        AND sync_history_id = #{syncHistoryId}
        </if>
        ORDER BY processed_at DESC, seq ASC
        </script>
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "syncHistoryId", column = "sync_history_id"),
        @Result(property = "seq", column = "seq"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "status", column = "status",
                typeHandler = com.devhour.infrastructure.typehandler.DetailStatusTypeHandler.class),
        @Result(property = "result", column = "result"),
        @Result(property = "processedAt", column = "processed_at")
    })
    List<JiraSyncHistoryDetail> selectSuccessDetails(@Param("syncHistoryId") String syncHistoryId);

}