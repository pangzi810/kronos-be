package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;

/**
 * OktaユーザーSync Service テスト
 */
@DisplayName("Oktaユーザー同期サービス")
class OktaUserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    private OktaUserSyncService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OktaUserSyncService(userRepository);
    }

    @Test
    @DisplayName("新規Oktaユーザーを作成できる")
    void syncUser_NewOktaUser_CreatesUser() {
        // Arrange
        String oktaUserId = "00u123456789abcdef";
        String email = "john.doe@company.com";
        String fullName = "John Doe";
        
        Authentication auth = createMockAuthentication(oktaUserId, email, fullName);
        
        when(userRepository.findByOktaUserId(oktaUserId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = service.syncUser(auth);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(fullName, result.getFullName());
        assertEquals(oktaUserId, result.getOktaUserId());
        assertTrue(result.isActive());
        assertTrue(result.isOktaUser());

        verify(userRepository).findByOktaUserId(oktaUserId);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("既存のOktaユーザーを更新できる")
    void syncUser_ExistingOktaUser_UpdatesUser() {
        // Arrange
        String oktaUserId = "00u123456789abcdef";
        String email = "john.doe@company.com";
        String fullName = "John Doe Updated";
        String userId = "user123";
        
        User existingUser = User.restoreWithOkta(
            userId, "johndoe", "john.doe@company.com",
            "John Doe", true,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1),
            oktaUserId
        );
        
        Authentication auth = createMockAuthentication(oktaUserId, email, fullName);
        
        when(userRepository.findByOktaUserId(oktaUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = service.syncUser(auth);

        // Assert
        assertNotNull(result);
        assertEquals(fullName, result.getFullName());
        assertEquals(oktaUserId, result.getOktaUserId());

        verify(userRepository).findByOktaUserId(oktaUserId);
        verify(userRepository, never()).findByEmail(email);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("既存のメールアドレスユーザーをOktaとリンクできる")
    void syncUser_ExistingEmailUser_LinksToOkta() {
        // Arrange
        String oktaUserId = "00u123456789abcdef";
        String email = "john.doe@company.com";
        String fullName = "John Doe";
        String userId = "user123";
        
        User existingUser = User.restore(
            userId, "johndoe", email,
            "John Doe", true,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1)
        );
        
        Authentication auth = createMockAuthentication(oktaUserId, email, fullName);
        
        when(userRepository.findByOktaUserId(oktaUserId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = service.syncUser(auth);

        // Assert
        assertNotNull(result);
        assertEquals(fullName, result.getFullName());
        assertEquals(oktaUserId, result.getOktaUserId());
        assertTrue(result.isOktaUser());

        verify(userRepository).findByOktaUserId(oktaUserId);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("無効なAuthenticationでエラーが発生する")
    void syncUser_InvalidAuthentication_ThrowsException() {
        // Arrange
        Authentication invalidAuth = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.syncUser(invalidAuth));
        
        verify(userRepository, never()).findByOktaUserId(any());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("JWTでない認証でエラーが発生する")
    void syncUser_NonJwtAuthentication_ThrowsException() {
        // Arrange
        Authentication nonJwtAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "user", "password"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.syncUser(nonJwtAuth));
    }

    @Test
    @DisplayName("必須クレームが不足している場合エラーが発生する")
    void syncUser_MissingRequiredClaims_ThrowsException() {
        // Arrange - subクレームが不足
        Authentication auth = createMockAuthenticationMissingClaims();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.syncUser(auth));
    }

    @Test
    @DisplayName("JWTクレームからユーザー情報を抽出できる")
    void extractUserFromJwt_ValidJwt_ExtractsUserInfo() {
        // Arrange
        String oktaUserId = "00u123456789abcdef";
        String email = "john.doe@company.com";
        String fullName = "John Doe";
        String preferredUsername = "johndoe";
        
        Jwt jwt = createMockJwt(oktaUserId, email, fullName, preferredUsername);

        // Act
        OktaUserSyncService.OktaUserInfo userInfo = service.extractUserFromJwt(jwt);

        // Assert
        assertNotNull(userInfo);
        assertEquals(oktaUserId, userInfo.getOktaUserId());
        assertEquals(email, userInfo.getEmail());
        assertEquals(fullName, userInfo.getFullName());
        assertEquals(preferredUsername, userInfo.getPreferredUsername());
    }

    @Test
    @DisplayName("usernameが存在しない場合、Oktaユーザー同期はユーザー名を自動生成する")
    void syncUser_NoPreferredUsername_GeneratesUsername() {
        // Arrange
        String oktaUserId = "00u123456789abcdef";
        String email = "john.doe@company.com";
        String fullName = "John Doe";
        
        Authentication auth = createMockAuthenticationWithoutUsername(oktaUserId, email, fullName);
        
        when(userRepository.findByOktaUserId(oktaUserId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = service.syncUser(auth);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername()); // メールアドレスの@より前の部分
        
        verify(userRepository).save(any(User.class));
    }

    // Helper methods for creating mock objects

    private Authentication createMockAuthentication(String oktaUserId, String email, String fullName) {
        return createMockAuthentication(oktaUserId, email, fullName, "username");
    }

    private Authentication createMockAuthentication(String oktaUserId, String email, String fullName, String preferredUsername) {
        Jwt jwt = createMockJwt(oktaUserId, email, fullName, preferredUsername);
        return new JwtAuthenticationToken(jwt);
    }

    private Authentication createMockAuthenticationWithoutUsername(String oktaUserId, String email, String fullName) {
        Jwt jwt = createMockJwt(oktaUserId, email, fullName, null);
        return new JwtAuthenticationToken(jwt);
    }

    private Authentication createMockAuthenticationMissingClaims() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("email", "test@example.com")
                // sub claim missing
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private Jwt createMockJwt(String sub, String email, String name, String preferredUsername) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", sub)
                .claim("email", email)
                .claim("name", name);
        
        if (preferredUsername != null) {
            builder.claim("preferred_username", preferredUsername);
        }
        
        return builder.build();
    }
}