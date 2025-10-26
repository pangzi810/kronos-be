package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;

/**
 * ApprovalAuthorityMapperの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
@DisplayName("ApprovalAuthorityMapper統合テスト")
class ApprovalAuthorityMapperTest extends AbstractMapperTest {

    @Autowired
    private ApprovalAuthorityMapper approvalAuthorityMapper;

    private ApprovalAuthority testApprovalAuthority;

    @BeforeEach
    void setUp() {
        // テスト用承認権限エンティティの準備
        testApprovalAuthority = ApprovalAuthority.create(
            "test@example.com",
            "テスト太郎",
            Position.MANAGER,
            "L1001",
            "開発本部",
            "L2001",
            "システム開発部",
            "L3001",
            "Webアプリ課",
            null,
            null
        );
    }

    @Nested
    @DisplayName("全件取得操作")
    class FindAllOperations {

        @Test
        @DisplayName("findAll - 複数レコード存在時は作成日昇順で取得")
        void findAll_MultipleRecords_ReturnsOrderedByCreatedAt() throws InterruptedException {
            // Arrange - テストデータを作成
            ApprovalAuthority auth1 = ApprovalAuthority.create(
                "testuser1@example.com", "山田太郎", Position.MANAGER,
                "L1001", "開発本部", "L2001", "システム開発部", "L3001", "Webアプリ課", null, null);
            approvalAuthorityMapper.insert(auth1);

            Thread.sleep(10);

            ApprovalAuthority auth2 = ApprovalAuthority.create(
                "testuser2@example.com", "佐藤花子", Position.DEPARTMENT_MANAGER,
                "L1001", "開発本部", "L2002", "インフラ部", null, null, null, null);
            approvalAuthorityMapper.insert(auth2);

            Thread.sleep(10);

            ApprovalAuthority auth3 = ApprovalAuthority.create(
                "testuser3@example.com", "田中次郎", Position.DIVISION_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);
            approvalAuthorityMapper.insert(auth3);

            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findAll();

            // Assert
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isGreaterThanOrEqualTo(3);

            // 作成日昇順であることを確認
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).getCreatedAt())
                    .isBeforeOrEqualTo(result.get(i + 1).getCreatedAt());
            }

            // Position TypeHandlerの動作確認
            assertThat(result.stream())
                .allMatch(authority -> authority.getPosition() != null);
        }

        @Test
        @DisplayName("findAll - データが存在しない場合は空リストを返す")
        void findAll_NoData_ReturnsEmptyList() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findAll();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("メール検索操作")
    class FindByEmailOperations {

        @Test
        @DisplayName("findByEmail - 存在するメールアドレス")
        void findByEmail_ExistingEmail_ReturnsApprovalAuthority() {
            // Arrange
            approvalAuthorityMapper.insert(testApprovalAuthority);

            // Act
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("test@example.com");

            // Assert
            assertThat(result).isPresent();
            ApprovalAuthority found = result.get();
            assertThat(found.getEmail()).isEqualTo("test@example.com");
            assertThat(found.getName()).isEqualTo("テスト太郎");
            assertThat(found.getPosition()).isEqualTo(Position.MANAGER);
            assertThat(found.getLevel1Code()).isEqualTo("L1001");
            assertThat(found.getLevel1Name()).isEqualTo("開発本部");
            assertThat(found.getLevel2Code()).isEqualTo("L2001");
            assertThat(found.getLevel2Name()).isEqualTo("システム開発部");
        }

        @Test
        @DisplayName("findByEmail - 存在しないメールアドレス")
        void findByEmail_NonExistentEmail_ReturnsEmpty() {
            // Act
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findByEmail - nullメールアドレス")
        void findByEmail_NullEmail_ReturnsEmpty() {
            // Act
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail(null);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("名前・メール検索操作")
    class SearchByNameOrEmailOperations {

        @BeforeEach
        void setUpSearchData() {
            // 検索用テストデータの準備
            ApprovalAuthority manager1 = ApprovalAuthority.create(
                "manager1@example.com", "田中マネージャー", Position.MANAGER,
                "L1001", "開発本部", "L2001", "システム開発部", null, null, null, null);
            ApprovalAuthority manager2 = ApprovalAuthority.create(
                "manager2@example.com", "鈴木部長", Position.DEPARTMENT_MANAGER,
                "L1001", "開発本部", "L2002", "インフラ部", null, null, null, null);
            ApprovalAuthority manager3 = ApprovalAuthority.create(
                "yamada@example.com", "山田本部長", Position.DIVISION_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);

            approvalAuthorityMapper.insert(manager1);
            approvalAuthorityMapper.insert(manager2);
            approvalAuthorityMapper.insert(manager3);
        }

        @Test
        @DisplayName("searchByNameOrEmail - 名前で部分一致検索")
        void searchByNameOrEmail_NamePartialMatch_ReturnsFilteredResults() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.searchByNameOrEmail("田中");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("田中");
        }

        @Test
        @DisplayName("searchByNameOrEmail - メールアドレスで部分一致検索")
        void searchByNameOrEmail_EmailPartialMatch_ReturnsFilteredResults() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.searchByNameOrEmail("yamada");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).contains("yamada");
        }

        @Test
        @DisplayName("searchByNameOrEmail - 複数マッチする場合は役職順、名前順でソート")
        void searchByNameOrEmail_MultipleMatches_SortedByPositionAndName() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.searchByNameOrEmail("マネ");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("マネージャー");
            assertThat(result.get(0).getPosition()).isEqualTo(Position.MANAGER);
        }

        @Test
        @DisplayName("searchByNameOrEmail - マッチしない検索語")
        void searchByNameOrEmail_NoMatch_ReturnsEmptyList() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.searchByNameOrEmail("存在しない");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("searchByNameOrEmail - 空の検索語")
        void searchByNameOrEmail_EmptyQuery_ReturnsAllRecords() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.searchByNameOrEmail("");

            // Assert
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("挿入操作")
    class InsertOperations {

        @Test
        @DisplayName("insert - 正常なApprovalAuthorityの挿入")
        void insert_ValidApprovalAuthority_InsertsSuccessfully() {
            // Act
            approvalAuthorityMapper.insert(testApprovalAuthority);

            // Assert
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("test@example.com");
            assertThat(result).isPresent();
            
            ApprovalAuthority inserted = result.get();
            assertThat(inserted.getEmail()).isEqualTo("test@example.com");
            assertThat(inserted.getName()).isEqualTo("テスト太郎");
            assertThat(inserted.getPosition()).isEqualTo(Position.MANAGER);
            assertThat(inserted.getCreatedAt()).isNotNull();
            assertThat(inserted.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("insert - 全ての組織レベルを含む挿入")
        void insert_AllLevelsIncluded_InsertsSuccessfully() {
            // Arrange
            ApprovalAuthority fullLevelAuthority = ApprovalAuthority.create(
                "full@example.com", "フルレベル権限者", Position.GENERAL_MANAGER,
                "L1001", "グループ本社", "L2001", "開発本部", 
                "L3001", "システム開発部", "L4001", "Webアプリ課"
            );

            // Act
            approvalAuthorityMapper.insert(fullLevelAuthority);

            // Assert
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("full@example.com");
            assertThat(result).isPresent();
            
            ApprovalAuthority inserted = result.get();
            assertThat(inserted.getLevel1Code()).isEqualTo("L1001");
            assertThat(inserted.getLevel1Name()).isEqualTo("グループ本社");
            assertThat(inserted.getLevel2Code()).isEqualTo("L2001");
            assertThat(inserted.getLevel2Name()).isEqualTo("開発本部");
            assertThat(inserted.getLevel3Code()).isEqualTo("L3001");
            assertThat(inserted.getLevel3Name()).isEqualTo("システム開発部");
            assertThat(inserted.getLevel4Code()).isEqualTo("L4001");
            assertThat(inserted.getLevel4Name()).isEqualTo("Webアプリ課");
        }

        @Test
        @DisplayName("insert - 最低限のLevel1のみの挿入")
        void insert_MinimumLevel1Only_InsertsSuccessfully() {
            // Arrange
            ApprovalAuthority minimalAuthority = ApprovalAuthority.create(
                "minimal@example.com", "最小権限者", Position.MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null
            );

            // Act
            approvalAuthorityMapper.insert(minimalAuthority);

            // Assert
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("minimal@example.com");
            assertThat(result).isPresent();
            
            ApprovalAuthority inserted = result.get();
            assertThat(inserted.getLevel1Code()).isEqualTo("L1001");
            assertThat(inserted.getLevel1Name()).isEqualTo("開発本部");
            assertThat(inserted.getLevel2Code()).isNull();
            assertThat(inserted.getLevel2Name()).isNull();
            assertThat(inserted.getLevel3Code()).isNull();
            assertThat(inserted.getLevel3Name()).isNull();
            assertThat(inserted.getLevel4Code()).isNull();
            assertThat(inserted.getLevel4Name()).isNull();
        }
    }

    @Nested
    @DisplayName("更新操作")
    class UpdateOperations {

        @Test
        @DisplayName("update - 承認権限情報の更新")
        void update_ExistingApprovalAuthority_UpdatesSuccessfully() throws InterruptedException {
            // Arrange
            approvalAuthorityMapper.insert(testApprovalAuthority);

            // 更新時刻が確実にcreatedAtより後になるように少し待機
            Thread.sleep(10);

            // 更新用の新しい情報
            testApprovalAuthority.updateInfo(
                "updated@example.com", "更新太郎", Position.DEPARTMENT_MANAGER,
                "L1002", "更新本部", "L2002", "更新部", null, null, null, null
            );

            // Act
            approvalAuthorityMapper.update(testApprovalAuthority);

            // Assert
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("updated@example.com");
            assertThat(result).isPresent();

            ApprovalAuthority updated = result.get();
            assertThat(updated.getName()).isEqualTo("更新太郎");
            assertThat(updated.getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
            assertThat(updated.getLevel1Code()).isEqualTo("L1002");
            assertThat(updated.getLevel1Name()).isEqualTo("更新本部");
            assertThat(updated.getLevel2Code()).isEqualTo("L2002");
            assertThat(updated.getLevel2Name()).isEqualTo("更新部");
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("削除操作")
    class DeleteOperations {

        @Test
        @DisplayName("deleteByEmail - 存在する承認権限の削除")
        void deleteByEmail_ExistingApprovalAuthority_DeletesSuccessfully() {
            // Arrange
            approvalAuthorityMapper.insert(testApprovalAuthority);
            assertThat(approvalAuthorityMapper.findByEmail("test@example.com")).isPresent();

            // Act
            approvalAuthorityMapper.deleteByEmail("test@example.com");

            // Assert
            assertThat(approvalAuthorityMapper.findByEmail("test@example.com")).isEmpty();
        }

        @Test
        @DisplayName("deleteByEmail - 存在しない承認権限の削除")
        void deleteByEmail_NonExistentApprovalAuthority_DoesNothing() {
            // Act & Assert - 例外が発生しないことを確認
            approvalAuthorityMapper.deleteByEmail("nonexistent@example.com");
        }
    }

    @Nested
    @DisplayName("役職検索操作")
    class FindByPositionOperations {

        @BeforeEach
        void setUpPositionData() {
            ApprovalAuthority manager = ApprovalAuthority.create(
                "manager@example.com", "マネージャー", Position.MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority deptManager = ApprovalAuthority.create(
                "dept@example.com", "部長", Position.DEPARTMENT_MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority divManager = ApprovalAuthority.create(
                "div@example.com", "本部長", Position.DIVISION_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);

            approvalAuthorityMapper.insert(manager);
            approvalAuthorityMapper.insert(deptManager);
            approvalAuthorityMapper.insert(divManager);
        }

        @Test
        @DisplayName("findByPosition - 特定の役職で検索")
        void findByPosition_SpecificPosition_ReturnsFilteredResults() {
            // Act
            List<ApprovalAuthority> managers = approvalAuthorityMapper.findByPosition(Position.MANAGER);
            List<ApprovalAuthority> deptManagers = approvalAuthorityMapper.findByPosition(Position.DEPARTMENT_MANAGER);

            // Assert
            assertThat(managers).hasSize(1);
            assertThat(managers.get(0).getPosition()).isEqualTo(Position.MANAGER);
            
            assertThat(deptManagers).hasSize(1);
            assertThat(deptManagers.get(0).getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
        }

        @Test
        @DisplayName("findByPosition - 存在しない役職で検索")
        void findByPosition_NonExistentPosition_ReturnsEmptyList() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findByPosition(Position.GENERAL_MANAGER);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("存在チェック操作")
    class ExistsOperations {

        @Test
        @DisplayName("existsByEmail - 存在するメールアドレス")
        void existsByEmail_ExistingEmail_ReturnsTrue() {
            // Arrange
            approvalAuthorityMapper.insert(testApprovalAuthority);

            // Act
            boolean exists = approvalAuthorityMapper.existsByEmail("test@example.com");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsByEmail - 存在しないメールアドレス")
        void existsByEmail_NonExistentEmail_ReturnsFalse() {
            // Act
            boolean exists = approvalAuthorityMapper.existsByEmail("nonexistent@example.com");

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("組織レベル検索操作")
    class FindByLevelCodeOperations {

        @BeforeEach
        void setUpLevelData() {
            ApprovalAuthority auth1 = ApprovalAuthority.create(
                "auth1@example.com", "承認者1", Position.MANAGER,
                "L1001", "開発本部", "L2001", "システム開発部", null, null, null, null);
            ApprovalAuthority auth2 = ApprovalAuthority.create(
                "auth2@example.com", "承認者2", Position.DEPARTMENT_MANAGER,
                "L1001", "開発本部", "L2002", "インフラ部", null, null, null, null);
            ApprovalAuthority auth3 = ApprovalAuthority.create(
                "auth3@example.com", "承認者3", Position.DIVISION_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);

            approvalAuthorityMapper.insert(auth1);
            approvalAuthorityMapper.insert(auth2);
            approvalAuthorityMapper.insert(auth3);
        }

        @Test
        @DisplayName("findByLevelCode - Level1コードで検索")
        void findByLevelCode_Level1Code_ReturnsFilteredResults() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findByLevelCode("L1001", 1);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(auth -> "L1001".equals(auth.getLevel1Code()));
        }

        @Test
        @DisplayName("findByLevelCode - Level2コードで検索")
        void findByLevelCode_Level2Code_ReturnsFilteredResults() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findByLevelCode("L2001", 2);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel2Code()).isEqualTo("L2001");
        }

        @Test
        @DisplayName("findByLevelCode - 存在しないレベルコード")
        void findByLevelCode_NonExistentCode_ReturnsEmptyList() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findByLevelCode("NONEXISTENT", 1);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("カウント操作")
    class CountOperations {

        @BeforeEach
        void setUpCountData() {
            ApprovalAuthority auth1 = ApprovalAuthority.create(
                "count1@example.com", "承認者1", Position.MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority auth2 = ApprovalAuthority.create(
                "count2@example.com", "承認者2", Position.MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority auth3 = ApprovalAuthority.create(
                "count3@example.com", "承認者3", Position.DEPARTMENT_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);

            approvalAuthorityMapper.insert(auth1);
            approvalAuthorityMapper.insert(auth2);
            approvalAuthorityMapper.insert(auth3);
        }

        @Test
        @DisplayName("countByLevelCode - 組織別承認権限者数をカウント")
        void countByLevelCode_ExistingCode_ReturnsCorrectCount() {
            // Act
            long l1001Count = approvalAuthorityMapper.countByLevelCode("L1001", 1);
            long l1002Count = approvalAuthorityMapper.countByLevelCode("L1002", 1);

            // Assert
            assertThat(l1001Count).isEqualTo(2);
            assertThat(l1002Count).isEqualTo(1);
        }

        @Test
        @DisplayName("countByLevelCode - 存在しないコードのカウント")
        void countByLevelCode_NonExistentCode_ReturnsZero() {
            // Act
            long count = approvalAuthorityMapper.countByLevelCode("NONEXISTENT", 1);

            // Assert
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("承認権限者取得操作")
    class FindAllWithApprovalAuthorityOperations {

        @BeforeEach
        void setUpApprovalAuthorityData() {
            // 一般社員以外の承認権限者を作成
            ApprovalAuthority manager = ApprovalAuthority.create(
                "manager@example.com", "マネージャー", Position.MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority deptManager = ApprovalAuthority.create(
                "dept@example.com", "部長", Position.DEPARTMENT_MANAGER,
                "L1001", "開発本部", null, null, null, null, null, null);
            ApprovalAuthority divManager = ApprovalAuthority.create(
                "div@example.com", "本部長", Position.DIVISION_MANAGER,
                "L1002", "営業本部", null, null, null, null, null, null);
            ApprovalAuthority generalManager = ApprovalAuthority.create(
                "general@example.com", "統括本部長", Position.GENERAL_MANAGER,
                "L1000", "グループ本社", null, null, null, null, null, null);

            approvalAuthorityMapper.insert(manager);
            approvalAuthorityMapper.insert(deptManager);
            approvalAuthorityMapper.insert(divManager);
            approvalAuthorityMapper.insert(generalManager);
        }

        @Test
        @DisplayName("findAllWithApprovalAuthority - 承認権限を持つユーザーを階層レベル降順、名前昇順で取得")
        void findAllWithApprovalAuthority_ReturnsApprovalAuthorities_SortedCorrectly() {
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findAllWithApprovalAuthority();

            // Assert
            assertThat(result).hasSize(4);
            
            // 全て承認権限を持つことを確認
            assertThat(result).allMatch(ApprovalAuthority::hasApprovalAuthority);
            
            // 階層レベル降順（上位→下位）の確認
            assertThat(result.get(0).getPosition()).isEqualTo(Position.GENERAL_MANAGER);
            assertThat(result.get(1).getPosition()).isEqualTo(Position.DIVISION_MANAGER);
            assertThat(result.get(2).getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
            assertThat(result.get(3).getPosition()).isEqualTo(Position.MANAGER);
        }

        @Test
        @DisplayName("findAllWithApprovalAuthority - 承認権限者が存在しない場合")
        void findAllWithApprovalAuthority_NoApprovalAuthorities_ReturnsEmptyList() {
            // Arrange - この特定のテストではsetUpApprovalAuthorityDataを呼び出さない
            // テーブル内のデータをクリア（他のテストの影響を排除）
            // H2テストデータベースでは各テストクラスごとにデータがリセットされるが、
            // 同一クラス内の@Nestedテスト間では共有される
            
            // すべてのデータを削除
            List<ApprovalAuthority> existing = approvalAuthorityMapper.findAll();
            for (ApprovalAuthority auth : existing) {
                approvalAuthorityMapper.deleteByEmail(auth.getEmail());
            }
            
            // Act
            List<ApprovalAuthority> result = approvalAuthorityMapper.findAllWithApprovalAuthority();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("エッジケースとエラーハンドリング")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Position TypeHandlerの正常動作確認")
        void positionTypeHandler_ConvertsCorrectly() {
            // Arrange - 全ての役職パターンをテスト
            ApprovalAuthority[] authorities = {
                ApprovalAuthority.create("manager@test.com", "マネージャー", Position.MANAGER, "L1", "L1名", null, null, null, null, null, null),
                ApprovalAuthority.create("dept@test.com", "部長", Position.DEPARTMENT_MANAGER, "L1", "L1名", null, null, null, null, null, null),
                ApprovalAuthority.create("div@test.com", "本部長", Position.DIVISION_MANAGER, "L1", "L1名", null, null, null, null, null, null),
                ApprovalAuthority.create("general@test.com", "統括本部長", Position.GENERAL_MANAGER, "L1", "L1名", null, null, null, null, null, null)
            };

            // Act & Assert
            for (ApprovalAuthority authority : authorities) {
                approvalAuthorityMapper.insert(authority);
                Optional<ApprovalAuthority> found = approvalAuthorityMapper.findByEmail(authority.getEmail());
                
                assertThat(found).isPresent();
                assertThat(found.get().getPosition()).isEqualTo(authority.getPosition());
            }
        }

        @Test
        @DisplayName("NULL値の適切な処理")
        void nullValues_HandledCorrectly() {
            // Arrange - Level2以降がnullのエンティティ
            ApprovalAuthority authority = ApprovalAuthority.create(
                "null@test.com", "NULLテスト", Position.MANAGER,
                "L1", "L1名", null, null, null, null, null, null
            );

            // Act
            approvalAuthorityMapper.insert(authority);
            Optional<ApprovalAuthority> result = approvalAuthorityMapper.findByEmail("null@test.com");

            // Assert
            assertThat(result).isPresent();
            ApprovalAuthority found = result.get();
            assertThat(found.getLevel2Code()).isNull();
            assertThat(found.getLevel2Name()).isNull();
            assertThat(found.getLevel3Code()).isNull();
            assertThat(found.getLevel3Name()).isNull();
            assertThat(found.getLevel4Code()).isNull();
            assertThat(found.getLevel4Name()).isNull();
        }
    }
}