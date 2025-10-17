package com.devhour.application.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;

/**
 * Oktaユーザー同期サービス
 * 
 * Oktaから取得したユーザー情報をローカルデータベースと同期する
 * 
 * 責務:
 * - OktaユーザーID(sub claim)によるユーザー検索
 * - 新規Oktaユーザーの作成
 * - 既存ユーザーの情報更新
 * - 既存メールアドレスユーザーとのリンク
 */
@Service
@Transactional
@ConditionalOnProperty(name = "security.okta.enabled", havingValue = "true", matchIfMissing = false)
public class OktaUserSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(OktaUserSyncService.class);
    
    private final UserRepository userRepository;
    
    public OktaUserSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Oktaユーザーを同期する
     * 
     * 処理フロー:
     * 1. JWTトークンからユーザー情報を抽出
     * 2. Okta User ID（sub claim）でユーザー検索
     * 3. 見つからない場合、メールアドレスで検索（既存ユーザーとのリンク）
     * 4. それでも見つからない場合、新規ユーザー作成
     * 5. ユーザー情報を更新して保存
     * 
     * @param authentication 認証情報（JwtAuthenticationTokenが前提）
     * @return 同期されたUserエンティティ
     * @throws IllegalArgumentException 不正なパラメータの場合
     */
    public User syncUser(Authentication authentication) {
        logger.debug("Oktaユーザー同期を開始: {}", 
                    authentication != null ? authentication.getName() : "null");
        
        if (authentication == null) {
            throw new IllegalArgumentException("認証情報は必須です");
        }
        
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new IllegalArgumentException("JWT認証トークンが必要です");
        }
        
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtToken.getToken();
        
        // JWTからユーザー情報を抽出
        OktaUserInfo oktaUserInfo = extractUserFromJwt(jwt);
        logger.debug("Oktaユーザー情報を抽出: oktaUserId={}, email={}", 
                    oktaUserInfo.getOktaUserId(), oktaUserInfo.getEmail());
        
        // 1. Okta User IDでユーザーを検索
        Optional<User> existingUserByOkta = userRepository.findByOktaUserId(oktaUserInfo.getOktaUserId());
        if (existingUserByOkta.isPresent()) {
            logger.debug("既存Oktaユーザーを発見: {}", existingUserByOkta.get().getId());
            return updateExistingOktaUser(existingUserByOkta.get(), oktaUserInfo);
        }
        
        // 2. メールアドレスでユーザーを検索（既存ユーザーとのリンク）
        Optional<User> existingUserByEmail = userRepository.findByEmail(oktaUserInfo.getEmail());
        if (existingUserByEmail.isPresent()) {
            logger.info("メールアドレスが一致する既存ユーザーをOktaとリンク: userId={}, email={}", 
                       existingUserByEmail.get().getId(), oktaUserInfo.getEmail());
            return linkExistingUserToOkta(existingUserByEmail.get(), oktaUserInfo);
        }
        
        // 3. 新規ユーザー作成
        logger.info("新規Oktaユーザーを作成: email={}", oktaUserInfo.getEmail());
        return createNewOktaUser(oktaUserInfo);
    }
    
    /**
     * JWTトークンからユーザー情報を抽出する
     * 
     * @param jwt JWTトークン
     * @return 抽出されたユーザー情報
     * @throws IllegalArgumentException 必須クレームが不足している場合
     */
    public OktaUserInfo extractUserFromJwt(Jwt jwt) {
        // In Okta, 'sub' claim contains the email address
        // We use this as the unique identifier
        String subClaim = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        
        // If email claim is not present, use sub claim as email
        if (email == null || email.trim().isEmpty()) {
            email = subClaim;
        }
        
        // 必須クレームの検証
        if (subClaim == null || subClaim.trim().isEmpty()) {
            throw new IllegalArgumentException("JWTトークンにsub claimが含まれていません");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("JWTトークンにemail情報が含まれていません");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            // If name is not present, use email username part as fallback
            fullName = email.split("@")[0];
        }
        
        // Use email from sub claim as the Okta User ID (unique identifier)
        // This is stored in okta_user_id column in the database
        return new OktaUserInfo(subClaim, email, fullName, preferredUsername);
    }
    
    /**
     * 既存Oktaユーザーの情報を更新する
     */
    private User updateExistingOktaUser(User existingUser, OktaUserInfo oktaUserInfo) {
        existingUser.updateFromOkta(oktaUserInfo.getEmail(), oktaUserInfo.getFullName());
        User savedUser = userRepository.save(existingUser);
        
        logger.info("既存Oktaユーザーを更新: userId={}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * 既存ユーザーをOktaとリンクする
     */
    private User linkExistingUserToOkta(User existingUser, OktaUserInfo oktaUserInfo) {
        existingUser.linkToOkta(oktaUserInfo.getOktaUserId());
        existingUser.updateFromOkta(oktaUserInfo.getEmail(), oktaUserInfo.getFullName());
        User savedUser = userRepository.save(existingUser);
        
        logger.info("既存ユーザーをOktaとリンク: userId={}, oktaUserId={}", 
                   savedUser.getId(), oktaUserInfo.getOktaUserId());
        return savedUser;
    }
    
    /**
     * 新規Oktaユーザーを作成する
     */
    private User createNewOktaUser(OktaUserInfo oktaUserInfo) {
        // デフォルトでDEVELOPERロールを設定
        User newUser = User.createFromOkta(
            oktaUserInfo.getEmail(),
            oktaUserInfo.getFullName(),
            oktaUserInfo.getOktaUserId()
        );
        
        User savedUser = userRepository.save(newUser);
        
        logger.info("新規Oktaユーザーを作成: userId={}, email={}, oktaUserId={}", 
                   savedUser.getId(), savedUser.getEmail(), savedUser.getOktaUserId());
        return savedUser;
    }
    
    /**
     * Oktaユーザー情報を格納する内部クラス
     */
    public static class OktaUserInfo {
        private final String oktaUserId;
        private final String email;
        private final String fullName;
        private final String preferredUsername;
        
        public OktaUserInfo(String oktaUserId, String email, String fullName, String preferredUsername) {
            this.oktaUserId = oktaUserId;
            this.email = email;
            this.fullName = fullName;
            this.preferredUsername = preferredUsername;
        }
        
        public String getOktaUserId() { return oktaUserId; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getPreferredUsername() { return preferredUsername; }
    }
}