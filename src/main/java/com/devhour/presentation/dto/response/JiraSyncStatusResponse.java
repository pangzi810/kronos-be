package com.devhour.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Optional;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JIRA同期ステータスレスポンスDTO
 * 
 * 同期ステータス取得API (/api/jira/sync/status) のレスポンスボディ
 * 現在の同期実行状況と最後の同期結果を含む
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JiraSyncStatusResponse {
    
    /**
     * 同期実行中フラグ
     * true: 現在同期実行中, false: 同期待機中
     */
    @JsonProperty("isRunning")
    private boolean isRunning;
    
    /**
     * 現在実行中の同期ID（実行中の場合のみ）
     */
    private String currentSyncId;
    
    /**
     * 現在実行中の同期タイプ（実行中の場合のみ）
     */
    private String syncType;
    
    /**
     * 現在の同期開始日時（実行中の場合のみ）
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    
    /**
     * 現在の同期実行者（実行中の場合のみ）
     */
    private String executedBy;
    
    /**
     * 最後に完了した同期の情報
     */
    private LastCompletedSyncInfo lastCompletedSync;
    
    /**
     * 次回スケジュール同期予定時刻（スケジュール設定済みの場合）
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextScheduledSync;
    
    /**
     * 最後に完了した同期の詳細情報
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LastCompletedSyncInfo {
        
        /**
         * 完了した同期ID
         */
        private String syncId;
        
        /**
         * 同期タイプ
         */
        private String syncType;
        
        /**
         * 完了日時
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime completedAt;
        
        /**
         * 同期ステータス
         * COMPLETED, FAILED など
         */
        private String status;
        
        /**
         * 処理件数
         */
        private Integer processedCount;
        
        /**
         * エラーメッセージ（失敗時のみ）
         */
        private String errorMessage;
        
        /**
         * 実行者
         */
        private String executedBy;
        
        /**
         * 同期履歴から最後の完了同期情報を作成
         * 
         * @param syncHistory 完了済み同期履歴
         * @return LastCompletedSyncInfo インスタンス
         */
        public static LastCompletedSyncInfo fromSyncHistory(JiraSyncHistory syncHistory) {
            return LastCompletedSyncInfo.builder()
                    .syncId(syncHistory.getId())
                    .syncType(syncHistory.getSyncType().name())
                    .completedAt(syncHistory.getCompletedAt())
                    .status(syncHistory.getSyncStatus().name())
                    .processedCount(syncHistory.getTotalProjectsProcessed())
                    .errorMessage(syncHistory.getErrorDetails())
                    .executedBy(syncHistory.getTriggeredBy())
                    .build();
        }
    }
    
    /**
     * 同期実行中のステータスレスポンスを作成
     * 
     * @param runningSyncHistory 実行中の同期履歴
     * @return 実行中を示すSyncStatusResponse
     */
    public static JiraSyncStatusResponse running(JiraSyncHistory runningSyncHistory) {
        return JiraSyncStatusResponse.builder()
                .isRunning(true)
                .currentSyncId(runningSyncHistory.getId())
                .syncType(runningSyncHistory.getSyncType().name())
                .startedAt(runningSyncHistory.getStartedAt())
                .executedBy(runningSyncHistory.getTriggeredBy())
                .build();
    }
    
    /**
     * 同期実行中のステータスレスポンスを最後の完了同期情報付きで作成
     * 
     * @param runningSyncHistory 実行中の同期履歴
     * @param lastCompletedSync 最後の完了同期履歴
     * @return 実行中を示すSyncStatusResponse
     */
    public static JiraSyncStatusResponse running(JiraSyncHistory runningSyncHistory, JiraSyncHistory lastCompletedSync) {
        return JiraSyncStatusResponse.builder()
                .isRunning(true)
                .currentSyncId(runningSyncHistory.getId())
                .syncType(runningSyncHistory.getSyncType().name())
                .startedAt(runningSyncHistory.getStartedAt())
                .executedBy(runningSyncHistory.getTriggeredBy())
                .lastCompletedSync(LastCompletedSyncInfo.fromSyncHistory(lastCompletedSync))
                .build();
    }
    
    /**
     * 同期待機中（アイドル）のステータスレスポンスを作成
     * 
     * @return 待機中を示すSyncStatusResponse
     */
    public static JiraSyncStatusResponse idle() {
        return JiraSyncStatusResponse.builder()
                .isRunning(false)
                .build();
    }
    
    /**
     * 同期待機中のステータスレスポンスを最後の完了同期情報付きで作成
     * 
     * @param lastCompletedSync 最後の完了同期履歴
     * @return 待機中を示すSyncStatusResponse
     */
    public static JiraSyncStatusResponse idle(JiraSyncHistory lastCompletedSync) {
        return JiraSyncStatusResponse.builder()
                .isRunning(false)
                .lastCompletedSync(LastCompletedSyncInfo.fromSyncHistory(lastCompletedSync))
                .build();
    }
    
    /**
     * 同期待機中のステータスレスポンスを次回スケジュール情報付きで作成
     * 
     * @param lastCompletedSync 最後の完了同期履歴
     * @param nextScheduledSync 次回スケジュール同期予定時刻
     * @return 待機中を示すSyncStatusResponse
     */
    public static JiraSyncStatusResponse idle(JiraSyncHistory lastCompletedSync, LocalDateTime nextScheduledSync) {
        return JiraSyncStatusResponse.builder()
                .isRunning(false)
                .lastCompletedSync(LastCompletedSyncInfo.fromSyncHistory(lastCompletedSync))
                .nextScheduledSync(nextScheduledSync)
                .build();
    }
    
    /**
     * 同期履歴から適切なステータスレスポンスを作成
     * 
     * @param currentSync 現在の同期（Optional、実行中がない場合は空）
     * @param lastCompletedSync 最後の完了同期（Optional、履歴がない場合は空）
     * @return SyncStatusResponse インスタンス
     */
    public static JiraSyncStatusResponse from(Optional<JiraSyncHistory> currentSync, Optional<JiraSyncHistory> lastCompletedSync) {
        if (currentSync.isPresent()) {
            // 実行中の同期あり
            JiraSyncHistory running = currentSync.get();
            if (lastCompletedSync.isPresent()) {
                return running(running, lastCompletedSync.get());
            } else {
                return running(running);
            }
        } else {
            // 実行中の同期なし（アイドル状態）
            if (lastCompletedSync.isPresent()) {
                return idle(lastCompletedSync.get());
            } else {
                return idle();
            }
        }
    }
}