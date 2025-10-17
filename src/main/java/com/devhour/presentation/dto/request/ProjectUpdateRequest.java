package com.devhour.presentation.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * プロジェクト更新リクエスト
 */
public class ProjectUpdateRequest {
    
    @NotBlank(message = "プロジェクト名は必須です")
    @Size(max = 255, message = "プロジェクト名は255文字以内で入力してください")
    private String name;
    
    @Size(max = 1000, message = "プロジェクト説明は1000文字以内で入力してください")
    private String description;
    
    @NotNull(message = "開始日は必須です")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @NotNull(message = "予定終了日は必須です")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedEndDate;
    
    public ProjectUpdateRequest() {
    }
    
    public ProjectUpdateRequest(String name, String description, LocalDate startDate, LocalDate plannedEndDate) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }
    
    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }
}