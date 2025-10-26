package com.devhour.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import com.devhour.application.service.OktaUserSyncService;
import com.devhour.domain.model.entity.User;

/**
 * OktaJwtAuthenticationConverterのテストクラス（簡略版）
 * 
 * JWTトークンをSpring SecurityのAbstractAuthenticationTokenに変換する処理の
 * 基本的なテストを実行
 */
@DisplayName("OktaJwtAuthenticationConverter")
class OktaJwtAuthenticationConverterTest {

    private OktaJwtAuthenticationConverter converter;
    
    @Mock
    private OktaUserSyncService userSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new OktaJwtAuthenticationConverter(userSyncService);
    }

    @Nested
    @DisplayName("JWT変換処理")
    class JwtConversionTest {

        @Test
        @DisplayName("正常なJWTの変換")
        void shouldConvertValidJwt() {
            // Given
            Map<String, Object> headers = Map.of("alg", "RS256");
            Map<String, Object> claims = Map.of(
                "sub", "user123",
                "scp", List.of("work-hours:read", "projects:write")
            );
            Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

            User mockUser = User.createFromOkta("test@example.com", "Test User", "user123");
            // Set Okta user ID using reflection or a setter if available
            // For now, we'll mock the behavior
            
            when(userSyncService.syncUser(any())).thenReturn(mockUser);

            // When
            AbstractAuthenticationToken result = converter.convert(jwt);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(OktaAuthenticationToken.class);
            
            OktaAuthenticationToken authToken = (OktaAuthenticationToken) result;
            assertThat(authToken.getInternalUserId()).isNotNull(); // Generated UUID
            assertThat(authToken.getOktaUserId()).isEqualTo("user123");
            
            // スコープが権限に変換されているか確認
            Collection<GrantedAuthority> authorities = result.getAuthorities();
            assertThat(authorities).hasSize(2);
            assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_work-hours:read", "SCOPE_projects:write");
        }

        @Test
        @DisplayName("nullJWTの場合例外発生")
        void shouldThrowExceptionForNullJwt() {
            // When & Then
            assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT cannot be null");
        }

        @Test
        @DisplayName("ユーザー同期なしでも動作")
        void shouldWorkWithoutUserSync() {
            // Given
            converter = new OktaJwtAuthenticationConverter(); // 同期サービスなし
            
            Map<String, Object> headers = Map.of("alg", "RS256");
            Map<String, Object> claims = Map.of(
                "sub", "user123",
                "scp", List.of("work-hours:read")
            );
            Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

            // When
            AbstractAuthenticationToken result = converter.convert(jwt);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(OktaAuthenticationToken.class);
            
            OktaAuthenticationToken authToken = (OktaAuthenticationToken) result;
            assertThat(authToken.getOktaUserId()).isEqualTo("user123");
            assertThat(authToken.getInternalUserId()).isEqualTo("user123"); // 同期サービスなしの場合はOkta User IDを返す
        }

        @Test
        @DisplayName("スコープクレームの抽出")
        void shouldExtractScopesFromClaims() {
            // Given
            Map<String, Object> headers = Map.of("alg", "RS256");
            Map<String, Object> claims = Map.of(
                "sub", "user123",
                "scp", List.of("scope1", "scope2"),
                "scope", "scope3 scope4"
            );
            Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

            // When
            AbstractAuthenticationToken result = converter.convert(jwt);

            // Then
            Collection<GrantedAuthority> authorities = result.getAuthorities();
            assertThat(authorities).hasSize(4);
            assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_scope1", "SCOPE_scope2", "SCOPE_scope3", "SCOPE_scope4");
        }
    }
}