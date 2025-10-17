package com.devhour.presentation.dto.request;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 承認リクエストDTO
 */
public class WorkRecordApprovalRequest {
    
    @NotNull(message = "工数記録IDリストは必須です")
    @NotEmpty(message = "工数記録IDリストは空にできません")
    private List<String> workRecordIds;
    
    public WorkRecordApprovalRequest() {}
    
    public WorkRecordApprovalRequest(List<String> workRecordIds) {
        this.workRecordIds = workRecordIds;
    }
    
    public List<String> getWorkRecordIds() {
        return workRecordIds;
    }
    
    public void setWorkRecordIds(List<String> workRecordIds) {
        this.workRecordIds = workRecordIds;
    }
}