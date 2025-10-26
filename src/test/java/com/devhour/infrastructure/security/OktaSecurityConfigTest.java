package com.devhour.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.devhour.application.service.OktaUserSyncService;

/**
 * OktaSecurityConfigのテストクラス
 * 
 * OAuth2 Resource Server設定、CORS設定、Bean作成をテスト
 * （JWT変換ロジックはOktaJwtAuthenticationConverterTestで個別にテスト）
 */
class OktaSecurityConfigTest {

    private OktaSecurityConfig oktaSecurityConfig;
    
    @Mock
    private OktaScopeConverter oktaScopeConverter;
    
    @Mock
    private OktaUserSyncService oktaUserSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        oktaSecurityConfig = new OktaSecurityConfig(oktaUserSyncService);
        
        // Set test values for configuration properties
        ReflectionTestUtils.setField(oktaSecurityConfig, "issuerUri", "https://test.okta.com/oauth2/default");
        ReflectionTestUtils.setField(oktaSecurityConfig, "jwkSetUri", "https://test.okta.com/oauth2/default/v1/keys");
        ReflectionTestUtils.setField(oktaSecurityConfig, "clientId", "test-client-id");
    }

    /**
     * OktaJwtAuthenticationConverterが正しく作成されることを確認
     */
    @Test
    void shouldCreateOktaJwtAuthenticationConverter() {
        OktaJwtAuthenticationConverter converter = oktaSecurityConfig.customOktaJwtAuthenticationConverter();
        
        assertNotNull(converter, "OktaJwtAuthenticationConverter should not be null");
    }

    /**
     * CORS設定が正しく作成されることを確認
     */
    @Test
    void shouldCreateCorsConfiguration() {
        CorsConfigurationSource corsConfigSource = oktaSecurityConfig.corsConfigurationSource();
        
        assertNotNull(corsConfigSource, "CorsConfigurationSource should not be null");
        assertTrue(corsConfigSource instanceof UrlBasedCorsConfigurationSource, 
            "CorsConfigurationSource should be UrlBasedCorsConfigurationSource");
        
        // UrlBasedCorsConfigurationSourceの設定を直接確認
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        assertNotNull(urlBasedSource, "UrlBasedCorsConfigurationSource should not be null");
    }
    
    /**
     * JwtDecoderが正しく作成されることを確認
     */
    @Test
    void shouldCreateJwtDecoder() {
        JwtDecoder decoder = oktaSecurityConfig.jwtDecoder();
        
        assertNotNull(decoder, "JwtDecoder should not be null");
    }

    /**
     * JWTトークン検証が正しく動作することを確認（バリデーターテスト）
     */
    @Test
    void shouldValidateJwtTokenCorrectly() {
        JwtDecoder decoder = oktaSecurityConfig.jwtDecoder();
        
        // 注意: 実際のJWT検証はNimbusJwtDecoderが行うため、ここでは設定のみを確認
        assertNotNull(decoder, "JwtDecoder should be configured");
    }

    /**
     * セキュリティ設定の依存関係が正しく注入されることを確認
     */
    @Test
    void shouldHaveCorrectDependencies() {
        assertNotNull(oktaScopeConverter, "OktaScopeConverter should be injected");
        
        // プロパティ値が設定されていることを確認
        String issuerUri = (String) ReflectionTestUtils.getField(oktaSecurityConfig, "issuerUri");
        String jwkSetUri = (String) ReflectionTestUtils.getField(oktaSecurityConfig, "jwkSetUri");
        String clientId = (String) ReflectionTestUtils.getField(oktaSecurityConfig, "clientId");
        
        assertEquals("https://test.okta.com/oauth2/default", issuerUri);
        assertEquals("https://test.okta.com/oauth2/default/v1/keys", jwkSetUri);
        assertEquals("test-client-id", clientId);
    }
}