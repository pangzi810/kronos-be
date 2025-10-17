package com.devhour.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 未入力日取得APIのレスポンスDTO
 * 
 * 工数記録が未入力の日付リストを返却
 */
public record MissingDatesResponse(
    @JsonFormat(pattern = "yyyy-MM-dd")
    List<LocalDate> missingDates
) {
    
    /**
     * レスポンス作成用ファクトリメソッド
     * 
     * @param missingDates 未入力日のリスト
     * @return MissingDatesResponseオブジェクト
     */
    public static MissingDatesResponse of(List<LocalDate> missingDates) {
        return new MissingDatesResponse(missingDates != null ? missingDates : List.of());
    }
    
    /**
     * 未入力日の件数を取得
     * 
     * @return 未入力日の件数
     */
    public int getCount() {
        return missingDates != null ? missingDates.size() : 0;
    }
}