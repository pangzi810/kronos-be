package com.devhour.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;

/**
 * 作業カテゴリエンティティ
 * 
 * 工数記録で使用する作業カテゴリのマスターデータを管理
 * 
 * 責務:
 * - 作業カテゴリの基本情報管理
 * - カテゴリの表示順制御
 * - カテゴリの有効/無効状態管理
 * - カテゴリの表示設定管理
 */
public class WorkCategory {
    
    private String id;
    private CategoryCode code;
    private CategoryName name;
    private String description;
    private DisplayOrder displayOrder;
    private String colorCode;
    private boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    private WorkCategory() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }
    
    /**
     * プライベートコンストラクタ（新規作成用）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private WorkCategory(String id, CategoryCode code, CategoryName name, String description,
                        DisplayOrder displayOrder, String colorCode, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.colorCode = colorCode;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * プライベートコンストラクタ（復元用 - 完全版）
     * リポジトリからの読み込み時に使用
     */
    private WorkCategory(String id, CategoryCode code, CategoryName name, String description,
                        DisplayOrder displayOrder, String colorCode, boolean isActive,
                        String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.colorCode = colorCode;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 新しい作業カテゴリを作成するファクトリーメソッド
     * 
     * @param code カテゴリコード
     * @param name カテゴリ名
     * @param description カテゴリ説明
     * @param displayOrder 表示順
     * @param colorCode 表示色コード（オプション）
     * @return 新しいWorkCategoryエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static WorkCategory create(CategoryCode code, CategoryName name, String description,
                                    DisplayOrder displayOrder, String colorCode, String createdBy) {
        validateCreateParameters(code, name, displayOrder);
        validateColorCode(colorCode);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new WorkCategory(id, code, name, 
            description != null ? description.trim() : null,
            displayOrder, colorCode, createdBy, now);
    }
    
    /**
     * 既存作業カテゴリを復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     */
    public static WorkCategory restore(String id, CategoryCode code, CategoryName name, String description,
                                     DisplayOrder displayOrder, String colorCode, boolean isActive,
                                     String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        return new WorkCategory(id, code, name, description, displayOrder, colorCode,
                              isActive, createdBy, createdAt, updatedBy, updatedAt);
    }
    
    /**
     * 作業カテゴリ作成パラメータの検証
     */
    private static void validateCreateParameters(CategoryCode code, CategoryName name,
                                               DisplayOrder displayOrder) {
        if (code == null) {
            throw new IllegalArgumentException("カテゴリコードは必須です");
        }
        
        if (name == null) {
            throw new IllegalArgumentException("カテゴリ名は必須です");
        }
        
        if (displayOrder == null) {
            throw new IllegalArgumentException("表示順は必須です");
        }
    }
    
    /**
     * カラーコードの検証
     */
    private static void validateColorCode(String colorCode) {
        if (colorCode == null) {
            return; // null は許可（色指定なし）
        }
        
        colorCode = colorCode.trim().toUpperCase();
        if (!colorCode.matches("^#[0-9A-F]{6}$")) {
            throw new IllegalArgumentException(
                String.format("カラーコードは#RRGGBBの形式で入力してください（例: #FF5722）。入力値: '%s'", colorCode)
            );
        }
    }
    
    /**
     * 作業カテゴリ情報を更新
     * 
     * @param name 新しいカテゴリ名
     * @param description 新しい説明
     * @param displayOrder 新しい表示順
     * @param colorCode 新しいカラーコード
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateCategoryInfo(CategoryName name, String description,
                                 DisplayOrder displayOrder, String colorCode) {
        validateUpdateParameters(name, displayOrder);
        validateColorCode(colorCode);
        
        this.name = name;
        this.description = description != null ? description.trim() : null;
        this.displayOrder = displayOrder;
        this.colorCode = colorCode != null ? colorCode.trim().toUpperCase() : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新パラメータの検証
     */
    private void validateUpdateParameters(CategoryName name, DisplayOrder displayOrder) {
        if (name == null) {
            throw new IllegalArgumentException("カテゴリ名は必須です");
        }
        
        if (displayOrder == null) {
            throw new IllegalArgumentException("表示順は必須です");
        }
    }
    
    /**
     * 表示順を更新
     * 
     * @param newDisplayOrder 新しい表示順
     * @throws IllegalArgumentException 不正な表示順の場合
     */
    public void updateDisplayOrder(DisplayOrder newDisplayOrder) {
        if (newDisplayOrder == null) {
            throw new IllegalArgumentException("表示順は必須です");
        }
        
        this.displayOrder = newDisplayOrder;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * カテゴリを有効化
     */
    public void activate() {
        if (isActive) {
            return; // 既に有効な場合は何もしない
        }
        
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * カテゴリを無効化
     * 
     * @throws IllegalStateException システム必須カテゴリの場合
     */
    public void deactivate() {
        if (!isActive) {
            return; // 既に無効な場合は何もしない
        }

        
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * カテゴリが工数入力で使用可能かチェック
     * 
     * @return 使用可能な場合true
     */
    public boolean isUsableForWorkRecord() {
        return isActive;
    }
    
    /**
     * 指定した表示順より前にあるかチェック
     * 
     * @param other 比較対象の表示順
     * @return より前の場合true
     */
    public boolean isDisplayedBefore(DisplayOrder other) {
        return displayOrder.isBefore(other);
    }
    
    /**
     * 指定した表示順より後にあるかチェック
     * 
     * @param other 比較対象の表示順
     * @return より後の場合true
     */
    public boolean isDisplayedAfter(DisplayOrder other) {
        return displayOrder.isAfter(other);
    }
    
    /**
     * カテゴリの短縮表示名を取得
     * UI表示での省略形
     * 
     * @param maxLength 最大文字数
     * @return 短縮された名前
     */
    public String getShortName(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("最大文字数は1以上である必要があります");
        }
        
        String fullName = name.getValue();
        if (fullName.length() <= maxLength) {
            return fullName;
        }
        
        return fullName.substring(0, maxLength - 1) + "…";
    }
    
    /**
     * デフォルトカラーコードを取得
     * カラーコードが設定されていない場合のデフォルト色
     * 
     * @return カラーコード
     */
    public String getEffectiveColorCode() {
        return colorCode != null ? colorCode : "#607D8B"; // デフォルトはグレー
    }
    
    // ゲッター
    public String getId() { return id; }
    public CategoryCode getCode() { return code; }
    public CategoryName getName() { return name; }
    public String getDescription() { return description; }
    public DisplayOrder getDisplayOrder() { return displayOrder; }
    public String getColorCode() { return colorCode; }
    public boolean isActive() { return isActive; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkCategory that = (WorkCategory) obj;
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
        return String.format("WorkCategory{id='%s', code=%s, name='%s', displayOrder=%d, isActive=%s}", 
            id, code, name.getValue(), displayOrder.intValue(), isActive);
    }
}