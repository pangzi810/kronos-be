package com.devhour.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OktaScopeConverterのテストクラス
 * 
 * シンプルな文字列完全一致による権限チェック機能のテスト
 */
@DisplayName("OktaScopeConverter")
class OktaScopeConverterTest {

    private OktaScopeConverter scopeConverter;

    @BeforeEach
    void setUp() {
        scopeConverter = new OktaScopeConverter();
    }

    @Nested
    @DisplayName("権限チェック")
    class HasPermissionTest {

        @Test
        @DisplayName("ユーザーが必要な権限を持っている場合true")
        void shouldReturnTrueWhenUserHasRequiredPermission() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read", "projects:write", "users:read");
            String requiredPermission = "work-hours:read";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ユーザーが必要な権限を持っていない場合false")
        void shouldReturnFalseWhenUserDoesNotHaveRequiredPermission() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read", "projects:write");
            String requiredPermission = "users:read";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ユーザースコープがnullの場合false")
        void shouldReturnFalseWhenUserScopesIsNull() {
            // Given
            Set<String> userScopes = null;
            String requiredPermission = "work-hours:read";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ユーザースコープが空の場合false")
        void shouldReturnFalseWhenUserScopesIsEmpty() {
            // Given
            Set<String> userScopes = Set.of();
            String requiredPermission = "work-hours:read";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("必要な権限がnullの場合false")
        void shouldReturnFalseWhenRequiredPermissionIsNull() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read");
            String requiredPermission = null;

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("必要な権限が空文字の場合false")
        void shouldReturnFalseWhenRequiredPermissionIsEmpty() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read");
            String requiredPermission = "";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("権限に前後のスペースがある場合正常に処理")
        void shouldHandlePermissionWithWhitespace() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read", "projects:write");
            String requiredPermission = "  work-hours:read  ";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("完全一致が必要")
        void shouldRequireExactMatch() {
            // Given
            Set<String> userScopes = Set.of("work-hours:read");
            String requiredPermission = "work-hours:write";

            // When
            boolean result = scopeConverter.hasPermission(userScopes, requiredPermission);

            // Then
            assertThat(result).isFalse();
        }
    }
}