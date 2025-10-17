package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JqlQuery エンティティのテスト")
class JiraJqlQueryTest {

    @Nested
    @DisplayName("ファクトリーメソッド createNew のテスト")
    class CreateNewTest {

        @Test
        @DisplayName("正常なパラメータで新しいJQLクエリを作成できる")
        void createNew_WithValidParameters_ShouldCreateNewQuery() {
            // given
            String queryName = "アクティブプロジェクト検索";
            String jqlExpression = "project = TEST AND status = 'In Progress'";
            String templateId = "template-123";
            Integer priority = 10;
            String createdBy = "user-123";

            // when
            JiraJqlQuery query = JiraJqlQuery.createNew(queryName, jqlExpression, templateId, priority, createdBy);

            // then
            assertThat(query.getId()).isNotNull();
            assertThat(query.getQueryName()).isEqualTo(queryName);
            assertThat(query.getJqlExpression()).isEqualTo(jqlExpression);
            assertThat(query.getTemplateId()).isEqualTo(templateId);
            assertThat(query.getPriority()).isEqualTo(priority);
            assertThat(query.getCreatedBy()).isEqualTo(createdBy);
            assertThat(query.isActive()).isTrue();
            assertThat(query.getCreatedAt()).isNotNull();
            assertThat(query.getUpdatedAt()).isNotNull();
            assertThat(query.getUpdatedBy()).isNull();
            
            // 作成日時と更新日時は1秒以内の差であること
            assertThat(ChronoUnit.SECONDS.between(query.getCreatedAt(), query.getUpdatedAt())).isLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("優先度0で新しいJQLクエリを作成できる")
        void createNew_WithZeroPriority_ShouldCreateNewQuery() {
            // given
            String queryName = "基本検索";
            String jqlExpression = "project = TEST";
            String templateId = "template-123";
            Integer priority = 0;
            String createdBy = "user-123";

            // when
            JiraJqlQuery query = JiraJqlQuery.createNew(queryName, jqlExpression, templateId, priority, createdBy);

            // then
            assertThat(query.getPriority()).isEqualTo(0);
        }

        @Test
        @DisplayName("クエリ名がnullの場合例外を投げる")
        void createNew_WithNullQueryName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew(null, "project = TEST", "template-123", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("クエリ名は必須です");
        }

        @Test
        @DisplayName("クエリ名が空文字の場合例外を投げる")
        void createNew_WithEmptyQueryName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("", "project = TEST", "template-123", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("クエリ名は必須です");
        }

        @Test
        @DisplayName("クエリ名が空白のみの場合例外を投げる")
        void createNew_WithBlankQueryName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("   ", "project = TEST", "template-123", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("クエリ名は必須です");
        }

        @Test
        @DisplayName("JQL式がnullの場合例外を投げる")
        void createNew_WithNullJqlExpression_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", null, "template-123", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JQL式は必須です");
        }

        @Test
        @DisplayName("JQL式が空文字の場合例外を投げる")
        void createNew_WithEmptyJqlExpression_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "", "template-123", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JQL式は必須です");
        }

        @Test
        @DisplayName("テンプレートIDがnullの場合例外を投げる")
        void createNew_WithNullTemplateId_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "project = TEST", null, 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレートIDは必須です");
        }

        @Test
        @DisplayName("テンプレートIDが空文字の場合例外を投げる")
        void createNew_WithEmptyTemplateId_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "project = TEST", "", 0, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレートIDは必須です");
        }

        @Test
        @DisplayName("優先度が負の値の場合例外を投げる")
        void createNew_WithNegativePriority_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "project = TEST", "template-123", -1, "user-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("優先度は0以上である必要があります");
        }

        @Test
        @DisplayName("作成者がnullの場合例外を投げる")
        void createNew_WithNullCreatedBy_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "project = TEST", "template-123", 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("作成者は必須です");
        }

        @Test
        @DisplayName("作成者が空文字の場合例外を投げる")
        void createNew_WithEmptyCreatedBy_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraJqlQuery.createNew("テスト", "project = TEST", "template-123", 0, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("作成者は必須です");
        }
    }

    @Nested
    @DisplayName("ファクトリーメソッド restore のテスト")
    class RestoreTest {

        @Test
        @DisplayName("すべてのパラメータで既存JQLクエリを復元できる")
        void restore_WithAllParameters_ShouldRestoreQuery() {
            // given
            String id = "query-123";
            String queryName = "復元テスト";
            String jqlExpression = "project = TEST";
            String templateId = "template-123";
            Boolean isActive = false;
            Integer priority = 5;
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime updatedAt = LocalDateTime.now();
            String createdBy = "creator-123";
            String updatedBy = "updater-456";

            // when
            JiraJqlQuery query = JiraJqlQuery.restore(id, queryName, jqlExpression, templateId, 
                                            isActive, priority, createdAt, updatedAt, createdBy, updatedBy);

            // then
            assertThat(query.getId()).isEqualTo(id);
            assertThat(query.getQueryName()).isEqualTo(queryName);
            assertThat(query.getJqlExpression()).isEqualTo(jqlExpression);
            assertThat(query.getTemplateId()).isEqualTo(templateId);
            assertThat(query.isActive()).isEqualTo(isActive);
            assertThat(query.getPriority()).isEqualTo(priority);
            assertThat(query.getCreatedAt()).isEqualTo(createdAt);
            assertThat(query.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(query.getCreatedBy()).isEqualTo(createdBy);
            assertThat(query.getUpdatedBy()).isEqualTo(updatedBy);
        }
    }

    @Nested
    @DisplayName("ビジネスメソッドのテスト")
    class BusinessMethodsTest {

        @Test
        @DisplayName("activate()でクエリをアクティブ化できる")
        void activate_ShouldSetActiveTrue() {
            // given
            JiraJqlQuery query = createTestQuery();
            query.deactivate(); // 非アクティブにしてからテスト
            LocalDateTime beforeUpdate = query.getUpdatedAt();

            // when
            query.activate();

            // then
            assertThat(query.isActive()).isTrue();
            assertThat(query.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("deactivate()でクエリを非アクティブ化できる")
        void deactivate_ShouldSetActiveFalse() {
            // given
            JiraJqlQuery query = createTestQuery();
            LocalDateTime beforeUpdate = query.getUpdatedAt();

            // when
            query.deactivate();

            // then
            assertThat(query.isActive()).isFalse();
            assertThat(query.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("updatePriority()で優先度を更新できる")
        void updatePriority_WithValidParameters_ShouldUpdatePriority() {
            // given
            JiraJqlQuery query = createTestQuery();
            LocalDateTime beforeUpdate = query.getUpdatedAt();
            Integer newPriority = 20;
            String updatedBy = "updater-123";

            // when
            query.updatePriority(newPriority, updatedBy);

            // then
            assertThat(query.getPriority()).isEqualTo(newPriority);
            assertThat(query.getUpdatedBy()).isEqualTo(updatedBy);
            assertThat(query.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("updatePriority()で負の優先度を設定すると例外を投げる")
        void updatePriority_WithNegativePriority_ShouldThrowException() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThatThrownBy(() -> query.updatePriority(-1, "updater-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("優先度は0以上である必要があります");
        }

        @Test
        @DisplayName("updatePriority()で更新者がnullの場合例外を投げる")
        void updatePriority_WithNullUpdatedBy_ShouldThrowException() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThatThrownBy(() -> query.updatePriority(10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("更新者は必須です");
        }

        @Test
        @DisplayName("updateQuery()でJQL式を更新できる")
        void updateQuery_WithValidParameters_ShouldUpdateJqlExpression() {
            // given
            JiraJqlQuery query = createTestQuery();
            LocalDateTime beforeUpdate = query.getUpdatedAt();
            String newJqlExpression = "project = NEW AND status = 'Open'";
            String updatedBy = "updater-123";

            // when
            query.updateQuery(newJqlExpression, updatedBy);

            // then
            assertThat(query.getJqlExpression()).isEqualTo(newJqlExpression);
            assertThat(query.getUpdatedBy()).isEqualTo(updatedBy);
            assertThat(query.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("updateQuery()で空のJQL式を設定すると例外を投げる")
        void updateQuery_WithEmptyJqlExpression_ShouldThrowException() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThatThrownBy(() -> query.updateQuery("", "updater-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JQL式は必須です");
        }

        @Test
        @DisplayName("updateTemplate()でテンプレートIDを更新できる")
        void updateTemplate_WithValidParameters_ShouldUpdateTemplateId() {
            // given
            JiraJqlQuery query = createTestQuery();
            LocalDateTime beforeUpdate = query.getUpdatedAt();
            String newTemplateId = "new-template-456";
            String updatedBy = "updater-123";

            // when
            query.updateTemplate(newTemplateId, updatedBy);

            // then
            assertThat(query.getTemplateId()).isEqualTo(newTemplateId);
            assertThat(query.getUpdatedBy()).isEqualTo(updatedBy);
            assertThat(query.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("updateTemplate()で空のテンプレートIDを設定すると例外を投げる")
        void updateTemplate_WithEmptyTemplateId_ShouldThrowException() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThatThrownBy(() -> query.updateTemplate("", "updater-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレートIDは必須です");
        }

        @Test
        @DisplayName("isHigherPriorityThan()で優先度比較ができる")
        void isHigherPriorityThan_ShouldComparePriority() {
            // given
            JiraJqlQuery highPriorityQuery = JiraJqlQuery.createNew("高優先度", "project = TEST", "template-1", 10, "user-1");
            JiraJqlQuery lowPriorityQuery = JiraJqlQuery.createNew("低優先度", "project = TEST", "template-1", 5, "user-1");

            // when & then
            assertThat(highPriorityQuery.isHigherPriorityThan(lowPriorityQuery)).isTrue();
            assertThat(lowPriorityQuery.isHigherPriorityThan(highPriorityQuery)).isFalse();
        }

        @Test
        @DisplayName("isHigherPriorityThan()で同じ優先度の場合falseを返す")
        void isHigherPriorityThan_WithSamePriority_ShouldReturnFalse() {
            // given
            JiraJqlQuery query1 = JiraJqlQuery.createNew("クエリ1", "project = TEST", "template-1", 5, "user-1");
            JiraJqlQuery query2 = JiraJqlQuery.createNew("クエリ2", "project = TEST", "template-1", 5, "user-1");

            // when & then
            assertThat(query1.isHigherPriorityThan(query2)).isFalse();
            assertThat(query2.isHigherPriorityThan(query1)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals と hashCode のテスト")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("同じIDのJQLクエリは等しい")
        void equals_WithSameId_ShouldReturnTrue() {
            // given
            String id = "query-123";
            JiraJqlQuery query1 = JiraJqlQuery.restore(id, "クエリ1", "project = TEST", "template-1", 
                                             true, 0, LocalDateTime.now(), LocalDateTime.now(), "user-1", null);
            JiraJqlQuery query2 = JiraJqlQuery.restore(id, "クエリ2", "project = OTHER", "template-2", 
                                             false, 10, LocalDateTime.now(), LocalDateTime.now(), "user-2", "user-3");

            // when & then
            assertThat(query1).isEqualTo(query2);
            assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
        }

        @Test
        @DisplayName("異なるIDのJQLクエリは等しくない")
        void equals_WithDifferentId_ShouldReturnFalse() {
            // given
            JiraJqlQuery query1 = createTestQuery();
            JiraJqlQuery query2 = createTestQuery();

            // when & then
            assertThat(query1).isNotEqualTo(query2);
        }

        @Test
        @DisplayName("nullとの比較でfalseを返す")
        void equals_WithNull_ShouldReturnFalse() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThat(query).isNotEqualTo(null);
        }

        @Test
        @DisplayName("異なるクラスのオブジェクトとの比較でfalseを返す")
        void equals_WithDifferentClass_ShouldReturnFalse() {
            // given
            JiraJqlQuery query = createTestQuery();
            String other = "different object";

            // when & then
            assertThat(query).isNotEqualTo(other);
        }

        @Test
        @DisplayName("同じインスタンスの比較でtrueを返す")
        void equals_WithSameInstance_ShouldReturnTrue() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when & then
            assertThat(query).isEqualTo(query);
        }
    }

    @Nested
    @DisplayName("toString メソッドのテスト")
    class ToStringTest {

        @Test
        @DisplayName("toString()で適切な文字列表現を返す")
        void toString_ShouldReturnProperStringRepresentation() {
            // given
            JiraJqlQuery query = createTestQuery();

            // when
            String result = query.toString();

            // then
            assertThat(result).contains("JqlQuery");
            assertThat(result).contains(query.getId());
            assertThat(result).contains(query.getQueryName());
            assertThat(result).contains(query.getPriority().toString());
        }
    }

    /**
     * テスト用のJQLクエリエンティティを作成するヘルパーメソッド
     */
    private JiraJqlQuery createTestQuery() {
        return JiraJqlQuery.createNew("テストクエリ", "project = TEST", "template-123", 0, "user-123");
    }
}