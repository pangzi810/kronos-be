package com.devhour.domain.exception;

import com.devhour.domain.model.valueobject.ApprovalStatus;

/**
 * 承認処理に関する例外クラス
 * 
 * 承認ワークフローで発生する各種例外（権限、所有権、ステータス）を統合
 */
public class ApprovalException extends RuntimeException {
    
    /**
     * 承認例外の種類
     */
    public enum Type {
        /** 承認権限に関する例外 */
        AUTHORITY,
        /** 所有権に関する例外 */
        OWNERSHIP, 
        /** ステータス遷移に関する例外 */
        STATUS
    }
    
    private final Type type;
    private final String approverId;
    private final String workRecordId;
    private final String ownerId;
    private final ApprovalStatus currentStatus;
    private final ApprovalStatus targetStatus;
    private final String operation;
    
    // Private constructor for internal use
    private ApprovalException(Type type, String message, String approverId, String workRecordId, 
                             String ownerId, ApprovalStatus currentStatus, ApprovalStatus targetStatus, 
                             String operation) {
        super(message);
        this.type = type;
        this.approverId = approverId;
        this.workRecordId = workRecordId;
        this.ownerId = ownerId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
        this.operation = operation;
    }
    
    // Static factory methods for clarity
    public static ApprovalException authority(String approverId, String workRecordId) {
        String message = String.format("承認者 %s は工数記録 %s を承認する権限がありません", approverId, workRecordId);
        return new ApprovalException(Type.AUTHORITY, message, approverId, workRecordId, 
                                   null, null, null, null);
    }
    
    public static ApprovalException authority(String approverId, String workRecordId, String customMessage) {
        return new ApprovalException(Type.AUTHORITY, customMessage, approverId, workRecordId, 
                                   null, null, null, null);
    }
    
    public static ApprovalException ownership(String userId, String workRecordId, String ownerId) {
        String message = String.format("ユーザー %s は他のユーザー (%s) の工数記録 %s を操作することはできません", 
                                      userId, ownerId, workRecordId);
        return new ApprovalException(Type.OWNERSHIP, message, userId, workRecordId, 
                                   ownerId, null, null, null);
    }
    
    public static ApprovalException ownership(String userId, String workRecordId, String ownerId, String operation) {
        String message = String.format("ユーザー %s は他のユーザー (%s) の工数記録 %s に対して %s を実行することはできません", 
                                      userId, ownerId, workRecordId, operation);
        return new ApprovalException(Type.OWNERSHIP, message, userId, workRecordId, 
                                   ownerId, null, null, operation);
    }
    
    public static ApprovalException status(String workRecordId, ApprovalStatus currentStatus, ApprovalStatus targetStatus) {
        String message = String.format("工数記録 %s の承認ステータスを %s から %s に変更することはできません", 
                                      workRecordId, currentStatus.getDisplayName(), targetStatus.getDisplayName());
        return new ApprovalException(Type.STATUS, message, null, workRecordId, 
                                   null, currentStatus, targetStatus, null);
    }
    
    public static ApprovalException status(String workRecordId, ApprovalStatus currentStatus, String operation) {
        String message = String.format("工数記録 %s は現在 %s 状態のため %s を実行できません", 
                                      workRecordId, currentStatus.getDisplayName(), operation);
        return new ApprovalException(Type.STATUS, message, null, workRecordId, 
                                   null, currentStatus, null, operation);
    }
    
    // Getters
    public Type getType() {
        return type;
    }
    
    public String getApproverId() {
        return approverId;
    }
    
    public String getUserId() {
        return approverId; // For ownership violations, approverId holds userId
    }
    
    public String getWorkRecordId() {
        return workRecordId;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public ApprovalStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public ApprovalStatus getTargetStatus() {
        return targetStatus;
    }
    
    public String getOperation() {
        return operation;
    }
}