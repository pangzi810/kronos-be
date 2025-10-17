package com.devhour.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 同期履歴詳細レスポンスDTO
 * 
 * 管理者向けの特定同期の詳細情報表示に使用するレスポンスDTO
 * 同期履歴の基本情報と処理された各プロジェクトの詳細を含む
 * 
 * 要件対応:
 * - REQ-6.2: 管理者が特定同期の詳細情報を参照可能
 */
public class JiraSyncHistoryDetailResponse {
    
    private String syncHistoryId;
    private JiraSyncType syncType;
    private JiraSyncStatus syncStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    private Integer totalProjectsProcessed;
    private Integer successCount;
    private Integer errorCount;
    private String errorDetails;
    private String triggeredBy;
    private long durationMinutes;
    private double successRate;
    
    private List<SyncDetailItem> details;
    
    /**
     * デフォルトコンストラクター
     */
    public JiraSyncHistoryDetailResponse() {
    }
    
    /**
     * SyncHistoryエンティティからの変換コンストラクター
     */
    public JiraSyncHistoryDetailResponse(JiraSyncHistory syncHistory) {
        this.syncHistoryId = syncHistory.getId();
        this.syncType = syncHistory.getSyncType();
        this.syncStatus = syncHistory.getSyncStatus();
        this.startedAt = syncHistory.getStartedAt();
        this.completedAt = syncHistory.getCompletedAt();
        this.totalProjectsProcessed = syncHistory.getTotalProjectsProcessed();
        this.successCount = syncHistory.getSuccessCount();
        this.errorCount = syncHistory.getErrorCount();
        this.errorDetails = syncHistory.getErrorDetails();
        this.triggeredBy = syncHistory.getTriggeredBy();
        this.durationMinutes = syncHistory.getDurationMinutes();
        this.successRate = syncHistory.getSuccessRate();
        
        this.details = syncHistory.getDetails().stream()
            .map(SyncDetailItem::new)
            .collect(Collectors.toList());
    }
    
    // Getters and Setters
    public String getSyncHistoryId() {
        return syncHistoryId;
    }
    
    public void setSyncHistoryId(String syncHistoryId) {
        this.syncHistoryId = syncHistoryId;
    }
    
    public JiraSyncType getSyncType() {
        return syncType;
    }
    
    public void setSyncType(JiraSyncType syncType) {
        this.syncType = syncType;
    }
    
    public JiraSyncStatus getSyncStatus() {
        return syncStatus;
    }
    
    public void setSyncStatus(JiraSyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getTotalProjectsProcessed() {
        return totalProjectsProcessed;
    }
    
    public void setTotalProjectsProcessed(Integer totalProjectsProcessed) {
        this.totalProjectsProcessed = totalProjectsProcessed;
    }
    
    public Integer getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }
    
    public Integer getErrorCount() {
        return errorCount;
    }
    
    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public String getTriggeredBy() {
        return triggeredBy;
    }
    
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
    
    public long getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }
    
    public List<SyncDetailItem> getDetails() {
        return details;
    }
    
    public void setDetails(List<SyncDetailItem> details) {
        this.details = details;
    }
    
    /**
     * 同期詳細項目のネストクラス
     */
    public static class SyncDetailItem {

        private String detailId;
        private Integer seq;
        private String operation;
        private DetailStatus status;
        private String result;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processedAt;

        private long processingTimeMs;
        
        /**
         * デフォルトコンストラクター
         */
        public SyncDetailItem() {
        }
        
        /**
         * SyncHistoryDetailエンティティからの変換コンストラクター
         */
        public SyncDetailItem(JiraSyncHistoryDetail detail) {
            this.detailId = detail.getId();
            this.seq = detail.getSeq();
            // projectId, jiraIssueKey, and action fields removed
            this.operation = detail.getOperation();
            this.status = detail.getStatus();
            this.result = detail.getResult();
            this.processedAt = detail.getProcessedAt();
            
            // 処理時間を計算（同期履歴の開始時間が必要）
            if (detail.getSyncHistory() != null && detail.getSyncHistory().getStartedAt() != null) {
                this.processingTimeMs = detail.getProcessingTime(detail.getSyncHistory().getStartedAt());
            } else {
                this.processingTimeMs = 0;
            }
        }
        
        // Getters and Setters
        public String getDetailId() {
            return detailId;
        }
        
        public void setDetailId(String detailId) {
            this.detailId = detailId;
        }

        public Integer getSeq() {
            return seq;
        }

        public void setSeq(Integer seq) {
            this.seq = seq;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public DetailStatus getStatus() {
            return status;
        }
        
        public void setStatus(DetailStatus status) {
            this.status = status;
        }
        
        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
        
        public LocalDateTime getProcessedAt() {
            return processedAt;
        }
        
        public void setProcessedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
        }
        
        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
        
        public void setProcessingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }
    }
}