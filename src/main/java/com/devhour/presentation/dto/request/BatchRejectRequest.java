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
public class BatchRejectRequest {
    private String reason;
    
    private List<ApprovalRequest> requests;
    
    public static BatchRejectRequest of(String reason, List<ApprovalRequest> requests) {
        return BatchRejectRequest.builder()
                .reason(reason)
                .requests(requests)
                .build();
    }
}