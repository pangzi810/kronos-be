package com.devhour.infrastructure.security;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.devhour.application.service.OktaUserSyncService;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;

/**
 * Okta OAuth2 Resource Server設定クラス
 * 
 * 既存のJWT認証をOkta OAuth2に置き換え、スコープベースの認可を提供
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "security.okta.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class OktaSecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(OktaSecurityConfig.class);
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.jwt.audience}")
    private String audience;
    
    private final OktaJwtAuthenticationConverter jwtAuthenticationConverter;
    
    public OktaSecurityConfig(OktaUserSyncService oktaUserSyncService) {
        // Create converter once and reuse
        this.jwtAuthenticationConverter = new OktaJwtAuthenticationConverter(oktaUserSyncService);
    }
    
    /**
     * PasswordEncoder Bean
     * User service still needs this for password operations even with Okta
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * OAuth2 Resource Serverとしてのセキュリティフィルターチェーン設定
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化（OAuth2トークン使用のため）
            .csrf(csrf -> csrf.disable())
            
            // CORS設定を有効化
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // セッション管理をステートレスに設定
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // OAuth2 Resource Server設定
            .oauth2ResourceServer(oauth2 -> {
                oauth2.jwt(jwt -> {
                    // Create and configure JwtDecoder
                    JwtDecoder decoder = jwtDecoder();
                    jwt.decoder(decoder);
                    
                    // CRITICAL: Set our custom converter
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
                    
                    // Create custom JwtAuthenticationProvider with our converter
                    org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider customProvider = 
                        new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider(decoder);
                    customProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
                    
                    // Set the custom authentication manager that uses our provider
                    jwt.authenticationManager(authentication -> {
                        logger.debug("Custom authentication manager invoked for JWT");
                        return customProvider.authenticate(authentication);
                    });
                    
                })
                .authenticationEntryPoint((request, response, ex) -> {
                    logger.warn("JWT authentication failed for request: {} - {}", 
                        request.getRequestURI(), ex.getMessage());
                    response.setStatus(401);
                    response.getWriter().write("{\"error\": \"unauthorized\", \"message\": \"Invalid or missing JWT token\"}");
                    response.setContentType("application/json");
                });
            })
            
            // リクエストの認可設定
            .authorizeHttpRequests(auth -> auth
                // 認証不要なエンドポイント
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                
                // // JIRA接続設定エンドポイント（管理者のみ）
                // .requestMatchers("/api/jira/connection/**")
                //     .hasAuthority("SCOPE_jira-connection:read:all")
                
                // // JIRA管理エンドポイント（PMOおよび管理者）
                // .requestMatchers("/api/jira/queries/**")
                //     .hasAnyAuthority("SCOPE_jira-queries:read:all", "SCOPE_jira-queries:write:all")
                // .requestMatchers("/api/jira/templates/**")
                //     .hasAnyAuthority("SCOPE_jira-templates:read:all", "SCOPE_jira-templates:write:all")
                // .requestMatchers("/api/jira/sync/**")
                //     .hasAnyAuthority("SCOPE_jira-sync:read:all", "SCOPE_jira-sync:write:all")
                
                // その他のリクエストは認証必須
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    /**
     * Okta専用JWTAuthenticationConverter
     * 
     * Phase 3拡張: OktaUserSyncServiceを統合し、JWT変換時に自動ユーザー同期を実行
     * 
     * OktaScopeConverterを使用してスコープの3階層解析と階層展開を行い、
     * Spring Securityの権限システムに適合させる
     * 
     * Note: Converter is created in constructor to ensure single instance is used
     */
    @Bean
    @Primary
    public OktaJwtAuthenticationConverter customOktaJwtAuthenticationConverter() {
        return jwtAuthenticationConverter;
    }
    
    
    /**
     * JwtDecoderの設定
     * JWK Set URIから自動的に署名アルゴリズムを検出
     * issuer、audience、有効期限チェックを含む
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
        .withJwkSetUri(jwkSetUri)
        .jwtProcessorCustomizer(processor -> processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
            new JOSEObjectType("application/okta-internal-at+jwt"),
            new JOSEObjectType("at+jwt"),
            JOSEObjectType.JWT
        )))
        .build();

        // JWT validation rules
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new OAuth2TokenValidator<Jwt>() {
            @Override
            public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(Jwt jwt) {
                // Okta typically includes the client ID in the 'aud' claim for access tokens
                if (jwt.getAudience() != null &&
                    (jwt.getAudience().contains(clientId) || jwt.getAudience().contains(audience))) {
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience",
                        "The audience is invalid. Expected: " + clientId + " or " + audience, null));
            }
        };

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            withIssuer, withAudience);

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }
    
    /**
     * CORS設定 - フロントエンドとOktaドメインとの連携のため
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 開発環境とOktaドメインを許可
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4173",           // Vite preview default port
            "http://localhost:5173",           // Vite dev default port
            "http://127.0.0.1:4173",           // Vite preview with IP
            "http://127.0.0.1:5173",           // Vite dev with IP
            "https://*.okta.com",              // Oktaドメイン
            "https://*.oktapreview.com",       // Okta Previewドメイン
            "https://*.okta-emea.com"          // Okta EMEAドメイン
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
    
    /**
     * 認証イベントのリスニング - 監査ログ用
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String authorities = event.getAuthentication().getAuthorities().toString();
        
        logger.info("Authentication successful for user: {} with authorities: {}", 
            username, authorities);
    }
    
    @EventListener  
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String error = event.getException().getMessage();
        
        logger.warn("Authentication failed for user: {} - {}", username, error);
    }
}