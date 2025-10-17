package com.devhour.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.devhour.domain.model.entity.WorkRecordApprovalHistory;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 承認履歴レスポンスDTO
 */
public class ApprovalHistoryResponse {
    
    private String historyId;
    private String workRecordId;
    private String userId;
    private String projectId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    
    private String categoryHours;
    private BigDecimal totalHours;
    private String description;
    private String action;
    private String previousStatus;
    private String currentStatus;
    private String approverId;
    private String rejectionReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime occurredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    public static ApprovalHistoryResponse from(WorkRecordApprovalHistory history) {
        ApprovalHistoryResponse response = new ApprovalHistoryResponse();
        response.historyId = history.getHistoryId();
        response.workRecordId = history.getWorkRecordId();
        response.userId = history.getUserId();
        response.projectId = history.getProjectId();
        response.workDate = history.getWorkDate();
        response.categoryHours = history.getCategoryHours();
        response.totalHours = history.getTotalHours();
        response.description = history.getDescription();
        response.action = history.getAction();
        response.previousStatus = history.getPreviousStatus();
        response.currentStatus = history.getCurrentStatus();
        response.approverId = history.getApproverId();
        response.rejectionReason = history.getRejectionReason();
        response.occurredAt = history.getOccurredAt();
        response.createdAt = history.getCreatedAt();
        
        return response;
    }
    
    // Getters
    public String getHistoryId() { return historyId; }
    public String getWorkRecordId() { return workRecordId; }
    public String getUserId() { return userId; }
    public String getProjectId() { return projectId; }
    public LocalDate getWorkDate() { return workDate; }
    public String getCategoryHours() { return categoryHours; }
    public BigDecimal getTotalHours() { return totalHours; }
    public String getDescription() { return description; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getCurrentStatus() { return currentStatus; }
    public String getApproverId() { return approverId; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}