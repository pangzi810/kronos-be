package com.devhour.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ユーザー情報レスポンスDTO
 * 
 * GET /api/auth/me エンドポイントのレスポンスで使用
 * 現在認証されているユーザーの情報を返す
 * 
 * セキュリティのため、パスワード等の機密情報は含まない
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    
    /**
     * ユーザーID
     */
    private String userId;
    
    /**
     * Okta ユーザーID
     */
    private String oktaUserId;
    
    /**
     * ユーザー名
     */
    private String username;
    
    /**
     * メールアドレス
     */
    private String email;
    
    /**
     * フルネーム
     */
    private String fullName;
    
    /**
     * 最終ログイン時刻
     * ISO 8601形式でフォーマット
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;
    
    /**
     * 最終更新日時
     * ISO 8601形式でフォーマット
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdatedAt;
    
    /**
     * UserエンティティからUserInfoResponseに変換するヘルパーメソッド
     * 
     * @param user ユーザーエンティティ
     * @return UserInfoResponse
     */
    public static UserInfoResponse from(com.devhour.domain.model.entity.User user) {
        if (user == null) {
            return null;
        }
        
        return UserInfoResponse.builder()
                .userId(user.getId())
                .oktaUserId(user.getOktaUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .lastLoginAt(user.getLastLoginAt())
                .lastUpdatedAt(user.getUpdatedAt())
                .build();
    }
}