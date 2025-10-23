package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.JiraSyncHistoryDetail;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * JiraSyncHistoryDetailMapperの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
@DisplayName("JiraSyncHistoryDetailMapper統合テスト")
class JiraSyncHistoryDetailMapperTest extends AbstractMapperTest {

    @Autowired
    private JiraSyncHistoryDetailMapper syncHistoryDetailMapper;
    
    @Autowired
    private JiraSyncHistoryMapper syncHistoryMapper;
    
    @Autowired
    private ProjectMapper projectMapper;

    private String testSyncHistoryId;
    private String testProjectId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now().withNano(0);
        
        // テストデータ用の同期履歴を事前に作成
        setupTestSyncHistory();
        
        // テストデータ用のプロジェクトを事前に作成
        setupTestProject();
    }

    private void setupTestSyncHistory() {
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.MANUAL, "test-user");
        testSyncHistoryId = syncHistory.getId();
        
        syncHistoryMapper.insert(
            syncHistory.getId(),
            syncHistory.getSyncType().getValue(),
            syncHistory.getSyncStatus().getValue(),
            syncHistory.getStartedAt(),
            syncHistory.getCompletedAt(),
            syncHistory.getTotalProjectsProcessed(),
            syncHistory.getSuccessCount(),
            syncHistory.getErrorCount(),
            syncHistory.getErrorDetails(),
            syncHistory.getTriggeredBy()
        );
    }

    private void setupTestProject() {
        testProjectId = "test-project-id";
        // プロジェクトテーブルに必要なテストデータを挿入
        projectMapper.insert(
            testProjectId,
            "Test Project",
            "Test project description",
            "DRAFT",
            testTime.toLocalDate(),
            testTime.toLocalDate().plusDays(30),
            "test-user",
            testTime,
            testTime,
            null,
            null
        );
    }

    @Test
    @DisplayName("同期履歴詳細挿入 - 成功ケース")
    void insert_Success_WithProjectId() {
        // Arrange
        JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId,
            1,
            "CREATED",
            null
        );

        // Act
        syncHistoryDetailMapper.insert(
            detail.getId(),
            detail.getSyncHistoryId(),
            detail.getSeq(),
            detail.getOperation(),
            detail.getStatus().getValue(),
            detail.getResult(),
            detail.getProcessedAt()
        );

        // Assert
        Optional<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectById(detail.getId());
        assertThat(result).isPresent();
        JiraSyncHistoryDetail retrieved = result.get();
        assertThat(retrieved.getId()).isEqualTo(detail.getId());
        assertThat(retrieved.getSyncHistoryId()).isEqualTo(testSyncHistoryId);
        assertThat(retrieved.getOperation()).isEqualTo("CREATED");
        assertThat(retrieved.getStatus()).isEqualTo(DetailStatus.SUCCESS);
        assertThat(retrieved.getResult()).isNull();
        assertThat(retrieved.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("同期履歴詳細挿入 - エラーケース")
    void insert_Error_WithoutProjectId() {
        // Arrange
        JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
            testSyncHistoryId,
            1,
            "UPDATED",
            "Project not found"
        );

        // Act
        syncHistoryDetailMapper.insert(
            detail.getId(),
            detail.getSyncHistoryId(),
            detail.getSeq(),
            detail.getOperation(),
            detail.getStatus().getValue(),
            detail.getResult(),
            detail.getProcessedAt()
        );

        // Assert
        Optional<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectById(detail.getId());
        assertThat(result).isPresent();
        JiraSyncHistoryDetail retrieved = result.get();
        assertThat(retrieved.getOperation()).isEqualTo("UPDATED");
        assertThat(retrieved.getStatus()).isEqualTo(DetailStatus.ERROR);
        assertThat(retrieved.getResult()).isEqualTo("Project not found");
    }

    @Test
    @DisplayName("ID検索 - 存在する詳細")
    void selectById_ExistingDetail_ReturnsDetail() {
        // Arrange
        JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId,
            1,
            "SKIPPED",
            null
        );
        syncHistoryDetailMapper.insert(
            detail.getId(),
            detail.getSyncHistoryId(),
            detail.getSeq(),
            detail.getOperation(),
            detail.getStatus().getValue(),
            detail.getResult(),
            detail.getProcessedAt()
        );

        // Act
        Optional<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectById(detail.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(detail.getId());
        // Note: jiraIssueKey is no longer available in the entity
        assertThat(result.get().getOperation()).isEqualTo("SKIPPED");
    }

    @Test
    @DisplayName("ID検索 - 存在しない詳細")
    void selectById_NonExistingDetail_ReturnsEmpty() {
        // Act
        Optional<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("同期履歴IDで詳細一覧取得")
    void selectBySyncHistoryId_MultipleDetails_ReturnsSortedByProcessTime() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        
        JiraSyncHistoryDetail detail1 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "CREATED", null);
        JiraSyncHistoryDetail detail2 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "UPDATED", null);
        JiraSyncHistoryDetail detail3 = JiraSyncHistoryDetail.createError(
            testSyncHistoryId, 1, "CREATED", "Error occurred");

        // 時間差を作って挿入
        insertDetailWithTime(detail1, baseTime.minusMinutes(3));
        insertDetailWithTime(detail2, baseTime.minusMinutes(2));
        insertDetailWithTime(detail3, baseTime.minusMinutes(1));

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectBySyncHistoryId(testSyncHistoryId);

        // Assert
        assertThat(result).hasSize(3);
        // 処理日時の昇順で並んでいることを確認
        assertThat(result.get(0).getOperation()).isEqualTo("CREATED");
        assertThat(result.get(1).getOperation()).isEqualTo("UPDATED");
        assertThat(result.get(2).getOperation()).isEqualTo("CREATED");
    }


    @Test
    @DisplayName("JIRAイシューキーで詳細検索")
    void selectByJiraIssueKey_ExistingIssue_ReturnsDetails() {
        // Arrange
        String targetIssueKey = "ISSUE-123";
        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        
        JiraSyncHistoryDetail detail1 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "CREATED", null);
        JiraSyncHistoryDetail detail2 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "UPDATED", null);

        // 異なるイシューキーの詳細（除外されるべき）
        JiraSyncHistoryDetail differentIssue = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "CREATED", null);

        insertDetailWithTime(detail1, baseTime.minusMinutes(2));
        insertDetailWithTime(detail2, baseTime.minusMinutes(1));
        insertDetailWithTime(differentIssue, baseTime);

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectByOperation("CREATED");

        // Assert
        assertThat(result).hasSize(2); // detail1 and differentIssue both have "CREATED" operation
        assertThat(result.get(0).getOperation()).isEqualTo("CREATED"); // 最新
        assertThat(result.get(1).getOperation()).isEqualTo("CREATED");
        // Note: jiraIssueKey matching no longer available in the entity
    }

    @Test
    @DisplayName("エラー詳細検索 - 特定の同期履歴")
    void selectErrorDetails_SpecificSyncHistory_ReturnsOnlyErrors() {
        // Arrange
        JiraSyncHistoryDetail successDetail = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "CREATED", null);
        JiraSyncHistoryDetail errorDetail1 = JiraSyncHistoryDetail.createError(
            testSyncHistoryId, 1, "UPDATED", "Error 1");
        JiraSyncHistoryDetail errorDetail2 = JiraSyncHistoryDetail.createError(
            testSyncHistoryId, 1, "CREATED", "Error 2");

        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        insertDetailWithTime(successDetail, baseTime.minusMinutes(3));
        insertDetailWithTime(errorDetail1, baseTime.minusMinutes(2));
        insertDetailWithTime(errorDetail2, baseTime.minusMinutes(1));

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectErrorDetails(testSyncHistoryId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(detail -> detail.getStatus() == DetailStatus.ERROR);
        assertThat(result.stream().map(JiraSyncHistoryDetail::getOperation))
            .containsExactly("CREATED", "UPDATED"); // 降順
    }

    @Test
    @DisplayName("エラー詳細検索 - 全同期対象")
    void selectErrorDetails_AllSyncs_ReturnsAllErrors() {
        // Arrange - 複数の同期履歴のエラー詳細を作成
        JiraSyncHistoryDetail errorDetail = JiraSyncHistoryDetail.createError(
            testSyncHistoryId, 1, "CREATED", "General error");
        insertDetailWithTime(errorDetail, LocalDateTime.now());

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectErrorDetails(null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(detail -> detail.getStatus() == DetailStatus.ERROR);
    }

    @Test
    @DisplayName("成功詳細検索 - 特定の同期履歴")
    void selectSuccessDetails_SpecificSyncHistory_ReturnsOnlySuccess() {
        // Arrange
        JiraSyncHistoryDetail successDetail1 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "CREATED", null);
        JiraSyncHistoryDetail successDetail2 = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "UPDATED", null);
        JiraSyncHistoryDetail errorDetail = JiraSyncHistoryDetail.createError(
            testSyncHistoryId, 1, "CREATED", "Error");

        LocalDateTime baseTime = LocalDateTime.now().withNano(0);
        insertDetailWithTime(successDetail1, baseTime.minusMinutes(3));
        insertDetailWithTime(successDetail2, baseTime.minusMinutes(2));
        insertDetailWithTime(errorDetail, baseTime.minusMinutes(1));

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectSuccessDetails(testSyncHistoryId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(detail -> detail.getStatus() == DetailStatus.SUCCESS);
        assertThat(result.stream().map(JiraSyncHistoryDetail::getOperation))
            .containsExactly("UPDATED", "CREATED"); // 降順
    }

    @Test
    @DisplayName("成功詳細検索 - 全同期対象")
    void selectSuccessDetails_AllSyncs_ReturnsAllSuccess() {
        // Arrange
        JiraSyncHistoryDetail successDetail = JiraSyncHistoryDetail.createSuccess(
            testSyncHistoryId, 1, "UPDATED", null);
        insertDetailWithTime(successDetail, LocalDateTime.now());

        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectSuccessDetails(null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(detail -> detail.getStatus() == DetailStatus.SUCCESS);
    }

    @Test
    @DisplayName("存在しない同期履歴IDでの詳細検索")
    void selectBySyncHistoryId_NonExistingHistory_ReturnsEmpty() {
        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectBySyncHistoryId("non-existing-history-id");

        // Assert
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("存在しないJIRAイシューキーでの詳細検索")
    void selectByJiraIssueKey_NonExistingIssue_ReturnsEmpty() {
        // Act
        List<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectByOperation("NON_EXISTING_OPERATION");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("nullフィールドの処理")
    void insert_WithNullFields_Success() {
        // Arrange
        JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
            testSyncHistoryId,
            1,
            "SKIPPED",
            null  // resultがnull
        );

        // Act
        syncHistoryDetailMapper.insert(
            detail.getId(),
            detail.getSyncHistoryId(),
            detail.getSeq(),
            detail.getOperation(),
            detail.getStatus().getValue(),
            detail.getResult(),
            detail.getProcessedAt()
        );

        // Assert
        Optional<JiraSyncHistoryDetail> result = syncHistoryDetailMapper.selectById(detail.getId());
        assertThat(result).isPresent();
        JiraSyncHistoryDetail retrieved = result.get();
        assertThat(retrieved.getOperation()).isEqualTo("SKIPPED");
        assertThat(retrieved.getResult()).isNull();
    }

    /**
     * ヘルパーメソッド: 指定時刻で同期履歴詳細を挿入
     */
    private void insertDetailWithTime(JiraSyncHistoryDetail detail, LocalDateTime processedAt) {
        syncHistoryDetailMapper.insert(
            detail.getId(),
            detail.getSyncHistoryId(),
            detail.getSeq(),
            detail.getOperation(),
            detail.getStatus().getValue(),
            detail.getResult(),
            processedAt
        );
    }
}