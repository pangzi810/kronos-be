package com.devhour.domain.model.valueobject;

import java.util.Objects;

/**
 * 表示順の値オブジェクト
 * 
 * ビジネスルール:
 * - 1-999の範囲内の整数
 * - nullは不許可
 * - 不変オブジェクトとして実装
 * 
 * 使用例: 1, 2, 3, ... (カテゴリの表示順序)
 */
public record DisplayOrder(Integer value) {
    
    // 表示順の範囲
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 999;
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、不正な値の場合は例外をスロー
     * 
     * @param value 表示順の整数値
     * @throws IllegalArgumentException 不正な値の場合
     */
    public DisplayOrder {
        validateDisplayOrder(value);
    }
    
    /**
     * 表示順の検証
     * 
     * @param value 検証対象の値
     * @throws IllegalArgumentException 検証エラーの場合
     */
    private void validateDisplayOrder(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("表示順はnullにできません");
        }
        
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(
                String.format("表示順は%d-%dの範囲で入力してください。入力値: %d", MIN_VALUE, MAX_VALUE, value)
            );
        }
    }
    
    /**
     * 整数値からDisplayOrderを安全に生成
     * 
     * @param value 表示順の整数値
     * @return DisplayOrderオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static DisplayOrder of(Integer value) {
        return new DisplayOrder(value);
    }
    
    /**
     * int値からDisplayOrderを生成
     * 
     * @param value 表示順のint値
     * @return DisplayOrderオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static DisplayOrder of(int value) {
        return new DisplayOrder(value);
    }
    
    /**
     * 次の表示順を取得
     * 
     * @return 現在の値+1のDisplayOrder、MAX_VALUEを超える場合は例外
     * @throws IllegalArgumentException 最大値を超える場合
     */
    public DisplayOrder next() {
        if (value >= MAX_VALUE) {
            throw new IllegalArgumentException(
                String.format("表示順が最大値(%d)に達しているため、次の値を生成できません", MAX_VALUE)
            );
        }
        return new DisplayOrder(value + 1);
    }
    
    /**
     * 前の表示順を取得
     * 
     * @return 現在の値-1のDisplayOrder、MIN_VALUEを下回る場合は例外
     * @throws IllegalArgumentException 最小値を下回る場合
     */
    public DisplayOrder previous() {
        if (value <= MIN_VALUE) {
            throw new IllegalArgumentException(
                String.format("表示順が最小値(%d)に達しているため、前の値を生成できません", MIN_VALUE)
            );
        }
        return new DisplayOrder(value - 1);
    }
    
    /**
     * 別の表示順との比較
     * 
     * @param other 比較対象
     * @return 負の数（より小さい）、0（同じ）、正の数（より大きい）
     */
    public int compareTo(DisplayOrder other) {
        return Integer.compare(this.value, other.value);
    }
    
    /**
     * 指定した表示順より前かどうか
     * 
     * @param other 比較対象
     * @return より小さい場合true
     */
    public boolean isBefore(DisplayOrder other) {
        return this.value < other.value;
    }
    
    /**
     * 指定した表示順より後かどうか
     * 
     * @param other 比較対象
     * @return より大きい場合true
     */
    public boolean isAfter(DisplayOrder other) {
        return this.value > other.value;
    }
    
    /**
     * プリミティブint値を取得
     * データベース保存時やソート処理時に使用
     * 
     * @return int値
     */
    public int intValue() {
        return value;
    }
    
    /**
     * 文字列表現を取得
     */
    @Override
    public String toString() {
        return value.toString();
    }
    
    /**
     * 等価性の比較
     * recordクラスのため自動実装されるが、明示的にドキュメント化
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DisplayOrder that = (DisplayOrder) obj;
        return Objects.equals(this.value, that.value);
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