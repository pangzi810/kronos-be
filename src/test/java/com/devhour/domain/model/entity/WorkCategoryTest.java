package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;

/**
 * WorkCategoryエンティティのユニットテスト
 */
@DisplayName("WorkCategoryエンティティ")
class WorkCategoryTest {

    @Test
    @DisplayName("作業カテゴリ作成 - 正常ケース")
    void create_Success() {
        // Act
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Assert
        assertThat(category.getId()).isNotNull();
        assertThat(category.getCode()).isEqualTo(CategoryCode.of("DEV"));
        assertThat(category.getName()).isEqualTo(CategoryName.of("開発作業"));
        assertThat(category.getDescription()).isEqualTo("プログラミングやコードレビューなどの開発作業");
        assertThat(category.getDisplayOrder()).isEqualTo(DisplayOrder.of(10));
        assertThat(category.getColorCode()).isEqualTo("#3498db");
        assertThat(category.isActive()).isTrue();
    }

    @Test
    @DisplayName("作業カテゴリ作成 - カテゴリコードがnullの場合は例外")
    void create_NullCategoryCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            null,
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カテゴリコードは必須です");
    }

    @Test
    @DisplayName("作業カテゴリ作成 - カテゴリ名がnullの場合は例外")
    void create_NullCategoryName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            null,
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カテゴリ名は必須です");
    }

    @Test
    @DisplayName("作業カテゴリ作成 - 表示順がnullの場合は例外")
    void create_NullDisplayOrder_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            null,
            "#3498db",
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("表示順は必須です");
    }

    @Test
    @DisplayName("作業カテゴリ作成 - カラーコードが不正形式の場合は例外")
    void create_InvalidColorCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "invalid-color",
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("作業カテゴリ作成 - 説明がnullでも正常作成")
    void create_NullDescription_Success() {
        // Act
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            null,
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Assert
        assertThat(category.getDescription()).isNull();
    }

    @Test
    @DisplayName("作業カテゴリ作成 - カラーコードがnullでも正常作成（デフォルト色使用）")
    void create_NullColorCode_Success() {
        // Act
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            null,
            "test-user"
        );

        // Assert
        assertThat(category.getColorCode()).isNull(); // nullが設定される
    }

    @Test
    @DisplayName("カテゴリ情報更新 - 正常ケース")
    void updateCategoryInfo_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act
        category.updateCategoryInfo(
            CategoryName.of("新開発作業"),
            "新しい説明",
            DisplayOrder.of(15),
            "#e74c3c"
        );

        // Assert
        assertThat(category.getName()).isEqualTo(CategoryName.of("新開発作業"));
        assertThat(category.getDescription()).isEqualTo("新しい説明");
        assertThat(category.getDisplayOrder()).isEqualTo(DisplayOrder.of(15));
        assertThat(category.getColorCode()).isEqualTo("#E74C3C");
    }

    @Test
    @DisplayName("カテゴリ情報更新 - 非活性の場合でも更新可能")
    void updateCategoryInfo_InactiveCategory_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );
        category.deactivate();

        // Act & Assert (例外が発生しないことを確認)
        assertThatCode(() -> category.updateCategoryInfo(
            CategoryName.of("新カスタム作業"),
            "新しい説明",
            DisplayOrder.of(15),
            "#e74c3c"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("表示順変更 - 正常ケース")
    void updateDisplayOrder_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act
        category.updateDisplayOrder(DisplayOrder.of(20));

        // Assert
        assertThat(category.getDisplayOrder()).isEqualTo(DisplayOrder.of(20));
    }

    @Test
    @DisplayName("表示順変更 - nullの場合は例外")
    void updateDisplayOrder_NullDisplayOrder_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.updateDisplayOrder(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("表示順は必須です");
    }

    @Test
    @DisplayName("カテゴリ非活性化 - 非システム必須カテゴリ")
    void deactivate_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act
        category.deactivate();

        // Assert
        assertThat(category.isActive()).isFalse();
    }

    @Test
    @DisplayName("カテゴリ再活性化")
    void activate_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );
        category.deactivate();

        // Act
        category.activate();

        // Assert
        assertThat(category.isActive()).isTrue();
    }

    @Test
    @DisplayName("工数記録で使用可能かチェック - 活性の場合")
    void isUsableForWorkRecord_Active_ReturnsTrue() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThat(category.isUsableForWorkRecord()).isTrue();
    }

    @Test
    @DisplayName("工数記録で使用可能かチェック - 非活性の場合")
    void isUsableForWorkRecord_Inactive_ReturnsFalse() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );
        category.deactivate();

        // Act & Assert
        assertThat(category.isUsableForWorkRecord()).isFalse();
    }

    @Test
    @DisplayName("表示順前後判定 - より前の順番")
    void isDisplayedBefore_Earlier_ReturnsTrue() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThat(category.isDisplayedBefore(DisplayOrder.of(20))).isTrue();
        assertThat(category.isDisplayedBefore(DisplayOrder.of(10))).isFalse();
    }

    @Test
    @DisplayName("表示順前後判定 - より後の順番")
    void isDisplayedAfter_Later_ReturnsTrue() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThat(category.isDisplayedAfter(DisplayOrder.of(5))).isTrue();   // 10 > 5
        assertThat(category.isDisplayedAfter(DisplayOrder.of(10))).isFalse(); // 10 == 10
        assertThat(category.isDisplayedAfter(DisplayOrder.of(20))).isFalse(); // 10 < 20
    }

    @Test
    @DisplayName("短縮名取得")
    void getShortName_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThat(category.getShortName(5)).isEqualTo("開発作業");  // 4文字なので5文字以内で省略なし
        assertThat(category.getShortName(3)).isEqualTo("開発…");   // 3文字で切り詰め
    }

    @Test
    @DisplayName("短縮名取得 - 不正な最大文字数")
    void getShortName_InvalidMaxLength_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.getShortName(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("最大文字数は1以上である必要があります");
    }

    @Test
    @DisplayName("有効なカラーコード取得")
    void getEffectiveColorCode_Success() {
        // Arrange
        WorkCategory categoryWithColor = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        WorkCategory categoryWithoutColor = WorkCategory.create(
            CategoryCode.of("MEETING"),
            CategoryName.of("会議"),
            "説明",
            DisplayOrder.of(20),
            null,
            "test-user"
        );

        // Act & Assert
        assertThat(categoryWithColor.getEffectiveColorCode()).isEqualTo("#3498db");
        assertThat(categoryWithoutColor.getEffectiveColorCode()).isEqualTo("#607D8B");
    }

    @Test
    @DisplayName("等価性判定 - 同じIDの場合")
    void equals_SameId_ReturnsTrue() {
        // Arrange
        WorkCategory category1 = WorkCategory.restore(
            "same-id",
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業1"),
            "説明1",
            DisplayOrder.of(10),
            "#3498db",
            true,
            "system",
            java.time.LocalDateTime.now(),
            "system",
            java.time.LocalDateTime.now()
        );

        WorkCategory category2 = WorkCategory.restore(
            "same-id",
            CategoryCode.of("MEETING"), // 他の項目が違っても
            CategoryName.of("会議2"),
            "説明2",
            DisplayOrder.of(20),
            "#e74c3c",
            false,
            "system",
            java.time.LocalDateTime.now(),
            "system",
            java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(category1).isEqualTo(category2);
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なるIDの場合")
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        WorkCategory category1 = WorkCategory.restore(
            "id-1",
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            true,
            "system",
            java.time.LocalDateTime.now(),
            "system",
            java.time.LocalDateTime.now()
        );

        WorkCategory category2 = WorkCategory.restore(
            "id-2",
            CategoryCode.of("DEV"), // 内容が全く同じでも
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            true,
            "system",
            java.time.LocalDateTime.now(),
            "system",
            java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("toString - デバッグ用文字列表現")
    void toString_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act
        String result = category.toString();

        // Assert
        assertThat(result).contains("WorkCategory");
        assertThat(result).contains("DEV");
        assertThat(result).contains("開発作業");
    }

    @Test
    @DisplayName("カラーコード検証 - 空文字の場合は例外")
    void create_EmptyColorCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "",  // 空文字
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カラーコード検証 - スペースのみの場合は例外")
    void create_WhitespaceOnlyColorCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "   ",  // スペースのみ
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カラーコード検証 - #記号なしの場合は例外")
    void create_ColorCodeWithoutHash_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "3498db",  // #記号なし
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カラーコード検証 - 5桁の場合は例外")
    void create_FiveDigitColorCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498d",  // 5桁
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カラーコード検証 - 7桁の場合は例外")
    void create_SevenDigitColorCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498dbb",  // 7桁
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カラーコード検証 - 無効な文字を含む場合は例外")
    void create_ColorCodeWithInvalidChars_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#34G8db",  // Gは無効
            "test-user"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("カテゴリ情報更新 - カテゴリ名がnullの場合は例外")
    void updateCategoryInfo_NullName_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.updateCategoryInfo(
            null,  // null名前
            "新しい説明",
            DisplayOrder.of(15),
            "#e74c3c"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カテゴリ名は必須です");
    }

    @Test
    @DisplayName("カテゴリ情報更新 - 表示順がnullの場合は例外")
    void updateCategoryInfo_NullDisplayOrder_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.updateCategoryInfo(
            CategoryName.of("新カスタム作業"),
            "新しい説明",
            null,  // null表示順
            "#e74c3c"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("表示順は必須です");
    }

    @Test
    @DisplayName("カテゴリ情報更新 - 無効なカラーコードの場合は例外")
    void updateCategoryInfo_InvalidColorCode_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.updateCategoryInfo(
            CategoryName.of("新カスタム作業"),
            "新しい説明",
            DisplayOrder.of(15),
            "invalid-color"  // 無効なカラーコード
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カラーコードは#RRGGBBの形式で入力してください");
    }

    @Test
    @DisplayName("短縮名取得 - 負数の最大文字数は例外")
    void getShortName_NegativeMaxLength_ThrowsException() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "プログラミングやコードレビューなどの開発作業",
            DisplayOrder.of(10),
            "#3498db",
            "test-user"
        );

        // Act & Assert
        assertThatThrownBy(() -> category.getShortName(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("最大文字数は1以上である必要があります");
    }

    @Test
    @DisplayName("作業カテゴリ作成 - 説明が空文字（スペースのみ）でも正常")
    void create_WhitespaceOnlyDescription_Success() {
        // Act
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("CUSTOM"),
            CategoryName.of("カスタム作業"),
            "   ",  // スペースのみ - trimされて空文字になる
            DisplayOrder.of(10),
            "#3498db",
            "user1"  // 作成者
        );

        // Assert
        assertThat(category.getDescription()).isEqualTo("");  // trimされて空文字になる
    }

    @Test
    @DisplayName("カラーコード作成時の保存テスト - 小文字入力はそのまま保存される")
    void create_LowerCaseColorCode_PreservedAsIs() {
        // Act
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498db",  // 小文字
            "user1"  // 作成者
        );

        // Assert - create時は入力値がそのまま保存される
        assertThat(category.getColorCode()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("カテゴリ情報更新 - 説明が空文字（スペースのみ）でも正常")
    void updateCategoryInfo_WhitespaceOnlyDescription_Success() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("CUSTOM"),
            CategoryName.of("カスタム作業"),
            "旧説明",
            DisplayOrder.of(10),
            "#3498db",
            "user1"  // 作成者
        );

        // Act
        category.updateCategoryInfo(
            CategoryName.of("新カスタム作業"),
            "   ",  // スペースのみ - trimされて空文字になる
            DisplayOrder.of(15),
            "#e74c3c"
        );

        // Assert
        assertThat(category.getDescription()).isEqualTo("");  // trimされて空文字になる
    }

    @Test
    @DisplayName("カテゴリ情報更新 - カラーコードが小文字でも大文字に正規化される")
    void updateCategoryInfo_LowerCaseColorCode_NormalizedToUpperCase() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("CUSTOM"),
            CategoryName.of("カスタム作業"),
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            "user1"  // 作成者
        );

        // Act
        category.updateCategoryInfo(
            CategoryName.of("新カスタム作業"),
            "新しい説明",
            DisplayOrder.of(15),
            "#e74c3c"  // 小文字
        );

        // Assert
        assertThat(category.getColorCode()).isEqualTo("#E74C3C");  // 大文字に正規化される
    }

    @Test
    @DisplayName("境界値テスト - 短縮名が最大文字数と同じ場合は省略記号なし")
    void getShortName_ExactMaxLength_NoEllipsis() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発"),  // 2文字
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            "system"  // 作成者
        );

        // Act & Assert
        assertThat(category.getShortName(2)).isEqualTo("開発");  // 省略記号なし
    }

    @Test
    @DisplayName("境界値テスト - 短縮名が最大文字数より1文字多い場合は省略")
    void getShortName_OneLongerThanMaxLength_WithEllipsis() {
        // Arrange
        WorkCategory category = WorkCategory.create(
            CategoryCode.of("DEV"),
            CategoryName.of("開発作業"),  // 4文字
            "説明",
            DisplayOrder.of(10),
            "#3498db",
            "system"  // 作成者
        );

        // Act & Assert
        assertThat(category.getShortName(3)).isEqualTo("開発…");  // 3文字で切り詰め
    }
}