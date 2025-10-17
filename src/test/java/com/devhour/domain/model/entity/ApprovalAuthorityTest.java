package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devhour.domain.model.valueobject.Position;

/**
 * 承認権限エンティティテスト
 */
@DisplayName("承認権限エンティティ")
class ApprovalAuthorityTest {

    @Test
    @DisplayName("承認権限作成 - 正常ケース（マネージャー）")
    void create_Manager_Success() {
        // Act
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        );

        // Assert
        assertThat(authority.getId()).isNotNull();
        assertThat(authority.getEmail()).isEqualTo("manager@example.com");
        assertThat(authority.getName()).isEqualTo("山田マネージャー");
        assertThat(authority.getPosition()).isEqualTo(Position.MANAGER);
        assertThat(authority.getLevel1Code()).isEqualTo("DEPT001");
        assertThat(authority.getLevel1Name()).isEqualTo("開発部");
        assertThat(authority.getCreatedAt()).isNotNull();
        assertThat(authority.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("承認権限作成 - 正常ケース（統括本部長）")
    void create_GeneralManager_Success() {
        // Act
        ApprovalAuthority authority = ApprovalAuthority.create(
            "gm@example.com",
            "田中統括本部長",
            Position.GENERAL_MANAGER,
            "CORP001", "本社",
            "DIV001", "技術統括本部",
            "DEPT001", "開発部",
            "SEC001", "バックエンド課"
        );

        // Assert
        assertThat(authority.getId()).isNotNull();
        assertThat(authority.getEmail()).isEqualTo("gm@example.com");
        assertThat(authority.getName()).isEqualTo("田中統括本部長");
        assertThat(authority.getPosition()).isEqualTo(Position.GENERAL_MANAGER);
        assertThat(authority.getLevel1Code()).isEqualTo("CORP001");
        assertThat(authority.getLevel1Name()).isEqualTo("本社");
        assertThat(authority.getLevel2Code()).isEqualTo("DIV001");
        assertThat(authority.getLevel2Name()).isEqualTo("技術統括本部");
        assertThat(authority.getLevel3Code()).isEqualTo("DEPT001");
        assertThat(authority.getLevel3Name()).isEqualTo("開発部");
        assertThat(authority.getLevel4Code()).isEqualTo("SEC001");
        assertThat(authority.getLevel4Name()).isEqualTo("バックエンド課");
    }

    @Test
    @DisplayName("承認権限作成 - emailがnullの場合は例外")
    void create_NullEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            null,
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは必須です");
    }

    @Test
    @DisplayName("承認権限作成 - emailが空文字の場合は例外")
    void create_EmptyEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "",
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは必須です");
    }

    @Test
    @DisplayName("承認権限作成 - emailが不正形式の場合は例外")
    void create_InvalidEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "invalid-email",
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスの形式が正しくありません");
    }

    @Test
    @DisplayName("承認権限作成 - nameがnullの場合は例外")
    void create_NullName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            null,
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("名前は必須です");
    }

    @Test
    @DisplayName("承認権限作成 - nameが空文字の場合は例外")
    void create_EmptyName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "",
            Position.MANAGER,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("名前は必須です");
    }

    @Test
    @DisplayName("承認権限作成 - positionがnullの場合は例外")
    void create_NullPosition_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            null,
            "DEPT001",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("役職は必須です");
    }

    @Test
    @DisplayName("承認権限作成 - level1Codeがnullの場合は例外")
    void create_NullLevel1Code_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            Position.MANAGER,
            null,
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Level1組織コードは必須です");
    }

    @Test
    @DisplayName("承認権限作成 - level1Nameがnullの場合は例外")
    void create_NullLevel1Name_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            null,
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Level1組織名は必須です");
    }

    @Test
    @DisplayName("hasApprovalAuthority - マネージャー以上は承認権限あり")
    void hasApprovalAuthority_ManagerOrAbove_ReturnsTrue() {
        // Arrange
        ApprovalAuthority manager = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority deptManager = ApprovalAuthority.create(
            "dept@example.com", "部長", Position.DEPARTMENT_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority divManager = ApprovalAuthority.create(
            "div@example.com", "本部長", Position.DIVISION_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority genManager = ApprovalAuthority.create(
            "gen@example.com", "統括本部長", Position.GENERAL_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(manager.hasApprovalAuthority()).isTrue();
        assertThat(deptManager.hasApprovalAuthority()).isTrue();
        assertThat(divManager.hasApprovalAuthority()).isTrue();
        assertThat(genManager.hasApprovalAuthority()).isTrue();
    }

    @Test
    @DisplayName("hasApprovalAuthority - 一般社員は承認権限なし")
    void hasApprovalAuthority_Employee_ReturnsFalse() {
        // Arrange
        ApprovalAuthority employee = ApprovalAuthority.create(
            "employee@example.com", "一般社員", Position.EMPLOYEE,
            "DEPT001", "開発部", null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(employee.hasApprovalAuthority()).isFalse();
    }

    @Test
    @DisplayName("getHierarchyLevel - 役職別の階層レベル")
    void getHierarchyLevel_ByPosition_ReturnsCorrectLevel() {
        // Arrange
        ApprovalAuthority employee = ApprovalAuthority.create(
            "employee@example.com", "一般社員", Position.EMPLOYEE,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority manager = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority deptManager = ApprovalAuthority.create(
            "dept@example.com", "部長", Position.DEPARTMENT_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority divManager = ApprovalAuthority.create(
            "div@example.com", "本部長", Position.DIVISION_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );
        ApprovalAuthority genManager = ApprovalAuthority.create(
            "gen@example.com", "統括本部長", Position.GENERAL_MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(employee.getHierarchyLevel()).isEqualTo(0);
        assertThat(manager.getHierarchyLevel()).isEqualTo(1);
        assertThat(deptManager.getHierarchyLevel()).isEqualTo(2);
        assertThat(divManager.getHierarchyLevel()).isEqualTo(3);
        assertThat(genManager.getHierarchyLevel()).isEqualTo(4);
    }

    @Test
    @DisplayName("getOrganizationPath - 組織名のパス生成")
    void getOrganizationPath_WithMultipleLevels_ReturnsCorrectPath() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "gm@example.com", "田中統括本部長", Position.GENERAL_MANAGER,
            "CORP001", "本社",
            "DIV001", "技術統括本部",
            "DEPT001", "開発部",
            "SEC001", "バックエンド課"
        );

        // Act
        String path = authority.getOrganizationPath();

        // Assert
        assertThat(path).isEqualTo("本社 > 技術統括本部 > 開発部 > バックエンド課");
    }

    @Test
    @DisplayName("getOrganizationPath - Level1のみの場合")
    void getOrganizationPath_Level1Only_ReturnsLevel1Name() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act
        String path = authority.getOrganizationPath();

        // Assert
        assertThat(path).isEqualTo("開発部");
    }

    @Test
    @DisplayName("getOrganizationCodePath - 組織コードのパス生成")
    void getOrganizationCodePath_WithMultipleLevels_ReturnsCorrectPath() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "gm@example.com", "田中統括本部長", Position.GENERAL_MANAGER,
            "CORP001", "本社",
            "DIV001", "技術統括本部",
            "DEPT001", "開発部",
            "SEC001", "バックエンド課"
        );

        // Act
        String path = authority.getOrganizationCodePath();

        // Assert
        assertThat(path).isEqualTo("CORP001 > DIV001 > DEPT001 > SEC001");
    }

    @Test
    @DisplayName("getOrganizationCodePath - Level1のみの場合")
    void getOrganizationCodePath_Level1Only_ReturnsLevel1Code() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act
        String path = authority.getOrganizationCodePath();

        // Assert
        assertThat(path).isEqualTo("DEPT001");
    }

    @Test
    @DisplayName("updateInfo - 正常ケース")
    void updateInfo_Success() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "old@example.com", "旧名前", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act
        authority.updateInfo(
            "new@example.com", "新名前", Position.DEPARTMENT_MANAGER,
            "CORP001", "本社",
            "DIV001", "技術統括本部", 
            null, null, null, null
        );

        // Assert
        assertThat(authority.getEmail()).isEqualTo("new@example.com");
        assertThat(authority.getName()).isEqualTo("新名前");
        assertThat(authority.getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
        assertThat(authority.getLevel1Code()).isEqualTo("CORP001");
        assertThat(authority.getLevel1Name()).isEqualTo("本社");
        assertThat(authority.getLevel2Code()).isEqualTo("DIV001");
        assertThat(authority.getLevel2Name()).isEqualTo("技術統括本部");
    }

    @Test
    @DisplayName("等価性判定 - 同じIDの場合")
    void equals_SameId_ReturnsTrue() {
        // Arrange
        ApprovalAuthority authority1 = ApprovalAuthority.restore(
            "same-id", "email1@example.com", "名前1", Position.MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null,
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        ApprovalAuthority authority2 = ApprovalAuthority.restore(
            "same-id", "email2@example.com", "名前2", Position.DEPARTMENT_MANAGER,
            "CORP001", "本社", null, null, null, null, null, null,
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(authority1).isEqualTo(authority2);
        assertThat(authority1.hashCode()).isEqualTo(authority2.hashCode());
    }

    @Test
    @DisplayName("等価性判定 - 異なるIDの場合")
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        ApprovalAuthority authority1 = ApprovalAuthority.restore(
            "id-1", "email@example.com", "名前", Position.MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null,
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        ApprovalAuthority authority2 = ApprovalAuthority.restore(
            "id-2", "email@example.com", "名前", Position.MANAGER,
            "DEPT001", "開発部", null, null, null, null, null, null,
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        // Act & Assert
        assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    @DisplayName("toString - 適切な文字列表現")
    void toString_ReturnsFormattedString() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "山田マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act
        String result = authority.toString();

        // Assert
        assertThat(result).contains("ApprovalAuthority{");
        assertThat(result).contains("id=");
        assertThat(result).contains("email='manager@example.com'");
        assertThat(result).contains("position=MANAGER");
    }

    @Test
    @DisplayName("メールアドレス境界値テスト - 255文字は作成可能")
    void create_ExactlyTwoHundredFiftyFiveCharacterEmail_Success() {
        // 255文字のメールアドレス
        String email = "a".repeat(239) + "@example.com";  // 合計255文字

        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> ApprovalAuthority.create(
            email,
            "テストユーザー",
            Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("メールアドレス境界値テスト - 256文字は例外")
    void create_TwoHundredFiftySixCharacterEmail_ThrowsException() {
        // 256文字のメールアドレス
        String email = "a".repeat(244) + "@example.com";  // 244 + 12 = 256文字

        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            email,
            "テストユーザー",
            Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("メールアドレスは255文字以内で入力してください");
    }

    @Test
    @DisplayName("名前境界値テスト - 255文字は作成可能")
    void create_ExactlyTwoHundredFiftyFiveCharacterName_Success() {
        // 255文字の名前
        String name = "a".repeat(255);

        // Act & Assert - 例外が発生しないことを確認
        assertThatCode(() -> ApprovalAuthority.create(
            "test@example.com",
            name,
            Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("名前境界値テスト - 256文字は例外")
    void create_TwoHundredFiftySixCharacterName_ThrowsException() {
        // 256文字の名前
        String name = "a".repeat(256);

        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "test@example.com",
            name,
            Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("名前は255文字以内で入力してください");
    }

    @Test
    @DisplayName("承認権限作成 - level1Codeが空文字の場合は例外")
    void create_EmptyLevel1Code_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            Position.MANAGER,
            "",
            "開発部",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Level1組織コードは必須です");
    }

    @Test
    @DisplayName("承認権限作成 - level1Nameが空文字の場合は例外")
    void create_EmptyLevel1Name_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> ApprovalAuthority.create(
            "manager@example.com",
            "山田マネージャー",
            Position.MANAGER,
            "DEPT001",
            "",
            null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Level1組織名は必須です");
    }

    @Test
    @DisplayName("getOrganizationPath - 空の組織名をスキップ")
    void getOrganizationPath_SkipsEmptyNames() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            "DIV001", "",  // Level2Nameは空文字
            null, null, null, null
        );

        // Act
        String path = authority.getOrganizationPath();

        // Assert
        assertThat(path).isEqualTo("開発部");
    }

    @Test
    @DisplayName("getOrganizationCodePath - 空の組織コードをスキップ")
    void getOrganizationCodePath_SkipsEmptyCodes() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            "", "技術統括本部",  // Level2Codeは空文字
            null, null, null, null
        );

        // Act
        String path = authority.getOrganizationCodePath();

        // Assert
        assertThat(path).isEqualTo("DEPT001");
    }

    @Test
    @DisplayName("equals - nullとの比較")
    void equals_WithNull_ReturnsFalse() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(authority.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equals - 異なるクラスとの比較")
    void equals_WithDifferentClass_ReturnsFalse() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(authority.equals("string")).isFalse();
    }

    @Test
    @DisplayName("equals - 同じインスタンスとの比較")
    void equals_WithSameInstance_ReturnsTrue() {
        // Arrange
        ApprovalAuthority authority = ApprovalAuthority.create(
            "manager@example.com", "マネージャー", Position.MANAGER,
            "DEPT001", "開発部",
            null, null, null, null, null, null
        );

        // Act & Assert
        assertThat(authority.equals(authority)).isTrue();
    }
}