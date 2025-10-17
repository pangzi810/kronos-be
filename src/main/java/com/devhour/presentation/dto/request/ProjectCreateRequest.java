package com.devhour.presentation.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * プロジェクト作成リクエスト
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    
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
}