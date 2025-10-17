package com.devhour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * アプリケーション全般の設定クラス
 * 
 * 汎用的なSpring Beanの定義を行う
 * 特定の機能に特化しない共通的なBeanをここに配置
 */
@Configuration
public class ApplicationConfiguration {
    
    /**
     * ObjectMapper Bean
     * 
     * JSON シリアライゼーション・デシリアライゼーション用のObjectMapper
     * 
     * @return 設定済みObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 時間API（LocalDateTime等）のサポートを追加
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
}