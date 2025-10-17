package com.devhour.presentation.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * プロジェクト完了リクエスト
 */
public class ProjectCompleteRequest {
    
    @NotNull(message = "実際の終了日は必須です")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate actualEndDate;
    
    public ProjectCompleteRequest() {
    }
    
    public ProjectCompleteRequest(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
    
    public LocalDate getActualEndDate() {
        return actualEndDate;
    }
    
    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
}