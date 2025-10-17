package com.devhour.infrastructure.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MyBatis TypeHandler for CategoryHours JSON column
 * カテゴリ別工数のJSONデータとMapオブジェクト間の変換を行う
 * 
 * JSON形式例: {"BRD": 1.5, "DEV": 6.0, "MEETING": 0.5}
 * Map形式: Map<String, BigDecimal>
 */
@Component
@MappedTypes(Map.class)
public class CategoryHoursTypeHandler extends BaseTypeHandler<Map<String, BigDecimal>> {

    private final ObjectMapper objectMapper;

    public CategoryHoursTypeHandler() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * PreparedStatementにJSONパラメータを設定
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, BigDecimal> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting CategoryHours map to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * ResultSetからJSON文字列を取得してMapに変換（カラム名指定）
     */
    @Override
    public Map<String, BigDecimal> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonToMap(json);
    }

    /**
     * ResultSetからJSON文字列を取得してMapに変換（カラムインデックス指定）
     */
    @Override
    public Map<String, BigDecimal> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    /**
     * CallableStatementからJSON文字列を取得してMapに変換
     */
    @Override
    public Map<String, BigDecimal> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    /**
     * JSON文字列をMap<String, BigDecimal>に変換
     * 
     * @param json JSON文字列
     * @return カテゴリコードと工数のマップ、nullの場合は空のマップを返す
     */
    private Map<String, BigDecimal> parseJsonToMap(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            TypeReference<Map<String, BigDecimal>> typeRef = new TypeReference<Map<String, BigDecimal>>() {};
            Map<String, BigDecimal> result = objectMapper.readValue(json, typeRef);
            return result != null ? result : new HashMap<>();
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing CategoryHours JSON: " + json + ", error: " + e.getMessage(), e);
        }
    }

    /**
     * Map<String, BigDecimal>をJSON文字列に変換
     * 
     * @param categoryHours カテゴリコードと工数のマップ
     * @return JSON文字列
     */
    public String mapToJson(Map<String, BigDecimal> categoryHours) throws JsonProcessingException {
        if (categoryHours == null || categoryHours.isEmpty()) {
            return "{}";
        }
        return objectMapper.writeValueAsString(categoryHours);
    }
}