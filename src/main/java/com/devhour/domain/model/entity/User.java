package com.devhour.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * ユーザーエンティティ
 * 
 * システムのユーザーを表すエンティティ
 * 
 * 責務:
 * - ユーザー基本情報の管理
 * - 認証情報の管理
 * - ユーザー状態管理
 */
public class User {
    
    /**
     * ユーザーステータス列挙型
     */
    public enum UserStatus {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE"),
        SUSPENDED("SUSPENDED");
        
        private final String value;
        
        UserStatus(String value) {
            this.value = value;
        }
        
        public String getValue() { return value; }
        
        /**
         * 文字列からUserStatusに変換
         */
        public static UserStatus fromValue(String value) {
            for (UserStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("不正なステータス値: " + value);
        }
        
        /**
         * アクティブステータスかどうか
         */
        public boolean isActive() {
            return this == ACTIVE;
        }
    }
    
    
    private String id;
    private String username;
    private String email;
    private String fullName;
    private UserStatus userStatus;
    private LocalDateTime lastLoginAt;  // 最後のログイン時刻
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String oktaUserId;  // Okta User ID (sub claim from JWT token)
    
    private User() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }
    
    /**
     * プライベートコンストラクタ
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private User(String id, String username, String email,
                String fullName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.userStatus = UserStatus.ACTIVE;
        this.lastLoginAt = null;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.oktaUserId = null;
    }
    
    /**
     * 既存ユーザーの復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private User(String id, String username, String email,
                String fullName, UserStatus userStatus,
                LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.userStatus = userStatus;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.oktaUserId = null;
    }
    
    /**
     * 既存ユーザーの復元用コンストラクタ（Okta対応版）
     * リポジトリからの読み込み時に使用
     */
    private User(String id, String username, String email,
                String fullName, UserStatus userStatus,
                LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt, String oktaUserId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.userStatus = userStatus;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.oktaUserId = oktaUserId;
    }
    
    /**
     * 新しいユーザーを作成するファクトリーメソッド
     * Note: Okta認証を使用するため、パスワードは不要
     * 
     * @param username ユーザー名
     * @param email メールアドレス
     * @param fullName フルネーム
     * @return 新しいUserエンティティ
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public static User create(String username, String email, String fullName) {
        validateCreateParameters(username, email, fullName);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new User(id, username.trim(), email.trim().toLowerCase(), 
                       fullName.trim(), now);
    }
    
    /**
     * 既存ユーザーを復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id ユーザーID
     * @param username ユーザー名
     * @param email メールアドレス
     * @param fullName フルネーム
     * @param isActive アクティブフラグ
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 復元されたUserエンティティ
     */
    public static User restore(String id, String username, String email,
                              String fullName, boolean isActive,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        UserStatus status = isActive ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        return new User(id, username, email, fullName,
                       status, null, createdAt, updatedAt);
    }
    
    /**
     * 既存ユーザーを復元するファクトリーメソッド（Okta対応版）
     * リポジトリからの読み込み時に使用
     * 
     * @param id ユーザーID
     * @param username ユーザー名
     * @param email メールアドレス
     * @param fullName フルネーム
     * @param isActive アクティブフラグ
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @param oktaUserId Okta User ID
     * @return 復元されたUserエンティティ
     */
    public static User restoreWithOkta(String id, String username, String email,
                                      String fullName, boolean isActive,
                                      LocalDateTime createdAt, LocalDateTime updatedAt, String oktaUserId) {
        UserStatus status = isActive ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        return new User(id, username, email, fullName,
                       status, null, createdAt, updatedAt, oktaUserId);
    }
    
    /**
     * Oktaユーザー向けの新規ユーザー作成
     * 
     * @param email メールアドレス
     * @param fullName フルネーム
     * @param oktaUserId Okta User ID (sub claim)
     * @return 作成されたUserエンティティ
     */
    public static User createFromOkta(String email, String fullName, String oktaUserId) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("フルネームは必須です");
        }
        if (oktaUserId == null || oktaUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Okta User IDは必須です");
        }
        
        String id = UUID.randomUUID().toString();
        String username = generateUsernameFromEmail(email);
        LocalDateTime now = LocalDateTime.now();
        
        User user = new User(id, username, email.trim().toLowerCase(), 
                           fullName.trim(), UserStatus.ACTIVE, null, now, now, oktaUserId.trim());
        
        return user;
    }
    
    /**
     * メールアドレスからユーザー名を生成
     * @の前の部分を使用し、既に使用されている場合は数字を付加
     */
    private static String generateUsernameFromEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length > 0) {
            return parts[0];
        }
        return "user" + System.currentTimeMillis();
    }
    
    /**
     * ユーザー作成パラメータの検証
     */
    private static void validateCreateParameters(String username, String email, String fullName) {
        // ユーザー名の検証
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        
        if (username.trim().length() < 3 || username.trim().length() > 50) {
            throw new IllegalArgumentException("ユーザー名は3-50文字で入力してください");
        }
        
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("ユーザー名は英数字、ハイフン、アンダースコアのみ使用可能です");
        }
        
        // メールアドレスの検証
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        
        if (email.trim().length() > 255) {
            throw new IllegalArgumentException("メールアドレスは255文字以内で入力してください");
        }
        
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません");
        }
        
        // フルネームの検証
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("フルネームは必須です");
        }
        
        if (fullName.trim().length() > 255) {
            throw new IllegalArgumentException("フルネームは255文字以内で入力してください");
        }
    }
    
    /**
     * ユーザー情報を更新
     * 
     * @param email 新しいメールアドレス
     * @param fullName 新しいフルネーム
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException 更新不可能な状態の場合
     */
    public void updateUserInfo(String email, String fullName) {
        if (!userStatus.isActive()) {
            throw new IllegalStateException("無効化されたユーザーは更新できません");
        }
        
        validateUpdateParameters(email, fullName);
        
        this.email = email.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * ユーザー更新パラメータの検証
     */
    private void validateUpdateParameters(String email, String fullName) {
        // メールアドレスの検証
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        
        if (email.trim().length() > 255) {
            throw new IllegalArgumentException("メールアドレスは255文字以内で入力してください");
        }
        
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません");
        }
        
        // フルネームの検証
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("フルネームは必須です");
        }
        
        if (fullName.trim().length() > 255) {
            throw new IllegalArgumentException("フルネームは255文字以内で入力してください");
        }
    }
    
    
    /**
     * ユーザーを有効化（後方互換性のため保持）
     */
    public void activate() {
        activateUser();
    }
    
    /**
     * ユーザーを無効化（後方互換性のため保持）
     */
    public void deactivate() {
        deactivateUser();
    }
    
    /**
     * ユーザーをACTIVE状態に変更
     * INACTIVEやSUSPENDEDからACTIVEに遷移
     */
    public void activateUser() {
        if (userStatus == UserStatus.ACTIVE) {
            return; // 既にACTIVEな場合は何もしない
        }
        
        this.userStatus = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * ユーザーをINACTIVE状態に変更
     */
    public void deactivateUser() {
        if (userStatus == UserStatus.INACTIVE) {
            return; // 既にINACTIVEな場合は何もしない
        }
        
        this.userStatus = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * ユーザーをSUSPENDED状態に変更
     */
    public void suspendUser() {
        if (userStatus == UserStatus.SUSPENDED) {
            return; // 既にSUSPENDEDな場合は何もしない
        }
        
        this.userStatus = UserStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 最後のログイン時刻を更新
     * 
     * @param lastLoginAt ログイン時刻
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        if (lastLoginAt == null) {
            throw new IllegalArgumentException("ログイン時刻は必須です");
        }
        
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = LocalDateTime.now();
    }
    
    
    
    
    
    
    
    
    /**
     * 名前を取得（フルネームから抽出）
     * 
     * @return 名前（姓名の名）
     */
    public String getFirstName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }
    
    /**
     * 姓を取得（フルネームから抽出）
     * 
     * @return 姓
     */
    public String getLastName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : "";
    }
    
    /**
     * Okta User IDを設定
     * 既存ユーザーをOkta認証に移行する場合に使用
     * 
     * @param oktaUserId Okta User ID (sub claim)
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException 更新不可能な状態の場合
     */
    public void linkToOkta(String oktaUserId) {
        if (!userStatus.isActive()) {
            throw new IllegalStateException("無効化されたユーザーはOktaとリンクできません");
        }
        
        if (oktaUserId == null || oktaUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Okta User IDは必須です");
        }
        
        this.oktaUserId = oktaUserId.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * OktaからユーザーInfo情報で更新
     * Oktaユーザー同期時に使用
     * 
     * @param email Oktaから取得したメールアドレス
     * @param fullName Oktaから取得したフルネーム
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException 更新不可能な状態の場合
     */
    public void updateFromOkta(String email, String fullName) {
        if (!userStatus.isActive()) {
            throw new IllegalStateException("無効化されたユーザーは更新できません");
        }
        
        if (oktaUserId == null || oktaUserId.trim().isEmpty()) {
            throw new IllegalStateException("OktaユーザーIDが設定されていません");
        }
        
        validateUpdateParameters(email, fullName);
        
        this.email = email.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Oktaユーザーかどうかを判定
     * 
     * @return Oktaユーザーの場合true
     */
    public boolean isOktaUser() {
        return oktaUserId != null && !oktaUserId.trim().isEmpty();
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public UserStatus getUserStatus() { return userStatus; }
    public boolean isActive() { return userStatus.isActive(); }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getOktaUserId() { return oktaUserId; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }
    
    /**
     * ハッシュコード（IDベース）
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 文字列表現
     */
    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s'}", id, username);
    }
}