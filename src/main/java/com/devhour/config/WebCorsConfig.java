package com.devhour.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Global CORS configuration
 * 
 * This configuration is always active regardless of security settings
 * to ensure frontend can communicate with backend during development
 */
@Configuration
public class WebCorsConfig {
    
    /**
     * CORS設定 - フロントエンドとの連携のため
     * 
     * This bean will be used when Okta security is disabled
     * When Okta security is enabled, it has its own CORS configuration
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "security.okta.enabled", 
        havingValue = "false", 
        matchIfMissing = true
    )
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 開発環境を許可
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",           // フロントエンド開発サーバー
            "http://localhost:5173",           // Vite default port  
            "http://localhost:5174",           // Vite alternate port
            "http://127.0.0.1:5173",           // Vite with IP
            "http://127.0.0.1:5174"            // Vite alternate with IP
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Request-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // プリフライトリクエストのキャッシュ時間
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}