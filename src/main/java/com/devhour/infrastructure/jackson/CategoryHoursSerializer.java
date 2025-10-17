package com.devhour.infrastructure.jackson;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * CategoryHours用のJacksonシリアライザー
 * 
 * CategoryHoursオブジェクトをJSON形式に変換
 */
public class CategoryHoursSerializer extends JsonSerializer<CategoryHours> {

    @Override
    public void serialize(CategoryHours value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        gen.writeStartObject();
        
        // カテゴリコードと工数のマップをJSONオブジェクトとして出力
        for (Map.Entry<CategoryCode, BigDecimal> entry : value.hours().entrySet()) {
            gen.writeNumberField(entry.getKey().value(), entry.getValue());
        }
        
        gen.writeEndObject();
    }
}