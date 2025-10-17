package com.devhour.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson設定クラス
 *
 * 全APIレスポンスの日付・時刻形式を統一する設定
 * - LocalDate: yyyy-MM-dd形式
 * - LocalDateTime: yyyy-MM-ddTHH:mm:ss.fff+0900形式
 */
@Configuration
public class JacksonConfig {

    /**
     * カスタマイズされたObjectMapperを設定
     *
     * @return 設定済みObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}