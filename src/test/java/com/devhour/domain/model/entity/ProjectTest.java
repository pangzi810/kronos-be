package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.ProjectStatus;

/**
 * Projectエンティティのユニットテスト
 */
@DisplayName("Projectエンティティ")
class ProjectTest {

    @Test
    @DisplayName("プロジェクト作成 - 正常ケース")
    void create_Success() {
        // Act
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Assert
        assertThat(project.getId()).isNotNull();
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getDescription()).isEqualTo("Test description");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.DRAFT);
        assertThat(project.getCreatedBy()).isEqualTo("manager123");
    }

    @Test
    @DisplayName("プロジェクト作成 - 名前がnullの場合は例外")
    void create_NullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            null,
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は必須です");
    }

    @Test
    @DisplayName("プロジェクト作成 - 名前が空の場合は例外")
    void create_EmptyName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は必須です");
    }

    @Test
    @DisplayName("プロジェクト作成 - 終了日が開始日より前の場合は例外")
    void create_EndDateBeforeStartDate_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            "manager123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("予定終了日は開始日以降の日付を設定してください");
    }

    @Test
    @DisplayName("プロジェクト開始")
    void startProject_Success() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        project.start();

        // Assert
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("プロジェクト完了")
    void completeProject_Success() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );
        project.start();

        // Act
        project.close(LocalDate.now());

        // Assert
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CLOSED);
    }

    @Test
    @DisplayName("プロジェクト情報更新")
    void updateProject_Success() {
        // Arrange
        Project project = Project.create(
            "Old Name",
            "Old description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        project.updateProjectInfo(
            "New Name",
            "New description",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusMonths(7)
        );

        // Assert
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getDescription()).isEqualTo("New description");
    }

    @Test
    @DisplayName("プロジェクト期間内判定 - 期間内の場合はtrue")
    void isDateWithinProjectPeriod_WithinPeriod_ReturnsTrue() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Project project = Project.create(
            "Test Project",
            "Test description",
            startDate,
            endDate,
            "manager123"
        );

        // Act & Assert
        assertThat(project.isDateWithinProjectPeriod(LocalDate.of(2024, 6, 15))).isTrue();
        assertThat(project.isDateWithinProjectPeriod(startDate)).isTrue();
        assertThat(project.isDateWithinProjectPeriod(endDate)).isTrue();
    }

    @Test
    @DisplayName("プロジェクト期間内判定 - 期間外の場合はfalse")
    void isDateWithinProjectPeriod_OutsidePeriod_ReturnsFalse() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Project project = Project.create(
            "Test Project",
            "Test description",
            startDate,
            endDate,
            "manager123"
        );

        // Act & Assert
        assertThat(project.isDateWithinProjectPeriod(LocalDate.of(2023, 12, 31))).isFalse();
        assertThat(project.isDateWithinProjectPeriod(LocalDate.of(2025, 1, 1))).isFalse();
    }

    @Test
    @DisplayName("等価性判定 - 同じIDの場合はtrue")
    void equals_SameId_ReturnsTrue() {
        // Arrange
        Project project1 = Project.restore(
            "same-id",
            "Project 1",
            "Description 1",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null, // actualEndDate
            ProjectStatus.DRAFT,
            "manager123",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay()
        );

        Project project2 = Project.restore(
            "same-id",
            "Project 2", // 名前が違っても
            "Description 2", // 説明が違っても
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null, // actualEndDate
            ProjectStatus.IN_PROGRESS, // ステータスが違っても
            "manager456", // マネージャーが違っても
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay()
        );

        // Act & Assert
        assertThat(project1).isEqualTo(project2);
        assertThat(project1.hashCode()).isEqualTo(project2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なるIDの場合はfalse")
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        Project project1 = Project.restore(
            "id-1",
            "Same Project",
            "Same Description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null, // actualEndDate
            ProjectStatus.DRAFT,
            "manager123",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay()
        );

        Project project2 = Project.restore(
            "id-2",
            "Same Project", // 内容が全く同じでも
            "Same Description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null, // actualEndDate
            ProjectStatus.DRAFT,
            "manager123",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay()
        );

        // Act & Assert
        assertThat(project1).isNotEqualTo(project2);
    }

    @Test
    @DisplayName("プロジェクト作成 - プロジェクト名が256文字の場合は例外")
    void create_TooLongName_ThrowsException() {
        // 256文字の名前
        String longName = "a".repeat(256);
        
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            longName,
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は255文字以内で入力してください");
    }

    @Test
    @DisplayName("プロジェクト作成 - プロジェクト名が空文字（スペースのみ）の場合は例外")
    void create_WhitespaceOnlyName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "   ",  // スペースのみ
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は必須です");
    }

    @Test
    @DisplayName("プロジェクト作成 - 作成者IDが空文字（スペースのみ）の場合は例外")
    void create_WhitespaceOnlyCreatedBy_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "   "  // スペースのみ
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("作成者IDは必須です");
    }

    @Test
    @DisplayName("プロジェクト更新 - 完了状態での更新は例外")
    void updateProjectInfo_CompletedProject_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        );
        project.start();
        project.close(LocalDate.now().plusDays(10));

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "新しい名前",
            "新しい説明",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusMonths(4)
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("完了または中止されたプロジェクトは更新できません");
    }

    @Test
    @DisplayName("プロジェクト更新 - 256文字のプロジェクト名は例外")
    void updateProjectInfo_TooLongName_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        );
        
        // 256文字の名前
        String longName = "a".repeat(256);

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            longName,
            "新しい説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(4)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は255文字以内で入力してください");
    }

    @Test
    @DisplayName("プロジェクト更新 - 空文字（スペースのみ）のプロジェクト名は例外")
    void updateProjectInfo_WhitespaceOnlyName_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "   ",  // スペースのみ
            "新しい説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(4)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は必須です");
    }

    @Test
    @DisplayName("プロジェクト開始 - PLANNING状態以外では例外")
    void start_NonPlanningStatus_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        );
        project.start(); // 既に開始済み（IN_PROGRESS状態になる）

        // Act & Assert
        assertThatThrownBy(() -> project.start())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("プロジェクトを開始できません");
    }

    @Test
    @DisplayName("プロジェクト完了 - 進行中状態以外では例外")
    void complete_NonInProgressStatus_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            "user123"
        );
        // PLANNING状態のまま完了しようとする

        // Act & Assert
        assertThatThrownBy(() -> project.close(LocalDate.now().plusDays(10)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("プロジェクトを完了できません");
    }

    @Test
    @DisplayName("プロジェクト完了 - 完了日が開始日より前の場合は例外")
    void complete_ActualEndDateBeforeStartDate_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "テストプロジェクト",
            "説明",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusMonths(3),
            "user123"
        );
        project.start();

        // Act & Assert
        assertThatThrownBy(() -> project.close(LocalDate.now().plusDays(5)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("実際の終了日は開始日以降の日付を設定してください");
    }

    @Test
    @DisplayName("toString() メソッドのテスト")
    void toString_ReturnsFormattedString() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        String result = project.toString();

        // Assert
        assertThat(result).contains("Project{");
        assertThat(result).contains("id=");
        assertThat(result).contains("name='Test Project'");
        assertThat(result).contains("status=PLANNING");
    }

    @Test
    @DisplayName("プロジェクト作成 - 開始日がnullの場合は例外")
    void create_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            null,
            LocalDate.now().plusMonths(6),
            "manager123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("開始日は必須です");
    }

    @Test
    @DisplayName("プロジェクト作成 - 終了日がnullの場合は例外")
    void create_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            null,
            "manager123"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("予定終了日は必須です");
    }

    @Test
    @DisplayName("プロジェクト更新 - 開始日がnullの場合は例外")
    void updateProjectInfo_NullStartDate_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "New Name",
            "New description",
            null,
            LocalDate.now().plusMonths(7)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("開始日は必須です");
    }

    @Test
    @DisplayName("プロジェクト更新 - 終了日がnullの場合は例外")
    void updateProjectInfo_NullEndDate_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "New Name",
            "New description",
            LocalDate.now(),
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("予定終了日は必須です");
    }

    @Test
    @DisplayName("プロジェクト更新 - 終了日が開始日より前の場合は例外")
    void updateProjectInfo_EndDateBeforeStartDate_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "New Name",
            "New description",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(5)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("予定終了日は開始日以降の日付を設定してください");
    }

    @Test
    @DisplayName("プロジェクト完了 - 実際の終了日がnullの場合は例外")
    void complete_NullActualEndDate_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );
        project.start();

        // Act & Assert
        assertThatThrownBy(() -> project.close(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("実際の終了日は必須です");
    }

    @Test
    @DisplayName("isDateWithinProjectPeriod - 日付がnullの場合はfalse")
    void isDateWithinProjectPeriod_NullDate_ReturnsFalse() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThat(project.isDateWithinProjectPeriod(null)).isFalse();
    }

    @Test
    @DisplayName("updateProjectInfo - descriptionがnullの場合はnullを設定")
    void updateProjectInfo_NullDescription_SetsNull() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Initial description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        project.updateProjectInfo(
            "New Name",
            null,
            LocalDate.now(),
            LocalDate.now().plusMonths(7)
        );

        // Assert
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getDescription()).isNull();
    }

    @Test
    @DisplayName("updateProjectInfo - descriptionの前後の空白をトリム")
    void updateProjectInfo_DescriptionWithSpaces_Trims() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Initial description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        project.updateProjectInfo(
            "New Name",
            "  New description with spaces  ",
            LocalDate.now(),
            LocalDate.now().plusMonths(7)
        );

        // Assert
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getDescription()).isEqualTo("New description with spaces");
    }

    @Test
    @DisplayName("isDateWithinProjectPeriod - 実際の終了日が設定されている場合はそれを使用")
    void isDateWithinProjectPeriod_WithActualEndDate_UsesActualEndDate() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate plannedEndDate = LocalDate.of(2024, 6, 30);
        LocalDate actualEndDate = LocalDate.of(2024, 5, 31); // 予定より早く終了
        
        Project project = Project.restore(
            "test-id",
            "Test Project",
            "Test description",
            startDate,
            plannedEndDate,
            actualEndDate,
            ProjectStatus.CLOSED,
            "manager123",
            startDate.atStartOfDay(),
            LocalDate.now().atStartOfDay()
        );

        // Act & Assert
        // 実際の終了日（5/31）以前はtrue
        assertThat(project.isDateWithinProjectPeriod(LocalDate.of(2024, 5, 15))).isTrue();
        assertThat(project.isDateWithinProjectPeriod(actualEndDate)).isTrue();
        
        // 実際の終了日（5/31）より後はfalse（予定終了日6/30より前でも）
        assertThat(project.isDateWithinProjectPeriod(LocalDate.of(2024, 6, 15))).isFalse();
    }

    @Test
    @DisplayName("equals - nullとの比較はfalse")
    void equals_WithNull_ReturnsFalse() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThat(project.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equals - 異なるクラスのオブジェクトとの比較はfalse")
    void equals_WithDifferentClass_ReturnsFalse() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThat(project.equals("String Object")).isFalse();
        assertThat(project.equals(123)).isFalse();
    }

    @Test
    @DisplayName("プロジェクト作成 - createdByが空白のみの場合は例外")
    void create_CreatedByOnlySpaces_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "   " // 空白のみ
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("作成者IDは必須です");
    }

    // ========== JIRA統合機能テスト ==========

    @Test
    @DisplayName("プロジェクト作成 - JIRAイシューキー付きで作成")
    void createWithJiraIssueKey_Success() {
        // Act
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );

        // Assert
        assertThat(project.getId()).isNotNull();
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getJiraIssueKey()).isEqualTo("PROJ-123");
        assertThat(project.hasJiraIntegration()).isTrue();
        assertThat(project.isJiraProject()).isTrue();
    }

    @Test
    @DisplayName("プロジェクト作成 - JIRAイシューキーなしで作成")
    void createWithoutJiraIssueKey_Success() {
        // Act
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            null,
            null
        );

        // Assert
        assertThat(project.getId()).isNotNull();
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getJiraIssueKey()).isNull();
        assertThat(project.hasJiraIntegration()).isFalse();
        assertThat(project.isJiraProject()).isFalse();
    }

    @Test
    @DisplayName("プロジェクト作成 - 不正なJIRAイシューキー形式で例外")
    void createWithInvalidJiraIssueKey_ThrowsException() {
        // Act & Assert - 小文字が含まれる場合
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "proj-123",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("JIRAイシューキーの形式が正しくありません");

        // Act & Assert - 0から始まる番号
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-0",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("JIRAイシューキーの形式が正しくありません");

        // Act & Assert - ハイフンなし
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ123",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("JIRAイシューキーの形式が正しくありません");

        // Act & Assert - 空文字
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("JIRAイシューキーの形式が正しくありません");

        // Act & Assert - 空白のみ
        assertThatThrownBy(() -> Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "  ",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("JIRAイシューキーの形式が正しくありません");
    }

    @Test
    @DisplayName("プロジェクト復元 - JIRAイシューキー付きで復元")
    void restoreWithJiraIssueKey_Success() {
        // Act
        Project project = Project.restore(
            "test-id",
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null,
            ProjectStatus.DRAFT,
            "manager123",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay(),
            "DEVHOUR-456",
            null
        );

        // Assert
        assertThat(project.getId()).isEqualTo("test-id");
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getJiraIssueKey()).isEqualTo("DEVHOUR-456");
        assertThat(project.hasJiraIntegration()).isTrue();
    }

    @Test
    @DisplayName("プロジェクト復元 - JIRAイシューキーなしで復元")
    void restoreWithoutJiraIssueKey_Success() {
        // Act
        Project project = Project.restore(
            "test-id",
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            null,
            ProjectStatus.DRAFT,
            "manager123",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atStartOfDay(),
            null,
            null
        );

        // Assert
        assertThat(project.getId()).isEqualTo("test-id");
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getJiraIssueKey()).isNull();
        assertThat(project.hasJiraIntegration()).isFalse();
    }

    @Test
    @DisplayName("JIRAイシューキー割り当て - 正常ケース")
    void assignJiraIssueKey_Success() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        project.assignJiraIssueKey("PROJ-789");

        // Assert
        assertThat(project.getJiraIssueKey()).isEqualTo("PROJ-789");
        assertThat(project.hasJiraIntegration()).isTrue();
        assertThat(project.isJiraProject()).isTrue();
    }

    @Test
    @DisplayName("JIRAイシューキー割り当て - 不正形式で例外")
    void assignJiraIssueKey_InvalidFormat_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.assignJiraIssueKey("invalid-key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JIRAイシューキーの形式が正しくありません");
    }

    @Test
    @DisplayName("JIRAイシューキー割り当て - nullで例外")
    void assignJiraIssueKey_NullKey_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.assignJiraIssueKey(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JIRAイシューキーは必須です");
    }

    @Test
    @DisplayName("JIRA統合解除")
    void removeJiraIntegration_Success() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );

        // Act
        project.removeJiraIntegration();

        // Assert
        assertThat(project.getJiraIssueKey()).isNull();
        assertThat(project.hasJiraIntegration()).isFalse();
        assertThat(project.isJiraProject()).isFalse();
    }

    @Test
    @DisplayName("JIRAからの情報更新 - 正常ケース")
    void updateFromJira_Success() {
        // Arrange
        Project project = Project.create(
            "Old Name",
            "Old description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );

        // Act
        project.updateFromJira(
            "New JIRA Name",
            "New JIRA description",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusMonths(7),
            ProjectStatus.IN_PROGRESS,
            null
        );

        // Assert
        assertThat(project.getName()).isEqualTo("New JIRA Name");
        assertThat(project.getDescription()).isEqualTo("New JIRA description");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(project.getPlannedEndDate()).isEqualTo(LocalDate.now().plusMonths(7));
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(project.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("JIRAからの情報更新 - JIRA統合されていないプロジェクトで例外")
    void updateFromJira_NonJiraProject_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateFromJira(
            "New Name",
            "New description",
            LocalDate.now(),
            LocalDate.now().plusMonths(7),
            ProjectStatus.IN_PROGRESS,
            null
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("JIRA統合されていないプロジェクトは、JIRAからの情報更新はできません");
    }

    @Test
    @DisplayName("JIRAからの情報更新 - 完了状態のプロジェクトで例外")
    void updateFromJira_CompletedProject_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );
        project.start();
        project.close(LocalDate.now());

        // Act & Assert
        assertThatThrownBy(() -> project.updateFromJira(
            "New Name",
            "New description",
            LocalDate.now(),
            LocalDate.now().plusMonths(7),
            ProjectStatus.IN_PROGRESS,
            null
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("完了または中止されたプロジェクトは、JIRAからの情報更新はできません");
    }

    @Test
    @DisplayName("同一JIRAイシュー判定 - プロジェクトオブジェクト同士")
    void hasSameJiraIssue_WithProject_Success() {
        // Arrange
        Project project1 = Project.create(
            "Project 1",
            "Description 1",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );
        
        Project project2 = Project.create(
            "Project 2",
            "Description 2",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager456",
            "PROJ-123",
            null
        );
        
        Project project3 = Project.create(
            "Project 3",
            "Description 3",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager789",
            "PROJ-456",
            null
        );
        
        Project projectWithoutJira = Project.create(
            "Project without JIRA",
            "Description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager000"
        );

        // Act & Assert
        assertThat(project1.hasSameJiraIssue(project2)).isTrue();
        assertThat(project1.hasSameJiraIssue(project3)).isFalse();
        assertThat(project1.hasSameJiraIssue(projectWithoutJira)).isFalse();
        assertThat(projectWithoutJira.hasSameJiraIssue(project1)).isFalse();
    }

    @Test
    @DisplayName("同一JIRAイシュー判定 - イシューキー文字列")
    void hasSameJiraIssue_WithString_Success() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );
        
        Project projectWithoutJira = Project.create(
            "Project without JIRA",
            "Description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager000"
        );

        // Act & Assert
        assertThat(project.hasSameJiraIssue("PROJ-123")).isTrue();
        assertThat(project.hasSameJiraIssue("PROJ-456")).isFalse();
        assertThat(project.hasSameJiraIssue((String) null)).isFalse();
        assertThat(projectWithoutJira.hasSameJiraIssue("PROJ-123")).isFalse();
        assertThat(projectWithoutJira.hasSameJiraIssue((String) null)).isTrue();
    }

    @Test
    @DisplayName("有効なJIRAイシューキー形式のテスト")
    void jiraIssueKeyValidation_ValidFormats() {
        // 有効な形式でプロジェクト作成が成功することを確認
        assertThatCode(() -> Project.create(
            "Test Project", "Description", LocalDate.now(), LocalDate.now().plusMonths(6), 
            "manager123", "PROJ-123",
            null
        )).doesNotThrowAnyException();

        assertThatCode(() -> Project.create(
            "Test Project", "Description", LocalDate.now(), LocalDate.now().plusMonths(6),
            "manager123", "DEVHOUR-456", null
        )).doesNotThrowAnyException();

        assertThatCode(() -> Project.create(
            "Test Project", "Description", LocalDate.now(), LocalDate.now().plusMonths(6),
            "manager123", "TEST_PROJECT-789", null
        )).doesNotThrowAnyException();

        assertThatCode(() -> Project.create(
            "Test Project", "Description", LocalDate.now(), LocalDate.now().plusMonths(6),
            "manager123", "A1B2C3-999", null
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("toString() メソッド - JIRAイシューキー付き")
    void toString_WithJiraIssueKey_ReturnsFormattedString() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123",
            "PROJ-123",
            null
        );

        // Act
        String result = project.toString();

        // Assert
        assertThat(result).contains("Project{");
        assertThat(result).contains("id=");
        assertThat(result).contains("name='Test Project'");
        assertThat(result).contains("status=PLANNING");
        assertThat(result).contains("jiraIssueKey='PROJ-123'");
    }

    @Test
    @DisplayName("toString() メソッド - JIRAイシューキーなし")
    void toString_WithoutJiraIssueKey_ReturnsFormattedString() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act
        String result = project.toString();

        // Assert
        assertThat(result).contains("Project{");
        assertThat(result).contains("id=");
        assertThat(result).contains("name='Test Project'");
        assertThat(result).contains("status=PLANNING");
        assertThat(result).contains("jiraIssueKey=null");
    }

    @Test
    @DisplayName("プロジェクト更新 - nameが空白のみの場合は例外")
    void updateProjectInfo_NameOnlySpaces_ThrowsException() {
        // Arrange
        Project project = Project.create(
            "Test Project",
            "Test description",
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "manager123"
        );

        // Act & Assert
        assertThatThrownBy(() -> project.updateProjectInfo(
            "   ", // 空白のみ
            "New description",
            LocalDate.now(),
            LocalDate.now().plusMonths(7)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("プロジェクト名は必須です");
    }

}