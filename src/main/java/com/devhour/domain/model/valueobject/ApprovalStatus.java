package com.devhour.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 承認ステータス値オブジェクト
 * 
 * 工数記録の承認状態を表現する列挙型
 */
public enum ApprovalStatus {
    
    /**
     * 未入力 - 工数記録が未入力または承認情報がない状態
     */
    NOT_ENTERED("NOT_ENTERED", "未入力"),
    
    /**
     * 承認待ち - 工数記録が保存され、上長の承認を待っている状態
     */
    PENDING("PENDING", "承認待ち"),
    
    /**
     * 承認済み - 上長により承認された状態
     */
    APPROVED("APPROVED", "承認済み"),
    
    /**
     * 却下 - 上長により却下された状態
     */
    REJECTED("REJECTED", "却下");
    
    private final String value;
    private final String displayName;
    
    ApprovalStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * データベース保存用の値を取得
     * 
     * @return ステータス値
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
     * 文字列からApprovalStatusを取得
     * 
     * @param value ステータス文字列
     * @return ApprovalStatus
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static ApprovalStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (ApprovalStatus status : ApprovalStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException(
            "Invalid approval status value: " + value);
    }
    
    /**
     * 未入力状態かを判定
     * 
     * @return 未入力の場合true
     */
    public boolean isNotEntered() {
        return this == NOT_ENTERED;
    }
    
    /**
     * 承認待ち状態かを判定
     * 
     * @return 承認待ちの場合true
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 承認済み状態かを判定
     * 
     * @return 承認済みの場合true
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 却下状態かを判定
     * 
     * @return 却下の場合true
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    /**
     * 編集可能な状態かを判定
     * 承認済みの工数は編集不可
     * 
     * @return 編集可能な場合true
     */
    public boolean isEditable() {
        return this != APPROVED;
    }
    
    /**
     * 承認可能な状態かを判定
     * 
     * @return 承認可能な場合true
     */
    public boolean canApprove() {
        return this == PENDING;
    }
    
    /**
     * 却下可能な状態かを判定
     * 
     * @return 却下可能な場合true
     */
    public boolean canReject() {
        return this == PENDING;
    }
    
    /**
     * 指定されたステータスへの遷移が可能かを判定
     * 
     * @param newStatus 遷移先のステータス
     * @return 遷移可能な場合true
     */
    public boolean canTransitionTo(ApprovalStatus newStatus) {
        if (this == newStatus) {
            return false; // 同じステータスへの遷移は不可
        }
        
        return switch (this) {
            case NOT_ENTERED -> newStatus == PENDING;
            case PENDING -> newStatus == APPROVED || newStatus == REJECTED;
            case APPROVED -> false; // 承認済みからの遷移は不可
            case REJECTED -> newStatus == PENDING; // 却下から承認待ちへの再申請は可能
        };
    }
    
    @Override
    public String toString() {
        return value;
    }
}