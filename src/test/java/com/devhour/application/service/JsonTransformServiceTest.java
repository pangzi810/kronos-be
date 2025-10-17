package com.devhour.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import com.devhour.application.service.JsonTransformService.JsonTransformException;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor.ValidationResult;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor.VelocityTemplateException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JsonTransformServiceのテストクラス
 * 
 * JIRA同期機能におけるJSON変換サービスの包括的なテスト
 * 
 * テスト対象:
 * - JIRA APIレスポンスの変換処理
 * - レスポンステンプレートの管理操作（CRUD）
 * - テンプレート検証機能
 * - エラーハンドリング
 * - トランザクション処理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonTransformService実装テスト")
class JsonTransformServiceTest {

    @Mock
    private VelocityTemplateProcessor velocityTemplateProcessor;

    @Mock
    private JiraResponseTemplateRepository responseTemplateRepository;

    private ObjectMapper objectMapper;

    private JsonTransformService jsonTransformService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper(); // Use real ObjectMapper instead of mock
        jsonTransformService = new JsonTransformService(
            velocityTemplateProcessor, responseTemplateRepository, objectMapper);
    }

    @Nested
    @DisplayName("JIRA Response Transformation Tests")
    class TransformResponseTests {

        private static final String SAMPLE_JIRA_RESPONSE = """
            {
              "key": "PROJ-123",
              "fields": {
                "summary": "Sample Project",
                "description": "This is a sample project",
                "status": {
                  "name": "In Progress"
                },
                "created": "2023-01-01T10:00:00.000Z",
                "updated": "2023-01-15T15:30:00.000Z",
                "assignee": {
                  "displayName": "John Doe"
                }
              }
            }""";

        @Test
        @DisplayName("正常なJIRAレスポンス変換 - テンプレート名指定")
        void transformResponse_ValidInput_ReturnsTransformedJson() {
            // Given
            String templateName = "Standard Project Template";
            JiraResponseTemplate template = createSampleTemplate(templateName, createSampleVelocityTemplate());

            when(responseTemplateRepository.findByTemplateName(templateName))
                .thenReturn(Optional.of(template));
            when(velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE,
                com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE))
                .thenReturn("{\"issueKey\": \"PROJ-123\"}");
            when(velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, template.getVelocityTemplate()))
                .thenReturn("{\"customField1\": \"value1\"}");

            // When
            String result = jsonTransformService.transformResponse(SAMPLE_JIRA_RESPONSE, templateName);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("issueKey");
            assertThat(result).contains("customFields");
            verify(responseTemplateRepository).findByTemplateName(templateName);
            verify(velocityTemplateProcessor).transformResponse(SAMPLE_JIRA_RESPONSE,
                com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE);
            verify(velocityTemplateProcessor).transformResponse(SAMPLE_JIRA_RESPONSE, template.getVelocityTemplate());
        }

        @Test
        @DisplayName("存在しないテンプレート名指定時のエラー")
        void transformResponse_NonExistentTemplate_ThrowsException() {
            // Given
            String nonExistentTemplateName = "Non-existent Template";
            when(responseTemplateRepository.findByTemplateName(nonExistentTemplateName))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse(SAMPLE_JIRA_RESPONSE, nonExistentTemplateName))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Template not found: " + nonExistentTemplateName);

            verify(responseTemplateRepository).findByTemplateName(nonExistentTemplateName);
            verifyNoInteractions(velocityTemplateProcessor);
        }

        @Test
        @DisplayName("null JIRAレスポンス指定時のエラー")
        void transformResponse_NullJiraResponse_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse(null, "templateName"))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("JIRA response cannot be null or empty");

            verifyNoInteractions(responseTemplateRepository, velocityTemplateProcessor);
        }

        @Test
        @DisplayName("空文字列JIRAレスポンス指定時のエラー")
        void transformResponse_EmptyJiraResponse_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse("", "templateName"))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("JIRA response cannot be null or empty");

            verifyNoInteractions(responseTemplateRepository, velocityTemplateProcessor);
        }

        @Test
        @DisplayName("nullテンプレート名指定時のエラー")
        void transformResponse_NullTemplateName_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse(SAMPLE_JIRA_RESPONSE, null))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Template name cannot be null or empty");

            verifyNoInteractions(responseTemplateRepository, velocityTemplateProcessor);
        }

        @Test
        @DisplayName("Velocity変換エラー時のエラーハンドリング")
        void transformResponse_VelocityTemplateError_ThrowsJsonTransformException() {
            // Given
            String templateName = "Error Template";
            JiraResponseTemplate template = createSampleTemplate(templateName, createSampleVelocityTemplate());

            when(responseTemplateRepository.findByTemplateName(templateName))
                .thenReturn(Optional.of(template));
            when(velocityTemplateProcessor.transformResponse(any(), any()))
                .thenThrow(new VelocityTemplateException("Template processing failed"));

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse(SAMPLE_JIRA_RESPONSE, templateName))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Failed to transform JIRA response")
                .hasCauseInstanceOf(VelocityTemplateException.class);

            verify(responseTemplateRepository).findByTemplateName(templateName);
            verify(velocityTemplateProcessor).transformResponse(eq(SAMPLE_JIRA_RESPONSE), any(String.class));
        }

        private String createSampleVelocityTemplate() {
            return """
                {
                  "projectCode": "$key",
                  "projectName": "$fields.summary",
                  "projectDescription": "$!fields.description",
                  "projectStatus": "$fields.status.name",
                  "createdAt": "$dateUtils.formatDate($fields.created)",
                  "updatedAt": "$dateUtils.formatDate($fields.updated)",
                  "assigneeName": "$!fields.assignee.displayName"
                }""";
        }
    }

    @Nested
    @DisplayName("Template Management Tests")
    class TemplateManagementTests {

        @Test
        @DisplayName("新規テンプレート作成 - 正常ケース")
        void createTemplate_ValidInput_CreatesTemplateSuccessfully() {
            // Given
            String templateName = "New Test Template";
            String templateDescription = "Test template description";
            String velocityTemplate = """
                {
                  "projectCode": "$key",
                  "projectName": "$fields.summary"
                }""";

            when(responseTemplateRepository.existsByTemplateName(templateName)).thenReturn(false);
            when(velocityTemplateProcessor.validateTemplate(velocityTemplate))
                .thenReturn(ValidationResult.success("Template is valid"));

            JiraResponseTemplate savedTemplate = createSampleTemplate(templateName, velocityTemplate);
            when(responseTemplateRepository.save(any(JiraResponseTemplate.class))).thenReturn(savedTemplate);

            // When
            JiraResponseTemplate result = jsonTransformService.createTemplate(templateName, templateDescription, velocityTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTemplateName()).isEqualTo(templateName);
            assertThat(result.getVelocityTemplate()).isEqualTo(velocityTemplate);
            assertThat(result.getTemplateDescription()).isEqualTo(templateDescription);

            verify(responseTemplateRepository).existsByTemplateName(templateName);
            verify(velocityTemplateProcessor).validateTemplate(velocityTemplate);
            verify(responseTemplateRepository).save(any(JiraResponseTemplate.class));
        }

        @Test
        @DisplayName("重複テンプレート名での作成エラー")
        void createTemplate_DuplicateTemplateName_ThrowsException() {
            // Given
            String duplicateTemplateName = "Existing Template";
            when(responseTemplateRepository.existsByTemplateName(duplicateTemplateName)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.createTemplate(
                duplicateTemplateName, "Description", "template"))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Template name already exists: " + duplicateTemplateName);

            verify(responseTemplateRepository).existsByTemplateName(duplicateTemplateName);
            verifyNoMoreInteractions(responseTemplateRepository);
            verifyNoInteractions(velocityTemplateProcessor);
        }

        @Test
        @DisplayName("無効なテンプレート構文での作成エラー")
        void createTemplate_InvalidTemplate_ThrowsException() {
            // Given
            String templateName = "Invalid Template";
            String invalidTemplate = "invalid velocity template";

            when(responseTemplateRepository.existsByTemplateName(templateName)).thenReturn(false);
            when(velocityTemplateProcessor.validateTemplate(invalidTemplate))
                .thenReturn(ValidationResult.error("Invalid template syntax"));

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.createTemplate(
                templateName, "Description", invalidTemplate))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Template validation failed: Invalid template syntax");

            verify(responseTemplateRepository).existsByTemplateName(templateName);
            verify(velocityTemplateProcessor).validateTemplate(invalidTemplate);
            verifyNoMoreInteractions(responseTemplateRepository);
        }

        @Test
        @DisplayName("テンプレート更新 - 正常ケース")
        void updateTemplate_ValidInput_UpdatesTemplateSuccessfully() {
            // Given
            String templateName = "Update Test Template";
            String newDescription = "Updated description";
            String newVelocityTemplate = """
                {
                  "projectCode": "$key",
                  "projectName": "$fields.summary",
                  "updatedField": "new field"
                }""";

            JiraResponseTemplate existingTemplate = createSampleTemplate(templateName, "old template");
            when(responseTemplateRepository.findByTemplateName(templateName))
                .thenReturn(Optional.of(existingTemplate));
            when(velocityTemplateProcessor.validateTemplate(newVelocityTemplate))
                .thenReturn(ValidationResult.success("Template is valid"));

            JiraResponseTemplate updatedTemplate = createSampleTemplate(templateName, newVelocityTemplate);
            when(responseTemplateRepository.save(any(JiraResponseTemplate.class))).thenReturn(updatedTemplate);

            // When
            JiraResponseTemplate result = jsonTransformService.updateTemplate(templateName, newDescription, newVelocityTemplate);

            // Then
            assertThat(result).isNotNull();
            verify(responseTemplateRepository).findByTemplateName(templateName);
            verify(velocityTemplateProcessor).validateTemplate(newVelocityTemplate);
            verify(responseTemplateRepository).save(any(JiraResponseTemplate.class));
        }

        @Test
        @DisplayName("存在しないテンプレート更新エラー")
        void updateTemplate_NonExistentTemplate_ThrowsException() {
            // Given
            String nonExistentTemplateName = "Non-existent Template";
            when(responseTemplateRepository.findByTemplateName(nonExistentTemplateName))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.updateTemplate(
                nonExistentTemplateName, "Description", "template"))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Template not found: " + nonExistentTemplateName);

            verify(responseTemplateRepository).findByTemplateName(nonExistentTemplateName);
            verifyNoMoreInteractions(responseTemplateRepository);
            verifyNoInteractions(velocityTemplateProcessor);
        }

        @Test
        @DisplayName("テンプレート一覧取得 - 正常ケース")
        void listTemplates_ReturnsAllTemplates() {
            // Given
            List<JiraResponseTemplate> expectedTemplates = Arrays.asList(
                createSampleTemplate("Template1", "velocity1"),
                createSampleTemplate("Template2", "velocity2"),
                createSampleTemplate("Template3", "velocity3")
            );
            when(responseTemplateRepository.findAll()).thenReturn(expectedTemplates);

            // When
            List<JiraResponseTemplate> result = jsonTransformService.listTemplates();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyElementsOf(expectedTemplates);
            verify(responseTemplateRepository).findAll();
        }

        @Test
        @DisplayName("テンプレート一覧取得 - 空のリスト")
        void listTemplates_EmptyRepository_ReturnsEmptyList() {
            // Given
            when(responseTemplateRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<JiraResponseTemplate> result = jsonTransformService.listTemplates();

            // Then
            assertThat(result).isEmpty();
            verify(responseTemplateRepository).findAll();
        }
    }


    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("データベースエラー時のエラーハンドリング")
        void createTemplate_DatabaseError_ThrowsJsonTransformException() {
            // Given
            String templateName = "DB Error Template";
            String velocityTemplate = """
                {
                  "projectCode": "$key"
                }""";

            when(responseTemplateRepository.existsByTemplateName(templateName)).thenReturn(false);
            when(velocityTemplateProcessor.validateTemplate(velocityTemplate))
                .thenReturn(ValidationResult.success("Template is valid"));
            when(responseTemplateRepository.save(any(JiraResponseTemplate.class)))
                .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.createTemplate(
                templateName, "Description", velocityTemplate))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Failed to create template")
                .hasCauseInstanceOf(DataIntegrityViolationException.class);

            verify(responseTemplateRepository).existsByTemplateName(templateName);
            verify(velocityTemplateProcessor).validateTemplate(velocityTemplate);
            verify(responseTemplateRepository).save(any(JiraResponseTemplate.class));
        }

        @Test
        @DisplayName("JSON処理エラー時のエラーハンドリング")
        void transformResponse_JsonProcessingError_ThrowsJsonTransformException() {
            // Given
            String templateName = "JSON Error Template";
            String invalidJson = "invalid json";
            JiraResponseTemplate template = createSampleTemplate(templateName, "template");

            when(responseTemplateRepository.findByTemplateName(templateName))
                .thenReturn(Optional.of(template));
            when(velocityTemplateProcessor.transformResponse(eq(invalidJson),
                eq(com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE)))
                .thenReturn("invalid json syntax");
            when(velocityTemplateProcessor.transformResponse(invalidJson, template.getVelocityTemplate()))
                .thenReturn("{\"valid\": \"json\"}");

            // When & Then
            assertThatThrownBy(() -> jsonTransformService.transformResponse(invalidJson, templateName))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("Failed to merge common and custom fields");

            verify(responseTemplateRepository).findByTemplateName(templateName);
            verify(velocityTemplateProcessor).transformResponse(invalidJson,
                com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE);
            verify(velocityTemplateProcessor).transformResponse(invalidJson, template.getVelocityTemplate());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("大容量JIRAレスポンス処理")
        void transformResponse_LargeJiraResponse_ProcessedSuccessfully() {
            // Given
            StringBuilder largeResponse = new StringBuilder();
            largeResponse.append("{");
            largeResponse.append("\"key\": \"LARGE-123\",");
            largeResponse.append("\"fields\": {");
            for (int i = 0; i < 1000; i++) {
                largeResponse.append("\"field").append(i).append("\": \"value").append(i).append("\"");
                if (i < 999) largeResponse.append(",");
            }
            largeResponse.append("}}");

            String templateName = "Large Response Template";
            JiraResponseTemplate template = createSampleTemplate(templateName, "{\"result\": \"processed\"}");

            when(responseTemplateRepository.findByTemplateName(templateName))
                .thenReturn(Optional.of(template));
            when(velocityTemplateProcessor.transformResponse(largeResponse.toString(),
                com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE))
                .thenReturn("{\"issueKey\": \"LARGE-123\"}");
            when(velocityTemplateProcessor.transformResponse(largeResponse.toString(), template.getVelocityTemplate()))
                .thenReturn("{\"result\": \"processed\"}");

            // When
            String result = jsonTransformService.transformResponse(largeResponse.toString(), templateName);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("issueKey");
            assertThat(result).contains("customFields");
            verify(responseTemplateRepository).findByTemplateName(templateName);
            verify(velocityTemplateProcessor).transformResponse(largeResponse.toString(),
                com.devhour.domain.service.DataMappingDomainService.CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE);
            verify(velocityTemplateProcessor).transformResponse(largeResponse.toString(), template.getVelocityTemplate());
        }

        @Test
        @DisplayName("特殊文字を含むテンプレート処理")
        void createTemplate_SpecialCharactersInTemplate_CreatesSuccessfully() {
            // Given
            String templateName = "Special Chars Template";
            String velocityTemplate = """
                {
                  "projectCode": "$key",
                  "specialField": "Special chars: äöü αβγ 中文 😀",
                  "escapedField": "Quote: \\"test\\"",
                  "unicodeField": "\\u3042\\u3044\\u3046"
                }""";

            when(responseTemplateRepository.existsByTemplateName(templateName)).thenReturn(false);
            when(velocityTemplateProcessor.validateTemplate(velocityTemplate))
                .thenReturn(ValidationResult.success("Template is valid"));

            JiraResponseTemplate savedTemplate = createSampleTemplate(templateName, velocityTemplate);
            when(responseTemplateRepository.save(any(JiraResponseTemplate.class))).thenReturn(savedTemplate);

            // When
            JiraResponseTemplate result = jsonTransformService.createTemplate(templateName, "Special chars test", velocityTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getVelocityTemplate()).isEqualTo(velocityTemplate);
            verify(responseTemplateRepository).existsByTemplateName(templateName);
            verify(velocityTemplateProcessor).validateTemplate(velocityTemplate);
            verify(responseTemplateRepository).save(any(JiraResponseTemplate.class));
        }

        @Test
        @DisplayName("長いテンプレート名での処理")
        void createTemplate_LongTemplateName_ProcessedCorrectly() {
            // Given
            String longTemplateName = "A".repeat(255); // Maximum length template name
            String velocityTemplate = "{\"result\": \"test\"}";

            when(responseTemplateRepository.existsByTemplateName(longTemplateName)).thenReturn(false);
            when(velocityTemplateProcessor.validateTemplate(velocityTemplate))
                .thenReturn(ValidationResult.success("Template is valid"));

            JiraResponseTemplate savedTemplate = createSampleTemplate(longTemplateName, velocityTemplate);
            when(responseTemplateRepository.save(any(JiraResponseTemplate.class))).thenReturn(savedTemplate);

            // When
            JiraResponseTemplate result = jsonTransformService.createTemplate(longTemplateName, "Long name test", velocityTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTemplateName()).isEqualTo(longTemplateName);
            verify(responseTemplateRepository).existsByTemplateName(longTemplateName);
            verify(velocityTemplateProcessor).validateTemplate(velocityTemplate);
            verify(responseTemplateRepository).save(any(JiraResponseTemplate.class));
        }
    }

    /**
     * テスト用のResponseTemplateエンティティ作成ヘルパーメソッド
     */
    private JiraResponseTemplate createSampleTemplate(String templateName, String velocityTemplate) {
        return JiraResponseTemplate.restore(
            "test-id-" + templateName.hashCode(),
            templateName,
            velocityTemplate,
            "Test template description",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }
}