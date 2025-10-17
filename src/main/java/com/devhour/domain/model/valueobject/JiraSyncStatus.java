package com.devhour.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 同期ステータス値オブジェクト
 * 
 * JIRA同期の実行状態を表現する列挙型
 * 同期の進行状況と最終結果を管理する
 */
public enum JiraSyncStatus {
    
    /**
     * 実行中 - 同期処理が現在実行されている状態
     */
    IN_PROGRESS("IN_PROGRESS", "実行中"),
    
    /**
     * 完了 - 同期処理が正常に完了した状態
     */
    COMPLETED("COMPLETED", "完了"),
    
    /**
     * 失敗 - 同期処理中にエラーが発生し失敗した状態
     */
    FAILED("FAILED", "失敗");
    
    private final String value;
    private final String displayName;
    
    JiraSyncStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * データベース保存用の値を取得
     * 
     * @return 同期ステータス値
     */
    @JsonValue
    public String getValue() {
        return value;
    }
    
    /**
     * 画面表示用の名称を取得
     * 
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 文字列からSyncStatusを取得
     * 
     * @param value 同期ステータス文字列
     * @return SyncStatus
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static JiraSyncStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("同期ステータスはnullにできません");
        }
        
        for (JiraSyncStatus syncStatus : JiraSyncStatus.values()) {
            if (syncStatus.value.equals(value)) {
                return syncStatus;
            }
        }
        
        throw new IllegalArgumentException(
            "不正な同期ステータスです: " + value + " (許可された値: IN_PROGRESS, COMPLETED, FAILED)");
    }
    
    /**
     * 実行中かを判定
     * 
     * @return 実行中の場合true
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }
    
    /**
     * 完了したかを判定
     * 
     * @return 完了の場合true
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * 失敗したかを判定
     * 
     * @return 失敗の場合true
     */
    public boolean isFailed() {
        return this == FAILED;
    }
    
    /**
     * 終了状態かを判定
     * 完了または失敗の場合に終了状態とみなす
     * 
     * @return 終了状態の場合true
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED;
    }
    
    /**
     * 指定されたステータスへの遷移が可能かを判定
     * 
     * @param newStatus 遷移先のステータス
     * @return 遷移可能な場合true
     */
    public boolean canTransitionTo(JiraSyncStatus newStatus) {
        if (this == newStatus) {
            return false; // 同じステータスへの遷移は不可
        }
        
        return switch (this) {
            case IN_PROGRESS -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED, FAILED -> false; // 終了状態からの遷移は不可
        };
    }
    
    @Override
    public String toString() {
        return value;
    }
}