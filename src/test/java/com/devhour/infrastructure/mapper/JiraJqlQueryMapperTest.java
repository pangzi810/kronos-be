package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import com.devhour.domain.model.entity.JiraJqlQuery;

/**
 * JqlQueryMapperの統合テスト
 * 
 * JIRA同期機能のJQLクエリマッパーのデータアクセス操作をテスト
 * MyBatisを使用したアノテーションベースのマッピングの動作確認
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("JqlQueryMapper統合テスト")
class JiraJqlQueryMapperTest {

    @Autowired
    private JiraJqlQueryMapper jqlQueryMapper;

    private String testTemplateId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTemplateId = "test-template-id";
        testTime = LocalDateTime.now().withNano(0);

        // Clear existing data for test isolation
        jqlQueryMapper.selectAllWithPagination(Integer.MAX_VALUE, 0).forEach(query ->
            jqlQueryMapper.deleteById(query.getId())
        );

        // テストデータ用のテンプレートを事前に作成
        setupTestTemplate();
    }

    private void setupTestTemplate() {
        // response_template テーブルに必要なテストデータを挿入
        // （外部キー制約のため）
        jqlQueryMapper.insertTestTemplate(testTemplateId, "Test Template", 
            "Test Velocity Template", "Test template description", testTime, testTime);
    }

    @Test
    @DisplayName("JQLクエリ挿入 - 正常ケース")
    void insert_Success() {
        // Arrange
        JiraJqlQuery jqlQuery = JiraJqlQuery.createNew(
            "Test Query",
            "project = TEST AND status = Open",
            testTemplateId,
            100,
            "test-user"
        );

        // Act
        jqlQueryMapper.insert(
            jqlQuery.getId(),
            jqlQuery.getQueryName(),
            jqlQuery.getJqlExpression(),
            jqlQuery.getTemplateId(),
            jqlQuery.isActive(),
            jqlQuery.getPriority(),
            jqlQuery.getCreatedAt(),
            jqlQuery.getUpdatedAt(),
            jqlQuery.getCreatedBy(),
            jqlQuery.getUpdatedBy()
        );

        // Assert
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById(jqlQuery.getId());
        assertThat(result).isPresent();
        JiraJqlQuery retrieved = result.get();
        assertThat(retrieved.getId()).isEqualTo(jqlQuery.getId());
        assertThat(retrieved.getQueryName()).isEqualTo("Test Query");
        assertThat(retrieved.getJqlExpression()).isEqualTo("project = TEST AND status = Open");
        assertThat(retrieved.getTemplateId()).isEqualTo(testTemplateId);
        assertThat(retrieved.isActive()).isTrue();
        assertThat(retrieved.getPriority()).isEqualTo(100);
        assertThat(retrieved.getCreatedBy()).isEqualTo("test-user");
        assertThat(retrieved.getCreatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedBy()).isNull();
    }

    @Test
    @DisplayName("ID検索 - 存在するJQLクエリ")
    void selectById_ExistingQuery_ReturnsQuery() {
        // Arrange
        JiraJqlQuery jqlQuery = JiraJqlQuery.createNew(
            "Find By ID Test",
            "project = FINDID",
            testTemplateId,
            50,
            "test-user"
        );
        jqlQueryMapper.insert(
            jqlQuery.getId(),
            jqlQuery.getQueryName(),
            jqlQuery.getJqlExpression(),
            jqlQuery.getTemplateId(),
            jqlQuery.isActive(),
            jqlQuery.getPriority(),
            jqlQuery.getCreatedAt(),
            jqlQuery.getUpdatedAt(),
            jqlQuery.getCreatedBy(),
            jqlQuery.getUpdatedBy()
        );

        // Act
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById(jqlQuery.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(jqlQuery.getId());
        assertThat(result.get().getQueryName()).isEqualTo("Find By ID Test");
        assertThat(result.get().getJqlExpression()).isEqualTo("project = FINDID");
    }

    @Test
    @DisplayName("ID検索 - 存在しないJQLクエリ")
    void selectById_NonExistingQuery_ReturnsEmpty() {
        // Act
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JQLクエリ更新 - 正常ケース")
    void update_Success() {
        // Arrange
        JiraJqlQuery originalQuery = JiraJqlQuery.createNew(
            "Original Query",
            "project = ORIGINAL",
            testTemplateId,
            75,
            "test-user"
        );
        jqlQueryMapper.insert(
            originalQuery.getId(),
            originalQuery.getQueryName(),
            originalQuery.getJqlExpression(),
            originalQuery.getTemplateId(),
            originalQuery.isActive(),
            originalQuery.getPriority(),
            originalQuery.getCreatedAt(),
            originalQuery.getUpdatedAt(),
            originalQuery.getCreatedBy(),
            originalQuery.getUpdatedBy()
        );

        // JQLクエリを更新
        originalQuery.updateQuery("project = UPDATED", "updater-user");
        originalQuery.updatePriority(150, "updater-user");

        // Act
        int updateCount = jqlQueryMapper.update(
            originalQuery.getId(),
            originalQuery.getQueryName(),
            originalQuery.getJqlExpression(),
            originalQuery.getTemplateId(),
            originalQuery.isActive(),
            originalQuery.getPriority(),
            originalQuery.getUpdatedAt(),
            originalQuery.getUpdatedBy()
        );

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById(originalQuery.getId());
        assertThat(result).isPresent();
        JiraJqlQuery updated = result.get();
        assertThat(updated.getJqlExpression()).isEqualTo("project = UPDATED");
        assertThat(updated.getPriority()).isEqualTo(150);
        assertThat(updated.getUpdatedBy()).isEqualTo("updater-user");
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("JQLクエリ削除 - 正常ケース")
    void deleteById_Success() {
        // Arrange
        JiraJqlQuery jqlQuery = JiraJqlQuery.createNew(
            "Delete Test Query",
            "project = DELETE",
            testTemplateId,
            25,
            "test-user"
        );
        jqlQueryMapper.insert(
            jqlQuery.getId(),
            jqlQuery.getQueryName(),
            jqlQuery.getJqlExpression(),
            jqlQuery.getTemplateId(),
            jqlQuery.isActive(),
            jqlQuery.getPriority(),
            jqlQuery.getCreatedAt(),
            jqlQuery.getUpdatedAt(),
            jqlQuery.getCreatedBy(),
            jqlQuery.getUpdatedBy()
        );

        // Act
        int deleteCount = jqlQueryMapper.deleteById(jqlQuery.getId());

        // Assert
        assertThat(deleteCount).isEqualTo(1);
        
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById(jqlQuery.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("アクティブクエリ取得 - 優先度順")
    void selectActiveQueriesOrderByPriority_MultipleQueries_ReturnsSortedByPriority() {
        // Arrange
        JiraJqlQuery highPriorityQuery = JiraJqlQuery.createNew(
            "High Priority Query",
            "priority = High",
            testTemplateId,
            300,
            "test-user"
        );
        
        JiraJqlQuery lowPriorityQuery = JiraJqlQuery.createNew(
            "Low Priority Query",
            "priority = Low",
            testTemplateId,
            100,
            "test-user"
        );
        
        JiraJqlQuery inactiveQuery = JiraJqlQuery.createNew(
            "Inactive Query",
            "status = Inactive",
            testTemplateId,
            200,
            "test-user"
        );
        inactiveQuery.deactivate(); // 非アクティブ化

        jqlQueryMapper.insert(
            highPriorityQuery.getId(),
            highPriorityQuery.getQueryName(),
            highPriorityQuery.getJqlExpression(),
            highPriorityQuery.getTemplateId(),
            highPriorityQuery.isActive(),
            highPriorityQuery.getPriority(),
            highPriorityQuery.getCreatedAt(),
            highPriorityQuery.getUpdatedAt(),
            highPriorityQuery.getCreatedBy(),
            highPriorityQuery.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            lowPriorityQuery.getId(),
            lowPriorityQuery.getQueryName(),
            lowPriorityQuery.getJqlExpression(),
            lowPriorityQuery.getTemplateId(),
            lowPriorityQuery.isActive(),
            lowPriorityQuery.getPriority(),
            lowPriorityQuery.getCreatedAt(),
            lowPriorityQuery.getUpdatedAt(),
            lowPriorityQuery.getCreatedBy(),
            lowPriorityQuery.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            inactiveQuery.getId(),
            inactiveQuery.getQueryName(),
            inactiveQuery.getJqlExpression(),
            inactiveQuery.getTemplateId(),
            inactiveQuery.isActive(),
            inactiveQuery.getPriority(),
            inactiveQuery.getCreatedAt(),
            inactiveQuery.getUpdatedAt(),
            inactiveQuery.getCreatedBy(),
            inactiveQuery.getUpdatedBy()
        );

        // Act
        List<JiraJqlQuery> result = jqlQueryMapper.selectActiveQueriesOrderByPriority();

        // Assert
        assertThat(result).hasSize(2); // アクティブなクエリのみ
        assertThat(result.get(0).getPriority()).isEqualTo(100); // 低い優先度が先（ASC順）
        assertThat(result.get(1).getPriority()).isEqualTo(300);
        assertThat(result).allMatch(JiraJqlQuery::isActive);
    }

    @Test
    @DisplayName("テンプレートID検索 - 正常ケース")
    void selectByTemplateId_ExistingTemplate_ReturnsQueries() {
        // Arrange
        String specificTemplateId = "specific-template-id";
        jqlQueryMapper.insertTestTemplate(specificTemplateId, "Specific Template", 
            "Specific template", "Description", testTime, testTime);

        JiraJqlQuery query1 = JiraJqlQuery.createNew(
            "Query 1",
            "project = Q1",
            specificTemplateId,
            200,
            "test-user"
        );
        
        JiraJqlQuery query2 = JiraJqlQuery.createNew(
            "Query 2",
            "project = Q2",
            specificTemplateId,
            100,
            "test-user"
        );
        
        // 他のテンプレートのクエリ
        JiraJqlQuery differentTemplateQuery = JiraJqlQuery.createNew(
            "Different Template Query",
            "project = DIFF",
            testTemplateId,
            150,
            "test-user"
        );

        jqlQueryMapper.insert(
            query1.getId(),
            query1.getQueryName(),
            query1.getJqlExpression(),
            query1.getTemplateId(),
            query1.isActive(),
            query1.getPriority(),
            query1.getCreatedAt(),
            query1.getUpdatedAt(),
            query1.getCreatedBy(),
            query1.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            query2.getId(),
            query2.getQueryName(),
            query2.getJqlExpression(),
            query2.getTemplateId(),
            query2.isActive(),
            query2.getPriority(),
            query2.getCreatedAt(),
            query2.getUpdatedAt(),
            query2.getCreatedBy(),
            query2.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            differentTemplateQuery.getId(),
            differentTemplateQuery.getQueryName(),
            differentTemplateQuery.getJqlExpression(),
            differentTemplateQuery.getTemplateId(),
            differentTemplateQuery.isActive(),
            differentTemplateQuery.getPriority(),
            differentTemplateQuery.getCreatedAt(),
            differentTemplateQuery.getUpdatedAt(),
            differentTemplateQuery.getCreatedBy(),
            differentTemplateQuery.getUpdatedBy()
        );

        // Act
        List<JiraJqlQuery> result = jqlQueryMapper.selectByTemplateId(specificTemplateId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPriority()).isEqualTo(100); // 優先度ASC順
        assertThat(result.get(1).getPriority()).isEqualTo(200);
        assertThat(result).allMatch(query -> query.getTemplateId().equals(specificTemplateId));
    }

    @Test
    @DisplayName("クエリ名検索 - 存在するクエリ")
    void selectByQueryName_ExistingQuery_ReturnsQuery() {
        // Arrange
        String queryName = "Unique Query Name";
        JiraJqlQuery jqlQuery = JiraJqlQuery.createNew(
            queryName,
            "project = UNIQUE",
            testTemplateId,
            75,
            "test-user"
        );
        jqlQueryMapper.insert(
            jqlQuery.getId(),
            jqlQuery.getQueryName(),
            jqlQuery.getJqlExpression(),
            jqlQuery.getTemplateId(),
            jqlQuery.isActive(),
            jqlQuery.getPriority(),
            jqlQuery.getCreatedAt(),
            jqlQuery.getUpdatedAt(),
            jqlQuery.getCreatedBy(),
            jqlQuery.getUpdatedBy()
        );

        // Act
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectByQueryName(queryName);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getQueryName()).isEqualTo(queryName);
        assertThat(result.get().getId()).isEqualTo(jqlQuery.getId());
    }

    @Test
    @DisplayName("クエリ名検索 - 存在しないクエリ")
    void selectByQueryName_NonExistingQuery_ReturnsEmpty() {
        // Act
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectByQueryName("Non Existing Query");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("アクティブクエリ数カウント")
    void countActiveQueries_VariousStatuses_ReturnsCorrectCount() {
        // Arrange
        JiraJqlQuery activeQuery1 = JiraJqlQuery.createNew(
            "Active Query 1",
            "status = Open",
            testTemplateId,
            100,
            "test-user"
        );
        
        JiraJqlQuery activeQuery2 = JiraJqlQuery.createNew(
            "Active Query 2",
            "status = In Progress",
            testTemplateId,
            200,
            "test-user"
        );
        
        JiraJqlQuery inactiveQuery = JiraJqlQuery.createNew(
            "Inactive Query",
            "status = Closed",
            testTemplateId,
            300,
            "test-user"
        );
        inactiveQuery.deactivate();

        jqlQueryMapper.insert(
            activeQuery1.getId(),
            activeQuery1.getQueryName(),
            activeQuery1.getJqlExpression(),
            activeQuery1.getTemplateId(),
            activeQuery1.isActive(),
            activeQuery1.getPriority(),
            activeQuery1.getCreatedAt(),
            activeQuery1.getUpdatedAt(),
            activeQuery1.getCreatedBy(),
            activeQuery1.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            activeQuery2.getId(),
            activeQuery2.getQueryName(),
            activeQuery2.getJqlExpression(),
            activeQuery2.getTemplateId(),
            activeQuery2.isActive(),
            activeQuery2.getPriority(),
            activeQuery2.getCreatedAt(),
            activeQuery2.getUpdatedAt(),
            activeQuery2.getCreatedBy(),
            activeQuery2.getUpdatedBy()
        );
        jqlQueryMapper.insert(
            inactiveQuery.getId(),
            inactiveQuery.getQueryName(),
            inactiveQuery.getJqlExpression(),
            inactiveQuery.getTemplateId(),
            inactiveQuery.isActive(),
            inactiveQuery.getPriority(),
            inactiveQuery.getCreatedAt(),
            inactiveQuery.getUpdatedAt(),
            inactiveQuery.getCreatedBy(),
            inactiveQuery.getUpdatedBy()
        );

        // Act
        long count = jqlQueryMapper.countActiveQueries();

        // Assert
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("ページネーション対応クエリ取得")
    void selectAllWithPagination_WithLimitOffset_ReturnsCorrectPage() {
        // Arrange
        // 複数のクエリを作成（異なる優先度で）
        for (int i = 1; i <= 5; i++) {
            JiraJqlQuery query = JiraJqlQuery.createNew(
                "Query " + i,
                "project = TEST" + i,
                testTemplateId,
                i * 10,
                "test-user"
            );
            jqlQueryMapper.insert(
                query.getId(),
                query.getQueryName(),
                query.getJqlExpression(),
                query.getTemplateId(),
                query.isActive(),
                query.getPriority(),
                query.getCreatedAt(),
                query.getUpdatedAt(),
                query.getCreatedBy(),
                query.getUpdatedBy()
            );
        }

        // Act
        List<JiraJqlQuery> firstPage = jqlQueryMapper.selectAllWithPagination(2, 0);
        List<JiraJqlQuery> secondPage = jqlQueryMapper.selectAllWithPagination(2, 2);

        // Assert
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        
        // 優先度順（ASC）で並んでいることを確認
        assertThat(firstPage.get(0).getPriority()).isLessThan(firstPage.get(1).getPriority());
        assertThat(secondPage.get(0).getPriority()).isLessThan(secondPage.get(1).getPriority());
        assertThat(firstPage.get(1).getPriority()).isLessThan(secondPage.get(0).getPriority());
    }

    @Test
    @DisplayName("アクティブ状態更新")
    void updateActiveStatus_ValidInput_UpdatesStatus() {
        // Arrange
        JiraJqlQuery jqlQuery = JiraJqlQuery.createNew(
            "Status Update Test",
            "project = STATUS",
            testTemplateId,
            100,
            "test-user"
        );
        jqlQueryMapper.insert(
            jqlQuery.getId(),
            jqlQuery.getQueryName(),
            jqlQuery.getJqlExpression(),
            jqlQuery.getTemplateId(),
            jqlQuery.isActive(),
            jqlQuery.getPriority(),
            jqlQuery.getCreatedAt(),
            jqlQuery.getUpdatedAt(),
            jqlQuery.getCreatedBy(),
            jqlQuery.getUpdatedBy()
        );
        
        LocalDateTime updateTime = LocalDateTime.now().withNano(0);

        // Act
        int updateCount = jqlQueryMapper.updateActiveStatus(
            jqlQuery.getId(), 
            false, 
            updateTime, 
            "updater-user"
        );

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<JiraJqlQuery> result = jqlQueryMapper.selectById(jqlQuery.getId());
        assertThat(result).isPresent();
        JiraJqlQuery updated = result.get();
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getUpdatedBy()).isEqualTo("updater-user");
        assertThat(updated.getUpdatedAt()).isEqualToIgnoringNanos(updateTime);
    }

    @Test
    @DisplayName("空のテンプレートID検索")
    void selectByTemplateId_NoMatchingTemplate_ReturnsEmptyList() {
        // Act
        List<JiraJqlQuery> result = jqlQueryMapper.selectByTemplateId("non-existing-template-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ゼロ件のページネーション")
    void selectAllWithPagination_NoData_ReturnsEmptyList() {
        // Act（すべてのテストデータをクリアした状態で実行）
        List<JiraJqlQuery> result = jqlQueryMapper.selectAllWithPagination(10, 0);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("存在しないIDの更新操作")
    void update_NonExistingId_ReturnsZero() {
        // Arrange
        JiraJqlQuery nonExistingQuery = JiraJqlQuery.restore(
            "non-existing-id",
            "Non Existing Query",
            "project = NONE",
            testTemplateId,
            true,
            100,
            testTime,
            testTime,
            "test-user",
            "updater-user"
        );

        // Act
        int updateCount = jqlQueryMapper.update(
            nonExistingQuery.getId(),
            nonExistingQuery.getQueryName(),
            nonExistingQuery.getJqlExpression(),
            nonExistingQuery.getTemplateId(),
            nonExistingQuery.isActive(),
            nonExistingQuery.getPriority(),
            nonExistingQuery.getUpdatedAt(),
            nonExistingQuery.getUpdatedBy()
        );

        // Assert
        assertThat(updateCount).isEqualTo(0);
    }

    @Test
    @DisplayName("存在しないIDの削除操作")
    void deleteById_NonExistingId_ReturnsZero() {
        // Act
        int deleteCount = jqlQueryMapper.deleteById("non-existing-id");

        // Assert
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    @DisplayName("存在しないIDのアクティブ状態更新")
    void updateActiveStatus_NonExistingId_ReturnsZero() {
        // Act
        int updateCount = jqlQueryMapper.updateActiveStatus(
            "non-existing-id", 
            false, 
            LocalDateTime.now(), 
            "updater-user"
        );

        // Assert
        assertThat(updateCount).isEqualTo(0);
    }
}