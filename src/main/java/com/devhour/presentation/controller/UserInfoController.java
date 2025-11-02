package com.devhour.presentation.controller;

import com.devhour.infrastructure.security.OktaGroupsExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ユーザー情報取得コントローラー
 *
 * 現在ログイン中のユーザーの情報（JWT クレーム含む）を取得するエンドポイントを提供
 */
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    private final OktaGroupsExtractor groupsExtractor;

    public UserInfoController(OktaGroupsExtractor groupsExtractor) {
        this.groupsExtractor = groupsExtractor;
    }

    /**
     * 現在のユーザー情報を取得（groupsクレーム含む）
     *
     * @param jwt JWTトークン
     * @return ユーザー情報
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> userInfo = new HashMap<>();

        // 基本情報
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("email", jwt.getClaimAsString("email"));
        userInfo.put("name", jwt.getClaimAsString("name"));
        userInfo.put("preferred_username", jwt.getClaimAsString("preferred_username"));

        // グループ情報
        List<String> groups = groupsExtractor.extractGroups(jwt);
        userInfo.put("groups", groups);

        // スコープ情報
        userInfo.put("scopes", jwt.getClaimAsStringList("scp"));

        // その他のクレーム
        userInfo.put("iss", jwt.getIssuer());
        userInfo.put("aud", jwt.getAudience());
        userInfo.put("exp", jwt.getExpiresAt());
        userInfo.put("iat", jwt.getIssuedAt());

        return ResponseEntity.ok(userInfo);
    }

    /**
     * 現在のユーザーのグループ情報のみを取得
     *
     * @param jwt JWTトークン
     * @return グループリスト
     */
    @GetMapping("/me/groups")
    public ResponseEntity<Map<String, Object>> getCurrentUserGroups(
            @AuthenticationPrincipal Jwt jwt) {

        List<String> groups = groupsExtractor.extractGroups(jwt);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", jwt.getSubject());
        response.put("groups", groups);
        response.put("groupCount", groups.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 全てのJWTクレームを取得（デバッグ用）
     *
     * @param jwt JWTトークン
     * @return 全クレーム
     */
    @GetMapping("/me/claims")
    public ResponseEntity<Map<String, Object>> getAllClaims(
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> allClaims = new HashMap<>(jwt.getClaims());

        return ResponseEntity.ok(allClaims);
    }

    /**
     * 認証情報の詳細を取得（デバッグ用）
     *
     * @param authentication 認証情報
     * @return 認証詳細
     */
    @GetMapping("/me/auth")
    public ResponseEntity<Map<String, Object>> getAuthenticationInfo(
            Authentication authentication) {

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("name", authentication.getName());
        authInfo.put("authenticated", authentication.isAuthenticated());
        authInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        authInfo.put("authorities", authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList());

        return ResponseEntity.ok(authInfo);
    }
}
