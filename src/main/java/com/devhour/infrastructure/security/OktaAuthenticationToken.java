package com.devhour.infrastructure.security;

import com.devhour.domain.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

/**
 * Okta JWT認証トークンの拡張実装
 * 
 * JWTトークンに加えて、システム内部のユーザー情報を保持する
 * カスタム認証トークンクラス
 * 
 * 主な機能:
 * - Okta JWTの保持
 * - 内部ユーザーエンティティの保持
 * - 内部ユーザーIDをPrincipalとして使用
 */
public class OktaAuthenticationToken extends JwtAuthenticationToken {
    
    private final User internalUser;
    private final String oktaUserId;
    
    /**
     * コンストラクタ
     * 
     * @param jwt Okta JWT
     * @param authorities 権限コレクション
     * @param internalUser 内部ユーザーエンティティ
     */
    public OktaAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, User internalUser) {
        super(jwt, authorities, internalUser != null ? internalUser.getId() : jwt.getSubject());
        this.internalUser = internalUser;
        this.oktaUserId = jwt.getSubject();
    }
    
    /**
     * 内部ユーザーエンティティを取得
     * 
     * @return 内部ユーザーエンティティ（存在しない場合はnull）
     */
    public User getInternalUser() {
        return internalUser;
    }
    
    /**
     * 内部ユーザーIDを取得
     * 
     * @return 内部ユーザーID（存在しない場合はOkta User ID）
     */
    public String getInternalUserId() {
        return internalUser != null ? internalUser.getId() : oktaUserId;
    }
    
    /**
     * Okta User IDを取得
     * 
     * @return Okta User ID (JWT subject)
     */
    public String getOktaUserId() {
        return oktaUserId;
    }
    
    /**
     * ユーザーのフルネームを取得
     * 
     * @return フルネーム（内部ユーザーが存在しない場合はJWTのnameクレーム）
     */
    public String getFullName() {
        if (internalUser != null) {
            return internalUser.getFullName();
        }
        // JWTからnameクレームを取得
        Object nameClaim = getToken().getClaim("name");
        return nameClaim != null ? nameClaim.toString() : null;
    }
    
    /**
     * ユーザーのメールアドレスを取得
     * 
     * @return メールアドレス（内部ユーザーが存在しない場合はJWTのemailクレーム）
     */
    public String getEmail() {
        if (internalUser != null) {
            return internalUser.getEmail();
        }
        // JWTからemailクレームを取得
        Object emailClaim = getToken().getClaim("email");
        return emailClaim != null ? emailClaim.toString() : null;
    }
    
    /**
     * 内部ユーザーが存在するかチェック
     * 
     * @return 内部ユーザーが存在する場合true
     */
    public boolean hasInternalUser() {
        return internalUser != null;
    }
    
    @Override
    public String toString() {
        return "OktaAuthenticationToken{" +
                "internalUserId='" + getInternalUserId() + '\'' +
                ", oktaUserId='" + oktaUserId + '\'' +
                ", authorities=" + getAuthorities() +
                '}';
    }
}