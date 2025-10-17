package com.devhour.presentation.dto.request;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import com.devhour.domain.model.valueobject.CategoryHours;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工数記録作成リクエスト
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkRecordSaveRequest {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkRecordDto {
        
        @NotBlank(message = "プロジェクトIDは必須です")
        private String projectId;

        @NotNull(message = "作業日は必須です")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate workDate;

        @NotNull(message = "カテゴリ別工数は必須です")
        private CategoryHours categoryHours;

        @Size(max = 500, message = "作業内容は500文字以内で入力してください")
        private String description;
    }    

    private List<WorkRecordDto> records;

    private List<String> deletedRecordIds;

    private LocalDate date;
}
