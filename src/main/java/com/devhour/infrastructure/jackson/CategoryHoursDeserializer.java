package com.devhour.infrastructure.jackson;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CategoryHours用のJacksonデシリアライザー
 * 
 * JSON形式のカテゴリ別工数データをCategoryHoursオブジェクトに変換
 */
public class CategoryHoursDeserializer extends JsonDeserializer<CategoryHours> {

    @Override
    public CategoryHours deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        
        // JSONオブジェクトからカテゴリコードと工数のマップを作成
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String categoryCodeStr = field.getKey();
            BigDecimal hours = field.getValue().decimalValue();
            
            CategoryCode categoryCode = CategoryCode.of(categoryCodeStr);
            hoursMap.put(categoryCode, hours);
        }
        
        return CategoryHours.of(hoursMap);
    }
}