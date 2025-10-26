package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.devhour.domain.model.entity.JiraResponseTemplate;

/**
 * JiraResponseTemplateMapperの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
@DisplayName("JiraResponseTemplateMapper統合テスト")
class JiraResponseTemplateMapperTest extends AbstractMapperTest {

    @Autowired
    private JiraResponseTemplateMapper responseTemplateMapper;

    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now().withNano(0);
        // Clear existing data for test isolation
        responseTemplateMapper.selectAll().forEach(template ->
            responseTemplateMapper.deleteById(template.getId())
        );
    }

    @Test
    @DisplayName("レスポンステンプレート挿入 - 正常ケース")
    void insert_Success() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Test Template",
            "{\"name\": \"$name\", \"description\": \"$description\"}",
            "テストテンプレートの説明"
        );

        // Act
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Assert
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(template.getId());
        assertThat(result).isPresent();
        JiraResponseTemplate retrieved = result.get();
        assertThat(retrieved.getId()).isEqualTo(template.getId());
        assertThat(retrieved.getTemplateName()).isEqualTo("Test Template");
        assertThat(retrieved.getVelocityTemplate()).isEqualTo("{\"name\": \"$name\", \"description\": \"$description\"}");
        assertThat(retrieved.getTemplateDescription()).isEqualTo("テストテンプレートの説明");
        assertThat(retrieved.getCreatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("レスポンステンプレート挿入 - 説明なしケース")
    void insert_WithoutDescription_Success() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Template Without Description",
            "{\"status\": \"$status\"}",
            null
        );

        // Act
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Assert
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(template.getId());
        assertThat(result).isPresent();
        JiraResponseTemplate retrieved = result.get();
        assertThat(retrieved.getTemplateDescription()).isNull();
        assertThat(retrieved.getTemplateName()).isEqualTo("Template Without Description");
        assertThat(retrieved.getVelocityTemplate()).isEqualTo("{\"status\": \"$status\"}");
    }

    @Test
    @DisplayName("レスポンステンプレート挿入 - 大きなテンプレート")
    void insert_LargeTemplate_Success() {
        // Arrange
        StringBuilder largeTemplate = new StringBuilder();
        largeTemplate.append("{\"result\": [");
        for (int i = 0; i < 100; i++) {
            largeTemplate.append("{\"field").append(i).append("\": \"$field").append(i).append("\"}");
            if (i < 99) largeTemplate.append(",");
        }
        largeTemplate.append("]}");

        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Large Template",
            largeTemplate.toString(),
            "大きなテンプレートのテスト"
        );

        // Act
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Assert
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(template.getId());
        assertThat(result).isPresent();
        JiraResponseTemplate retrieved = result.get();
        assertThat(retrieved.getVelocityTemplate()).isEqualTo(largeTemplate.toString());
        assertThat(retrieved.getVelocityTemplate().length()).isGreaterThan(1000);
    }

    @Test
    @DisplayName("ID検索 - 存在するテンプレート")
    void selectById_ExistingTemplate_ReturnsTemplate() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Find By ID Test",
            "{\"id\": \"$id\", \"key\": \"$key\"}",
            "ID検索テスト"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(template.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(template.getId());
        assertThat(result.get().getTemplateName()).isEqualTo("Find By ID Test");
        assertThat(result.get().getVelocityTemplate()).isEqualTo("{\"id\": \"$id\", \"key\": \"$key\"}");
    }

    @Test
    @DisplayName("ID検索 - 存在しないテンプレート")
    void selectById_NonExistingTemplate_ReturnsEmpty() {
        // Act
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("レスポンステンプレート更新 - 正常ケース")
    void update_Success() {
        // Arrange
        JiraResponseTemplate originalTemplate = JiraResponseTemplate.createNew(
            "Original Template",
            "{\"original\": \"$value\"}",
            "元の説明"
        );
        responseTemplateMapper.insert(
            originalTemplate.getId(),
            originalTemplate.getTemplateName(),
            originalTemplate.getVelocityTemplate(),
            originalTemplate.getTemplateDescription(),
            originalTemplate.getCreatedAt(),
            originalTemplate.getUpdatedAt()
        );

        // テンプレートを更新
        originalTemplate.updateTemplate("{\"updated\": \"$newValue\"}");
        originalTemplate.updateName("Updated Template Name");
        originalTemplate.updateDescription("更新された説明");

        // Act
        int updateCount = responseTemplateMapper.update(
            originalTemplate.getId(),
            originalTemplate.getTemplateName(),
            originalTemplate.getVelocityTemplate(),
            originalTemplate.getTemplateDescription(),
            originalTemplate.getUpdatedAt()
        );

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(originalTemplate.getId());
        assertThat(result).isPresent();
        JiraResponseTemplate updated = result.get();
        assertThat(updated.getTemplateName()).isEqualTo("Updated Template Name");
        assertThat(updated.getVelocityTemplate()).isEqualTo("{\"updated\": \"$newValue\"}");
        assertThat(updated.getTemplateDescription()).isEqualTo("更新された説明");
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("レスポンステンプレート削除 - 正常ケース")
    void deleteById_Success() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Delete Test Template",
            "{\"delete\": true}",
            "削除テスト"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        int deleteCount = responseTemplateMapper.deleteById(template.getId());

        // Assert
        assertThat(deleteCount).isEqualTo(1);
        
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectById(template.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("テンプレート名検索 - 存在するテンプレート")
    void selectByTemplateName_ExistingTemplate_ReturnsTemplate() {
        // Arrange
        String templateName = "Unique Template Name";
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            templateName,
            "{\"unique\": \"$field\"}",
            "ユニークなテンプレート"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectByTemplateName(templateName);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateName()).isEqualTo(templateName);
        assertThat(result.get().getId()).isEqualTo(template.getId());
    }

    @Test
    @DisplayName("テンプレート名検索 - 存在しないテンプレート")
    void selectByTemplateName_NonExistingTemplate_ReturnsEmpty() {
        // Act
        Optional<JiraResponseTemplate> result = responseTemplateMapper.selectByTemplateName("Non Existing Template");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("全テンプレート取得 - 複数テンプレート")
    void selectAll_MultipleTemplates_ReturnsSortedByName() {
        // Arrange
        JiraResponseTemplate template1 = JiraResponseTemplate.createNew(
            "B Template",
            "{\"b\": \"$value\"}",
            "B template"
        );
        JiraResponseTemplate template2 = JiraResponseTemplate.createNew(
            "A Template",
            "{\"a\": \"$value\"}",
            "A template"
        );
        JiraResponseTemplate template3 = JiraResponseTemplate.createNew(
            "C Template",
            "{\"c\": \"$value\"}",
            "C template"
        );

        responseTemplateMapper.insert(
            template1.getId(),
            template1.getTemplateName(),
            template1.getVelocityTemplate(),
            template1.getTemplateDescription(),
            template1.getCreatedAt(),
            template1.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template2.getId(),
            template2.getTemplateName(),
            template2.getVelocityTemplate(),
            template2.getTemplateDescription(),
            template2.getCreatedAt(),
            template2.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template3.getId(),
            template3.getTemplateName(),
            template3.getVelocityTemplate(),
            template3.getTemplateDescription(),
            template3.getCreatedAt(),
            template3.getUpdatedAt()
        );

        // Act
        List<JiraResponseTemplate> result = responseTemplateMapper.selectAll();

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTemplateName()).isEqualTo("A Template");
        assertThat(result.get(1).getTemplateName()).isEqualTo("B Template");
        assertThat(result.get(2).getTemplateName()).isEqualTo("C Template");
    }

    @Test
    @DisplayName("ページネーション対応テンプレート取得")
    void selectAllWithPagination_WithLimitOffset_ReturnsCorrectPage() {
        // Arrange
        // 複数のテンプレートを作成（異なる作成時間で）
        for (int i = 1; i <= 5; i++) {
            JiraResponseTemplate template = JiraResponseTemplate.createNew(
                "Template " + i,
                "{\"template\": " + i + "}",
                "テンプレート " + i
            );
            responseTemplateMapper.insert(
                template.getId(),
                template.getTemplateName(),
                template.getVelocityTemplate(),
                template.getTemplateDescription(),
                testTime.plusSeconds(i), // 作成時間を少しずつ変える
                testTime.plusSeconds(i)
            );
        }

        // Act
        List<JiraResponseTemplate> firstPage = responseTemplateMapper.selectAllWithPagination(2, 0);
        List<JiraResponseTemplate> secondPage = responseTemplateMapper.selectAllWithPagination(2, 2);

        // Assert
        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        
        // 作成日時降順（新しいものが先頭）で並んでいることを確認
        assertThat(firstPage.get(0).getCreatedAt()).isAfter(firstPage.get(1).getCreatedAt());
        assertThat(secondPage.get(0).getCreatedAt()).isAfter(secondPage.get(1).getCreatedAt());
        assertThat(firstPage.get(1).getCreatedAt()).isAfter(secondPage.get(0).getCreatedAt());
    }

    @Test
    @DisplayName("全テンプレート数カウント")
    void countAll_VariousTemplates_ReturnsCorrectCount() {
        // Arrange
        JiraResponseTemplate template1 = JiraResponseTemplate.createNew(
            "Count Template 1",
            "{\"count\": 1}",
            null
        );
        JiraResponseTemplate template2 = JiraResponseTemplate.createNew(
            "Count Template 2",
            "{\"count\": 2}",
            "説明付き"
        );
        JiraResponseTemplate template3 = JiraResponseTemplate.createNew(
            "Count Template 3",
            "{\"count\": 3}",
            null
        );

        responseTemplateMapper.insert(
            template1.getId(),
            template1.getTemplateName(),
            template1.getVelocityTemplate(),
            template1.getTemplateDescription(),
            template1.getCreatedAt(),
            template1.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template2.getId(),
            template2.getTemplateName(),
            template2.getVelocityTemplate(),
            template2.getTemplateDescription(),
            template2.getCreatedAt(),
            template2.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template3.getId(),
            template3.getTemplateName(),
            template3.getVelocityTemplate(),
            template3.getTemplateDescription(),
            template3.getCreatedAt(),
            template3.getUpdatedAt()
        );

        // Act
        int count = responseTemplateMapper.countAll();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("名前パターン検索 - 一致するテンプレート")
    void searchByNamePattern_MatchingTemplates_ReturnsMatches() {
        // Arrange
        JiraResponseTemplate template1 = JiraResponseTemplate.createNew(
            "Project Template Alpha",
            "{\"project\": \"alpha\"}",
            "プロジェクトアルファ"
        );
        JiraResponseTemplate template2 = JiraResponseTemplate.createNew(
            "Project Template Beta",
            "{\"project\": \"beta\"}",
            "プロジェクトベータ"
        );
        JiraResponseTemplate template3 = JiraResponseTemplate.createNew(
            "User Template",
            "{\"user\": \"data\"}",
            "ユーザーテンプレート"
        );

        responseTemplateMapper.insert(
            template1.getId(),
            template1.getTemplateName(),
            template1.getVelocityTemplate(),
            template1.getTemplateDescription(),
            template1.getCreatedAt(),
            template1.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template2.getId(),
            template2.getTemplateName(),
            template2.getVelocityTemplate(),
            template2.getTemplateDescription(),
            template2.getCreatedAt(),
            template2.getUpdatedAt()
        );
        responseTemplateMapper.insert(
            template3.getId(),
            template3.getTemplateName(),
            template3.getVelocityTemplate(),
            template3.getTemplateDescription(),
            template3.getCreatedAt(),
            template3.getUpdatedAt()
        );

        // Act
        List<JiraResponseTemplate> result = responseTemplateMapper.searchByNamePattern("Project");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTemplateName()).isEqualTo("Project Template Alpha");
        assertThat(result.get(1).getTemplateName()).isEqualTo("Project Template Beta");
        assertThat(result).allMatch(template -> template.getTemplateName().contains("Project"));
    }

    @Test
    @DisplayName("名前パターン検索 - 一致しないパターン")
    void searchByNamePattern_NoMatches_ReturnsEmpty() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Sample Template",
            "{\"sample\": true}",
            "サンプル"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        List<JiraResponseTemplate> result = responseTemplateMapper.searchByNamePattern("NoMatch");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("テンプレート名存在チェック - 存在する場合")
    void existsByTemplateName_ExistingName_ReturnsTrue() {
        // Arrange
        String templateName = "Exists Check Template";
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            templateName,
            "{\"exists\": true}",
            "存在チェック"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        boolean exists = responseTemplateMapper.existsByTemplateName(templateName);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("テンプレート名存在チェック - 存在しない場合")
    void existsByTemplateName_NonExistingName_ReturnsFalse() {
        // Act
        boolean exists = responseTemplateMapper.existsByTemplateName("Non Existing Template Name");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("テンプレート名存在チェック（ID除外） - 同名だが異なるID")
    void existsByTemplateNameExcludingId_SameNameDifferentId_ReturnsTrue() {
        // Arrange
        String existingTemplateName = "Existing Template";
        JiraResponseTemplate existingTemplate = JiraResponseTemplate.createNew(
            existingTemplateName,
            "{\"existing\": true}",
            "既存テンプレート"
        );

        responseTemplateMapper.insert(
            existingTemplate.getId(),
            existingTemplate.getTemplateName(),
            existingTemplate.getVelocityTemplate(),
            existingTemplate.getTemplateDescription(),
            existingTemplate.getCreatedAt(),
            existingTemplate.getUpdatedAt()
        );

        // 異なるIDでの同名チェック（実際には存在するテンプレート名をチェック）
        String differentId = "different-template-id";

        // Act
        boolean exists = responseTemplateMapper.existsByTemplateNameExcludingId(existingTemplateName, differentId);

        // Assert
        assertThat(exists).isTrue(); // 既存テンプレートが存在し、異なるIDを除外してもまだ存在するためtrue
    }

    @Test
    @DisplayName("テンプレート名存在チェック（ID除外） - 自分自身のみ")
    void existsByTemplateNameExcludingId_OnlySelfExists_ReturnsFalse() {
        // Arrange
        String templateName = "Self Only Template";
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            templateName,
            "{\"self\": true}",
            "自分のみ"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        boolean exists = responseTemplateMapper.existsByTemplateNameExcludingId(templateName, template.getId());

        // Assert
        assertThat(exists).isFalse(); // 自分自身を除外すると存在しない
    }

    @Test
    @DisplayName("存在しないIDの更新操作")
    void update_NonExistingId_ReturnsZero() {
        // Arrange
        JiraResponseTemplate nonExistingTemplate = JiraResponseTemplate.restore(
            "non-existing-id",
            "Non Existing Template",
            "{\"none\": true}",
            "存在しない",
            testTime,
            testTime
        );

        // Act
        int updateCount = responseTemplateMapper.update(
            nonExistingTemplate.getId(),
            nonExistingTemplate.getTemplateName(),
            nonExistingTemplate.getVelocityTemplate(),
            nonExistingTemplate.getTemplateDescription(),
            nonExistingTemplate.getUpdatedAt()
        );

        // Assert
        assertThat(updateCount).isEqualTo(0);
    }

    @Test
    @DisplayName("存在しないIDの削除操作")
    void deleteById_NonExistingId_ReturnsZero() {
        // Act
        int deleteCount = responseTemplateMapper.deleteById("non-existing-id");

        // Assert
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    @DisplayName("空のパターン検索")
    void searchByNamePattern_EmptyPattern_ReturnsAllTemplates() {
        // Arrange
        JiraResponseTemplate template = JiraResponseTemplate.createNew(
            "Empty Pattern Test",
            "{\"empty\": true}",
            "空パターンテスト"
        );
        responseTemplateMapper.insert(
            template.getId(),
            template.getTemplateName(),
            template.getVelocityTemplate(),
            template.getTemplateDescription(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );

        // Act
        List<JiraResponseTemplate> result = responseTemplateMapper.searchByNamePattern("");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTemplateName()).isEqualTo("Empty Pattern Test");
    }

    @Test
    @DisplayName("ゼロ件のページネーション")
    void selectAllWithPagination_NoData_ReturnsEmptyList() {
        // Act（すべてのテストデータをクリアした状態で実行）
        List<JiraResponseTemplate> result = responseTemplateMapper.selectAllWithPagination(10, 0);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ゼロ件のカウント")
    void countAll_NoData_ReturnsZero() {
        // Act（すべてのテストデータをクリアした状態で実行）
        int count = responseTemplateMapper.countAll();

        // Assert
        assertThat(count).isEqualTo(0);
    }
}