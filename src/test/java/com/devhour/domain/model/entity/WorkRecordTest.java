package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;

/**
 * WorkRecordエンティティのユニットテスト
 */
@DisplayName("WorkRecordエンティティ")
class WorkRecordTest {

    @Test
    @DisplayName("工数記録作成 - 正常ケース")
    void create_Success() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );

        // Act
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Assert
        assertThat(record.getId()).isNotNull();
        assertThat(record.getUserId()).isEqualTo("user123");
        assertThat(record.getProjectId()).isEqualTo("project123");
        assertThat(record.getCategoryHours()).isEqualTo(categoryHours);
        assertThat(record.getDescription()).isEqualTo("開発作業");
    }

    @Test
    @DisplayName("工数記録作成 - ユーザーIDがnullの場合は例外")
    void create_NullUserId_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );

        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            null,
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザーIDは必須です");
    }

    @Test
    @DisplayName("工数記録作成 - 作業日が未来の場合は例外")
    void create_FutureWorkDate_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );

        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().plusDays(1),
            categoryHours,
            "開発作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("作業日は当日以前の日付を入力してください");
    }

    @Test
    @DisplayName("工数記録作成 - 作業日が90日以前の場合は例外")
    void create_TooOldWorkDate_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );

        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(91),
            categoryHours,
            "開発作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("作業日は過去90日以内の日付を入力してください");
    }

    @Test
    @DisplayName("工数記録作成 - カテゴリ別工数がnullの場合は例外")
    void create_NullCategoryHours_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            null,
            "開発作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カテゴリ別工数は必須です");
    }

    @Test
    @DisplayName("工数記録更新 - 正常ケース")
    void updateWorkRecord_Success() {
        // Arrange
        CategoryHours originalHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            originalHours,
            "開発作業",
            "system"
        );

        CategoryHours newHours = CategoryHours.of(
            Map.of(
                CategoryCode.DEV, new BigDecimal("6.0"),
                CategoryCode.MEETING, new BigDecimal("2.0")
            )
        );

        // Act
        record.updateWorkRecord(newHours, "開発作業とミーティング", "system");

        // Assert
        assertThat(record.getCategoryHours()).isEqualTo(newHours);
        assertThat(record.getDescription()).isEqualTo("開発作業とミーティング");
    }



    @Test
    @DisplayName("特定カテゴリの工数取得 - 存在するカテゴリ")
    void getHoursForCategory_ExistingCategory_ReturnsHours() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act
        BigDecimal hours = record.getCategoryHours().getHours(CategoryCode.DEV);

        // Assert
        assertThat(hours).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("特定カテゴリの工数取得 - 存在しないカテゴリ")
    void getHoursForCategory_NonExistingCategory_ReturnsZero() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act
        BigDecimal hours = record.getCategoryHours().getHours(CategoryCode.MEETING);

        // Assert
        assertThat(hours).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("カテゴリ工数存在チェック - 存在する場合")
    void hasHoursForCategory_ExistingCategory_ReturnsTrue() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act & Assert
        assertThat(record.getCategoryHours().hasHours(CategoryCode.DEV)).isTrue();
    }

    @Test
    @DisplayName("カテゴリ工数存在チェック - 存在しない場合")
    void hasHoursForCategory_NonExistingCategory_ReturnsFalse() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act & Assert
        assertThat(record.getCategoryHours().hasHours(CategoryCode.MEETING)).isFalse();
    }

    @Test
    @DisplayName("同日作業記録チェック - 同じ日付・ユーザー・プロジェクト")
    void isSameDayRecord_SameUserProjectDate_ReturnsTrue() {
        // Arrange
        LocalDate workDate = LocalDate.now().minusDays(1);
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        
        WorkRecord record1 = WorkRecord.create(
            "user123",
            "project123",
            workDate,
            categoryHours,
            "開発作業1",
            "system"
        );

        WorkRecord record2 = WorkRecord.create(
            "user123",
            "project123", 
            workDate,
            categoryHours,
            "開発作業2",
            "system"
        );

        // Act & Assert
        assertThat(record1.belongsToUser("user123")).isTrue();
        assertThat(record1.belongsToProject("project123")).isTrue();
        assertThat(record1.isRecordForDate(workDate)).isTrue();
        assertThat(record2.belongsToUser("user123")).isTrue();
        assertThat(record2.belongsToProject("project123")).isTrue();
        assertThat(record2.isRecordForDate(workDate)).isTrue();
    }

    @Test
    @DisplayName("同日作業記録チェック - 異なるユーザー")
    void isSameDayRecord_DifferentUser_ReturnsFalse() {
        // Arrange
        LocalDate workDate = LocalDate.now().minusDays(1);
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );
        
        WorkRecord record1 = WorkRecord.create(
            "user123",
            "project123",
            workDate,
            categoryHours,
            "開発作業",
            "system"
        );

        WorkRecord record2 = WorkRecord.create(
            "user456", // 異なるユーザー
            "project123",
            workDate,
            categoryHours,
            "開発作業",
            "system"
        );

        // Act & Assert
        assertThat(record1.belongsToUser("user123")).isTrue();
        assertThat(record2.belongsToUser("user456")).isTrue();
        assertThat(record1.belongsToUser("user456")).isFalse();
        assertThat(record2.belongsToUser("user123")).isFalse();
    }

    @Test
    @DisplayName("等価性判定 - 同じIDの場合")
    void equals_SameId_ReturnsTrue() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("6.0"))
        );

        WorkRecord record1 = WorkRecord.restore(
            "same-id",
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業1",
            "system",
            LocalDate.now().minusDays(1).atStartOfDay(),
            "system",
            LocalDate.now().minusDays(1).atStartOfDay()
        );

        WorkRecord record2 = WorkRecord.restore(
            "same-id",
            "user456", // 他の項目が違っても
            "project456",
            LocalDate.now().minusDays(2),
            CategoryHours.empty(),
            "開発作業2",
            "system",
            LocalDate.now().minusDays(2).atStartOfDay(),
            "system",
            LocalDate.now().minusDays(2).atStartOfDay()
        );

        // Act & Assert
        assertThat(record1).isEqualTo(record2);
        assertThat(record1.hashCode()).isEqualTo(record2.hashCode());
    }

    @Test
    @DisplayName("工数記録作成 - ユーザーIDが空文字（スペースのみ）の場合は例外")
    void create_WhitespaceOnlyUserId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "   ",  // スペースのみ
            "project123",
            LocalDate.now().minusDays(1),
            CategoryHours.of(Map.of(CategoryCode.DEV, new BigDecimal("8.0"))),
            "テスト作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ユーザーIDは必須です");
    }

    @Test
    @DisplayName("工数記録作成 - プロジェクトIDが空文字（スペースのみ）の場合は例外")
    void create_WhitespaceOnlyProjectId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "   ",  // スペースのみ
            LocalDate.now().minusDays(1),
            CategoryHours.of(Map.of(CategoryCode.DEV, new BigDecimal("8.0"))),
            "テスト作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクトIDは必須です");
    }

    @Test
    @DisplayName("工数記録作成 - 全カテゴリが0時間の場合は例外")
    void create_NoHours_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            CategoryHours.empty(),  // 工数なし
            "テスト作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("少なくとも1つのカテゴリに工数を入力してください");
    }


    @Test
    @DisplayName("工数記録更新 - 全カテゴリが0時間の場合は例外")
    void updateWorkRecord_NoHours_ThrowsException() {
        // Arrange
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            CategoryHours.of(Map.of(CategoryCode.DEV, new BigDecimal("8.0"))),
            "テスト作業",
            "system"
        );

        // Act & Assert
        assertThatThrownBy(() -> record.updateWorkRecord(
            CategoryHours.empty(),  // 工数なし
            "更新された作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("少なくとも1つのカテゴリに工数を入力してください");
    }


    @Test
    @DisplayName("作業日境界値テスト - 当日は作成可能")
    void create_TodayWorkDate_Success() {
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now(),  // 当日
            CategoryHours.of(Map.of(CategoryCode.DEV, new BigDecimal("8.0"))),
            "テスト作業",
            "system"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("作業日境界値テスト - 90日前は作成可能")
    void create_ExactlyNinetyDaysAgo_Success() {
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(90),  // ちょうど90日前
            CategoryHours.of(Map.of(CategoryCode.DEV, new BigDecimal("8.0"))),
            "テスト作業",
            "system"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("合計工数境界値テスト - ちょうど24時間は作成可能")
    void create_Exactly24Hours_Success() {
        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            CategoryHours.of(Map.of(
                CategoryCode.DEV, new BigDecimal("16.0"),
                CategoryCode.MEETING, new BigDecimal("8.0")  // 合計24.0時間
            )),
            "テスト作業",
            "system"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("toString() メソッドのテスト")
    void toString_ReturnsFormattedString() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act
        String result = record.toString();

        // Assert
        assertThat(result).contains("WorkRecord{");
        assertThat(result).contains("id=");
        assertThat(result).contains("userId='user123'");
        assertThat(result).contains("projectId='project123'");
        assertThat(result).contains("workDate=");
    }


    @Test
    @DisplayName("工数記録作成 - 作業日がnullの場合は例外")
    void create_NullWorkDate_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );

        // Act & Assert
        assertThatThrownBy(() -> WorkRecord.create(
            "user123",
            "project123",
            null,
            categoryHours,
            "開発作業",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("作業日は必須です");
    }

    @Test
    @DisplayName("工数記録更新 - カテゴリ別工数がnullの場合は例外")
    void updateWorkRecord_NullCategoryHours_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );
        WorkRecord record = WorkRecord.create(
            "user123",
            "project123",
            LocalDate.now().minusDays(1),
            categoryHours,
            "開発作業",
            "system"
        );

        // Act & Assert
        assertThatThrownBy(() -> record.updateWorkRecord(
            null,
            "更新説明",
            "system"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("カテゴリ別工数は必須です");
    }

    @Test
    @DisplayName("工数記録更新 - 30日以上前の記録は更新不可")
    void updateWorkRecord_OldRecord_ThrowsException() {
        // Arrange
        CategoryHours categoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("8.0"))
        );
        WorkRecord record = WorkRecord.restore(
            "test-id",
            "user123",
            "project123",
            LocalDate.now().minusDays(35),
            categoryHours,
            "古い記録",
            "system",
            LocalDate.now().minusDays(35).atStartOfDay(),
            "system",
            LocalDate.now().minusDays(35).atStartOfDay()
        );

        CategoryHours newCategoryHours = CategoryHours.of(
            Map.of(CategoryCode.DEV, new BigDecimal("7.0"))
        );

        // Act & Assert
        assertThatThrownBy(() -> record.updateWorkRecord(
            newCategoryHours,
            "更新試行",
            "system"
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("工数記録は作成から30日以内のみ更新可能です");
    }
}