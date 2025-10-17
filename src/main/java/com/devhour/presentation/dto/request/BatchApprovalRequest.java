package com.devhour.presentation.dto.request;

import java.util.List;

import com.devhour.application.service.DailyApprovalApplicationService.ApprovalRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchApprovalRequest {
    
    private List<ApprovalRequest> requests;
    
    public static BatchApprovalRequest of(List<ApprovalRequest> requests) {
        return BatchApprovalRequest.builder()
                .requests(requests)
                .build();
    }
}