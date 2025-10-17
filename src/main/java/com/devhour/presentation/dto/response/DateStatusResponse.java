package com.devhour.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日付毎のステータス情報レスポンス
 */
public class DateStatusResponse {
    
    @JsonProperty("dateStatuses")
    private final Map<String, DateStatus> dateStatuses;
    
    private DateStatusResponse(Map<String, DateStatus> dateStatuses) {
        this.dateStatuses = dateStatuses;
    }
    
    public static DateStatusResponse of(Map<LocalDate, DateStatus> dateStatuses) {
        Map<String, DateStatus> statusMap = dateStatuses.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    Map.Entry::getValue
                ));
        return new DateStatusResponse(statusMap);
    }
    
    public Map<String, DateStatus> getDateStatuses() {
        return dateStatuses;
    }
    
    /**
     * 日付のステータス情報
     */
    public static class DateStatus {
        @JsonProperty("hasWorkRecord")
        private final boolean hasWorkRecord;
        
        @JsonProperty("approvalStatus")
        private final ApprovalStatus approvalStatus;
        
        @JsonProperty("totalHours")
        private final double totalHours;
        
        public DateStatus(boolean hasWorkRecord, ApprovalStatus approvalStatus, double totalHours) {
            this.hasWorkRecord = hasWorkRecord;
            this.approvalStatus = approvalStatus;
            this.totalHours = totalHours;
        }
        
        public static DateStatus empty() {
            return new DateStatus(false, ApprovalStatus.NOT_ENTERED, 0.0);
        }
        
        public static DateStatus withRecord(ApprovalStatus approvalStatus, double totalHours) {
            return new DateStatus(true, approvalStatus, totalHours);
        }
        
        // Getters
        public boolean isHasWorkRecord() { return hasWorkRecord; }
        public ApprovalStatus getApprovalStatus() { return approvalStatus; }
        public double getTotalHours() { return totalHours; }
    }
}