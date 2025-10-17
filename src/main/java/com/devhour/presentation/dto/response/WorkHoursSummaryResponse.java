package com.devhour.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 工数集計レスポンスDTO
 * 
 * 開発者の工数履歴の集計情報を返すためのDTO
 * 日別・プロジェクト別・カテゴリ別の集計データを含む
 */
public class WorkHoursSummaryResponse {
    
    private String userId;
    private String userFullName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private BigDecimal totalHours;
    private int totalDays;
    private BigDecimal averageHoursPerDay;
    
    // プロジェクト別集計 - プロジェクト名 -> 工数
    private Map<String, BigDecimal> projectHours;
    
    // カテゴリ別集計 - カテゴリ名 -> 工数
    private Map<String, BigDecimal> categoryHours;
    
    // 日別集計 - 日付 -> 工数
    private Map<LocalDate, BigDecimal> dailyHours;
    
    // 週別集計情報
    private List<WeeklySummary> weeklySummaries;
    
    /**
     * デフォルトコンストラクター
     */
    public WorkHoursSummaryResponse() {
    }
    
    /**
     * 全フィールドコンストラクター
     */
    public WorkHoursSummaryResponse(String userId, String userFullName, LocalDate startDate, LocalDate endDate,
                                   BigDecimal totalHours, int totalDays, BigDecimal averageHoursPerDay,
                                   Map<String, BigDecimal> projectHours, Map<String, BigDecimal> categoryHours,
                                   Map<LocalDate, BigDecimal> dailyHours, List<WeeklySummary> weeklySummaries) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalHours = totalHours;
        this.totalDays = totalDays;
        this.averageHoursPerDay = averageHoursPerDay;
        this.projectHours = projectHours;
        this.categoryHours = categoryHours;
        this.dailyHours = dailyHours;
        this.weeklySummaries = weeklySummaries;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public BigDecimal getTotalHours() {
        return totalHours;
    }
    
    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }
    
    public int getTotalDays() {
        return totalDays;
    }
    
    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }
    
    public BigDecimal getAverageHoursPerDay() {
        return averageHoursPerDay;
    }
    
    public void setAverageHoursPerDay(BigDecimal averageHoursPerDay) {
        this.averageHoursPerDay = averageHoursPerDay;
    }
    
    public Map<String, BigDecimal> getProjectHours() {
        return projectHours;
    }
    
    public void setProjectHours(Map<String, BigDecimal> projectHours) {
        this.projectHours = projectHours;
    }
    
    public Map<String, BigDecimal> getCategoryHours() {
        return categoryHours;
    }
    
    public void setCategoryHours(Map<String, BigDecimal> categoryHours) {
        this.categoryHours = categoryHours;
    }
    
    public Map<LocalDate, BigDecimal> getDailyHours() {
        return dailyHours;
    }
    
    public void setDailyHours(Map<LocalDate, BigDecimal> dailyHours) {
        this.dailyHours = dailyHours;
    }
    
    public List<WeeklySummary> getWeeklySummaries() {
        return weeklySummaries;
    }
    
    public void setWeeklySummaries(List<WeeklySummary> weeklySummaries) {
        this.weeklySummaries = weeklySummaries;
    }
    
    /**
     * 週別集計情報のネストクラス
     */
    public static class WeeklySummary {
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate weekStartDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate weekEndDate;
        
        private BigDecimal totalHours;
        private int workingDays;
        private BigDecimal averageHoursPerDay;
        
        /**
         * デフォルトコンストラクター
         */
        public WeeklySummary() {
        }
        
        /**
         * 全フィールドコンストラクター
         */
        public WeeklySummary(LocalDate weekStartDate, LocalDate weekEndDate, 
                           BigDecimal totalHours, int workingDays, BigDecimal averageHoursPerDay) {
            this.weekStartDate = weekStartDate;
            this.weekEndDate = weekEndDate;
            this.totalHours = totalHours;
            this.workingDays = workingDays;
            this.averageHoursPerDay = averageHoursPerDay;
        }
        
        // Getters and Setters
        public LocalDate getWeekStartDate() {
            return weekStartDate;
        }
        
        public void setWeekStartDate(LocalDate weekStartDate) {
            this.weekStartDate = weekStartDate;
        }
        
        public LocalDate getWeekEndDate() {
            return weekEndDate;
        }
        
        public void setWeekEndDate(LocalDate weekEndDate) {
            this.weekEndDate = weekEndDate;
        }
        
        public BigDecimal getTotalHours() {
            return totalHours;
        }
        
        public void setTotalHours(BigDecimal totalHours) {
            this.totalHours = totalHours;
        }
        
        public int getWorkingDays() {
            return workingDays;
        }
        
        public void setWorkingDays(int workingDays) {
            this.workingDays = workingDays;
        }
        
        public BigDecimal getAverageHoursPerDay() {
            return averageHoursPerDay;
        }
        
        public void setAverageHoursPerDay(BigDecimal averageHoursPerDay) {
            this.averageHoursPerDay = averageHoursPerDay;
        }
    }
}