package com.devhour.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;

@SpringBootTest
@Transactional
@DisplayName("ApprovalAuthorityRepository テスト")
class ApprovalAuthorityRepositoryTest {

    @Autowired
    private ApprovalAuthorityRepository repository;
    
    private ApprovalAuthority testAuthority;
    private ApprovalAuthority managerAuthority;
    private ApprovalAuthority departmentManagerAuthority;
    
    @BeforeEach
    void setUp() {
        // テスト用承認権限エンティティの準備
        testAuthority = ApprovalAuthority.create(
            "test.manager@company.com",
            "テスト マネージャー",
            Position.MANAGER,
            "L1001",
            "開発本部",
            "L2001", 
            "システム部",
            null,
            null,
            null,
            null
        );
        
        managerAuthority = ApprovalAuthority.create(
            "manager.sub@company.com",
            "サブ マネージャー",
            Position.MANAGER,
            "L1001",
            "開発本部",
            "L2002",
            "インフラ部", 
            null,
            null,
            null,
            null
        );
        
        departmentManagerAuthority = ApprovalAuthority.create(
            "dept.manager@company.com",
            "部長",
            Position.DEPARTMENT_MANAGER,
            "L1001",
            "開発本部",
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    @Nested
    @DisplayName("全件取得機能")
    class FindAllOperations {
        
        @Test
        @DisplayName("全承認権限を取得できる")
        void shouldFindAllApprovalAuthorities() {
            // Given
            repository.save(testAuthority);
            repository.save(managerAuthority);
            repository.save(departmentManagerAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.findAll();
            
            // Then
            assertThat(found).hasSize(3);
            assertThat(found).extracting(ApprovalAuthority::getEmail)
                .containsExactlyInAnyOrder(
                    "test.manager@company.com",
                    "manager.sub@company.com",
                    "dept.manager@company.com"
                );
        }
        
        @Test
        @DisplayName("データが存在しない場合は空リストを返す")
        void shouldReturnEmptyListWhenNoData() {
            // When
            List<ApprovalAuthority> found = repository.findAll();
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("メールアドレス検索機能")
    class FindByEmailOperations {
        
        @Test
        @DisplayName("メールアドレスで承認権限を検索できる")
        void shouldFindByEmail() {
            // Given
            repository.save(testAuthority);
            
            // When
            Optional<ApprovalAuthority> found = repository.findByEmail("test.manager@company.com");
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("テスト マネージャー");
            assertThat(found.get().getPosition()).isEqualTo(Position.MANAGER);
        }
        
        @Test
        @DisplayName("存在しないメールアドレスの場合は空を返す")
        void shouldReturnEmptyWhenEmailNotFound() {
            // When
            Optional<ApprovalAuthority> found = repository.findByEmail("nonexistent@company.com");
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("名前・メール検索機能")
    class SearchByNameOrEmailOperations {
        
        @Test
        @DisplayName("名前の部分一致で検索できる")
        void shouldSearchByNamePattern() {
            // Given
            repository.save(testAuthority);
            repository.save(managerAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.searchByNameOrEmail("マネージャー");
            
            // Then
            assertThat(found).hasSize(2);
            assertThat(found).extracting(ApprovalAuthority::getName)
                .allMatch(name -> name.contains("マネージャー"));
        }
        
        @Test
        @DisplayName("メールアドレスの部分一致で検索できる")
        void shouldSearchByEmailPattern() {
            // Given
            repository.save(testAuthority);
            repository.save(departmentManagerAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.searchByNameOrEmail("manager@company");
            
            // Then
            assertThat(found).hasSize(2);
            assertThat(found).extracting(ApprovalAuthority::getEmail)
                .allMatch(email -> email.contains("manager@company"));
        }
        
        @Test
        @DisplayName("検索結果は階層レベル降順、名前昇順でソートされる")
        void shouldReturnResultsSortedByHierarchyAndName() {
            // Given
            repository.save(managerAuthority); // MANAGER (レベル1)
            repository.save(testAuthority); // MANAGER (レベル1) 
            repository.save(departmentManagerAuthority); // DEPARTMENT_MANAGER (レベル2)
            
            // When
            List<ApprovalAuthority> found = repository.searchByNameOrEmail("@company");
            
            // Then
            assertThat(found).hasSize(3);
            // 階層レベル降順（部長 > マネージャー）、同レベル内では名前昇順
            assertThat(found.get(0).getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
            assertThat(found.get(1).getPosition()).isEqualTo(Position.MANAGER);
            assertThat(found.get(2).getPosition()).isEqualTo(Position.MANAGER);
        }
        
        @Test
        @DisplayName("マッチしない場合は空リストを返す")
        void shouldReturnEmptyWhenNoMatch() {
            // Given
            repository.save(testAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.searchByNameOrEmail("存在しない検索語");
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("保存機能")
    class SaveOperations {
        
        @Test
        @DisplayName("新規承認権限を保存できる")
        void shouldSaveNewApprovalAuthority() {
            // When
            ApprovalAuthority saved = repository.save(testAuthority);
            
            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo("test.manager@company.com");
            assertThat(saved.getName()).isEqualTo("テスト マネージャー");
        }
        
        @Test
        @DisplayName("既存承認権限を更新できる")
        void shouldUpdateExistingApprovalAuthority() {
            // Given
            repository.save(testAuthority);
            // Don't change email to avoid constraint violation - just update other fields
            testAuthority.updateInfo(
                testAuthority.getEmail(), // Keep the same email
                "更新された マネージャー",
                Position.DEPARTMENT_MANAGER,
                "L1002",
                "営業本部",
                null, null, null, null, null, null
            );
            
            // When
            ApprovalAuthority updated = repository.save(testAuthority);
            Optional<ApprovalAuthority> found = repository.findByEmail(testAuthority.getEmail());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("更新された マネージャー");
            assertThat(found.get().getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
            assertThat(found.get().getLevel1Name()).isEqualTo("営業本部");
        }
    }

    @Nested
    @DisplayName("削除機能")
    class DeleteOperations {
        
        @Test
        @DisplayName("メールアドレスで承認権限を削除できる")
        void shouldDeleteByEmail() {
            // Given
            repository.save(testAuthority);
            
            // When
            repository.deleteByEmail("test.manager@company.com");
            Optional<ApprovalAuthority> found = repository.findByEmail("test.manager@company.com");
            
            // Then
            assertThat(found).isEmpty();
        }
        
        @Test
        @DisplayName("存在しないメールアドレスの削除は例外を起こさない")
        void shouldNotThrowExceptionWhenDeletingNonExistentEmail() {
            // When & Then
            repository.deleteByEmail("nonexistent@company.com");
            // 例外が発生しないことを確認
        }
    }

    @Nested
    @DisplayName("役職検索機能")
    class FindByPositionOperations {
        
        @Test
        @DisplayName("役職で承認権限を検索できる")
        void shouldFindByPosition() {
            // Given
            repository.save(testAuthority); // MANAGER
            repository.save(managerAuthority); // MANAGER
            repository.save(departmentManagerAuthority); // DEPARTMENT_MANAGER
            
            // When
            List<ApprovalAuthority> managers = repository.findByPosition(Position.MANAGER);
            List<ApprovalAuthority> deptManagers = repository.findByPosition(Position.DEPARTMENT_MANAGER);
            
            // Then
            assertThat(managers).hasSize(2);
            assertThat(managers).allMatch(auth -> auth.getPosition() == Position.MANAGER);
            
            assertThat(deptManagers).hasSize(1);
            assertThat(deptManagers.get(0).getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
        }
        
        @Test
        @DisplayName("該当する役職が存在しない場合は空リストを返す")
        void shouldReturnEmptyWhenPositionNotFound() {
            // Given
            repository.save(testAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.findByPosition(Position.GENERAL_MANAGER);
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("組織レベル検索機能")
    class FindByLevelCodeOperations {
        
        @Test
        @DisplayName("Level1コードで承認権限を検索できる")
        void shouldFindByLevel1Code() {
            // Given
            repository.save(testAuthority);
            repository.save(managerAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.findByLevelCode("L1001", 1);
            
            // Then
            assertThat(found).hasSize(2);
            assertThat(found).allMatch(auth -> "L1001".equals(auth.getLevel1Code()));
        }
        
        @Test
        @DisplayName("Level2コードで承認権限を検索できる")
        void shouldFindByLevel2Code() {
            // Given
            repository.save(testAuthority); // L2001
            repository.save(managerAuthority); // L2002
            
            // When
            List<ApprovalAuthority> found = repository.findByLevelCode("L2001", 2);
            
            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getLevel2Code()).isEqualTo("L2001");
        }
        
        @Test
        @DisplayName("不正なレベル指定時は例外を投げる")
        void shouldThrowExceptionForInvalidLevel() {
            // When & Then
            assertThatThrownBy(() -> repository.findByLevelCode("L1001", 0))
                .isInstanceOf(IllegalArgumentException.class);
                
            assertThatThrownBy(() -> repository.findByLevelCode("L1001", 5))
                .isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("該当する組織コードが存在しない場合は空リストを返す")
        void shouldReturnEmptyWhenLevelCodeNotFound() {
            // Given
            repository.save(testAuthority);
            
            // When
            List<ApprovalAuthority> found = repository.findByLevelCode("NONEXISTENT", 1);
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("存在チェック機能")
    class ExistsOperations {
        
        @Test
        @DisplayName("存在する場合はtrueを返す")
        void shouldReturnTrueWhenExists() {
            // Given
            repository.save(testAuthority);
            
            // When
            boolean exists = repository.existsByEmail("test.manager@company.com");
            
            // Then
            assertThat(exists).isTrue();
        }
        
        @Test
        @DisplayName("存在しない場合はfalseを返す")
        void shouldReturnFalseWhenNotExists() {
            // When
            boolean exists = repository.existsByEmail("nonexistent@company.com");
            
            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("カウント機能")
    class CountOperations {
        
        @Test
        @DisplayName("組織レベルコードで承認権限者数をカウントできる")
        void shouldCountByLevelCode() {
            // Given
            repository.save(testAuthority);
            repository.save(managerAuthority);
            repository.save(departmentManagerAuthority);
            
            // When
            long count = repository.countByLevelCode("L1001", 1);
            
            // Then
            assertThat(count).isEqualTo(3);
        }
        
        @Test
        @DisplayName("該当する組織が存在しない場合は0を返す")
        void shouldReturnZeroWhenLevelCodeNotFound() {
            // When
            long count = repository.countByLevelCode("NONEXISTENT", 1);
            
            // Then
            assertThat(count).isEqualTo(0);
        }
        
        @Test
        @DisplayName("不正なレベル指定時は例外を投げる")
        void shouldThrowExceptionForInvalidLevelInCount() {
            // When & Then
            assertThatThrownBy(() -> repository.countByLevelCode("L1001", 0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("承認権限保有者検索機能")
    class FindAllWithApprovalAuthorityOperations {
        
        @Test
        @DisplayName("承認権限を持つユーザーのみを取得する")
        void shouldFindOnlyUsersWithApprovalAuthority() {
            // Given - 一般社員の承認権限エンティティは実際には作成されないが、テスト用に他のエンティティで検証
            repository.save(testAuthority); // MANAGER
            repository.save(managerAuthority); // MANAGER  
            repository.save(departmentManagerAuthority); // DEPARTMENT_MANAGER
            
            // When
            List<ApprovalAuthority> found = repository.findAllWithApprovalAuthority();
            
            // Then
            assertThat(found).hasSize(3);
            assertThat(found).allMatch(ApprovalAuthority::hasApprovalAuthority);
        }
        
        @Test
        @DisplayName("結果は階層レベル降順、名前昇順でソートされる")
        void shouldReturnResultsSortedByHierarchyAndNameForApprovalAuthority() {
            // Given
            repository.save(managerAuthority); // MANAGER
            repository.save(testAuthority); // MANAGER
            repository.save(departmentManagerAuthority); // DEPARTMENT_MANAGER
            
            // When
            List<ApprovalAuthority> found = repository.findAllWithApprovalAuthority();
            
            // Then
            assertThat(found).hasSize(3);
            // 階層レベル降順で部長が最初に来ることを確認
            assertThat(found.get(0).getPosition()).isEqualTo(Position.DEPARTMENT_MANAGER);
        }
        
        @Test
        @DisplayName("承認権限者が存在しない場合は空リストを返す")
        void shouldReturnEmptyWhenNoApprovalAuthority() {
            // When
            List<ApprovalAuthority> found = repository.findAllWithApprovalAuthority();
            
            // Then
            assertThat(found).isEmpty();
        }
    }
}