package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CategoryName - 作業カテゴリ名の値オブジェクトのテスト")
class CategoryNameTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTest {

        @Test
        @DisplayName("正常な値でインスタンスを生成できる")
        void shouldCreateInstanceWithValidValue() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.value()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("前後に空白がある値でインスタンスを生成できる")
        void shouldCreateInstanceWithWhitespace() {
            CategoryName categoryName = new CategoryName("  開発作業  ");
            assertThat(categoryName.value()).isEqualTo("  開発作業  ");
        }

        @Test
        @DisplayName("1文字の値でインスタンスを生成できる")
        void shouldCreateInstanceWithSingleCharacter() {
            CategoryName categoryName = new CategoryName("A");
            assertThat(categoryName.value()).isEqualTo("A");
        }

        @Test
        @DisplayName("50文字の値でインスタンスを生成できる")
        void shouldCreateInstanceWithMaxLength() {
            String maxLengthName = "あ".repeat(50);
            CategoryName categoryName = new CategoryName(maxLengthName);
            assertThat(categoryName.value()).isEqualTo(maxLengthName);
        }
    }

    @Nested
    @DisplayName("バリデーションのテスト")
    class ValidationTest {

        @Test
        @DisplayName("nullの値で例外をスローする")
        void shouldThrowExceptionForNullValue() {
            assertThatThrownBy(() -> new CategoryName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名はnullにできません");
        }

        @Test
        @DisplayName("空文字で例外をスローする")
        void shouldThrowExceptionForEmptyValue() {
            assertThatThrownBy(() -> new CategoryName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名は空文字または空白のみにできません");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", "　"})
        @DisplayName("空白のみの値で例外をスローする")
        void shouldThrowExceptionForBlankValue(String value) {
            assertThatThrownBy(() -> new CategoryName(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名は空文字または空白のみにできません");
        }

        @Test
        @DisplayName("51文字の値で例外をスローする")
        void shouldThrowExceptionForTooLongValue() {
            String tooLongName = "あ".repeat(51);
            assertThatThrownBy(() -> new CategoryName(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("カテゴリ名は50文字以下で入力してください")
                .hasMessageContaining("入力値の長さ: 51");
        }

        @Test
        @DisplayName("トリム後に51文字になる値で例外をスローする")
        void shouldThrowExceptionForTooLongValueAfterTrim() {
            String tooLongName = "  " + "あ".repeat(51) + "  ";
            assertThatThrownBy(() -> new CategoryName(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("カテゴリ名は50文字以下で入力してください")
                .hasMessageContaining("入力値の長さ: 51");
        }
    }

    @Nested
    @DisplayName("ofメソッドのテスト")
    class OfMethodTest {

        @Test
        @DisplayName("正常な値でインスタンスを生成できる")
        void shouldCreateInstanceWithValidValue() {
            CategoryName categoryName = CategoryName.of("開発作業");
            assertThat(categoryName.value()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("前後の空白を自動的にトリムする")
        void shouldTrimWhitespace() {
            CategoryName categoryName = CategoryName.of("  開発作業  ");
            assertThat(categoryName.value()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("nullの値で例外をスローする")
        void shouldThrowExceptionForNullValue() {
            assertThatThrownBy(() -> CategoryName.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名はnullにできません");
        }

        @Test
        @DisplayName("空文字で例外をスローする")
        void shouldThrowExceptionForEmptyValue() {
            assertThatThrownBy(() -> CategoryName.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名は空文字または空白のみにできません");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", "　"})
        @DisplayName("空白のみの値で例外をスローする")
        void shouldThrowExceptionForBlankValue(String value) {
            assertThatThrownBy(() -> CategoryName.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("カテゴリ名は空文字または空白のみにできません");
        }

        @Test
        @DisplayName("複数の空白を含む値を正しくトリムする")
        void shouldTrimMultipleSpaces() {
            CategoryName categoryName = CategoryName.of("  \t開発作業\n  ");
            assertThat(categoryName.value()).isEqualTo("開発作業");
        }
    }

    @Nested
    @DisplayName("getValueメソッドのテスト")
    class GetValueTest {

        @Test
        @DisplayName("トリム済みの値を返す")
        void shouldReturnTrimmedValue() {
            CategoryName categoryName = new CategoryName("  開発作業  ");
            assertThat(categoryName.getValue()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("元々トリム済みの値はそのまま返す")
        void shouldReturnOriginalIfAlreadyTrimmed() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.getValue()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("複数の空白を含む値を正しくトリムする")
        void shouldTrimComplexWhitespace() {
            CategoryName categoryName = new CategoryName("  \t\n開発作業  \t\n");
            assertThat(categoryName.getValue()).isEqualTo("開発作業");
        }
    }

    @Nested
    @DisplayName("lengthメソッドのテスト")
    class LengthTest {

        @Test
        @DisplayName("文字数を正しく返す")
        void shouldReturnCorrectLength() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.length()).isEqualTo(4);
        }

        @Test
        @DisplayName("トリム後の文字数を返す")
        void shouldReturnTrimmedLength() {
            CategoryName categoryName = new CategoryName("  開発作業  ");
            assertThat(categoryName.length()).isEqualTo(4);
        }

        @Test
        @DisplayName("1文字の場合の長さ")
        void shouldReturnLengthForSingleChar() {
            CategoryName categoryName = new CategoryName("A");
            assertThat(categoryName.length()).isEqualTo(1);
        }

        @Test
        @DisplayName("50文字の場合の長さ")
        void shouldReturnLengthForMaxLength() {
            String maxLengthName = "あ".repeat(50);
            CategoryName categoryName = new CategoryName(maxLengthName);
            assertThat(categoryName.length()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("toStringメソッドのテスト")
    class ToStringTest {

        @Test
        @DisplayName("トリム済みの値を文字列として返す")
        void shouldReturnTrimmedString() {
            CategoryName categoryName = new CategoryName("  開発作業  ");
            assertThat(categoryName.toString()).isEqualTo("開発作業");
        }

        @Test
        @DisplayName("元々トリム済みの値をそのまま返す")
        void shouldReturnOriginalString() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.toString()).isEqualTo("開発作業");
        }
    }

    @Nested
    @DisplayName("equalsメソッドのテスト")
    class EqualsTest {

        @Test
        @DisplayName("同じインスタンスは等しい")
        void shouldBeEqualToSameInstance() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.equals(categoryName)).isTrue();
        }

        @Test
        @DisplayName("同じ値のインスタンスは等しい")
        void shouldBeEqualToSameValue() {
            CategoryName categoryName1 = new CategoryName("開発作業");
            CategoryName categoryName2 = new CategoryName("開発作業");
            assertThat(categoryName1.equals(categoryName2)).isTrue();
        }

        @Test
        @DisplayName("トリム後に同じ値になるインスタンスは等しい")
        void shouldBeEqualAfterTrim() {
            CategoryName categoryName1 = new CategoryName("  開発作業  ");
            CategoryName categoryName2 = new CategoryName("開発作業");
            assertThat(categoryName1.equals(categoryName2)).isTrue();
        }

        @Test
        @DisplayName("異なる値のインスタンスは等しくない")
        void shouldNotBeEqualToDifferentValue() {
            CategoryName categoryName1 = new CategoryName("開発作業");
            CategoryName categoryName2 = new CategoryName("レビュー");
            assertThat(categoryName1.equals(categoryName2)).isFalse();
        }

        @Test
        @DisplayName("nullとは等しくない")
        void shouldNotBeEqualToNull() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.equals(null)).isFalse();
        }

        @Test
        @DisplayName("異なるクラスのオブジェクトとは等しくない")
        @SuppressWarnings({"unlikely-arg-type", "EqualsBetweenInconvertibleTypes"})
        void shouldNotBeEqualToDifferentClass() {
            CategoryName categoryName = new CategoryName("開発作業");
            assertThat(categoryName.equals("開発作業")).isFalse();
        }

        @Test
        @DisplayName("大文字小文字を区別する")
        void shouldDistinguishCase() {
            CategoryName categoryName1 = new CategoryName("Development");
            CategoryName categoryName2 = new CategoryName("development");
            assertThat(categoryName1.equals(categoryName2)).isFalse();
        }
    }

    @Nested
    @DisplayName("hashCodeメソッドのテスト")
    class HashCodeTest {

        @Test
        @DisplayName("同じ値のインスタンスは同じハッシュコードを持つ")
        void shouldHaveSameHashCodeForSameValue() {
            CategoryName categoryName1 = new CategoryName("開発作業");
            CategoryName categoryName2 = new CategoryName("開発作業");
            assertThat(categoryName1.hashCode()).isEqualTo(categoryName2.hashCode());
        }

        @Test
        @DisplayName("トリム後に同じ値になるインスタンスは同じハッシュコードを持つ")
        void shouldHaveSameHashCodeAfterTrim() {
            CategoryName categoryName1 = new CategoryName("  開発作業  ");
            CategoryName categoryName2 = new CategoryName("開発作業");
            assertThat(categoryName1.hashCode()).isEqualTo(categoryName2.hashCode());
        }

        @Test
        @DisplayName("異なる値のインスタンスは（通常）異なるハッシュコードを持つ")
        void shouldHaveDifferentHashCodeForDifferentValue() {
            CategoryName categoryName1 = new CategoryName("開発作業");
            CategoryName categoryName2 = new CategoryName("レビュー");
            assertThat(categoryName1.hashCode()).isNotEqualTo(categoryName2.hashCode());
        }
    }

    @Nested
    @DisplayName("境界値のテスト")
    class BoundaryTest {

        @ParameterizedTest
        @CsvSource({
            "A, 1",
            "AB, 2",
            "開発, 2",
            "開発作業, 4",
            "プロジェクト管理作業, 10"
        })
        @DisplayName("様々な長さの有効な値")
        void shouldAcceptVariousLengths(String value, int expectedLength) {
            CategoryName categoryName = CategoryName.of(value);
            assertThat(categoryName.length()).isEqualTo(expectedLength);
        }

        @Test
        @DisplayName("最大長境界値のテスト - 49文字")
        void shouldAccept49Characters() {
            String name = "あ".repeat(49);
            CategoryName categoryName = new CategoryName(name);
            assertThat(categoryName.length()).isEqualTo(49);
        }

        @Test
        @DisplayName("最大長境界値のテスト - 50文字")
        void shouldAccept50Characters() {
            String name = "あ".repeat(50);
            CategoryName categoryName = new CategoryName(name);
            assertThat(categoryName.length()).isEqualTo(50);
        }

        @Test
        @DisplayName("最大長境界値のテスト - 51文字で例外")
        void shouldReject51Characters() {
            String name = "あ".repeat(51);
            assertThatThrownBy(() -> new CategoryName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("50文字以下");
        }
    }

    @Nested
    @DisplayName("実際の使用例のテスト")
    class UseCaseTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "BRD作成",
            "PRD作成",
            "アーキテクチャ設計",
            "開発作業",
            "コードレビュー",
            "テスト",
            "バグ修正",
            "ドキュメント作成",
            "会議",
            "リファクタリング"
        })
        @DisplayName("一般的なカテゴリ名を受け入れる")
        void shouldAcceptCommonCategoryNames(String name) {
            CategoryName categoryName = CategoryName.of(name);
            assertThat(categoryName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("日本語と英数字の混在")
        void shouldAcceptMixedCharacters() {
            CategoryName categoryName = CategoryName.of("Phase1 開発");
            assertThat(categoryName.getValue()).isEqualTo("Phase1 開発");
        }

        @Test
        @DisplayName("記号を含むカテゴリ名")
        void shouldAcceptNamesWithSymbols() {
            CategoryName categoryName = CategoryName.of("開発・テスト");
            assertThat(categoryName.getValue()).isEqualTo("開発・テスト");
        }
    }
}