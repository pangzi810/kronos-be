package com.devhour.infrastructure.typehandler;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MyBatis TypeHandler for CategoryHours value object JSON column
 * CategoryHours値オブジェクトとJSONデータ間の変換を行う
 * 
 * JSON形式例: {"BRD": 1.5, "DEV": 6.0, "MEETING": 0.5}
 * CategoryHours形式: CategoryHours(Map<CategoryCode, BigDecimal>)
 */
@Component
@MappedTypes(CategoryHours.class)
public class CategoryHoursValueObjectTypeHandler extends BaseTypeHandler<CategoryHours> {

    private final ObjectMapper objectMapper;

    public CategoryHoursValueObjectTypeHandler() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * PreparedStatementにJSONパラメータを設定
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CategoryHours parameter, JdbcType jdbcType) throws SQLException {
        try {
            // CategoryHours内のMap<CategoryCode, BigDecimal>をMap<String, BigDecimal>に変換
            Map<String, BigDecimal> stringMap = new HashMap<>();
            parameter.hours().forEach((code, hours) -> 
                stringMap.put(code.value(), hours)
            );
            
            String json = objectMapper.writeValueAsString(stringMap);
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting CategoryHours to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * ResultSetからJSON文字列を取得してCategoryHoursに変換（カラム名指定）
     */
    @Override
    public CategoryHours getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonToCategoryHours(json);
    }

    /**
     * ResultSetからJSON文字列を取得してCategoryHoursに変換（カラムインデックス指定）
     */
    @Override
    public CategoryHours getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonToCategoryHours(json);
    }

    /**
     * CallableStatementからJSON文字列を取得してCategoryHoursに変換
     */
    @Override
    public CategoryHours getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonToCategoryHours(json);
    }

    /**
     * JSON文字列をCategoryHoursに変換
     * 
     * @param json JSON文字列
     * @return CategoryHours値オブジェクト、nullの場合は空のCategoryHoursを返す
     */
    private CategoryHours parseJsonToCategoryHours(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return CategoryHours.empty();
        }

        try {
            // Handle potential double-encoded JSON
            String actualJson = json;
            if (json.startsWith("\"") && json.endsWith("\"")) {
                // If wrapped in quotes, it might be double-encoded - try to unescape
                actualJson = objectMapper.readValue(json, String.class);
            }

            // まず汎用的なMapとして読み込む
            @SuppressWarnings("unchecked")
            Map<String, Object> rawMap = objectMapper.readValue(actualJson, Map.class);

            // Map<String, BigDecimal>に変換
            Map<String, BigDecimal> stringMap = new HashMap<>();
            rawMap.forEach((key, value) -> {
                BigDecimal decimalValue;
                if (value instanceof BigDecimal) {
                    decimalValue = (BigDecimal) value;
                } else if (value instanceof Number) {
                    decimalValue = new BigDecimal(value.toString());
                } else {
                    decimalValue = new BigDecimal(value.toString());
                }
                stringMap.put(key, decimalValue);
            });

            // Map<String, BigDecimal>をMap<CategoryCode, BigDecimal>に変換
            Map<CategoryCode, BigDecimal> codeMap = new HashMap<>();
            stringMap.forEach((codeStr, hours) ->
                codeMap.put(CategoryCode.of(codeStr), hours)
            );

            return CategoryHours.of(codeMap);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing CategoryHours JSON: " + json + ", error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SQLException("Error creating CategoryHours from JSON: " + json + ", error: " + e.getMessage(), e);
        }
    }
}