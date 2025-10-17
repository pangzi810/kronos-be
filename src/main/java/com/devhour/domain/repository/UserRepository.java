package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.User;

/**
 * ユーザーリポジトリインターフェース
 * 
 * ユーザーエンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepository パターンの実装
 * 
 * 責務:
 * - ユーザーエンティティの CRUD 操作
 * - 認証・認可に関する検索機能
 * - ビジネス要件に基づく検索・フィルタリング機能
 */
public interface UserRepository {
    
    /**
     * ユーザーIDでユーザーを検索
     * 
     * @param userId ユーザーID
     * @return ユーザーエンティティ（存在しない場合は空のOptional）
     */
    Optional<User> findById(String userId);
    
    /**
     * ユーザー名でユーザーを検索
     * 認証時に使用
     * 
     * @param username ユーザー名
     * @return ユーザーエンティティ（存在しない場合は空のOptional）
     */
    Optional<User> findByUsername(String username);
    
    /**
     * メールアドレスでユーザーを検索
     * ユーザー登録時の重複チェックに使用
     * 
     * @param email メールアドレス
     * @return ユーザーエンティティ（存在しない場合は空のOptional）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * アクティブユーザーをメールアドレスで検索
     * Oktaユーザープロビジョニング時にアクティブなユーザーの存在確認で使用
     * 削除済み（deleted_at != NULL）のユーザーは除外される
     * 
     * @param email メールアドレス
     * @return アクティブなユーザーエンティティ（存在しない場合は空のOptional）
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    /**
     * Okta User IDでユーザーを検索
     * Okta認証時にユーザーを特定するために使用
     * 
     * @param oktaUserId Okta User ID (sub claim)
     * @return ユーザーエンティティ（存在しない場合は空のOptional）
     */
    Optional<User> findByOktaUserId(String oktaUserId);
    
    /**
     * アクティブなユーザー一覧を取得
     * 
     * @return アクティブなユーザーのリスト
     */
    List<User> findAllActive();
    
    
    /**
     * 全ユーザー一覧を取得
     * 管理機能で使用（無効ユーザーも含む）
     * 
     * @return 全ユーザーのリスト
     */
    List<User> findAll();
    
    /**
     * ユーザー名の存在チェック
     * ユーザー登録時の重複チェックに使用
     * 
     * @param username ユーザー名
     * @return 存在する場合true
     */
    boolean existsByUsername(String username);
    
    /**
     * メールアドレスの存在チェック
     * ユーザー登録時の重複チェックに使用
     * 
     * @param email メールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);
    
    /**
     * Okta User IDの存在チェック
     * Okta認証時の重複チェックに使用
     * 
     * @param oktaUserId Okta User ID
     * @return 存在する場合true
     */
    boolean existsByOktaUserId(String oktaUserId);
    
    /**
     * ユーザーの存在チェック
     * 
     * @param userId ユーザーID
     * @return 存在する場合true
     */
    boolean existsById(String userId);
    
    /**
     * ユーザーを保存
     * 新規作成・更新の両方で使用
     * 
     * @param user 保存対象のユーザーエンティティ
     * @return 保存されたユーザーエンティティ
     */
    User save(User user);
    
    /**
     * ユーザーを削除
     * 物理削除ではなく、通常は isActive = false にする論理削除を推奨
     * 
     * @param userId 削除対象のユーザーID
     */
    void deleteById(String userId);
    
    /**
     * フルネームでの部分一致検索
     * ユーザー検索機能で使用
     * 
     * @param fullNamePattern 検索パターン（部分一致）
     * @return マッチしたユーザーのリスト
     */
    List<User> searchByFullName(String fullNamePattern);
    
    /**
     * ページネーション対応のユーザー検索
     * ユーザー一覧API(/api/users)で使用
     * 
     * @param criteria 検索条件（ページネーション、フィルタリング、検索）
     * @return 条件に合致するユーザーのリスト
     */
    List<User> findUsersWithPagination(com.devhour.presentation.dto.UserSearchCriteria criteria);
    
    /**
     * ユーザー数をカウント
     * ユーザー一覧APIのページネーション用
     * 
     * @param criteria 検索条件（フィルタリング、検索）
     * @return 条件に合致するユーザーの総数
     */
    long countUsers(com.devhour.presentation.dto.UserSearchCriteria criteria);
    
    /**
     * 複数条件での検索
     * 管理画面でのフィルタリングに使用
     * 
     * @param username ユーザー名（部分一致、nullの場合は条件から除外）
     * @param email メールアドレス（部分一致、nullの場合は条件から除外）
     * @param isActive アクティブ状態（nullの場合は条件から除外）
     * @return 検索条件にマッチしたユーザーのリスト
     */
    List<User> searchUsers(String username, String email, Boolean isActive);


    /**
     * ユーザーのアクティブ状態を更新
     * 
     * @param userId ユーザーID
     * @param isActive 新しいアクティブ状態
     */
    void updateActiveStatus(String userId, boolean isActive);
    
    /**
     * ユーザーステータスを更新
     * 
     * @param userId ユーザーID
     * @param userStatus 新しいユーザーステータス（ACTIVE, INACTIVE, SUSPENDED）
     */
    default void updateUserStatus(String userId, User.UserStatus userStatus) {
        // デフォルト実装では既存メソッドを使用して後方互換性を保持
        updateActiveStatus(userId, userStatus.isActive());
    }
    
    /**
     * 最終ログイン時刻を更新
     * 
     * @param userId ユーザーID
     * @param lastLoginAt 最終ログイン時刻
     */
    default void updateLastLoginAt(String userId, java.time.LocalDateTime lastLoginAt) {
        // 実装クラスでオーバーライドされる予定
        throw new UnsupportedOperationException("updateLastLoginAt method must be implemented in the repository implementation");
    }
}