package com.devhour.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;
import com.devhour.infrastructure.mapper.ApprovalAuthorityMapper;

/**
 * ApprovalAuthorityRepositoryImplの単体テスト
 * 
 * MyBatisマッパーをモックしてリポジトリ層のテストを実装
 * トランザクション管理、バリデーション、ビジネスロジックをテスト
 */
@DisplayName("ApprovalAuthorityRepositoryImpl単体テスト")
class ApprovalAuthorityRepositoryImplTest {

    @Mock
    private ApprovalAuthorityMapper mapper;

    private ApprovalAuthorityRepositoryImpl repository;

    private ApprovalAuthority testManager;
    private ApprovalAuthority testDepartmentManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ApprovalAuthorityRepositoryImpl(mapper);

        // テスト用の承認権限者（マネージャー）
        testManager = ApprovalAuthority.create(
            "manager@example.com",
            "Manager Tanaka", 
            Position.MANAGER,
            "DEV001", "開発本部",
            "DEV001-01", "第1開発部",
            null, null,
            null, null
        );

        // テスト用の承認権限者（部長）
        testDepartmentManager = ApprovalAuthority.create(
            "dept.manager@example.com",
            "Department Manager Suzuki",
            Position.DEPARTMENT_MANAGER,
            "DEV001", "開発本部",
            "DEV001-01", "第1開発部", 
            "DEV001-01-01", "第1開発課",
            null, null
        );

    }

    @Test
    @DisplayName("findAll: 全承認権限を取得できる")
    void findAll_ReturnsAllApprovalAuthorities() {
        // Arrange
        List<ApprovalAuthority> expected = Arrays.asList(testManager, testDepartmentManager);
        when(mapper.findAll()).thenReturn(expected);

        // Act
        List<ApprovalAuthority> result = repository.findAll();

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(2);
        verify(mapper).findAll();
    }

    @Test
    @DisplayName("findByEmail: 正常なメールアドレスで承認権限を検索できる")
    void findByEmail_ValidEmail_ReturnsApprovalAuthority() {
        // Arrange
        String email = "manager@example.com";
        when(mapper.findByEmail(email)).thenReturn(Optional.of(testManager));

        // Act
        Optional<ApprovalAuthority> result = repository.findByEmail(email);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("manager@example.com");
        assertThat(result.get().getName()).isEqualTo("Manager Tanaka");
        assertThat(result.get().getPosition()).isEqualTo(Position.MANAGER);
        verify(mapper).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail: 存在しないメールアドレスでは空を返す")
    void findByEmail_NonExistentEmail_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(mapper.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<ApprovalAuthority> result = repository.findByEmail(email);

        // Assert
        assertThat(result).isEmpty();
        verify(mapper).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail: nullメールアドレスでは例外を発生させる")
    void findByEmail_NullEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.findByEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("メールアドレスは必須です");
    }

    @Test
    @DisplayName("findByEmail: 空文字メールアドレスでは例外を発生させる")
    void findByEmail_EmptyEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.findByEmail(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("メールアドレスは必須です");
    }

    @Test
    @DisplayName("searchByNameOrEmail: クエリで部分一致検索できる")
    void searchByNameOrEmail_ValidQuery_ReturnsMatches() {
        // Arrange
        String query = "Manager";
        List<ApprovalAuthority> expected = Arrays.asList(testManager, testDepartmentManager);
        when(mapper.searchByNameOrEmail(query)).thenReturn(expected);

        // Act
        List<ApprovalAuthority> result = repository.searchByNameOrEmail(query);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(2);
        verify(mapper).searchByNameOrEmail(query);
    }

    @Test
    @DisplayName("searchByNameOrEmail: nullクエリでは例外を発生させる")
    void searchByNameOrEmail_NullQuery_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.searchByNameOrEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("検索クエリは必須です");
    }

    @Test
    @DisplayName("searchByNameOrEmail: 空文字クエリでは例外を発生させる")
    void searchByNameOrEmail_EmptyQuery_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.searchByNameOrEmail(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("検索クエリは必須です");
    }

    @Test
    @DisplayName("save: 新規承認権限を保存できる（insert）")
    void save_NewApprovalAuthority_InsertsSuccessfully() {
        // Arrange
        when(mapper.findByEmail(testManager.getEmail())).thenReturn(Optional.empty());

        // Act
        ApprovalAuthority result = repository.save(testManager);

        // Assert
        assertThat(result).isEqualTo(testManager);
        verify(mapper).findByEmail(testManager.getEmail());
        verify(mapper).insert(testManager);
    }

    @Test
    @DisplayName("save: 既存承認権限を更新できる（update）")
    void save_ExistingApprovalAuthority_UpdatesSuccessfully() {
        // Arrange
        ApprovalAuthority existing = ApprovalAuthority.restore(
            testManager.getId(),
            testManager.getEmail(),
            "Old Name", 
            Position.EMPLOYEE,
            "OLD001", "旧組織",
            null, null,
            null, null,
            null, null,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1)
        );
        when(mapper.findByEmail(testManager.getEmail())).thenReturn(Optional.of(existing));

        // Act
        ApprovalAuthority result = repository.save(testManager);

        // Assert
        assertThat(result).isEqualTo(testManager);
        verify(mapper).findByEmail(testManager.getEmail());
        verify(mapper).update(testManager);
    }

    @Test
    @DisplayName("save: nullエンティティでは例外を発生させる")
    void save_NullEntity_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("承認権限エンティティは必須です");
    }

    @Test
    @DisplayName("deleteByEmail: メールアドレスで削除できる")
    void deleteByEmail_ValidEmail_DeletesSuccessfully() {
        // Arrange
        String email = "manager@example.com";

        // Act
        repository.deleteByEmail(email);

        // Assert
        verify(mapper).deleteByEmail(email);
    }

    @Test
    @DisplayName("deleteByEmail: nullメールアドレスでは例外を発生させる")
    void deleteByEmail_NullEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.deleteByEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("メールアドレスは必須です");
    }

    @Test
    @DisplayName("findByPosition: 役職で検索できる")
    void findByPosition_ValidPosition_ReturnsMatches() {
        // Arrange
        Position position = Position.MANAGER;
        List<ApprovalAuthority> expected = Arrays.asList(testManager);
        when(mapper.findByPosition(position)).thenReturn(expected);

        // Act
        List<ApprovalAuthority> result = repository.findByPosition(position);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(1);
        verify(mapper).findByPosition(position);
    }

    @Test
    @DisplayName("findByPosition: null役職では例外を発生させる")
    void findByPosition_NullPosition_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.findByPosition(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("役職は必須です");
    }

    @Test
    @DisplayName("findByLevelCode: 組織レベルコードで検索できる")
    void findByLevelCode_ValidParameters_ReturnsMatches() {
        // Arrange
        String levelCode = "DEV001";
        int level = 1;
        List<ApprovalAuthority> expected = Arrays.asList(testManager, testDepartmentManager);
        when(mapper.findByLevelCode(levelCode, level)).thenReturn(expected);

        // Act
        List<ApprovalAuthority> result = repository.findByLevelCode(levelCode, level);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(2);
        verify(mapper).findByLevelCode(levelCode, level);
    }

    @Test
    @DisplayName("findByLevelCode: nullレベルコードでは例外を発生させる")
    void findByLevelCode_NullLevelCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.findByLevelCode(null, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("組織レベルコードは必須です");
    }

    @Test
    @DisplayName("findByLevelCode: 範囲外レベルでは例外を発生させる")
    void findByLevelCode_InvalidLevel_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.findByLevelCode("DEV001", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("レベルは1から4の間で指定してください");

        assertThatThrownBy(() -> repository.findByLevelCode("DEV001", 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("レベルは1から4の間で指定してください");
    }

    @Test
    @DisplayName("existsByEmail: 存在するメールアドレスではtrueを返す")
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // Arrange
        String email = "manager@example.com";
        when(mapper.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = repository.existsByEmail(email);

        // Assert
        assertThat(result).isTrue();
        verify(mapper).existsByEmail(email);
    }

    @Test
    @DisplayName("existsByEmail: 存在しないメールアドレスではfalseを返す")
    void existsByEmail_NonExistentEmail_ReturnsFalse() {
        // Arrange
        String email = "nonexistent@example.com";
        when(mapper.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = repository.existsByEmail(email);

        // Assert
        assertThat(result).isFalse();
        verify(mapper).existsByEmail(email);
    }

    @Test
    @DisplayName("existsByEmail: nullメールアドレスでは例外を発生させる")
    void existsByEmail_NullEmail_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.existsByEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("メールアドレスは必須です");
    }

    @Test
    @DisplayName("countByLevelCode: 組織レベル別の承認権限者数をカウントできる")
    void countByLevelCode_ValidParameters_ReturnsCount() {
        // Arrange
        String levelCode = "DEV001";
        int level = 1;
        long expectedCount = 3L;
        when(mapper.countByLevelCode(levelCode, level)).thenReturn(expectedCount);

        // Act
        long result = repository.countByLevelCode(levelCode, level);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(mapper).countByLevelCode(levelCode, level);
    }

    @Test
    @DisplayName("countByLevelCode: nullレベルコードでは例外を発生させる")
    void countByLevelCode_NullLevelCode_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.countByLevelCode(null, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("組織レベルコードは必須です");
    }

    @Test
    @DisplayName("countByLevelCode: 範囲外レベルでは例外を発生させる")
    void countByLevelCode_InvalidLevel_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> repository.countByLevelCode("DEV001", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("レベルは1から4の間で指定してください");
    }

    @Test
    @DisplayName("findAllWithApprovalAuthority: 承認権限を持つユーザー一覧を取得できる")
    void findAllWithApprovalAuthority_ReturnsAllWithApprovalAuthority() {
        // Arrange
        List<ApprovalAuthority> expected = Arrays.asList(testManager, testDepartmentManager);
        when(mapper.findAllWithApprovalAuthority()).thenReturn(expected);

        // Act
        List<ApprovalAuthority> result = repository.findAllWithApprovalAuthority();

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(2);
        verify(mapper).findAllWithApprovalAuthority();
    }
}