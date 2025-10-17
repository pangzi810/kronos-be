package com.devhour.presentation.dto.response;

import java.time.LocalDateTime;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JIRA手動同期実行レスポンスDTO
 * 
 * 手動同期実行API (/api/jira/sync/manual) のレスポンスボディ
 * 同期実行の開始と実行ID、ステータスを含む
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JiraSyncResponse {
    
    /**
     * 同期実行ID
     * 同期履歴の一意識別子
     */
    private String syncId;
    
    /**
     * 実行結果メッセージ
     */
    private String message;
    
    /**
     * 同期タイプ
     * MANUAL または SCHEDULED
     */
    private String status;
    
    /**
     * 同期開始日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    
    /**
     * 実行者
     * オプショナルフィールド
     */
    private String executedBy;
    
    /**
     * エラーメッセージ（失敗時のみ）
     * オプショナルフィールド
     */
    private String errorMessage;
    
    /**
     * 同期実行成功のレスポンスを作成
     * 
     * @param syncHistory 同期履歴エンティティ
     * @return 成功を示すSyncResponse
     */
    public static JiraSyncResponse success(JiraSyncHistory syncHistory) {
        return JiraSyncResponse.builder()
                .syncId(syncHistory.getId())
                .message("手動同期を開始しました")
                .status(syncHistory.getSyncType().name())
                .startedAt(syncHistory.getStartedAt())
                .executedBy(syncHistory.getTriggeredBy())
                .build();
    }
    
    /**
     * 同期実行成功のレスポンスをカスタムメッセージで作成
     * 
     * @param syncHistory 同期履歴エンティティ
     * @param message カスタム成功メッセージ
     * @return 成功を示すSyncResponse
     */
    public static JiraSyncResponse success(JiraSyncHistory syncHistory, String message) {
        return JiraSyncResponse.builder()
                .syncId(syncHistory.getId())
                .message(message)
                .status(syncHistory.getSyncType().name())
                .startedAt(syncHistory.getStartedAt())
                .executedBy(syncHistory.getTriggeredBy())
                .build();
    }
    
    /**
     * 同期実行失敗のレスポンスを作成
     * 
     * @param message エラーメッセージ
     * @return 失敗を示すSyncResponse
     */
    public static JiraSyncResponse failure(String message) {
        return JiraSyncResponse.builder()
                .message(message)
                .errorMessage(message)
                .startedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 同期実行失敗のレスポンスを詳細エラー情報付きで作成
     * 
     * @param message エラーメッセージ
     * @param errorMessage 詳細エラーメッセージ
     * @return 失敗を示すSyncResponse
     */
    public static JiraSyncResponse failure(String message, String errorMessage) {
        return JiraSyncResponse.builder()
                .message(message)
                .errorMessage(errorMessage)
                .startedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 同期実行競合エラー（既に実行中）のレスポンスを作成
     * 
     * @param message 競合エラーメッセージ
     * @return 競合を示すSyncResponse
     */
    public static JiraSyncResponse conflict(String message) {
        return JiraSyncResponse.builder()
                .message(message)
                .errorMessage("別の同期処理が実行中です")
                .startedAt(LocalDateTime.now())
                .build();
    }
}