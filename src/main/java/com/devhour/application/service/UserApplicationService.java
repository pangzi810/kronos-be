package com.devhour.application.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;
import com.devhour.presentation.dto.UserSearchCriteria;
import com.devhour.presentation.dto.response.UserListResponse;

/**
 * ユーザーアプリケーションサービス
 * 
 * ユーザー管理に関するユースケースを実装
 * 
 * 責務:
 * - ユーザーの登録・更新・削除
 * - ユーザー認証関連の処理
 * - ユーザー検索・一覧取得
 * - ビジネスルールの調整とトランザクション管理
 */
@Service
@Transactional
public class UserApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);
    
    private final UserRepository userRepository;
    
    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 新しいユーザーを作成 (Okta認証使用のためパスワード不要)
     * 
     * @param username ユーザー名
     * @param email メールアドレス
     * @param fullName フルネーム
     * @return 作成されたユーザー
     * @throws IllegalArgumentException ビジネスルール違反の場合
     * @throws IllegalStateException 既存ユーザーとの重複の場合
     */
    public User createUser(String username, String email, String fullName) {
        // 重複チェック
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException(String.format("ユーザー名 '%s' は既に使用されています", username));
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(String.format("メールアドレス '%s' は既に使用されています", email));
        }
        
        // ユーザー作成 (Okta認証使用のためパスワードは不要)
        User user = User.create(username, email, fullName);
        User savedUser = userRepository.save(user);
        
        
        return savedUser;
    }
    
    /**
     * ユーザー情報を更新
     * 
     * @param userId 更新対象のユーザーID
     * @param email 新しいメールアドレス
     * @param fullName 新しいフルネーム
     * @return 更新されたユーザー
     * @throws IllegalArgumentException ユーザーが存在しない場合
     * @throws IllegalStateException メールアドレスが重複する場合
     */
    public User updateUser(String userId, String email, String fullName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        // 他のユーザーが同じメールアドレスを使用していないかチェック
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new IllegalStateException(String.format("メールアドレス '%s' は既に使用されています", email));
        }
        
        user.updateUserInfo(email, fullName);
        User savedUser = userRepository.save(user);
        
        return savedUser;
    }
    
    
    /**
     * ユーザーを有効化
     * 
     * @param userId 有効化対象のユーザーID
     * @return 更新されたユーザー
     * @throws IllegalArgumentException ユーザーが存在しない場合
     */
    public User activateUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        user.activate();
        User savedUser = userRepository.save(user);
        
        return savedUser;
    }
    
    /**
     * ユーザーを無効化
     * 
     * @param userId 無効化対象のユーザーID
     * @return 更新されたユーザー
     * @throws IllegalArgumentException ユーザーが存在しない場合
     */
    public User deactivateUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        user.deactivate();
        User savedUser = userRepository.save(user);
        
        return savedUser;
    }
    
    /**
     * ユーザーを削除
     * 論理削除（無効化）を実行
     * 
     * @param userId 削除対象のユーザーID
     * @throws IllegalArgumentException ユーザーが存在しない場合
     */
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        // 論理削除として無効化
        user.deactivate();
        userRepository.save(user);
    }
    
    // === 検索・取得メソッド ===
    
    /**
     * ユーザーIDでユーザーを取得
     * 
     * @param userId ユーザーID
     * @return ユーザー（存在しない場合は空のOptional）
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * ユーザー名でユーザーを取得
     * 認証処理で使用
     * 
     * @param username ユーザー名
     * @return ユーザー（存在しない場合は空のOptional）
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * メールアドレスでユーザーを取得
     * 
     * @param email メールアドレス
     * @return ユーザー（存在しない場合は空のOptional）
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * アクティブなユーザー一覧を取得
     * 
     * @return アクティブなユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActive();
    }
    
    /**
     * アクティブな開発者一覧を取得
     * 
     * APIエンドポイント /api/users/active/developers で使用
     * パフォーマンス監視のため実行時間とデータ件数をログ出力
     * 
     * @return アクティブなユーザーのリスト（全アクティブユーザーを返す）
     */
    @Transactional(readOnly = true)
    public List<User> findActiveDevelopers() {
        logger.debug("アクティブなユーザー一覧の取得を開始");
        long startTime = System.currentTimeMillis();
        
        try {
            List<User> users = userRepository.findAllActive();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("アクティブなユーザー一覧の取得が完了: 件数={}, 実行時間={}ms", 
                       users.size(), duration);
            
            return users;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("アクティブなユーザー一覧の取得でエラーが発生: 実行時間={}ms", duration, e);
            throw e;
        }
    }
    
    /**
     * 全ユーザー一覧を取得
     * 管理機能で使用
     * 
     * @return 全ユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 全ユーザー一覧を取得
     * APIエンドポイント /api/users で使用
     * 
     * @return 全ユーザーのリスト（アクティブ・非アクティブ両方を含む）
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return users;
        } catch (Exception e) {
            throw new RuntimeException("ユーザー一覧の取得に失敗しました", e);
        }
    }
    
    /**
     * ページネーション対応の全ユーザー一覧を取得
     * APIエンドポイント /api/users で使用（ページネーション版）
     * 
     * @param criteria 検索条件（ページネーション、フィルタリング、検索）
     * @return ユーザーリストレスポンス（ユーザーリストとページネーション情報）
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsersWithPagination(
            UserSearchCriteria criteria) {
        try {
            // パラメータのバリデーション
            criteria.validate();
            
            // ユーザーを検索
            List<User> users = userRepository.findUsersWithPagination(criteria);
            
            // 総件数を取得
            long totalElements = userRepository.countUsers(criteria);
            
            // レスポンスを作成
            return UserListResponse.of(
                users, totalElements, criteria.getPage(), criteria.getSize());
            
        } catch (IllegalArgumentException e) {
            logger.error("不正なパラメータ: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("ユーザー一覧の取得に失敗しました", e);
        }
    }
    
    /**
     * フルネームでユーザーを検索
     * 
     * @param fullNamePattern 検索パターン（部分一致）
     * @return マッチしたユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> searchByFullName(String fullNamePattern) {
        return userRepository.searchByFullName(fullNamePattern);
    }
    
    // === 検証メソッド ===
    
    /**
     * ユーザー名の重複チェック
     * 
     * @param username ユーザー名
     * @return 重複する場合true
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * メールアドレスの重複チェック
     * 
     * @param email メールアドレス
     * @return 重複する場合true
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * ユーザーの存在チェック
     * 
     * @param userId ユーザーID
     * @return 存在する場合true
     */
    @Transactional(readOnly = true)
    public boolean isUserExists(String userId) {
        return userRepository.existsById(userId);
    }
    
    /**
     * 管理者ユーザー一覧を取得
     * statusパラメータによってフィルタリング
     * 
     * @param status ステータス条件（"ACTIVE", "INACTIVE", null）
     * @return 条件に合致するユーザーのリスト
     * @throws IllegalArgumentException 不正なstatusパラメータの場合
     */
    @Transactional(readOnly = true)
    public List<User> getAdminUsers(String status) {
        logger.debug("Getting admin users with status filter: {}", status);
        
        if (status == null || status.isEmpty()) {
            // ステータス指定なしの場合、全ユーザーを取得
            return userRepository.searchUsers(null, null, null);
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return userRepository.searchUsers(null, null, true);
            case "INACTIVE":
                return userRepository.searchUsers(null, null, false);
            default:
                throw new IllegalArgumentException("不正なステータスパラメータです: " + status);
        }
    }
    
    /**
     * 全管理者ユーザーを取得（ステータス不問）
     * 
     * @return 全ユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> getAllAdminUsers() {
        logger.debug("Getting all admin users");
        return userRepository.searchUsers(null, null, null);
    }
    
    /**
     * アクティブな管理者ユーザーのみを取得
     * 
     * @return アクティブなユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> getActiveAdminUsers() {
        logger.debug("Getting active admin users");
        return userRepository.searchUsers(null, null, true);
    }
    
    /**
     * 非アクティブな管理者ユーザーのみを取得
     * 
     * @return 非アクティブなユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> getInactiveAdminUsers() {
        logger.debug("Getting inactive admin users");
        return userRepository.searchUsers(null, null, false);
    }
}