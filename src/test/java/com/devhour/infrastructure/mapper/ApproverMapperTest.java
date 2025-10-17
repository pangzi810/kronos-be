package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.devhour.domain.model.entity.Approver;
import com.devhour.domain.model.entity.User;
import com.devhour.infrastructure.dto.ApproverGrouping;

/**
 * ApproverMapperの統合テスト
 * 
 * V44マイグレーション対応：メールアドレスベースの新機能をテスト
 * H2インメモリデータベースを使用してテスト
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
class ApproverMapperTest {

    @Autowired
    private ApproverMapper approverMapper;

    @Autowired
    private UserMapper userMapper;

    private Approver testApprover1;
    private Approver testApprover2;
    private Approver expiredApprover;
    private Approver futureApprover;

    @BeforeEach
    void setUp() {
        // Create test users for email addresses used in approver relationships
        createTestUser("developer@example.com", "developer");
        createTestUser("manager@example.com", "manager");
        createTestUser("senior@example.com", "senior");
        createTestUser("director@example.com", "director");
        createTestUser("intern@example.com", "intern");
        createTestUser("newbie@example.com", "newbie");

        // 現在有効な承認者関係
        testApprover1 = Approver.create(
            "developer@example.com",
            "manager@example.com",
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().plusDays(30)
        );

        // 別の有効な承認者関係
        testApprover2 = Approver.create(
            "senior@example.com",
            "director@example.com",
            LocalDateTime.now().minusDays(60),
            null // 無期限
        );

        // 期限切れの承認者関係
        expiredApprover = Approver.create(
            "intern@example.com",
            "senior@example.com",
            LocalDateTime.now().minusDays(90),
            LocalDateTime.now().minusDays(10)
        );

        // 未来に有効になる承認者関係
        futureApprover = Approver.create(
            "newbie@example.com",
            "manager@example.com",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(100)
        );
    }

    @AfterEach
    void tearDown() {
        // テストデータのクリーンアップ
        // 実際のプロダクションではsoft deleteを使用するが、テストでは物理削除
        approverMapper.deleteAllForTesting();
    }

    @Nested
    @DisplayName("基本的なCRUD操作")
    class BasicCrudOperations {

        @Test
        @DisplayName("承認者関係の挿入と取得")
        void testInsertAndFindById() {
            // Given
            approverMapper.insert(testApprover1);

            // When
            Optional<Approver> found = approverMapper.findById(testApprover1.getId());

            // Then
            assertThat(found).isPresent();
            Approver approver = found.get();
            assertThat(approver.getTargetEmail()).isEqualTo("developer@example.com");
            assertThat(approver.getApproverEmail()).isEqualTo("manager@example.com");
            assertThat(approver.getEffectiveFrom()).isCloseTo(testApprover1.getEffectiveFrom(), within(1, ChronoUnit.SECONDS));
            if (testApprover1.getEffectiveTo() != null) {
                assertThat(approver.getEffectiveTo()).isNotNull();
                // Compare dates since DB column is 'date' type, time precision is lost
                assertThat(approver.getEffectiveTo().toLocalDate()).isEqualTo(testApprover1.getEffectiveTo().toLocalDate());
            } else {
                assertThat(approver.getEffectiveTo()).isNull();
            }
        }

        @Test
        @DisplayName("承認者関係の更新")
        void testUpdate() {
            // Given
            approverMapper.insert(testApprover1);
            
            // When - 終了日を更新
            LocalDateTime newEndDate = LocalDateTime.now().plusDays(60);
            testApprover1 = Approver.restore(
                testApprover1.getId(),
                testApprover1.getTargetEmail(),
                testApprover1.getApproverEmail(),
                testApprover1.getEffectiveFrom(),
                newEndDate,
                testApprover1.getCreatedAt(),
                LocalDateTime.now()
            );
            approverMapper.update(testApprover1);

            // Then
            Optional<Approver> updated = approverMapper.findById(testApprover1.getId());
            assertThat(updated).isPresent();
            if (newEndDate != null) {
                assertThat(updated.get().getEffectiveTo()).isNotNull();
                // Compare dates since DB column is 'date' type, time precision is lost
                assertThat(updated.get().getEffectiveTo().toLocalDate()).isEqualTo(newEndDate.toLocalDate());
            } else {
                assertThat(updated.get().getEffectiveTo()).isNull();
            }
        }

        @Test
        @DisplayName("存在しないIDで検索した場合の動作")
        void testFindByNonExistentId() {
            // When
            Optional<Approver> found = approverMapper.findById("non-existent-id");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested 
    @DisplayName("メールアドレスベースの検索機能")
    class EmailBasedSearchOperations {

        @BeforeEach
        void setUpTestData() {
            approverMapper.insert(testApprover1);
            approverMapper.insert(testApprover2);
            approverMapper.insert(expiredApprover);
            approverMapper.insert(futureApprover);
        }

        @Test
        @DisplayName("対象者メールアドレスで承認者関係を検索")
        void testFindByTargetEmail() {
            // When
            List<Approver> found = approverMapper.findByTargetEmail("developer@example.com");

            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getApproverEmail()).isEqualTo("manager@example.com");
        }

        @Test
        @DisplayName("承認者メールアドレスで承認者関係を検索")
        void testFindByApproverEmail() {
            // When
            List<Approver> found = approverMapper.findByApproverEmail("manager@example.com");

            // Then
            assertThat(found).hasSize(2); // testApprover1とfutureApprover
            assertThat(found).anyMatch(a -> a.getTargetEmail().equals("developer@example.com"));
            assertThat(found).anyMatch(a -> a.getTargetEmail().equals("newbie@example.com"));
        }

        @Test
        @DisplayName("存在しないメールアドレスで検索した場合の動作")
        void testFindByNonExistentEmail() {
            // When
            List<Approver> foundByTarget = approverMapper.findByTargetEmail("nonexistent@example.com");
            List<Approver> foundByApprover = approverMapper.findByApproverEmail("nonexistent@example.com");

            // Then
            assertThat(foundByTarget).isEmpty();
            assertThat(foundByApprover).isEmpty();
        }
    }

    @Nested
    @DisplayName("日付ベースの有効性チェック")
    class DateBasedValidityChecks {

        @BeforeEach
        void setUpTestData() {
            approverMapper.insert(testApprover1);
            approverMapper.insert(testApprover2);
            approverMapper.insert(expiredApprover);
            approverMapper.insert(futureApprover);
        }

        @Test
        @DisplayName("指定日の有効な承認者を取得")
        void testFindValidApproversForDate() {
            // When - 現在日で検索
            LocalDate today = LocalDate.now();
            List<Approver> validToday = approverMapper.findValidApproversForDate("developer@example.com", today);

            // Then
            assertThat(validToday).hasSize(1);
            assertThat(validToday.get(0).getApproverEmail()).isEqualTo("manager@example.com");

            // When - 過去の日付で検索（期限切れを含む）
            LocalDate pastDate = LocalDate.now().minusDays(50);
            List<Approver> validInPast = approverMapper.findValidApproversForDate("intern@example.com", pastDate);

            // Then
            assertThat(validInPast).hasSize(1);
            assertThat(validInPast.get(0).getApproverEmail()).isEqualTo("senior@example.com");
        }

        @Test
        @DisplayName("有効な承認者関係の存在チェック")
        void testIsValidApprover() {
            // Given
            LocalDate today = LocalDate.now();

            // When & Then - 現在有効な関係
            assertTrue(approverMapper.isValidApprover("developer@example.com", "manager@example.com", today));

            // When & Then - 期限切れの関係
            assertFalse(approverMapper.isValidApprover("intern@example.com", "senior@example.com", today));

            // When & Then - 未来に有効になる関係
            assertFalse(approverMapper.isValidApprover("newbie@example.com", "manager@example.com", today));

            // When & Then - 未来の日付で有効になる関係
            LocalDate futureDate = LocalDate.now().plusDays(20);
            assertTrue(approverMapper.isValidApprover("newbie@example.com", "manager@example.com", futureDate));

            // When & Then - 存在しない関係
            assertFalse(approverMapper.isValidApprover("nonexistent@example.com", "manager@example.com", today));
        }

        @Test
        @DisplayName("無期限の承認者関係の有効性チェック")
        void testValidApproverWithNoEndDate() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate futureDate = LocalDate.now().plusYears(10);

            // When & Then - 無期限の関係（testApprover2）
            assertTrue(approverMapper.isValidApprover("senior@example.com", "director@example.com", today));
            assertTrue(approverMapper.isValidApprover("senior@example.com", "director@example.com", futureDate));
        }

        @Test
        @DisplayName("境界値での有効性チェック")
        void testBoundaryDateValidityChecks() {
            // Given
            LocalDate startDate = testApprover1.getEffectiveFrom().toLocalDate();
            LocalDate endDate = testApprover1.getEffectiveTo().toLocalDate();

            // When & Then - 開始日当日
            assertTrue(approverMapper.isValidApprover("developer@example.com", "manager@example.com", startDate));

            // When & Then - 終了日当日
            assertTrue(approverMapper.isValidApprover("developer@example.com", "manager@example.com", endDate));

            // When & Then - 開始日の前日
            assertFalse(approverMapper.isValidApprover("developer@example.com", "manager@example.com", startDate.minusDays(1)));

            // When & Then - 終了日の翌日
            assertFalse(approverMapper.isValidApprover("developer@example.com", "manager@example.com", endDate.plusDays(1)));
        }
    }

    @Nested
    @DisplayName("削除機能")
    class DeleteOperations {

        @BeforeEach
        void setUpTestData() {
            approverMapper.insert(testApprover1);
            approverMapper.insert(testApprover2);
        }

        @Test
        @DisplayName("対象者と承認者の組み合わせで削除")
        void testDeleteByTargetAndApprover() {
            // Given
            assertThat(approverMapper.findByTargetEmail("developer@example.com")).hasSize(1);

            // When
            approverMapper.deleteByTargetAndApprover("developer@example.com", "manager@example.com");

            // Then
            assertThat(approverMapper.findByTargetEmail("developer@example.com")).isEmpty();
            // 他のレコードは影響を受けない
            assertThat(approverMapper.findByTargetEmail("senior@example.com")).hasSize(1);
        }

        @Test
        @DisplayName("存在しない組み合わせでの削除")
        void testDeleteNonExistentCombination() {
            // Given
            int initialCount = approverMapper.countAll();

            // When
            approverMapper.deleteByTargetAndApprover("nonexistent@example.com", "manager@example.com");

            // Then - 何も削除されない
            assertThat(approverMapper.countAll()).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("バッチ処理向け機能")
    class BatchProcessingOperations {

        @BeforeEach
        void setUpTestData() {
            // Clear any existing data to ensure test isolation
            approverMapper.deleteAllForTesting();
            
            approverMapper.insert(testApprover1);
            approverMapper.insert(testApprover2);
            approverMapper.insert(expiredApprover);
            approverMapper.insert(futureApprover);
        }

        @Test
        @DisplayName("対象者でグループ化した承認者関係の取得")
        void testFindAllGroupedByTarget() {
            // When
            List<ApproverGrouping> groupings = approverMapper.findAllGroupedByTarget();

            // Then
            assertThat(groupings).hasSize(4); // 4つの異なる対象者
            
            // developer@example.comの承認者をチェック
            ApproverGrouping developerGroup = groupings.stream()
                .filter(g -> g.getTargetEmail().equals("developer@example.com"))
                .findFirst()
                .orElse(null);
            assertThat(developerGroup).isNotNull();
            assertThat(developerGroup.getApproverEmails()).contains("manager@example.com");
        }

        @Test
        @DisplayName("現在有効な承認者関係のみでグループ化")
        void testFindAllGroupedByTargetOnlyActive() {
            // When - 現在有効なもののみ取得するクエリ
            List<ApproverGrouping> activeGroupings = approverMapper.findActiveGroupedByTarget();

            // Then
            assertThat(activeGroupings).hasSize(2); // 現在有効なのは2つ（testApprover1, testApprover2）
            
            assertThat(activeGroupings).anyMatch(g -> 
                g.getTargetEmail().equals("developer@example.com") && 
                g.getApproverEmails().contains("manager@example.com"));
            
            assertThat(activeGroupings).anyMatch(g -> 
                g.getTargetEmail().equals("senior@example.com") && 
                g.getApproverEmails().contains("director@example.com"));
        }
    }

    @Nested
    @DisplayName("後方互換性テスト")
    class BackwardCompatibilityTests {

        @BeforeEach
        void setUpTestData() {
            approverMapper.insert(testApprover1);
            approverMapper.insert(testApprover2);
            approverMapper.insert(expiredApprover);
        }

        @Test
        @DisplayName("既存のfindCurrentByUserIdメソッド（Deprecated）")
        void testFindCurrentByUserId() {
            // When - メールアドレスをuserIdとして扱う
            Optional<Approver> found = approverMapper.findCurrentByUserId("developer@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getApproverEmail()).isEqualTo("manager@example.com");
        }

        @Test
        @DisplayName("既存のfindByUserIdAndDateメソッド（Deprecated）")
        void testFindByUserIdAndDate() {
            // When
            Optional<Approver> found = approverMapper.findByUserIdAndDate("developer@example.com", LocalDate.now());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getApproverEmail()).isEqualTo("manager@example.com");
        }

        @Test
        @DisplayName("既存のfindByApproverIdメソッド（Deprecated）")
        void testFindByApproverId() {
            // When
            List<Approver> found = approverMapper.findByApproverId("manager@example.com");

            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getTargetEmail()).isEqualTo("developer@example.com");
        }

        @Test
        @DisplayName("既存のfindHistoryByUserIdメソッド（Deprecated）")
        void testFindHistoryByUserId() {
            // When
            List<Approver> history = approverMapper.findHistoryByUserId("developer@example.com");

            // Then
            assertThat(history).hasSize(1);
            assertThat(history.get(0).getApproverEmail()).isEqualTo("manager@example.com");
        }
    }

    private void createTestUser(String email, String username) {
        // Check if user already exists to avoid duplicate key errors
        if (userMapper.findByEmail(email).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            userMapper.insert(username + "-id", username, email, "Test User " + username,
                             User.UserStatus.ACTIVE, null, now, now);
        }
    }
}