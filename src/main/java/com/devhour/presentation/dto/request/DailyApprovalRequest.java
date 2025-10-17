package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 日次承認リクエスト
 */
@Data
public class DailyApprovalRequest {
    
    @NotBlank(message = "申請者IDは必須です")
    private String userId;
    
    @NotNull(message = "作業日は必須です")
    private LocalDate workDate;
    
    private String rejectionReason;
    
    public DailyApprovalRequest() {}
    
    public DailyApprovalRequest(String userId, LocalDate workDate) {
        this.userId = userId;
        this.workDate = workDate;
    }
    
    public DailyApprovalRequest(String userId, LocalDate workDate, String rejectionReason) {
        this.userId = userId;
        this.workDate = workDate;
        this.rejectionReason = rejectionReason;
    }
}