package com.devhour.infrastructure.security;

import com.devhour.application.service.OktaUserSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.devhour.domain.model.entity.User;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Okta JWT認証コンバーター
 * 
 * JWTトークンをSpring SecurityのAbstractAuthenticationTokenに変換し、
 * OktaScopeConverterを使用してスコープベースの権限を処理する
 * 
 * Phase 3の拡張:
 * - JWT変換時にOktaユーザー同期サービスを呼び出し
 * - ユーザー情報の自動同期処理を実行
 * - 同期エラー時も認証処理は継続
 * 
 * Spring Security OAuth2 Resource Serverの統合ポイントとして機能し、
 * Oktaから発行されたJWTトークンの「scp」または「scope」クレームを
 * Spring Securityの権限システムに適合させる
 */
public class OktaJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(OktaJwtAuthenticationConverter.class);
    
    static {
        logger.warn("=========================================");
        logger.warn("OktaJwtAuthenticationConverter CLASS LOADED");
        logger.warn("=========================================");
    }

    private final OktaUserSyncService userSyncService;

    /**
     * コンストラクタ
     * 
     * @param userSyncService Oktaユーザー同期サービス（オプション）
     */
    public OktaJwtAuthenticationConverter(OktaUserSyncService userSyncService) {
        this.userSyncService = userSyncService; // nullも許可（ユーザー同期なしで動作可能）
        logger.warn("=========================================");
        logger.warn("OktaJwtAuthenticationConverter INSTANCE CREATED");
        logger.warn("=========================================");
    }
    
    /**
     * デフォルトコンストラクタ
     * ユーザー同期サービスなしで動作
     */
    public OktaJwtAuthenticationConverter() {
        this(null);
    }

    /**
     * JWTトークンをAbstractAuthenticationTokenに変換
     * 
     * Phase 3拡張: JWT変換後にユーザー同期処理を実行し、
     * 内部ユーザー情報を含むカスタム認証トークンを返す
     * 
     * @param jwt 変換対象のJWTトークン
     * @return 変換されたOktaAuthenticationToken（内部ユーザー情報を含む）
     * @throws IllegalArgumentException JWTがnullの場合
     */
    @Override
    public AbstractAuthenticationToken convert(@org.springframework.lang.NonNull Jwt jwt) {
        logger.warn("=========================================");
        logger.warn("=== OktaJwtAuthenticationConverter.convert() CALLED ===");
        logger.warn("=========================================");
        logger.info("=== OktaJwtAuthenticationConverter.convert() が呼び出されました ===");
        
        if (jwt == null) {
            throw new IllegalArgumentException("JWT cannot be null");
        }

        String oktaUserId = jwt.getClaimAsString("sub");
        logger.info("JWT認証トークン変換を開始: oktaUserId={}", oktaUserId);

        // JWTからスコープを抽出
        Set<String> scopes = extractScopes(jwt);

        // スコープを権限に変換
        Collection<GrantedAuthority> authorities = convertScopesToAuthorities(scopes);

        // 一時的なJwtAuthenticationTokenを作成（ユーザー同期のため）
        JwtAuthenticationToken tempToken = new JwtAuthenticationToken(jwt, authorities, oktaUserId);
        
        // Phase 3: ユーザー同期処理を実行し、内部ユーザーを取得
        User internalUser = syncUserIfAvailable(tempToken);

        // 内部ユーザー情報を含むカスタム認証トークンを作成
        OktaAuthenticationToken authenticationToken = new OktaAuthenticationToken(jwt, authorities, internalUser);

        String principalName = authenticationToken.getInternalUserId();
        logger.debug("JWT認証トークン変換を完了: oktaUserId={}, internalUserId={}, authorities={}", 
                    oktaUserId, principalName, authorities.size());
        
        return authenticationToken;
    }

    /**
     * 利用可能な場合にユーザー同期処理を実行
     * 
     * ユーザー同期サービスが設定されている場合に限り同期処理を実行し、
     * 同期エラーが発生しても認証処理は継続する
     * 
     * @param authenticationToken 認証トークン
     * @return 同期された内部ユーザー（同期できない場合はnull）
     */
    private User syncUserIfAvailable(JwtAuthenticationToken authenticationToken) {
        if (userSyncService == null) {
            logger.debug("ユーザー同期サービスが設定されていないため、同期処理をスキップします");
            return null;
        }

        try {
            logger.debug("Oktaユーザー同期処理を開始: subject={}", authenticationToken.getName());
            User internalUser = userSyncService.syncUser(authenticationToken);
            logger.debug("Oktaユーザー同期処理を完了: subject={}, internalUserId={}", 
                        authenticationToken.getName(), 
                        internalUser != null ? internalUser.getId() : "null");
            return internalUser;
        } catch (Exception e) {
            // ユーザー同期でエラーが発生しても認証処理は継続
            logger.warn("Oktaユーザー同期処理でエラーが発生しましたが、認証処理は継続します: subject={}, error={}", 
                       authenticationToken.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * JWTからスコープクレームを抽出
     * 
     * Oktaは「scp」クレームでスコープを提供するが、標準的な「scope」クレームも
     * フォールバックとして確認する
     * 
     * @param jwt JWTトークン
     * @return 抽出されたスコープのセット
     */
    private Set<String> extractScopes(Jwt jwt) {
        Set<String> scopes = new HashSet<>();

        // 「scp」クレームからスコープを抽出（Oktaの主要なスコープクレーム）
        extractScopesFromClaim(jwt, "scp", scopes);

        // 「scope」クレームからもスコープを抽出（標準的なOAuth2スコープクレーム）
        extractScopesFromClaim(jwt, "scope", scopes);

        return scopes;
    }

    /**
     * 特定のクレームからスコープを抽出してセットに追加
     * 
     * @param jwt JWTトークン
     * @param claimName クレーム名
     * @param scopes スコープを追加するセット
     */
    private void extractScopesFromClaim(Jwt jwt, String claimName, Set<String> scopes) {
        Object claimValue = jwt.getClaim(claimName);
        
        if (claimValue == null) {
            return;
        }

        if (claimValue instanceof List<?> list) {
            // リスト形式のスコープクレーム
            for (Object item : list) {
                if (item instanceof String scope && StringUtils.hasText(scope)) {
                    scopes.add(scope.trim());
                }
            }
        } else if (claimValue instanceof String scopeString && StringUtils.hasText(scopeString)) {
            // 文字列形式のスコープクレーム（スペース区切り）
            String[] scopeArray = scopeString.split("\\s+");
            for (String scope : scopeArray) {
                if (StringUtils.hasText(scope)) {
                    scopes.add(scope.trim());
                }
            }
        }
    }

    /**
     * スコープをSpring SecurityのGrantedAuthorityに変換
     * 
     * @param scopes 変換対象のスコープセット
     * @return GrantedAuthorityのコレクション
     */
    private Collection<GrantedAuthority> convertScopesToAuthorities(Set<String> scopes) {
        return scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }
}