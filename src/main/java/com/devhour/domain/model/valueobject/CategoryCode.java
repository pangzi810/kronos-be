package com.devhour.domain.model.valueobject;

import java.util.Objects;

/**
 * 作業カテゴリコードの値オブジェクト
 * 
 * ビジネスルール:
 * - 2-20文字の英大文字とアンダースコアのみ許可
 * - null、空文字は不許可
 * - 不変オブジェクトとして実装
 * 
 * 使用例: BRD, PRD, ARCHITECTURE, DEV, OPERATION, MEETING, OTHERS
 */
public record CategoryCode(String value) {
    
    // 検証パターン: 英大文字とアンダースコア、2-20文字
    private static final String VALIDATION_PATTERN = "^[A-Z_]{2,20}$";
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、不正な値の場合は例外をスロー
     * 
     * @param value カテゴリコード文字列
     * @throws IllegalArgumentException 不正な値の場合
     */
    public CategoryCode {
        validateCategoryCode(value);
    }
    
    /**
     * カテゴリコードの検証
     * 
     * @param value 検証対象の値
     * @throws IllegalArgumentException 検証エラーの場合
     */
    private void validateCategoryCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("カテゴリコードはnullにできません");
        }
        
        if (value.isBlank()) {
            throw new IllegalArgumentException("カテゴリコードは空文字にできません");
        }
        
        if (!value.matches(VALIDATION_PATTERN)) {
            throw new IllegalArgumentException(
                String.format("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください。入力値: '%s'", value)
            );
        }
    }
    
    /**
     * よく使用されるカテゴリコードの定数定義
     * テスト用やサンプルデータ作成時に使用
     */
    public static final CategoryCode BRD = new CategoryCode("BRD");
    public static final CategoryCode PRD = new CategoryCode("PRD");
    public static final CategoryCode ARCHITECTURE = new CategoryCode("ARCHITECTURE");
    public static final CategoryCode DEV = new CategoryCode("DEV");
    public static final CategoryCode OPERATION = new CategoryCode("OPERATION");
    public static final CategoryCode MEETING = new CategoryCode("MEETING");
    public static final CategoryCode OTHERS = new CategoryCode("OTHERS");
    
    /**
     * 文字列からCategoryCodeを安全に生成
     * 
     * @param value カテゴリコード文字列
     * @return CategoryCodeオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static CategoryCode of(String value) {
        return new CategoryCode(value);
    }
    
    /**
     * 文字列表現を取得
     * JSONシリアライゼーション等で使用
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * 等価性の比較
     * recordクラスのため自動実装されるが、明示的にドキュメント化
     */
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.value, ((CategoryCode) obj).value);
    }
    
    /**
     * ハッシュコード
     * recordクラスのため自動実装されるが、明示的にドキュメント化
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}