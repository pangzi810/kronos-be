package com.devhour.infrastructure.security;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Okta OAuth2 スコープの評価を行うユーティリティクラス
 * 
 * シンプルな文字列完全一致による権限チェック機能を提供します。
 */
@Component
public class OktaScopeConverter {
    
    /**
     * ユーザーが特定の権限を持っているかチェック
     * 
     * @param userScopes ユーザーが持つスコープセット
     * @param requiredPermission 必要な権限
     * @return 権限を持っている場合true、持っていない場合false
     */
    public boolean hasPermission(Set<String> userScopes, String requiredPermission) {
        if (userScopes == null || userScopes.isEmpty() || 
            requiredPermission == null || requiredPermission.trim().isEmpty()) {
            return false;
        }
        
        // 完全一致による権限チェック
        return userScopes.contains(requiredPermission.trim());
    }
}