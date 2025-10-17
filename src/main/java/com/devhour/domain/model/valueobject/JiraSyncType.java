package com.devhour.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 同期タイプ値オブジェクト
 * 
 * JIRA同期の実行方法を表現する列挙型
 * 手動実行とスケジュール実行を区別するために使用される
 */
public enum JiraSyncType {
    
    /**
     * 手動同期 - ユーザーが手動で実行した同期
     */
    MANUAL("MANUAL", "手動同期"),
    
    /**
     * スケジュール同期 - スケジューラーにより自動実行された同期
     */
    SCHEDULED("SCHEDULED", "スケジュール同期");
    
    private final String value;
    private final String displayName;
    
    JiraSyncType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * データベース保存用の値を取得
     * 
     * @return 同期タイプ値
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
     * 文字列からSyncTypeを取得
     * 
     * @param value 同期タイプ文字列
     * @return SyncType
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static JiraSyncType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("同期タイプはnullにできません");
        }
        
        for (JiraSyncType syncType : JiraSyncType.values()) {
            if (syncType.value.equals(value)) {
                return syncType;
            }
        }
        
        throw new IllegalArgumentException(
            "不正な同期タイプです: " + value + " (許可された値: MANUAL, SCHEDULED)");
    }
    
    /**
     * 手動同期かを判定
     * 
     * @return 手動同期の場合true
     */
    public boolean isManual() {
        return this == MANUAL;
    }
    
    /**
     * スケジュール同期かを判定
     * 
     * @return スケジュール同期の場合true
     */
    public boolean isScheduled() {
        return this == SCHEDULED;
    }
    
    @Override
    public String toString() {
        return value;
    }
}