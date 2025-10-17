package com.devhour.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CategoryHoursのユニットテスト
 */
@DisplayName("CategoryHours")
class CategoryHoursTest {

    @Test
    @DisplayName("空のカテゴリ工数作成")
    void empty_Success() {
        // Act
        CategoryHours categoryHours = CategoryHours.empty();

        // Assert
        assertThat(categoryHours.hours()).isEmpty();
        // Total hours check removed - aggregation not needed
    }

    @Test
    @DisplayName("マップからカテゴリ工数作成 - 正常ケース")
    void of_ValidMap_Success() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0"),
            CategoryCode.MEETING, new BigDecimal("1.5")
        );

        // Act
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Assert
        assertThat(categoryHours.hours()).hasSize(2);
        assertThat(categoryHours.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("8.0"));
        assertThat(categoryHours.getHours(CategoryCode.MEETING)).isEqualByComparingTo(new BigDecimal("1.5"));
    }

    @Test
    @DisplayName("単一カテゴリからカテゴリ工数作成")
    void of_SingleCategory_Success() {
        // Act
        CategoryHours categoryHours = CategoryHours.of(CategoryCode.DEV, new BigDecimal("8.0"));

        // Assert
        assertThat(categoryHours.hours()).hasSize(1);
        assertThat(categoryHours.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("8.0"));
    }

    @Test
    @DisplayName("nullマップからカテゴリ工数作成 - 空のマップとして扱われる")
    void of_NullMap_CreatesEmpty() {
        // Act
        CategoryHours categoryHours = CategoryHours.of(null);

        // Assert
        assertThat(categoryHours.hours()).isEmpty();
    }

    @Test
    @DisplayName("カテゴリ工数作成 - nullカテゴリコードがある場合は例外")
    void of_NullCategoryCode_ThrowsException() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        hoursMap.put(null, new BigDecimal("8.0"));

        // Act & Assert
        assertThatThrownBy(() -> CategoryHours.of(hoursMap))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリコードがnullです");
    }

    @Test
    @DisplayName("カテゴリ工数作成 - null工数がある場合は例外")
    void of_NullHours_ThrowsException() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        hoursMap.put(CategoryCode.DEV, null);

        // Act & Assert
        assertThatThrownBy(() -> CategoryHours.of(hoursMap))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリ 'DEV' の工数がnullです");
    }

    @Test
    @DisplayName("カテゴリ工数作成 - 負の工数がある場合は例外")
    void of_NegativeHours_ThrowsException() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("-1.0")
        );

        // Act & Assert
        assertThatThrownBy(() -> CategoryHours.of(hoursMap))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリ 'DEV' の工数は0以上である必要があります");
    }

    @Test
    @DisplayName("カテゴリ工数作成 - 24時間を超える工数がある場合は例外")
    void of_ExcessiveHours_ThrowsException() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("25.0")
        );

        // Act & Assert
        assertThatThrownBy(() -> CategoryHours.of(hoursMap))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("カテゴリ 'DEV' の工数は24時間以下である必要があります");
    }



    @Test
    @DisplayName("特定カテゴリの工数取得 - 存在するカテゴリ")
    void getHours_ExistingCategory_ReturnsHours() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act
        BigDecimal hours = categoryHours.getHours(CategoryCode.DEV);

        // Assert
        assertThat(hours).isEqualByComparingTo(new BigDecimal("8.0"));
    }

    @Test
    @DisplayName("特定カテゴリの工数取得 - 存在しないカテゴリ")
    void getHours_NonExistingCategory_ReturnsZero() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act
        BigDecimal hours = categoryHours.getHours(CategoryCode.MEETING);

        // Assert
        assertThat(hours).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("カテゴリ工数存在チェック - 存在する場合")
    void hasHours_ExistingCategory_ReturnsTrue() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act & Assert
        assertThat(categoryHours.hasHours(CategoryCode.DEV)).isTrue();
    }

    @Test
    @DisplayName("カテゴリ工数存在チェック - 存在しない場合")
    void hasHours_NonExistingCategory_ReturnsFalse() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act & Assert
        assertThat(categoryHours.hasHours(CategoryCode.MEETING)).isFalse();
    }

    @Test
    @DisplayName("カテゴリ工数存在チェック - 0時間の場合はfalse")
    void hasHours_ZeroHours_ReturnsFalse() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, BigDecimal.ZERO
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act & Assert
        assertThat(categoryHours.hasHours(CategoryCode.DEV)).isFalse();
    }

    @Test
    @DisplayName("工数記録有無チェック - 工数がある場合")
    void hasAnyHours_WithHours_ReturnsTrue() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act & Assert
        assertThat(categoryHours.hasAnyHours()).isTrue();
    }

    @Test
    @DisplayName("工数記録有無チェック - 工数がない場合")
    void hasAnyHours_WithoutHours_ReturnsFalse() {
        // Act & Assert
        assertThat(CategoryHours.empty().hasAnyHours()).isFalse();
    }

    @Test
    @DisplayName("工数追加")
    void addHours_Success() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("6.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act
        CategoryHours updated = categoryHours.addHours(CategoryCode.DEV, new BigDecimal("2.0"));

        // Assert
        assertThat(updated.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("8.0"));
        // 元のオブジェクトは変更されない
        assertThat(categoryHours.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("新しいカテゴリ工数追加")
    void addHours_NewCategory_Success() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("6.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act
        CategoryHours updated = categoryHours.addHours(CategoryCode.MEETING, new BigDecimal("1.5"));

        // Assert
        assertThat(updated.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("6.0"));
        assertThat(updated.getHours(CategoryCode.MEETING)).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(updated.getCategories()).hasSize(2);
    }

    @Test
    @DisplayName("工数設定")
    void setHours_Success() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("6.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);

        // Act
        CategoryHours updated = categoryHours.setHours(CategoryCode.DEV, new BigDecimal("8.0"));

        // Assert
        assertThat(updated.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("8.0"));
        // 元のオブジェクトは変更されない
        assertThat(categoryHours.getHours(CategoryCode.DEV)).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("JSON変換とパース - 正常ケース")
    void toJson_fromJson_RoundTrip() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("6.0"),
            CategoryCode.MEETING, new BigDecimal("1.5")
        );
        CategoryHours original = CategoryHours.of(hoursMap);

        // Act
        String json = original.toJson();
        CategoryHours restored = CategoryHours.fromJson(json);

        // Assert
        assertThat(restored).isEqualTo(original);
        assertThat(json).contains("DEV");
        assertThat(json).contains("6.0");
        assertThat(json).contains("MEETING");
        assertThat(json).contains("1.5");
    }

    @Test
    @DisplayName("空のJSON文字列からのパース")
    void fromJson_EmptyString_ReturnsEmpty() {
        // Act
        CategoryHours categoryHours = CategoryHours.fromJson("");

        // Assert
        assertThat(categoryHours.hours()).isEmpty();
    }

    @Test
    @DisplayName("nullJSON文字列からのパース")
    void fromJson_NullString_ReturnsEmpty() {
        // Act
        CategoryHours categoryHours = CategoryHours.fromJson(null);

        // Assert
        assertThat(categoryHours.hours()).isEmpty();
    }

    @Test
    @DisplayName("等価性判定 - 同じ内容の場合")
    void equals_SameContent_ReturnsTrue() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        CategoryHours hours1 = CategoryHours.of(hoursMap);
        CategoryHours hours2 = CategoryHours.of(hoursMap);

        // Act & Assert
        assertThat(hours1).isEqualTo(hours2);
        assertThat(hours1.hashCode()).isEqualTo(hours2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なる内容の場合")
    void equals_DifferentContent_ReturnsFalse() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap1 = Map.of(
            CategoryCode.DEV, new BigDecimal("8.0")
        );
        Map<CategoryCode, BigDecimal> hoursMap2 = Map.of(
            CategoryCode.DEV, new BigDecimal("6.0")
        );
        CategoryHours hours1 = CategoryHours.of(hoursMap1);
        CategoryHours hours2 = CategoryHours.of(hoursMap2);

        // Act & Assert
        assertThat(hours1).isNotEqualTo(hours2);
    }
}