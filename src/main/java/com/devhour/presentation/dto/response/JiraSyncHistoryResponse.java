package com.devhour.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 同期履歴レスポンスDTO
 * 
 * 管理者向けの同期履歴一覧表示に使用するレスポンスDTO
 * ページネーション情報と同期履歴の一覧を含む
 * 
 * 要件対応:
 * - REQ-6.1: 管理者が過去30日間の同期履歴を参照可能
 * - REQ-6.3: 管理者が期間フィルタで同期履歴を絞り込み可能
 */
public class JiraSyncHistoryResponse {
    
    private List<SyncHistorySummary> syncHistories;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean hasInProgress;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate filterStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate filterEndDate;
    
    /**
     * デフォルトコンストラクター
     */
    public JiraSyncHistoryResponse() {
    }
    
    /**
     * 全フィールドコンストラクター
     */
    public JiraSyncHistoryResponse(List<SyncHistorySummary> syncHistories, int currentPage, int pageSize, 
                              long totalRecords, boolean hasInProgress, LocalDate filterStartDate, LocalDate filterEndDate) {
        this.syncHistories = syncHistories;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.hasInProgress = hasInProgress;
        this.filterStartDate = filterStartDate;
        this.filterEndDate = filterEndDate;
    }
    
    // Getters and Setters
    public List<SyncHistorySummary> getSyncHistories() {
        return syncHistories;
    }
    
    public void setSyncHistories(List<SyncHistorySummary> syncHistories) {
        this.syncHistories = syncHistories;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public boolean isHasInProgress() {
        return hasInProgress;
    }
    
    public void setHasInProgress(boolean hasInProgress) {
        this.hasInProgress = hasInProgress;
    }
    
    public LocalDate getFilterStartDate() {
        return filterStartDate;
    }
    
    public void setFilterStartDate(LocalDate filterStartDate) {
        this.filterStartDate = filterStartDate;
    }
    
    public LocalDate getFilterEndDate() {
        return filterEndDate;
    }
    
    public void setFilterEndDate(LocalDate filterEndDate) {
        this.filterEndDate = filterEndDate;
    }
    
    /**
     * 同期履歴サマリーのネストクラス
     */
    public static class SyncHistorySummary {
        
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
        
        private List<SyncHistoryDetail> details;

        /**
         * デフォルトコンストラクター
         */
        public SyncHistorySummary() {
        }
        
        /**
         * SyncHistoryエンティティからの変換コンストラクター
         */
        public SyncHistorySummary(JiraSyncHistory syncHistory) {
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
                .map(detail -> new SyncHistoryDetail(
                    detail.getId(),
                    detail.getSeq(),
                    detail.getOperation(),
                    detail.getStatus(),
                    detail.getResult(),
                    detail.getProcessedAt()
                ))
                .toList();
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

        public List<SyncHistoryDetail> getDetails() {
            return details;
        }

        public void setDetails(List<SyncHistoryDetail> details) {
            this.details = details;
        }
    }

    public static class SyncHistoryDetail {
        private String id;
        private Integer seq;
        private String operation;
        private DetailStatus status;
        private String result;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processedAt;

        public SyncHistoryDetail() {
        }

        public SyncHistoryDetail(String id, Integer seq, String operation, DetailStatus status, String result, LocalDateTime processedAt) {
            this.id = id;
            this.seq = seq;
            this.operation = operation;
            this.status = status;
            this.result = result;
            this.processedAt = processedAt;
        }

        public String getId() {
            return id;
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
    }
}