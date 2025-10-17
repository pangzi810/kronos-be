package com.devhour.domain.model.valueobject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.devhour.infrastructure.jackson.CategoryHoursDeserializer;
import com.devhour.infrastructure.jackson.CategoryHoursSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * カテゴリ別工数の値オブジェクト
 * 
 * ビジネスルール:
 * - カテゴリコードと工数のマッピング
 * - 各工数は0-24時間の範囲内
 * - JSON形式でのシリアライゼーション対応
 * - 不変オブジェクトとして実装
 * 
 * JSON例: {"BRD": 1.5, "DEV": 6.0, "MEETING": 0.5}
 */
@JsonSerialize(using = CategoryHoursSerializer.class)
@JsonDeserialize(using = CategoryHoursDeserializer.class)
public record CategoryHours(Map<CategoryCode, BigDecimal> hours) {
    
    // 1日の最大工数時間
    private static final BigDecimal MIN_HOURS = BigDecimal.ZERO;
    private static final BigDecimal MAX_HOURS = new BigDecimal("24.00");
    
    // Jackson ObjectMapper for JSON conversion
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、不正な値の場合は例外をスロー
     * 
     * @param hours カテゴリコード→工数のマップ
     * @throws IllegalArgumentException 不正な値の場合
     */
    public CategoryHours {
        validateCategoryHours(hours);
        // 防御的コピーを作成して不変性を保証
        hours = hours != null ? Map.copyOf(hours) : Map.of();
    }
    
    /**
     * カテゴリ別工数の検証
     * 
     * @param hours 検証対象のマップ
     * @throws IllegalArgumentException 検証エラーの場合
     */
    private void validateCategoryHours(Map<CategoryCode, BigDecimal> hours) {
        if (hours == null) {
            return; // nullの場合は空のマップとして扱う
        }
        
        for (Map.Entry<CategoryCode, BigDecimal> entry : hours.entrySet()) {
            CategoryCode categoryCode = entry.getKey();
            BigDecimal hourValue = entry.getValue();
            
            if (categoryCode == null) {
                throw new IllegalArgumentException("カテゴリコードがnullです");
            }
            
            if (hourValue == null) {
                throw new IllegalArgumentException(
                    String.format("カテゴリ '%s' の工数がnullです", categoryCode.value())
                );
            }
            
            if (hourValue.compareTo(MIN_HOURS) < 0) {
                throw new IllegalArgumentException(
                    String.format("カテゴリ '%s' の工数は0以上である必要があります。入力値: %s", 
                        categoryCode.value(), hourValue)
                );
            }
            
            if (hourValue.compareTo(MAX_HOURS) > 0) {
                throw new IllegalArgumentException(
                    String.format("カテゴリ '%s' の工数は24時間以下である必要があります。入力値: %s", 
                        categoryCode.value(), hourValue)
                );
            }
        }
    }
    
    /**
     * 空のカテゴリ工数を作成
     * 
     * @return 空のCategoryHours
     */
    public static CategoryHours empty() {
        return new CategoryHours(Map.of());
    }
    
    /**
     * マップからCategoryHoursを安全に生成
     * 
     * @param hours カテゴリコード→工数のマップ
     * @return CategoryHoursオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static CategoryHours of(Map<CategoryCode, BigDecimal> hours) {
        return new CategoryHours(hours);
    }
    
    /**
     * 単一カテゴリの工数からCategoryHoursを生成
     * 
     * @param categoryCode カテゴリコード
     * @param hours 工数
     * @return CategoryHoursオブジェクト
     */
    public static CategoryHours of(CategoryCode categoryCode, BigDecimal hours) {
        return new CategoryHours(Map.of(categoryCode, hours));
    }
    

    
    /**
     * 特定カテゴリの工数を取得
     * 
     * @param categoryCode カテゴリコード
     * @return 工数（存在しない場合は0）
     */
    public BigDecimal getHours(CategoryCode categoryCode) {
        return hours.getOrDefault(categoryCode, BigDecimal.ZERO);
    }
    
    /**
     * 工数が記録されているカテゴリのセットを取得
     * 
     * @return カテゴリコードのセット
     */
    public Set<CategoryCode> getCategories() {
        return hours.keySet();
    }
    
    /**
     * 指定カテゴリに工数が記録されているかチェック
     * 
     * @param categoryCode カテゴリコード
     * @return 工数が記録されている場合true
     */
    public boolean hasHours(CategoryCode categoryCode) {
        BigDecimal categoryHours = hours.get(categoryCode);
        return categoryHours != null && categoryHours.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 工数が記録されているかチェック
     * 
     * @return 何らかの工数が記録されている場合true
     */
    public boolean hasAnyHours() {
        return !hours.isEmpty();
    }
    
    /**
     * 全カテゴリの合計工数を取得
     * 
     * @return 全カテゴリの合計工数
     */
    public BigDecimal getTotalHours() {
        return hours.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * カテゴリ工数を追加/更新
     * 新しいCategoryHoursオブジェクトを返す（不変性を維持）
     * 
     * @param categoryCode カテゴリコード
     * @param additionalHours 追加する工数
     * @return 更新されたCategoryHours
     */
    public CategoryHours addHours(CategoryCode categoryCode, BigDecimal additionalHours) {
        Map<CategoryCode, BigDecimal> newHours = new HashMap<>(hours);
        BigDecimal currentHours = newHours.getOrDefault(categoryCode, BigDecimal.ZERO);
        newHours.put(categoryCode, currentHours.add(additionalHours));
        return new CategoryHours(newHours);
    }
    
    /**
     * カテゴリ工数を設定
     * 新しいCategoryHoursオブジェクトを返す（不変性を維持）
     * 
     * @param categoryCode カテゴリコード
     * @param newHours 新しい工数
     * @return 更新されたCategoryHours
     */
    public CategoryHours setHours(CategoryCode categoryCode, BigDecimal newHours) {
        Map<CategoryCode, BigDecimal> newHoursMap = new HashMap<>(hours);
        newHoursMap.put(categoryCode, newHours);
        return new CategoryHours(newHoursMap);
    }
    
    /**
     * JSON文字列に変換
     * データベース保存時に使用
     * 
     * @return JSON文字列
     * @throws IllegalStateException JSON変換エラーの場合
     */
    public String toJson() {
        try {
            // CategoryCode → String のマップに変換
            Map<String, BigDecimal> codeHoursMap = hours.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().value(),
                    Map.Entry::getValue
                ));
            return OBJECT_MAPPER.writeValueAsString(codeHoursMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("CategoryHours のJSON変換に失敗しました: " + e.getMessage(), e);
        }
    }
    
    /**
     * JSON文字列に変換（エイリアスメソッド）
     * 互換性のために提供
     * 
     * @return JSON文字列
     * @throws IllegalStateException JSON変換エラーの場合
     */
    public String toJsonString() {
        return toJson();
    }
    
    /**
     * JSON文字列からCategoryHoursを生成
     * データベース読み込み時に使用
     * 
     * @param json JSON文字列
     * @return CategoryHoursオブジェクト
     * @throws IllegalArgumentException JSON解析エラーの場合
     */
    @JsonCreator
    public static CategoryHours fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return empty();
        }
        
        try {
            TypeReference<Map<String, BigDecimal>> typeRef = new TypeReference<Map<String, BigDecimal>>() {};
            Map<String, BigDecimal> codeHoursMap = OBJECT_MAPPER.readValue(json, typeRef);
            
            // String → CategoryCode のマップに変換
            Map<CategoryCode, BigDecimal> categoryHours = codeHoursMap.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> CategoryCode.of(entry.getKey()),
                    Map.Entry::getValue
                ));
                
            return new CategoryHours(categoryHours);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("CategoryHours のJSON解析に失敗しました: " + json + ", error: " + e.getMessage(), e);
        }
    }
    
    /**
     * デバッグ用の文字列表現
     */
    @Override
    public String toString() {
        return String.format("CategoryHours{categories=%s}", hours.size());
    }
    
    /**
     * 等価性の比較
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategoryHours that = (CategoryHours) obj;
        return Objects.equals(this.hours, that.hours);
    }
    
    /**
     * ハッシュコード
     */
    @Override
    public int hashCode() {
        return Objects.hash(hours);
    }
    
    /**
     * 複数のCategoryHoursオブジェクトからカテゴリ別工数を集計
     * 
     * @param categoryHoursList CategoryHoursのリスト
     * @return カテゴリ別工数マップ（カテゴリコード文字列 → 合計工数）
     */
    public static Map<String, BigDecimal> aggregateByCategory(List<CategoryHours> categoryHoursList) {
        Map<String, BigDecimal> aggregated = new HashMap<>();
        
        for (CategoryHours categoryHours : categoryHoursList) {
            if (categoryHours != null) {
                for (CategoryCode categoryCode : categoryHours.getCategories()) {
                    BigDecimal categoryHour = categoryHours.getHours(categoryCode);
                    aggregated.merge(categoryCode.value(), categoryHour, BigDecimal::add);
                }
            }
        }
        
        return aggregated;
    }
}