package com.devhour.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OktaSecurityConfig統合テスト
 * 
 * 実際のSecurityFilterChainを使用してOAuth2 Resource Server設定をテスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Okta Security Integration Test")
@Disabled("Integration test disabled - requires full Okta configuration")
class OktaSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 認証が必要なエンドポイントへの未認証アクセスが401を返すことを確認
     */
    @Test
    @DisplayName("未認証アクセスが401を返す")
    void shouldReturn401ForUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    /**
     * 有効なJWTトークンでの認証されたアクセスが成功することを確認
     */
    @Test
    @DisplayName("有効なJWTトークンでのアクセスが成功する")
    void shouldAllowAccessWithValidJwtToken() throws Exception {
        String validToken = createValidToken();

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    /**
     * 無効なJWTトークンでのアクセスが401を返すことを確認
     */
    @Test
    @DisplayName("無効なJWTトークンでのアクセスが401を返す")
    void shouldReturn401ForInvalidJwtToken() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    /**
     * CORS preflight リクエストが適切に処理されることを確認
     */
    @Test
    @DisplayName("CORS preflight リクエストが適切に処理される")
    void shouldHandleCorsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Requested-With"));
    }

    /**
     * Oktaドメインからのリクエストが許可されることを確認
     */
    @Test
    @DisplayName("Oktaドメインからのリクエストが許可される")
    void shouldAllowRequestsFromOktaDomain() throws Exception {
        mockMvc.perform(options("/api/users")
                .header("Origin", "https://dev-123456.okta.com")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    /**
     * スコープベース認可が正しく動作することを確認
     */
    @Test
    @DisplayName("スコープベース認可が正しく動作する")
    void shouldEnforceeScopeBasedAuthorization() throws Exception {
        String tokenWithLimitedScope = createTokenWithScope("work-hours:read:own");

        // JIRA管理エンドポイント（管理者のみアクセス可能）にアクセスしようとする
        mockMvc.perform(get("/api/jira/connection")
                .header("Authorization", "Bearer " + tokenWithLimitedScope))
                .andExpect(status().isForbidden());
    }

    /**
     * 管理者スコープでの保護されたエンドポイントへのアクセスが成功することを確認
     */
    @Test
    @DisplayName("管理者スコープでの保護されたエンドポイントアクセスが成功する")
    void shouldAllowAdminScopeToAccessProtectedEndpoints() throws Exception {
        String adminToken = createTokenWithScope("jira-connection:read:all");

        mockMvc.perform(get("/api/jira/connection")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 認証不要なエンドポイントへのアクセスが成功することを確認
     */
    @Test
    @DisplayName("認証不要なエンドポイントへのアクセスが成功する")
    void shouldAllowUnauthenticatedAccessToPublicEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    // Helper methods for creating test tokens

    private String createValidToken() {
        return "valid.test.token"; // MockJwtDecoderが適切に処理する
    }

    private String createTokenWithScope(String scope) {
        return "token.with.scope." + scope.replace(":", "_");
    }

    /**
     * テスト用のJwtDecoderを提供する設定
     */
    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        @Primary
        public JwtDecoder mockJwtDecoder() {
            return new JwtDecoder() {
                @Override
                public Jwt decode(String token) throws JwtException {
                    if (token.equals("invalid.jwt.token")) {
                        throw new JwtException("Invalid token");
                    }

                    Jwt.Builder builder = Jwt.withTokenValue(token)
                            .header("alg", "RS256")
                            .header("typ", "JWT")
                            .claim("sub", "00u123456789abcdef")
                            .claim("iss", "https://test.okta.com/oauth2/default")
                            .claim("aud", "test-client-id")
                            .claim("email", "testuser@example.com")
                            .claim("name", "Test User")
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(3600));

                    // スコープベーストークンの処理
                    if (token.startsWith("token.with.scope.")) {
                        String scope = token.substring("token.with.scope.".length()).replace("_", ":");
                        builder.claim("scp", java.util.Arrays.asList(scope));
                    } else {
                        // デフォルトスコープ
                        builder.claim("scp", java.util.Arrays.asList(
                            "work-hours:read:own",
                            "work-hours:write:own",
                            "projects:read:assigned",
                            "users:read:own"
                        ));
                    }

                    return builder.build();
                }
            };
        }
    }
}