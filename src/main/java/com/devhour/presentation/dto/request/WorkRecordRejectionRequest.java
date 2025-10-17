package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 却下リクエストDTO
 */
public class WorkRecordRejectionRequest {
    
    @NotBlank(message = "却下理由は必須です")
    @Size(min = 1, max = 500, message = "却下理由は1〜500文字で入力してください")
    private String rejectionReason;
    
    public WorkRecordRejectionRequest() {}
    
    public WorkRecordRejectionRequest(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}