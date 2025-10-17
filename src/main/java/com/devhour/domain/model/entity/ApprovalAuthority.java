package com.devhour.domain.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import com.devhour.domain.model.valueobject.Position;

/**
 * 承認権限エンティティ
 * 
 * 工数承認の権限を持つユーザー情報を管理するエンティティ
 * 組織階層に基づいた承認権限の管理も行う
 * 
 * 責務:
 * - 承認権限者の基本情報管理
 * - 組織階層情報の管理
 * - 承認権限の判定
 * - 組織パスの生成
 */
public class ApprovalAuthority {
    
    /**
     * メールアドレスの形式検証用正規表現
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private String id;
    private String email;
    private String name;
    private Position position;
    private String level1Code;
    private String level1Name;
    private String level2Code;
    private String level2Name;
    private String level3Code;
    private String level3Name;
    private String level4Code;
    private String level4Name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private ApprovalAuthority() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }
    
    /**
     * プライベートコンストラクタ
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private ApprovalAuthority(String id, String email, String name, Position position,
                             String level1Code, String level1Name,
                             String level2Code, String level2Name,
                             String level3Code, String level3Name,
                             String level4Code, String level4Name,
                             LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.position = position;
        this.level1Code = level1Code;
        this.level1Name = level1Name;
        this.level2Code = level2Code;
        this.level2Name = level2Name;
        this.level3Code = level3Code;
        this.level3Name = level3Name;
        this.level4Code = level4Code;
        this.level4Name = level4Name;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * 既存承認権限の復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private ApprovalAuthority(String id, String email, String name, Position position,
                             String level1Code, String level1Name,
                             String level2Code, String level2Name,
                             String level3Code, String level3Name,
                             String level4Code, String level4Name,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.position = position;
        this.level1Code = level1Code;
        this.level1Name = level1Name;
        this.level2Code = level2Code;
        this.level2Name = level2Name;
        this.level3Code = level3Code;
        this.level3Name = level3Name;
        this.level4Code = level4Code;
        this.level4Name = level4Name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 新しい承認権限を作成するファクトリーメソッド
     * 
     * @param email メールアドレス
     * @param name 名前
     * @param position 役職
     * @param level1Code Level1組織コード
     * @param level1Name Level1組織名
     * @param level2Code Level2組織コード（可選）
     * @param level2Name Level2組織名（可選）
     * @param level3Code Level3組織コード（可選）
     * @param level3Name Level3組織名（可選）
     * @param level4Code Level4組織コード（可選）
     * @param level4Name Level4組織名（可選）
     * @return 新しいApprovalAuthorityエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static ApprovalAuthority create(String email, String name, Position position,
                                          String level1Code, String level1Name,
                                          String level2Code, String level2Name,
                                          String level3Code, String level3Name,
                                          String level4Code, String level4Name) {
        validateCreateParameters(email, name, position, level1Code, level1Name);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new ApprovalAuthority(
            id, email.trim().toLowerCase(), name.trim(), position,
            level1Code != null ? level1Code.trim() : null, 
            level1Name != null ? level1Name.trim() : null,
            level2Code != null ? level2Code.trim() : null,
            level2Name != null ? level2Name.trim() : null,
            level3Code != null ? level3Code.trim() : null,
            level3Name != null ? level3Name.trim() : null,
            level4Code != null ? level4Code.trim() : null,
            level4Name != null ? level4Name.trim() : null,
            now
        );
    }
    
    /**
     * 既存承認権限を復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id ID
     * @param email メールアドレス
     * @param name 名前
     * @param position 役職
     * @param level1Code Level1組織コード
     * @param level1Name Level1組織名
     * @param level2Code Level2組織コード
     * @param level2Name Level2組織名
     * @param level3Code Level3組織コード
     * @param level3Name Level3組織名
     * @param level4Code Level4組織コード
     * @param level4Name Level4組織名
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 復元されたApprovalAuthorityエンティティ
     */
    public static ApprovalAuthority restore(String id, String email, String name, Position position,
                                           String level1Code, String level1Name,
                                           String level2Code, String level2Name,
                                           String level3Code, String level3Name,
                                           String level4Code, String level4Name,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ApprovalAuthority(
            id, email, name, position,
            level1Code, level1Name, level2Code, level2Name,
            level3Code, level3Name, level4Code, level4Name,
            createdAt, updatedAt
        );
    }
    
    /**
     * 承認権限作成パラメータの検証
     */
    private static void validateCreateParameters(String email, String name, Position position,
                                               String level1Code, String level1Name) {
        // メールアドレスの検証
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        
        if (email.trim().length() > 255) {
            throw new IllegalArgumentException("メールアドレスは255文字以内で入力してください");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません");
        }
        
        // 名前の検証
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名前は必須です");
        }
        
        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("名前は255文字以内で入力してください");
        }
        
        // 役職の検証
        if (position == null) {
            throw new IllegalArgumentException("役職は必須です");
        }
        
        // Level1組織情報の検証
        if (level1Code == null || level1Code.trim().isEmpty()) {
            throw new IllegalArgumentException("Level1組織コードは必須です");
        }
        
        if (level1Name == null || level1Name.trim().isEmpty()) {
            throw new IllegalArgumentException("Level1組織名は必須です");
        }
    }
    
    /**
     * 承認権限情報を更新
     * 
     * @param email 新しいメールアドレス
     * @param name 新しい名前
     * @param position 新しい役職
     * @param level1Code 新しいLevel1組織コード
     * @param level1Name 新しいLevel1組織名
     * @param level2Code 新しいLevel2組織コード
     * @param level2Name 新しいLevel2組織名
     * @param level3Code 新しいLevel3組織コード
     * @param level3Name 新しいLevel3組織名
     * @param level4Code 新しいLevel4組織コード
     * @param level4Name 新しいLevel4組織名
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateInfo(String email, String name, Position position,
                          String level1Code, String level1Name,
                          String level2Code, String level2Name,
                          String level3Code, String level3Name,
                          String level4Code, String level4Name) {
        validateCreateParameters(email, name, position, level1Code, level1Name);
        
        this.email = email.trim().toLowerCase();
        this.name = name.trim();
        this.position = position;
        this.level1Code = level1Code != null ? level1Code.trim() : null;
        this.level1Name = level1Name != null ? level1Name.trim() : null;
        this.level2Code = level2Code != null ? level2Code.trim() : null;
        this.level2Name = level2Name != null ? level2Name.trim() : null;
        this.level3Code = level3Code != null ? level3Code.trim() : null;
        this.level3Name = level3Name != null ? level3Name.trim() : null;
        this.level4Code = level4Code != null ? level4Code.trim() : null;
        this.level4Name = level4Name != null ? level4Name.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 承認権限を持っているかチェック
     * 一般社員以外は承認権限あり
     * 
     * @return 承認権限がある場合true
     */
    public boolean hasApprovalAuthority() {
        return position.hasApprovalAuthority();
    }
    
    /**
     * 階層レベルを取得
     * 役職に基づく組織内の階層レベル
     * 
     * @return 階層レベル（0が最下位、数値が大きいほど上位）
     */
    public int getHierarchyLevel() {
        return position.getHierarchyLevel();
    }
    
    /**
     * 組織パスを取得（組織名）
     * Level1からLevel4までの組織名を「>」で繋げたパス
     * 
     * @return 組織名のパス文字列
     */
    public String getOrganizationPath() {
        List<String> pathParts = new ArrayList<>();
        
        if (level1Name != null && !level1Name.trim().isEmpty()) {
            pathParts.add(level1Name.trim());
        }
        if (level2Name != null && !level2Name.trim().isEmpty()) {
            pathParts.add(level2Name.trim());
        }
        if (level3Name != null && !level3Name.trim().isEmpty()) {
            pathParts.add(level3Name.trim());
        }
        if (level4Name != null && !level4Name.trim().isEmpty()) {
            pathParts.add(level4Name.trim());
        }
        
        return String.join(" > ", pathParts);
    }
    
    /**
     * 組織コードパスを取得
     * Level1からLevel4までの組織コードを「>」で繋げたパス
     * 
     * @return 組織コードのパス文字列
     */
    public String getOrganizationCodePath() {
        List<String> pathParts = new ArrayList<>();
        
        if (level1Code != null && !level1Code.trim().isEmpty()) {
            pathParts.add(level1Code.trim());
        }
        if (level2Code != null && !level2Code.trim().isEmpty()) {
            pathParts.add(level2Code.trim());
        }
        if (level3Code != null && !level3Code.trim().isEmpty()) {
            pathParts.add(level3Code.trim());
        }
        if (level4Code != null && !level4Code.trim().isEmpty()) {
            pathParts.add(level4Code.trim());
        }
        
        return String.join(" > ", pathParts);
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Position getPosition() { return position; }
    public String getLevel1Code() { return level1Code; }
    public String getLevel1Name() { return level1Name; }
    public String getLevel2Code() { return level2Code; }
    public String getLevel2Name() { return level2Name; }
    public String getLevel3Code() { return level3Code; }
    public String getLevel3Name() { return level3Name; }
    public String getLevel4Code() { return level4Code; }
    public String getLevel4Name() { return level4Name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ApprovalAuthority that = (ApprovalAuthority) obj;
        return Objects.equals(id, that.id);
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
        return String.format("ApprovalAuthority{id='%s', email='%s', position=%s}", 
                           id, email, position);
    }
}