package com.devhour.presentation.dto.response;

import com.devhour.domain.model.entity.Approver;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 承認者関係レスポンスDTO
 * V44移行対応: メールアドレスベースの承認者関係
 */
public class ApproverResponse {
    
    private String id;
    private String userId; // V44移行後は対象者メールアドレス
    private String approverId; // V44移行後は承認者メールアドレス
    private String targetEmail; // V44追加: 対象者メールアドレス
    private String approverEmail; // V44追加: 承認者メールアドレス
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;
    
    private boolean isDeleted;
    private boolean isCurrentlyEffective;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public static ApproverResponse from(Approver approver) {
        ApproverResponse response = new ApproverResponse();
        response.id = approver.getId();
        response.userId = approver.getUserId(); // 後方互換性のため、メールアドレスを返す
        response.approverId = approver.getApproverId(); // 後方互換性のため、メールアドレスを返す
        response.targetEmail = approver.getTargetEmail();
        response.approverEmail = approver.getApproverEmail();
        response.effectiveFrom = approver.getEffectiveFrom().toLocalDate();
        response.effectiveTo = approver.getEffectiveTo() != null ? approver.getEffectiveTo().toLocalDate() : null;
        response.isDeleted = approver.isDeleted();
        response.isCurrentlyEffective = approver.isCurrentlyEffective();
        response.createdAt = approver.getCreatedAt();
        response.updatedAt = approver.getUpdatedAt();
        
        return response;
    }
    
    // Getters
    public String getId() { return id; }
    
    /**
     * @deprecated V44移行後はgetTargetEmail()を使用
     */
    @Deprecated
    public String getUserId() { return userId; }
    
    /**
     * @deprecated V44移行後はgetApproverEmail()を使用
     */
    @Deprecated
    public String getApproverId() { return approverId; }
    
    public String getTargetEmail() { return targetEmail; }
    public String getApproverEmail() { return approverEmail; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public boolean isDeleted() { return isDeleted; }
    public boolean isCurrentlyEffective() { return isCurrentlyEffective; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}