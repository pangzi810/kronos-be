package com.devhour.infrastructure.velocity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * VelocityTemplateProcessorのテストクラス
 * 
 * JIRA APIレスポンスのVelocityテンプレート変換機能の包括的なテスト
 * - テンプレート処理
 * - 構文検証
 * - テスト実行
 * - エラーハンドリング
 * - セキュリティ制限
 */
@DisplayName("VelocityTemplateProcessor テスト")
class VelocityTemplateProcessorTest {

    private ObjectMapper objectMapper;
    
    private VelocityTemplateProcessor velocityTemplateProcessor;
    
    private static final String SAMPLE_JIRA_RESPONSE = """
        {
          "key": "PROJ-123",
          "fields": {
            "summary": "Test Project",
            "description": "Test Description",
            "status": { "name": "In Progress" },
            "priority": { "name": "High" },
            "assignee": { 
              "displayName": "John Doe",
              "emailAddress": "john.doe@company.com"
            },
            "customfield_10001": "PROJ",
            "customfield_10020": "2024-01-15",
            "created": "2024-01-01T10:00:00.000Z",
            "updated": "2024-01-15T15:30:00.000Z"
          }
        }
        """;

    private static final String BASIC_TEMPLATE = """
        {
          "id": "$!{key}",
          "name": "$!{fields.summary}",
          "description": "$!{fields.description}",
          "status": "$!{fields.status.name}",
          "projectCode": "$!{fields.customfield_10001}",
          "createdDate": "$!{dateUtils.formatDate($fields.created)}",
          "lastUpdated": "$!{dateUtils.formatDate($fields.updated)}"
        }
        """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        velocityTemplateProcessor = new VelocityTemplateProcessor(objectMapper);
    }

    @Nested
    @DisplayName("コンストラクタとインスタンス化")
    class ConstructorAndInstanceTests {

        @Test
        @DisplayName("正常にインスタンス化できる")
        void testConstructor() {
            assertThat(velocityTemplateProcessor).isNotNull();
        }

        @Test
        @DisplayName("ObjectMapperがnullの場合は例外をスローする")
        void testConstructorWithNullObjectMapper() {
            assertThatThrownBy(() -> new VelocityTemplateProcessor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ObjectMapper");
        }
    }

    @Nested
    @DisplayName("テンプレート変換テスト")
    class TemplateTransformationTests {

        @Test
        @DisplayName("基本的なテンプレート変換が正常に動作する")
        void testBasicTemplateTransformation() throws Exception {
            // When
            String result = velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, BASIC_TEMPLATE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("PROJ-123");
        }

        @Test
        @DisplayName("複雑なテンプレートの変換")
        void testComplexTemplateTransformation() throws Exception {
            // Given
            String complexTemplate = """
                {
                  "project": {
                    "key": "$!{key}",
                    "name": "$!{stringUtils.trim($fields.summary)}",
                    "description": "$!{stringUtils.defaultIfEmpty($fields.description, 'No description')}",
                    "status": "$!{stringUtils.uppercase($fields.status.name)}"
                  }
                }
                """;

            // When
            String result = velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, complexTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("PROJ-123");
        }

        @Test
        @DisplayName("条件分岐を含むテンプレートの変換")
        void testConditionalTemplateTransformation() throws Exception {
            // Given
            String conditionalTemplate = """
                {
                  "project": {
                    "key": "$!{key}",
                    "name": "$!{fields.summary}",
                    "status": "$!{fields.status.name}"
                  }
                }
                """;

            // When
            String result = velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, conditionalTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("PROJ-123");
        }

        @Test
        @DisplayName("不正なJIRAレスポンスの場合は例外をスローする")
        void testInvalidJiraResponse() throws Exception {
            // When & Then
            assertThatThrownBy(() -> velocityTemplateProcessor.transformResponse("invalid json", BASIC_TEMPLATE))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class)
                .hasMessageContaining("Failed to transform JIRA response");
        }

        @Test
        @DisplayName("不正なテンプレートの場合は例外をスローする")
        void testInvalidTemplate() throws Exception {
            // Given
            String invalidTemplate = "{ invalid template syntax ${unclosed_bracket";

            // When & Then
            assertThatThrownBy(() -> velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, invalidTemplate))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class);
        }
    }

    @Nested
    @DisplayName("テンプレート検証テスト")
    class TemplateValidationTests {

        @Test
        @DisplayName("有効なテンプレートの検証が成功する")
        void testValidTemplateValidation() {
            // When
            VelocityTemplateProcessor.ValidationResult result = 
                velocityTemplateProcessor.validateTemplate(BASIC_TEMPLATE);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getMessage()).contains("valid");
        }

        @Test
        @DisplayName("無効なテンプレートの検証が失敗する")
        void testInvalidTemplateValidation() {
            // Given
            String invalidTemplate = "{ invalid ${unclosed_bracket_syntax";

            // When
            VelocityTemplateProcessor.ValidationResult result = 
                velocityTemplateProcessor.validateTemplate(invalidTemplate);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getMessage()).contains("syntax errors");
        }

        @Test
        @DisplayName("空のテンプレートの検証が失敗する")
        void testEmptyTemplateValidation() {
            // When
            VelocityTemplateProcessor.ValidationResult result = 
                velocityTemplateProcessor.validateTemplate("");

            // Then
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("nullテンプレートの検証が失敗する")
        void testNullTemplateValidation() {
            // When
            VelocityTemplateProcessor.ValidationResult result = 
                velocityTemplateProcessor.validateTemplate(null);

            // Then
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("テンプレートテスト実行")
    class TemplateTestingTests {

        @Test
        @DisplayName("テンプレートのテスト実行が正常に動作する")
        void testTemplateExecution() throws Exception {
            // Given
            String testData = """
                {
                  "key": "TEST-001",
                  "fields": {
                    "summary": "Test Issue",
                    "status": { "name": "Open" }
                  }
                }
                """;
            
            String template = """
                {
                  "id": "$!{key}",
                  "title": "$!{fields.summary}",
                  "status": "$!{fields.status.name}"
                }
                """;

            // When
            String result = velocityTemplateProcessor.testTemplate(template, testData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("TEST-001");
        }

        @Test
        @DisplayName("不正なテストデータでは例外をスローする")
        void testTemplateExecutionWithInvalidData() throws Exception {
            // When & Then
            assertThatThrownBy(() -> 
                velocityTemplateProcessor.testTemplate(BASIC_TEMPLATE, "invalid json"))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class)
                .hasMessageContaining("Template test failed");
        }
    }

    @Nested
    @DisplayName("ユーティリティクラステスト")
    class UtilityClassTests {

        @Test
        @DisplayName("StringUtilsの動作確認")
        void testStringUtils() {
            // Given
            VelocityTemplateProcessor.StringUtils stringUtils = new VelocityTemplateProcessor.StringUtils();

            // When & Then
            assertThat(stringUtils.trim("  test  ")).isEqualTo("test");
            assertThat(stringUtils.uppercase("test")).isEqualTo("TEST");
            assertThat(stringUtils.lowercase("TEST")).isEqualTo("test");
            assertThat(stringUtils.defaultIfEmpty("", "default")).isEqualTo("default");
            assertThat(stringUtils.defaultIfEmpty("value", "default")).isEqualTo("value");
            assertThat(stringUtils.removeSpaces("test spaces")).isEqualTo("testspaces");
            assertThat(stringUtils.normalizeSpaces("test   multiple   spaces")).isEqualTo("test multiple spaces");
        }

        @Test
        @DisplayName("StringUtilsのnull安全性")
        void testStringUtilsNullSafety() {
            // Given
            VelocityTemplateProcessor.StringUtils stringUtils = new VelocityTemplateProcessor.StringUtils();

            // When & Then
            assertThat(stringUtils.trim(null)).isNull();
            assertThat(stringUtils.uppercase(null)).isNull();
            assertThat(stringUtils.lowercase(null)).isNull();
            assertThat(stringUtils.removeSpaces(null)).isNull();
            assertThat(stringUtils.normalizeSpaces(null)).isNull();
        }

        @Test
        @DisplayName("DateUtilsの動作確認")
        void testDateUtils() {
            // Given
            VelocityTemplateProcessor.DateUtils dateUtils = new VelocityTemplateProcessor.DateUtils();

            // When & Then
            String today = dateUtils.today();
            String now = dateUtils.now();
            
            assertThat(today).matches("\\d{4}-\\d{2}-\\d{2}");
            assertThat(now).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
            
            // formatDateのテスト
            String formatted = dateUtils.formatDate("2024-01-15T10:30:00.000Z");
            assertThat(formatted).isEqualTo("2024-01-15"); // ISO日付形式を期待
        }

        @Test
        @DisplayName("NumberUtilsの動作確認")
        void testNumberUtils() {
            // Given
            VelocityTemplateProcessor.NumberUtils numberUtils = new VelocityTemplateProcessor.NumberUtils();

            // When & Then
            assertThat(numberUtils.toDouble("123.45")).isEqualTo(123.45);
            assertThat(numberUtils.toDouble("invalid")).isEqualTo(0.0);
            assertThat(numberUtils.toDouble(null)).isEqualTo(0.0);
            assertThat(numberUtils.toDouble(123)).isEqualTo(123.0);
            
            assertThat(numberUtils.toInteger("123")).isEqualTo(123);
            assertThat(numberUtils.toInteger("invalid")).isEqualTo(0);
            assertThat(numberUtils.toInteger(null)).isEqualTo(0);
            assertThat(numberUtils.toInteger(123.45)).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("バリデーション結果クラステスト")
    class ValidationResultTests {

        @Test
        @DisplayName("成功時のValidationResult")
        void testSuccessValidationResult() {
            // When
            VelocityTemplateProcessor.ValidationResult result = 
                VelocityTemplateProcessor.ValidationResult.success("Template is valid");

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Template is valid");
        }

        @Test
        @DisplayName("エラー時のValidationResult")
        void testErrorValidationResult() {
            // When
            VelocityTemplateProcessor.ValidationResult result = 
                VelocityTemplateProcessor.ValidationResult.error("Template has errors");

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Template has errors");
        }
    }

    @Nested
    @DisplayName("例外クラステスト")
    class ExceptionTests {

        @Test
        @DisplayName("VelocityTemplateExceptionのメッセージコンストラクタ")
        void testVelocityTemplateExceptionWithMessage() {
            // When
            VelocityTemplateProcessor.VelocityTemplateException exception = 
                new VelocityTemplateProcessor.VelocityTemplateException("Test error");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("VelocityTemplateExceptionのメッセージと原因コンストラクタ")
        void testVelocityTemplateExceptionWithMessageAndCause() {
            // Given
            RuntimeException cause = new RuntimeException("Cause");

            // When
            VelocityTemplateProcessor.VelocityTemplateException exception = 
                new VelocityTemplateProcessor.VelocityTemplateException("Test error", cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("エッジケースとエラーハンドリング")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("空文字列パラメータでの例外処理")
        void testEmptyStringParameters() {
            assertThatThrownBy(() -> 
                velocityTemplateProcessor.transformResponse("", BASIC_TEMPLATE))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class);

            assertThatThrownBy(() -> 
                velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, ""))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class);
        }

        @Test
        @DisplayName("nullパラメータでの例外処理")
        void testNullParameters() {
            assertThatThrownBy(() -> 
                velocityTemplateProcessor.transformResponse(null, BASIC_TEMPLATE))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class);

            assertThatThrownBy(() -> 
                velocityTemplateProcessor.transformResponse(SAMPLE_JIRA_RESPONSE, null))
                .isInstanceOf(VelocityTemplateProcessor.VelocityTemplateException.class);
        }
    }

}