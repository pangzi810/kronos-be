package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ResponseTemplate エンティティのテスト")
class JiraResponseTemplateTest {

    @Nested
    @DisplayName("ファクトリーメソッド createNew のテスト")
    class CreateNewTest {

        @Test
        @DisplayName("正常なパラメータで新しいレスポンステンプレートを作成できる")
        void createNew_WithValidParameters_ShouldCreateNewTemplate() {
            // given
            String templateName = "プロジェクト基本情報テンプレート";
            String velocityTemplate = "{\"name\": \"$!{fields.summary}\", \"status\": \"$!{fields.status.name}\"}";
            String templateDescription = "JIRA APIレスポンスからプロジェクト基本情報を抽出";

            // when
            JiraResponseTemplate template = JiraResponseTemplate.createNew(templateName, velocityTemplate, templateDescription);

            // then
            assertThat(template.getId()).isNotNull();
            assertThat(template.getTemplateName()).isEqualTo(templateName);
            assertThat(template.getVelocityTemplate()).isEqualTo(velocityTemplate);
            assertThat(template.getTemplateDescription()).isEqualTo(templateDescription);
            assertThat(template.getCreatedAt()).isNotNull();
            assertThat(template.getUpdatedAt()).isNotNull();

            // 作成日時と更新日時は1秒以内の差であること
            assertThat(ChronoUnit.SECONDS.between(template.getCreatedAt(), template.getUpdatedAt())).isLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("説明がnullでもテンプレートを作成できる")
        void createNew_WithNullDescription_ShouldCreateNewTemplate() {
            // given
            String templateName = "基本テンプレート";
            String velocityTemplate = "{\"name\": \"$!{fields.summary}\"}";

            // when
            JiraResponseTemplate template = JiraResponseTemplate.createNew(templateName, velocityTemplate, null);

            // then
            assertThat(template.getTemplateDescription()).isNull();
            assertThat(template.getTemplateName()).isEqualTo(templateName);
            assertThat(template.getVelocityTemplate()).isEqualTo(velocityTemplate);
        }

        @Test
        @DisplayName("説明が空白のみの場合nullに正規化される")
        void createNew_WithBlankDescription_ShouldNormalizeToNull() {
            // given
            String templateName = "基本テンプレート";
            String velocityTemplate = "{\"name\": \"$!{fields.summary}\"}";
            String blankDescription = "   ";

            // when
            JiraResponseTemplate template = JiraResponseTemplate.createNew(templateName, velocityTemplate, blankDescription);

            // then
            assertThat(template.getTemplateDescription()).isNull();
        }

        @Test
        @DisplayName("テンプレート名がnullの場合例外を投げる")
        void createNew_WithNullTemplateName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew(null, "{\"name\": \"$!{fields.summary}\"}", "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレート名は必須です");
        }

        @Test
        @DisplayName("テンプレート名が空文字の場合例外を投げる")
        void createNew_WithEmptyTemplateName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("", "{\"name\": \"$!{fields.summary}\"}", "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレート名は必須です");
        }

        @Test
        @DisplayName("テンプレート名が空白のみの場合例外を投げる")
        void createNew_WithBlankTemplateName_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("   ", "{\"name\": \"$!{fields.summary}\"}", "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレート名は必須です");
        }

        @Test
        @DisplayName("Velocityテンプレートがnullの場合例外を投げる")
        void createNew_WithNullVelocityTemplate_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("テスト", null, "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Velocityテンプレートは必須です");
        }

        @Test
        @DisplayName("Velocityテンプレートが空文字の場合例外を投げる")
        void createNew_WithEmptyVelocityTemplate_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("テスト", "", "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Velocityテンプレートは必須です");
        }

        @Test
        @DisplayName("Velocityテンプレートが空白のみの場合例外を投げる")
        void createNew_WithBlankVelocityTemplate_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("テスト", "   ", "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Velocityテンプレートが空です");
        }
    }

    @Nested
    @DisplayName("ファクトリーメソッド restore のテスト")
    class RestoreTest {

        @Test
        @DisplayName("すべてのパラメータでレスポンステンプレートを復元できる")
        void restore_WithAllParameters_ShouldRestoreTemplate() {
            // given
            String id = "template-123";
            String templateName = "復元テスト";
            String velocityTemplate = "{\"name\": \"$!{fields.summary}\"}";
            String templateDescription = "テスト用説明";
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // when
            JiraResponseTemplate template = JiraResponseTemplate.restore(
                id, templateName, velocityTemplate, templateDescription, createdAt, updatedAt);

            // then
            assertThat(template.getId()).isEqualTo(id);
            assertThat(template.getTemplateName()).isEqualTo(templateName);
            assertThat(template.getVelocityTemplate()).isEqualTo(velocityTemplate);
            assertThat(template.getTemplateDescription()).isEqualTo(templateDescription);
            assertThat(template.getCreatedAt()).isEqualTo(createdAt);
            assertThat(template.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("説明がnullでもレスポンステンプレートを復元できる")
        void restore_WithNullDescription_ShouldRestoreTemplate() {
            // given
            String id = "template-123";
            String templateName = "復元テスト";
            String velocityTemplate = "{\"name\": \"$!{fields.summary}\"}";
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // when
            JiraResponseTemplate template = JiraResponseTemplate.restore(
                id, templateName, velocityTemplate, null, createdAt, updatedAt);

            // then
            assertThat(template.getTemplateDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("ビジネスメソッドのテスト")
    class BusinessMethodsTest {

        @Test
        @DisplayName("updateTemplate()でVelocityテンプレートを更新できる")
        void updateTemplate_WithValidTemplate_ShouldUpdateVelocityTemplate() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            LocalDateTime beforeUpdate = template.getUpdatedAt();
            String newVelocityTemplate = "{\"name\": \"$!{fields.summary}\", \"status\": \"$!{fields.status.name}\", \"priority\": \"$!{fields.priority.name}\"}";

            // when
            template.updateTemplate(newVelocityTemplate);

            // then
            assertThat(template.getVelocityTemplate()).isEqualTo(newVelocityTemplate);
            assertThat(template.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("updateTemplate()で空のテンプレートの場合例外を投げる")
        void updateTemplate_WithEmptyTemplate_ShouldThrowException() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            String invalidTemplate = "   "; // 空白のみ

            // when & then
            assertThatThrownBy(() -> template.updateTemplate(invalidTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Velocityテンプレートが空です");
        }

        @Test
        @DisplayName("updateName()でテンプレート名を更新できる")
        void updateName_WithValidName_ShouldUpdateTemplateName() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            LocalDateTime beforeUpdate = template.getUpdatedAt();
            String newTemplateName = "更新されたテンプレート名";

            // when
            template.updateName(newTemplateName);

            // then
            assertThat(template.getTemplateName()).isEqualTo(newTemplateName);
            assertThat(template.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("updateName()で空のテンプレート名の場合例外を投げる")
        void updateName_WithEmptyName_ShouldThrowException() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then
            assertThatThrownBy(() -> template.updateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("テンプレート名は必須です");
        }

        @Test
        @DisplayName("updateDescription()でテンプレート説明を更新できる")
        void updateDescription_WithValidDescription_ShouldUpdateDescription() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            LocalDateTime beforeUpdate = template.getUpdatedAt();
            String newDescription = "更新されたテンプレート説明";

            // when
            template.updateDescription(newDescription);

            // then
            assertThat(template.getTemplateDescription()).isEqualTo(newDescription);
            assertThat(template.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("updateDescription()でnullを設定できる")
        void updateDescription_WithNull_ShouldSetNull() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            LocalDateTime beforeUpdate = template.getUpdatedAt();

            // when
            template.updateDescription(null);

            // then
            assertThat(template.getTemplateDescription()).isNull();
            assertThat(template.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("updateDescription()で空白のみの場合nullに正規化される")
        void updateDescription_WithBlankDescription_ShouldNormalizeToNull() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when
            template.updateDescription("   ");

            // then
            assertThat(template.getTemplateDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Velocityテンプレート検証のテスト")
    class ValidateVelocityTemplateTest {

        @Test
        @DisplayName("有効なVelocityテンプレートで検証が通る")
        void validateVelocityTemplate_WithValidTemplate_ShouldPass() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then - 例外が投げられないことを確認
            assertThatCode(() -> template.validateVelocityTemplate())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("複雑なVelocityテンプレートで検証が通る")
        void validateVelocityTemplate_WithComplexTemplate_ShouldPass() {
            // given
            String validTemplate = "#if($fields.priority){\"priority\": \"$!{fields.priority.name}\"}#end#foreach($component in $fields.components)\"$!{component.name}\"#end";
            JiraResponseTemplate template = JiraResponseTemplate.createNew("テスト", validTemplate, "説明");

            // when & then
            assertThatCode(() -> template.validateVelocityTemplate())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("空のVelocityテンプレートで検証が失敗する")
        void validateVelocityTemplate_WithEmptyTemplate_ShouldThrowException() {
            // given
            String emptyTemplate = "   ";
            
            // when & then
            assertThatThrownBy(() -> JiraResponseTemplate.createNew("テスト", emptyTemplate, "説明"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Velocityテンプレートが空です");
        }
    }

    @Nested
    @DisplayName("フィールド検出のテスト")
    class ContainsFieldTest {

        @Test
        @DisplayName("指定されたフィールドがテンプレートに含まれている場合trueを返す")
        void containsField_WithExistingField_ShouldReturnTrue() {
            // given
            String templateWithFields = "{\"name\": \"$!{fields.summary}\", \"status\": \"$!{fields.status.name}\", \"priority\": \"$!{fields.priority.name}\"}";
            JiraResponseTemplate template = JiraResponseTemplate.createNew("テスト", templateWithFields, "説明");

            // when & then
            assertThat(template.containsField("fields.summary")).isTrue();
            assertThat(template.containsField("fields.status")).isTrue();
            assertThat(template.containsField("fields.priority")).isTrue();
            assertThat(template.containsField("fields.status.name")).isTrue();
        }

        @Test
        @DisplayName("指定されたフィールドがテンプレートに含まれていない場合falseを返す")
        void containsField_WithNonExistingField_ShouldReturnFalse() {
            // given
            String templateWithFields = "{\"name\": \"$!{fields.summary}\", \"status\": \"$!{fields.status.name}\"}";
            JiraResponseTemplate template = JiraResponseTemplate.createNew("テスト", templateWithFields, "説明");

            // when & then
            assertThat(template.containsField("fields.assignee")).isFalse();
            assertThat(template.containsField("fields.components")).isFalse();
            assertThat(template.containsField("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("複雑なVelocityテンプレートでフィールド検出ができる")
        void containsField_WithComplexTemplate_ShouldDetectFields() {
            // given
            String complexTemplate = "#if($fields.priority){\"priority\": \"$!{fields.priority.name}\"}#end#foreach($component in $fields.components)\"$!{component.name}\"#end";
            JiraResponseTemplate template = JiraResponseTemplate.createNew("テスト", complexTemplate, "説明");

            // when & then
            assertThat(template.containsField("fields.priority")).isTrue();
            assertThat(template.containsField("fields.components")).isTrue();
            assertThat(template.containsField("component.name")).isTrue();
            assertThat(template.containsField("fields.assignee")).isFalse();
        }

        @Test
        @DisplayName("フィールド名がnullの場合falseを返す")
        void containsField_WithNullFieldName_ShouldReturnFalse() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then
            assertThat(template.containsField(null)).isFalse();
        }

        @Test
        @DisplayName("フィールド名が空文字の場合falseを返す")
        void containsField_WithEmptyFieldName_ShouldReturnFalse() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then
            assertThat(template.containsField("")).isFalse();
        }
    }

    @Nested
    @DisplayName("equals と hashCode のテスト")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("同じIDのレスポンステンプレートは等しい")
        void equals_WithSameId_ShouldReturnTrue() {
            // given
            String id = "template-123";
            JiraResponseTemplate template1 = JiraResponseTemplate.restore(
                id, "テンプレート1", "{\"name\": \"$!{fields.summary}\"}", "説明1",
                LocalDateTime.now(), LocalDateTime.now());
            JiraResponseTemplate template2 = JiraResponseTemplate.restore(
                id, "テンプレート2", "{\"status\": \"$!{fields.status.name}\"}", "説明2",
                LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(template1).isEqualTo(template2);
            assertThat(template1.hashCode()).isEqualTo(template2.hashCode());
        }

        @Test
        @DisplayName("異なるIDのレスポンステンプレートは等しくない")
        void equals_WithDifferentId_ShouldReturnFalse() {
            // given
            JiraResponseTemplate template1 = createTestTemplate();
            JiraResponseTemplate template2 = createTestTemplate();

            // when & then
            assertThat(template1).isNotEqualTo(template2);
        }

        @Test
        @DisplayName("nullとの比較でfalseを返す")
        void equals_WithNull_ShouldReturnFalse() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then
            assertThat(template).isNotEqualTo(null);
        }

        @Test
        @DisplayName("異なるクラスのオブジェクトとの比較でfalseを返す")
        void equals_WithDifferentClass_ShouldReturnFalse() {
            // given
            JiraResponseTemplate template = createTestTemplate();
            String other = "different object";

            // when & then
            assertThat(template).isNotEqualTo(other);
        }

        @Test
        @DisplayName("同じインスタンスの比較でtrueを返す")
        void equals_WithSameInstance_ShouldReturnTrue() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when & then
            assertThat(template).isEqualTo(template);
        }
    }

    @Nested
    @DisplayName("toString メソッドのテスト")
    class ToStringTest {

        @Test
        @DisplayName("toString()で適切な文字列表現を返す")
        void toString_ShouldReturnProperStringRepresentation() {
            // given
            JiraResponseTemplate template = createTestTemplate();

            // when
            String result = template.toString();

            // then
            assertThat(result).contains("ResponseTemplate");
            assertThat(result).contains(template.getId());
            assertThat(result).contains(template.getTemplateName());
        }
    }

    /**
     * テスト用のレスポンステンプレートエンティティを作成するヘルパーメソッド
     */
    private JiraResponseTemplate createTestTemplate() {
        return JiraResponseTemplate.createNew(
            "テストテンプレート",
            "{\"name\": \"$!{fields.summary}\", \"status\": \"$!{fields.status.name}\"}",
            "テスト用の説明"
        );
    }
}