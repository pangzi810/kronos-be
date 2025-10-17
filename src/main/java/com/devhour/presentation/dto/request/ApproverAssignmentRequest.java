package com.devhour.presentation.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 承認者割り当てリクエストDTO
 */
public class ApproverAssignmentRequest {
    
    @NotBlank(message = "ユーザーIDは必須です")
    private String userId;
    
    @NotBlank(message = "承認者IDは必須です")
    private String approverId;
    
    @NotNull(message = "有効開始日は必須です")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveFrom;
    
    public ApproverAssignmentRequest() {}
    
    public ApproverAssignmentRequest(String userId, String approverId, LocalDate effectiveFrom) {
        this.userId = userId;
        this.approverId = approverId;
        this.effectiveFrom = effectiveFrom;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getApproverId() {
        return approverId;
    }
    
    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }
    
    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
}