package com.devhour.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 詳細ステータス値オブジェクト
 * 
 * JIRA同期の詳細処理結果を表現する列挙型
 * 各プロジェクトの処理結果（成功/エラー）を管理する
 */
public enum DetailStatus {
    
    /**
     * 成功 - プロジェクトの同期処理が正常に完了
     */
    SUCCESS("SUCCESS", "成功"),
    
    /**
     * エラー - プロジェクトの同期処理中にエラーが発生
     */
    ERROR("ERROR", "エラー");
    
    private final String value;
    private final String displayName;
    
    DetailStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * データベース保存用の値を取得
     * 
     * @return 詳細ステータス値
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
     * 文字列からDetailStatusを取得
     * 
     * @param value 詳細ステータス文字列
     * @return DetailStatus
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static DetailStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("詳細ステータスはnullにできません");
        }
        
        for (DetailStatus detailStatus : DetailStatus.values()) {
            if (detailStatus.value.equals(value)) {
                return detailStatus;
            }
        }
        
        throw new IllegalArgumentException(
            "不正な詳細ステータスです: " + value + " (許可された値: SUCCESS, ERROR)");
    }
    
    /**
     * 成功かを判定
     * 
     * @return 成功の場合true
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * エラーかを判定
     * 
     * @return エラーの場合true
     */
    public boolean isError() {
        return this == ERROR;
    }
    
    @Override
    public String toString() {
        return value;
    }
}