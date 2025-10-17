package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.devhour.domain.model.entity.ApprovalAuthority;

/**
 * EmployeeRecord値オブジェクトのテスト
 */
@DisplayName("EmployeeRecord - 従業員レコードの値オブジェクトのテスト")
class EmployeeRecordTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTest {

        @Test
        @DisplayName("すべての必須項目でインスタンスを生成できる")
        void shouldCreateInstanceWithRequiredFields() {
            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com",
                "田中太郎", 
                "マネージャー",
                "001",
                "統括本部",
                "002", 
                "開発本部",
                "003",
                "システム部",
                "004",
                "第一グループ"
            );

            // Assert
            assertThat(record.email()).isEqualTo("test@example.com");
            assertThat(record.name()).isEqualTo("田中太郎");
            assertThat(record.position()).isEqualTo("マネージャー");
            assertThat(record.level1Code()).isEqualTo("001");
            assertThat(record.level1Name()).isEqualTo("統括本部");
            assertThat(record.level2Code()).isEqualTo("002");
            assertThat(record.level2Name()).isEqualTo("開発本部");
            assertThat(record.level3Code()).isEqualTo("003");
            assertThat(record.level3Name()).isEqualTo("システム部");
            assertThat(record.level4Code()).isEqualTo("004");
            assertThat(record.level4Name()).isEqualTo("第一グループ");
        }

        @Test
        @DisplayName("必須項目のみでインスタンスを生成できる")
        void shouldCreateInstanceWithRequiredFieldsOnly() {
            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com",
                "田中太郎",
                "一般社員",
                null, null, null, null, null, null, null, null
            );

            // Assert
            assertThat(record.email()).isEqualTo("test@example.com");
            assertThat(record.name()).isEqualTo("田中太郎");
            assertThat(record.position()).isEqualTo("一般社員");
            assertThat(record.level1Code()).isNull();
            assertThat(record.level1Name()).isNull();
            assertThat(record.level2Code()).isNull();
            assertThat(record.level2Name()).isNull();
            assertThat(record.level3Code()).isNull();
            assertThat(record.level3Name()).isNull();
            assertThat(record.level4Code()).isNull();
            assertThat(record.level4Name()).isNull();
        }

        @Test
        @DisplayName("前後に空白がある値でインスタンスを生成できる")
        void shouldCreateInstanceWithWhitespace() {
            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                "  test@example.com  ",
                "  田中太郎  ",
                "  マネージャー  ",
                "  001  ",
                "  統括本部  ",
                null, null, null, null, null, null
            );

            // Assert
            assertThat(record.email()).isEqualTo("test@example.com");
            assertThat(record.name()).isEqualTo("田中太郎");
            assertThat(record.position()).isEqualTo("マネージャー");
            assertThat(record.level1Code()).isEqualTo("001");
            assertThat(record.level1Name()).isEqualTo("統括本部");
        }
    }

    @Nested
    @DisplayName("バリデーションのテスト")
    class ValidationTest {

        @Test
        @DisplayName("emailがnullの場合は例外をスローする")
        void shouldThrowExceptionForNullEmail() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                null, "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("emailが空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyEmail() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("emailが空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankEmail() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "  ", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("メールアドレスは必須です");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "test@",
            "test.example.com",
            "test@example",
            "test@@example.com"
        })
        @DisplayName("不正な形式のメールアドレスの場合は例外をスローする")
        void shouldThrowExceptionForInvalidEmailFormat(String email) {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                email, "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("メールアドレスの形式が正しくありません");
        }

        @Test
        @DisplayName("nameがnullの場合は例外をスローする")
        void shouldThrowExceptionForNullName() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", null, "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("名前は必須です");
        }

        @Test
        @DisplayName("nameが空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyName() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("名前は必須です");
        }

        @Test
        @DisplayName("nameが空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankName() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "  ", "マネージャー",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("名前は必須です");
        }

        @Test
        @DisplayName("positionがnullの場合は例外をスローする")
        void shouldThrowExceptionForNullPosition() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "田中太郎", null,
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("役職は必須です");
        }

        @Test
        @DisplayName("positionが空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyPosition() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "田中太郎", "",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("役職は必須です");
        }

        @Test
        @DisplayName("positionが空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankPosition() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "田中太郎", "  ",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("役職は必須です");
        }

        @Test
        @DisplayName("無効な役職名の場合は例外をスローする")
        void shouldThrowExceptionForInvalidPosition() {
            // Act & Assert
            assertThatThrownBy(() -> new EmployeeRecord(
                "test@example.com", "田中太郎", "不正な役職",
                null, null, null, null, null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Japanese position name: 不正な役職");
        }
    }

    @Nested
    @DisplayName("hasApprovalAuthorityメソッドのテスト")
    class HasApprovalAuthorityTest {

        @Test
        @DisplayName("一般社員は承認権限なし")
        void shouldReturnFalseForEmployee() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "一般社員",
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.hasApprovalAuthority()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"マネージャー", "部長", "本部長", "統括本部長"})
        @DisplayName("マネージャー以上は承認権限あり")
        void shouldReturnTrueForManagerOrAbove(String position) {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", position,
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.hasApprovalAuthority()).isTrue();
        }
    }

    @Nested
    @DisplayName("getHighestLevelCodeメソッドのテスト")
    class GetHighestLevelCodeTest {

        @Test
        @DisplayName("level1のみ設定されている場合")
        void shouldReturnLevel1WhenOnlyLevel1Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelCode()).isEqualTo("001");
        }

        @Test
        @DisplayName("level2まで設定されている場合")
        void shouldReturnLevel2WhenLevel2Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelCode()).isEqualTo("002");
        }

        @Test
        @DisplayName("level3まで設定されている場合")
        void shouldReturnLevel3WhenLevel3Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelCode()).isEqualTo("003");
        }

        @Test
        @DisplayName("level4まで設定されている場合")
        void shouldReturnLevel4WhenLevel4Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                "004", "第一グループ"
            );

            // Act & Assert
            assertThat(record.getHighestLevelCode()).isEqualTo("004");
        }

        @Test
        @DisplayName("すべてnullの場合はnull")
        void shouldReturnNullWhenAllLevelsAreNull() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelCode()).isNull();
        }
    }

    @Nested
    @DisplayName("getHighestLevelNameメソッドのテスト")
    class GetHighestLevelNameTest {

        @Test
        @DisplayName("level1のみ設定されている場合")
        void shouldReturnLevel1WhenOnlyLevel1Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelName()).isEqualTo("統括本部");
        }

        @Test
        @DisplayName("level2まで設定されている場合")
        void shouldReturnLevel2WhenLevel2Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelName()).isEqualTo("開発本部");
        }

        @Test
        @DisplayName("level3まで設定されている場合")
        void shouldReturnLevel3WhenLevel3Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelName()).isEqualTo("システム部");
        }

        @Test
        @DisplayName("level4まで設定されている場合")
        void shouldReturnLevel4WhenLevel4Set() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                "004", "第一グループ"
            );

            // Act & Assert
            assertThat(record.getHighestLevelName()).isEqualTo("第一グループ");
        }

        @Test
        @DisplayName("すべてnullの場合はnull")
        void shouldReturnNullWhenAllLevelsAreNull() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getHighestLevelName()).isNull();
        }
    }

    @Nested
    @DisplayName("getOrganizationPathメソッドのテスト")
    class GetOrganizationPathTest {

        @Test
        @DisplayName("level1のみの場合")
        void shouldReturnPathWithLevel1Only() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("統括本部");
        }

        @Test
        @DisplayName("level2まで設定されている場合")
        void shouldReturnPathWithLevel2() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                null, null, null, null
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("統括本部 > 開発本部");
        }

        @Test
        @DisplayName("level3まで設定されている場合")
        void shouldReturnPathWithLevel3() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                null, null
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("統括本部 > 開発本部 > システム部");
        }

        @Test
        @DisplayName("level4まで設定されている場合")
        void shouldReturnPathWithLevel4() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                "004", "第一グループ"
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("統括本部 > 開発本部 > システム部 > 第一グループ");
        }

        @Test
        @DisplayName("すべてnullの場合は空文字")
        void shouldReturnEmptyStringWhenAllLevelsAreNull() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("");
        }

        @Test
        @DisplayName("level2とlevel4のみ設定されている場合（飛び石パターン）")
        void shouldHandleSkippedLevels() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                null, null,
                "002", "開発本部",
                null, null,
                "004", "第一グループ"
            );

            // Act & Assert
            assertThat(record.getOrganizationPath()).isEqualTo("開発本部 > 第一グループ");
        }
    }

    @Nested
    @DisplayName("toApprovalAuthorityメソッドのテスト")
    class ToApprovalAuthorityTest {

        @Test
        @DisplayName("全ての項目が設定されている場合")
        void shouldCreateApprovalAuthorityWithAllFields() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部",
                "002", "開発本部",
                "003", "システム部",
                "004", "第一グループ"
            );

            // Act
            ApprovalAuthority authority = record.toApprovalAuthority();

            // Assert
            assertThat(authority.getEmail()).isEqualTo("test@example.com");
            assertThat(authority.getName()).isEqualTo("田中太郎");
            assertThat(authority.getPosition()).isEqualTo(Position.MANAGER);
            assertThat(authority.getLevel1Code()).isEqualTo("001");
            assertThat(authority.getLevel1Name()).isEqualTo("統括本部");
            assertThat(authority.getLevel2Code()).isEqualTo("002");
            assertThat(authority.getLevel2Name()).isEqualTo("開発本部");
            assertThat(authority.getLevel3Code()).isEqualTo("003");
            assertThat(authority.getLevel3Name()).isEqualTo("システム部");
            assertThat(authority.getLevel4Code()).isEqualTo("004");
            assertThat(authority.getLevel4Name()).isEqualTo("第一グループ");
            assertThat(authority.getId()).isNotNull();
            assertThat(authority.getCreatedAt()).isNotNull();
            assertThat(authority.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("必須項目のみ設定されている場合")
        void shouldCreateApprovalAuthorityWithRequiredFieldsOnly() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );

            // Act
            ApprovalAuthority authority = record.toApprovalAuthority();

            // Assert
            assertThat(authority.getEmail()).isEqualTo("test@example.com");
            assertThat(authority.getName()).isEqualTo("田中太郎");
            assertThat(authority.getPosition()).isEqualTo(Position.MANAGER);
            assertThat(authority.getLevel1Code()).isEqualTo("001");
            assertThat(authority.getLevel1Name()).isEqualTo("統括本部");
            assertThat(authority.getLevel2Code()).isNull();
            assertThat(authority.getLevel2Name()).isNull();
            assertThat(authority.getLevel3Code()).isNull();
            assertThat(authority.getLevel3Name()).isNull();
            assertThat(authority.getLevel4Code()).isNull();
            assertThat(authority.getLevel4Name()).isNull();
        }

        @Test
        @DisplayName("一般社員の場合もエンティティを作成できる")
        void shouldCreateApprovalAuthorityForEmployee() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "一般社員",
                "001", "統括本部",
                null, null, null, null, null, null
            );

            // Act
            ApprovalAuthority authority = record.toApprovalAuthority();

            // Assert
            assertThat(authority.getPosition()).isEqualTo(Position.EMPLOYEE);
            assertThat(authority.hasApprovalAuthority()).isFalse();
        }

        @Test
        @DisplayName("Level1組織情報がnullの場合は例外をスローする")
        void shouldThrowExceptionWhenLevel1IsNull() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThatThrownBy(() -> record.toApprovalAuthority())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Level1組織コードは必須です");
        }

        @Test
        @DisplayName("Level1組織名がnullの場合は例外をスローする")
        void shouldThrowExceptionWhenLevel1NameIsNull() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", null, null, null, null, null, null, null
            );

            // Act & Assert
            assertThatThrownBy(() -> record.toApprovalAuthority())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Level1組織名は必須です");
        }
    }

    @Nested
    @DisplayName("CSVデータマッピングのテスト")
    class CsvDataMappingTest {

        @ParameterizedTest
        @CsvSource({
            "test@example.com, 田中太郎, 001, 統括本部, 002, 開発本部, 003, システム部, 004, 第一グループ, マネージャー",
            "yamada@example.com, 山田花子, 010, 営業統括本部, 011, 第一営業本部, , , , , 部長"
        })
        @DisplayName("CSVフォーマットの実際のデータパターンをテスト")
        void shouldHandleActualCsvDataPatterns(
            String email, String name,
            String level1Code, String level1Name,
            String level2Code, String level2Name,
            String level3Code, String level3Name,
            String level4Code, String level4Name,
            String position) {

            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                email, name, position,
                level1Code, level1Name,
                level2Code, level2Name,
                level3Code, level3Name,
                level4Code, level4Name
            );

            // Assert
            assertThat(record.email()).isEqualTo(email);
            assertThat(record.name()).isEqualTo(name);
            assertThat(record.position()).isEqualTo(position);
        }
    }

    @Nested
    @DisplayName("境界値のテスト")
    class BoundaryTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "test@example.com",
            "a@b.co",
            "very.long.email.address.with.many.characters@example.domain.com"
        })
        @DisplayName("有効なメールアドレス形式")
        void shouldAcceptValidEmailFormats(String email) {
            // Act & Assert - 例外が発生しないことを確認
            EmployeeRecord record = new EmployeeRecord(
                email, "田中太郎", "マネージャー",
                null, null, null, null, null, null, null, null
            );
            assertThat(record.email()).isEqualTo(email);
        }

        @Test
        @DisplayName("長い名前を受け入れる")
        void shouldAcceptLongName() {
            // Arrange
            String longName = "田中太郎".repeat(10); // 40文字

            // Act & Assert - 例外が発生しないことを確認
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", longName, "マネージャー",
                null, null, null, null, null, null, null, null
            );
            assertThat(record.name()).isEqualTo(longName);
        }
    }

    @Nested
    @DisplayName("実際の使用例のテスト")
    class UseCaseTest {

        @Test
        @DisplayName("典型的な管理職のデータ")
        void shouldHandleTypicalManagerData() {
            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                "manager@company.com",
                "管理太郎",
                "部長",
                "100", "IT統括本部",
                "110", "システム開発本部",
                "111", "基盤システム部",
                null, null
            );

            // Assert
            assertThat(record.hasApprovalAuthority()).isTrue();
            assertThat(record.getHighestLevelCode()).isEqualTo("111");
            assertThat(record.getHighestLevelName()).isEqualTo("基盤システム部");
            assertThat(record.getOrganizationPath()).isEqualTo("IT統括本部 > システム開発本部 > 基盤システム部");

            ApprovalAuthority authority = record.toApprovalAuthority();
            assertThat(authority.hasApprovalAuthority()).isTrue();
            assertThat(authority.getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
        }

        @Test
        @DisplayName("典型的な一般社員のデータ")
        void shouldHandleTypicalEmployeeData() {
            // Arrange & Act
            EmployeeRecord record = new EmployeeRecord(
                "employee@company.com",
                "開発花子",
                "一般社員",
                "200", "プロダクト統括本部",
                "210", "プロダクト開発本部",
                "211", "フロントエンド開発部",
                "2111", "UIグループ"
            );

            // Assert
            assertThat(record.hasApprovalAuthority()).isFalse();
            assertThat(record.getHighestLevelCode()).isEqualTo("2111");
            assertThat(record.getHighestLevelName()).isEqualTo("UIグループ");
            assertThat(record.getOrganizationPath())
                .isEqualTo("プロダクト統括本部 > プロダクト開発本部 > フロントエンド開発部 > UIグループ");

            ApprovalAuthority authority = record.toApprovalAuthority();
            assertThat(authority.hasApprovalAuthority()).isFalse();
            assertThat(authority.getPosition()).isEqualTo(Position.EMPLOYEE);
        }
    }

    @Nested
    @DisplayName("equalsとhashCodeのテスト")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("同じ値のインスタンスは等しい")
        void shouldBeEqualForSameValues() {
            // Arrange
            EmployeeRecord record1 = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );
            EmployeeRecord record2 = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record1).isEqualTo(record2);
            assertThat(record1.hashCode()).isEqualTo(record2.hashCode());
        }

        @Test
        @DisplayName("空白がトリムされた値は等しい")
        void shouldBeEqualAfterTrim() {
            // Arrange
            EmployeeRecord record1 = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );
            EmployeeRecord record2 = new EmployeeRecord(
                "  test@example.com  ", "  田中太郎  ", "  マネージャー  ",
                "  001  ", "  統括本部  ", null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record1).isEqualTo(record2);
            assertThat(record1.hashCode()).isEqualTo(record2.hashCode());
        }

        @Test
        @DisplayName("異なる値のインスタンスは等しくない")
        void shouldNotBeEqualForDifferentValues() {
            // Arrange
            EmployeeRecord record1 = new EmployeeRecord(
                "test1@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );
            EmployeeRecord record2 = new EmployeeRecord(
                "test2@example.com", "山田花子", "部長",
                "002", "営業本部", null, null, null, null, null, null
            );

            // Act & Assert
            assertThat(record1).isNotEqualTo(record2);
        }
    }

    @Nested
    @DisplayName("toStringのテスト")
    class ToStringTest {

        @Test
        @DisplayName("文字列表現に主要な情報が含まれる")
        void shouldContainMainInformationInString() {
            // Arrange
            EmployeeRecord record = new EmployeeRecord(
                "test@example.com", "田中太郎", "マネージャー",
                "001", "統括本部", null, null, null, null, null, null
            );

            // Act
            String result = record.toString();

            // Assert
            assertThat(result).contains("test@example.com");
            assertThat(result).contains("田中太郎");
            assertThat(result).contains("マネージャー");
        }
    }
}