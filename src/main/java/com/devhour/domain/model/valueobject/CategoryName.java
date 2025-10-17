package com.devhour.domain.model.valueobject;

import java.util.Objects;

/**
 * 作業カテゴリ名の値オブジェクト
 * 
 * ビジネスルール:
 * - 1-50文字の非空文字列
 * - null、空文字、空白のみは不許可
 * - 不変オブジェクトとして実装
 * 
 * 使用例: "BRD作成", "PRD作成", "アーキテクチャ設計"
 */
public record CategoryName(String value) {
    
    // 最大文字数
    private static final int MAX_LENGTH = 50;
    private static final int MIN_LENGTH = 1;
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、不正な値の場合は例外をスロー
     * 
     * @param value カテゴリ名文字列
     * @throws IllegalArgumentException 不正な値の場合
     */
    public CategoryName {
        validateCategoryName(value);
    }
    
    /**
     * カテゴリ名の検証
     * 
     * @param value 検証対象の値
     * @throws IllegalArgumentException 検証エラーの場合
     */
    private void validateCategoryName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("カテゴリ名はnullにできません");
        }
        
        if (value.isBlank()) {
            throw new IllegalArgumentException("カテゴリ名は空文字または空白のみにできません");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("カテゴリ名は%d文字以上で入力してください。入力値の長さ: %d", MIN_LENGTH, trimmedValue.length())
            );
        }
        
        if (trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("カテゴリ名は%d文字以下で入力してください。入力値の長さ: %d", MAX_LENGTH, trimmedValue.length())
            );
        }
    }
    
    /**
     * 文字列からCategoryNameを安全に生成
     * 前後の空白を自動トリム
     * 
     * @param value カテゴリ名文字列
     * @return CategoryNameオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static CategoryName of(String value) {
        if (value != null) {
            value = value.trim();
        }
        return new CategoryName(value);
    }
    
    /**
     * トリム済みの値を取得
     * データベース保存時やAPI応答時に使用
     * 
     * @return トリム済み文字列
     */
    public String getValue() {
        return value.trim();
    }
    
    /**
     * 文字列の長さを取得
     * 
     * @return 文字数（トリム後）
     */
    public int length() {
        return getValue().length();
    }
    
    /**
     * 文字列表現を取得
     * JSONシリアライゼーション等で使用
     */
    @Override
    public String toString() {
        return getValue();
    }
    
    /**
     * 等価性の比較
     * トリム済みの値で比較
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategoryName that = (CategoryName) obj;
        return Objects.equals(this.getValue(), that.getValue());
    }
    
    /**
     * ハッシュコード
     * トリム済みの値でハッシュコードを生成
     */
    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}