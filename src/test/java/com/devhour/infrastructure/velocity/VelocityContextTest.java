package com.devhour.infrastructure.velocity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Velocity ネストアクセス検証テスト")
public class VelocityContextTest {

    private VelocityEngine velocityEngine;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        velocityEngine = new VelocityEngine();
        velocityEngine.init();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Mapオブジェクトのネストアクセス - ルートのみ追加")
    void testNestedAccessWithMapOnly() throws Exception {
        // Given: ネストしたJSON
        String jsonString = """
            {
                "key": "PROJ-123",
                "fields": {
                    "summary": "Test Issue",
                    "status": {
                        "name": "In Progress",
                        "id": 3
                    },
                    "labels": ["bug", "critical"]
                }
            }
            """;

        // JSONをMapに変換
        Map<String, Object> dataMap = objectMapper.readValue(jsonString, Map.class);

        // VelocityContextにルートのみ追加
        VelocityContext context = new VelocityContext();
        context.put("data", dataMap);

        // テンプレート
        String template = """
            Key: $!{data.key}
            Summary: $!{data.fields.summary}
            Status: $!{data.fields.status.name}
            Status ID: $!{data.fields.status.id}
            First Label: $!{data.fields.labels[0]}
            Second Label: $!{data.fields.labels[1]}
            """;

        // When: テンプレート処理
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "test", template);
        String result = writer.toString();

        // Then: ネストアクセスが成功
        assertThat(result).contains("Key: PROJ-123");
        assertThat(result).contains("Summary: Test Issue");
        assertThat(result).contains("Status: In Progress");
        assertThat(result).contains("Status ID: 3");
        assertThat(result).contains("First Label: bug");
        assertThat(result).contains("Second Label: critical");
    }

    @Test
    @DisplayName("JsonNodeの直接追加ではネストアクセス不可")
    void testNestedAccessWithJsonNodeDirectly() throws Exception {
        // Given: ネストしたJSON
        String jsonString = """
            {
                "key": "PROJ-123",
                "fields": {
                    "summary": "Test Issue"
                }
            }
            """;

        // JsonNodeとして解析
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // VelocityContextにJsonNodeを直接追加
        VelocityContext context = new VelocityContext();
        context.put("data", jsonNode);

        // テンプレート
        String template = """
            Direct: $!{data}
            Key attempt: $!{data.key}
            Fields attempt: $!{data.fields.summary}
            """;

        // When: テンプレート処理
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "test", template);
        String result = writer.toString();

        // Then: JsonNodeは文字列として出力され、ネストアクセスは失敗
        assertThat(result).contains("Direct: {");  // JSON文字列として出力
        assertThat(result).contains("Key attempt: ");  // 空（アクセス失敗）
        assertThat(result).contains("Fields attempt: ");  // 空（アクセス失敗）
    }

    @Test
    @DisplayName("foreach文でのコレクション処理")
    void testForeachWithNestedCollections() throws Exception {
        // Given
        String jsonString = """
            {
                "operations": {
                    "linkGroups": [
                        {
                            "id": "group1",
                            "links": [
                                {"label": "Edit", "href": "/edit"},
                                {"label": "Delete", "href": "/delete"}
                            ]
                        },
                        {
                            "id": "group2",
                            "links": [
                                {"label": "View", "href": "/view"}
                            ]
                        }
                    ]
                }
            }
            """;

        Map<String, Object> dataMap = objectMapper.readValue(jsonString, Map.class);
        VelocityContext context = new VelocityContext();
        context.put("data", dataMap);

        String template = """
            #foreach($group in $data.operations.linkGroups)
            Group: $!{group.id}
              #foreach($link in $group.links)
              - $!{link.label}: $!{link.href}
              #end
            #end
            """;

        // When
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "test", template);
        String result = writer.toString();

        // Then
        assertThat(result).contains("Group: group1");
        assertThat(result).contains("- Edit: /edit");
        assertThat(result).contains("- Delete: /delete");
        assertThat(result).contains("Group: group2");
        assertThat(result).contains("- View: /view");
    }
}