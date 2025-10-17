package com.devhour.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CategoryCodeのユニットテスト
 */
@DisplayName("CategoryCode")
class CategoryCodeTest {

    @Test
    @DisplayName("正常なカテゴリコード作成")
    void of_ValidCode_Success() {
        // Act
        CategoryCode categoryCode = CategoryCode.of("DEV");

        // Assert
        assertThat(categoryCode.value()).isEqualTo("DEV");
    }

    @Test
    @DisplayName("定数カテゴリコード - 各定数が正しく定義されている")
    void constants_DefinedCorrectly() {
        // Act & Assert
        assertThat(CategoryCode.BRD.value()).isEqualTo("BRD");
        assertThat(CategoryCode.PRD.value()).isEqualTo("PRD");
        assertThat(CategoryCode.ARCHITECTURE.value()).isEqualTo("ARCHITECTURE");
        assertThat(CategoryCode.DEV.value()).isEqualTo("DEV");
        assertThat(CategoryCode.OPERATION.value()).isEqualTo("OPERATION");
        assertThat(CategoryCode.MEETING.value()).isEqualTo("MEETING");
        assertThat(CategoryCode.OTHERS.value()).isEqualTo("OTHERS");
    }

    @Test
    @DisplayName("カテゴリコード作成 - nullの場合は例外")
    void of_NullValue_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードはnullにできません");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 空文字の場合は例外")
    void of_EmptyValue_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは空文字にできません");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 空白文字の場合は例外")
    void of_BlankValue_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは空文字にできません");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 1文字の場合は例外")
    void of_TooShort_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("A"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 21文字の場合は例外")
    void of_TooLong_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("ABCDEFGHIJKLMNOPQRSTU"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 小文字を含む場合は例外")
    void of_ContainsLowerCase_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("Dev"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 数字を含む場合は例外")
    void of_ContainsDigits_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("DEV1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 特殊文字を含む場合は例外")
    void of_ContainsSpecialChars_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CategoryCode.of("DEV-TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードは2-20文字の英大文字とアンダースコアで入力してください");
    }

    @Test
    @DisplayName("カテゴリコード作成 - アンダースコアを含む場合は正常")
    void of_ContainsUnderscore_Success() {
        // Act
        CategoryCode categoryCode = CategoryCode.of("DEV_TEST");

        // Assert
        assertThat(categoryCode.value()).isEqualTo("DEV_TEST");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 境界値テスト（2文字）")
    void of_MinLength_Success() {
        // Act
        CategoryCode categoryCode = CategoryCode.of("AB");

        // Assert
        assertThat(categoryCode.value()).isEqualTo("AB");
    }

    @Test
    @DisplayName("カテゴリコード作成 - 境界値テスト（20文字）")
    void of_MaxLength_Success() {
        // Act
        CategoryCode categoryCode = CategoryCode.of("ABCDEFGHIJKLMNOPQRST");

        // Assert
        assertThat(categoryCode.value()).isEqualTo("ABCDEFGHIJKLMNOPQRST");
    }

    @Test
    @DisplayName("等価性判定 - 同じ値の場合")
    void equals_SameValue_ReturnsTrue() {
        // Arrange
        CategoryCode code1 = CategoryCode.of("DEV");
        CategoryCode code2 = CategoryCode.of("DEV");

        // Act & Assert
        assertThat(code1).isEqualTo(code2);
        assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なる値の場合")
    void equals_DifferentValue_ReturnsFalse() {
        // Arrange
        CategoryCode code1 = CategoryCode.of("DEV");
        CategoryCode code2 = CategoryCode.of("MEETING");

        // Act & Assert
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    @DisplayName("文字列表現")
    void toString_ReturnsValue() {
        // Arrange
        CategoryCode categoryCode = CategoryCode.of("DEV");

        // Act & Assert
        assertThat(categoryCode.toString()).isEqualTo("DEV");
    }
}