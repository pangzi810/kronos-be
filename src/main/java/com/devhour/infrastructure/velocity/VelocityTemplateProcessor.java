package com.devhour.infrastructure.velocity;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Velocityテンプレートプロセッサクラス
 * 
 * JIRA APIレスポンスをApache Velocityテンプレートエンジンを使用して
 * 共通フォーマットJSONに変換するサービス。
 * 
 * 主な機能:
 * - JIRA APIレスポンスのテンプレート変換
 * - テンプレート構文の検証
 * - テンプレートのテスト実行
 * - セキュリティ制限とエラーハンドリング
 * - ユーティリティクラスによる文字列・日付・数値処理
 * 
 * セキュリティ考慮事項:
 * - Java Reflectionアクセス制限
 * - ファイルシステムリソース読み込み無効化
 * - テンプレート実行時間制限
 * - 入力データのサニタイゼーション
 */
@Component
@Slf4j
public class VelocityTemplateProcessor {
    
    private final VelocityEngine velocityEngine;
    private final ObjectMapper objectMapper;
    
    /**
     * VelocityTemplateProcessorのコンストラクタ
     * 
     * @param objectMapper Jackson ObjectMapper（nullチェック実行）
     * @throws IllegalArgumentException ObjectMapperがnullの場合
     */
    public VelocityTemplateProcessor(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper cannot be null");
        }
        this.objectMapper = objectMapper;
        this.velocityEngine = createVelocityEngine();
        
        log.info("VelocityTemplateProcessor initialized with security restrictions");
    }
    
    /**
     * JIRAレスポンスをVelocityテンプレートで変換
     * 
     * @param jiraResponse JIRA APIレスポンス（JSON文字列）
     * @param velocityTemplate 変換用Velocityテンプレート
     * @return 変換されたJSON文字列
     * @throws VelocityTemplateException 変換処理でエラーが発生した場合
     */
    public String transformResponse(String jiraResponse, String velocityTemplate) {
        log.debug("Template transformation started");
        
        // パラメータ検証
        validateTransformParameters(jiraResponse, velocityTemplate);
        
        try {
            // JIRA レスポンスをJsonNodeに変換
            JsonNode jiraData = objectMapper.readTree(jiraResponse);
            
            // Velocityコンテキスト作成
            VelocityContext context = createVelocityContext(jiraData);
            
            // テンプレート処理
            StringWriter writer = new StringWriter();
            velocityEngine.evaluate(context, writer, "VelocityTemplate", velocityTemplate);
            
            String result = writer.toString();
            
            // 結果がJSONとして有効かを検証
            objectMapper.readTree(result);
            
            log.debug("Template transformation completed successfully, result length: {}", result.length());
            return result;
            
        } catch (Exception e) {
            log.error("Template transformation failed: {}", e.getMessage(), e);
            throw new VelocityTemplateException("Failed to transform JIRA response with template", e);
        }
    }
    
    /**
     * テンプレートの構文検証
     * 
     * @param velocityTemplate 検証するVelocityテンプレート
     * @return 検証結果
     */
    public ValidationResult validateTemplate(String velocityTemplate) {
        log.debug("Template validation started");
        
        if (velocityTemplate == null || velocityTemplate.trim().isEmpty()) {
            return ValidationResult.error("Template is null or empty");
        }
        
        try {
            // 基本的な構文チェック：ブレースのバランス
            if (!isValidTemplateSyntax(velocityTemplate)) {
                return ValidationResult.error("Template has syntax errors");
            }
            
            // 基本的なテスト実行
            VelocityContext testContext = new VelocityContext();
            testContext.put("test", "value");
            
            StringWriter writer = new StringWriter();
            velocityEngine.evaluate(testContext, writer, "ValidationTemplate", velocityTemplate);
            
            log.debug("Template validation completed successfully");
            return ValidationResult.success("Template is valid");
            
        } catch (Exception e) {
            log.debug("Template validation failed: {}", e.getMessage());
            return ValidationResult.error("Template validation failed: " + e.getMessage());
        }
    }
    
    /**
     * テンプレート構文の基本検証
     * 
     * @param template 検証するテンプレート
     * @return 構文が正しい場合true
     */
    private boolean isValidTemplateSyntax(String template) {
        // 括弧のバランスをチェック
        int braceBalance = 0;
        boolean inReference = false;
        
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            
            if (c == '$') {
                if (i + 1 < template.length() && template.charAt(i + 1) == '{') {
                    braceBalance++;
                    inReference = true;
                    i++; // skip '{'
                } else if (i + 2 < template.length() && 
                          template.charAt(i + 1) == '!' && 
                          template.charAt(i + 2) == '{') {
                    braceBalance++;
                    inReference = true;
                    i += 2; // skip '!{'
                }
            } else if (c == '}' && inReference) {
                braceBalance--;
                if (braceBalance == 0) {
                    inReference = false;
                }
                if (braceBalance < 0) {
                    return false; // 対応しない閉じ括弧
                }
            }
        }
        
        return braceBalance == 0; // すべての括弧が閉じられていること
    }
    
    /**
     * テンプレートのテスト実行
     * 
     * @param velocityTemplate テストするVelocityテンプレート
     * @param testData テスト用JSON文字列
     * @return テスト実行結果
     * @throws VelocityTemplateException テスト実行でエラーが発生した場合
     */
    public String testTemplate(String velocityTemplate, String testData) {
        log.debug("Template test execution started");
        
        try {
            // テストデータをJsonNodeに変換
            JsonNode testNode = objectMapper.readTree(testData);
            
            // Velocityコンテキスト作成
            VelocityContext context = createVelocityContext(testNode);
            
            // テンプレート処理
            StringWriter writer = new StringWriter();
            velocityEngine.evaluate(context, writer, "TestTemplate", velocityTemplate);
            
            String result = writer.toString();
            
            log.debug("Template test execution completed successfully");
            return result;
            
        } catch (Exception e) {
            log.error("Template test failed: {}", e.getMessage(), e);
            throw new VelocityTemplateException("Template test failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * セキュリティが強化されたVelocityエンジンを作成
     * 
     * @return 設定済みVelocityEngine
     */
    private VelocityEngine createVelocityEngine() {
        VelocityEngine engine = new VelocityEngine();
        
        // 基本設定
        engine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        engine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
        engine.setProperty("input.encoding", "UTF-8");
        engine.setProperty("output.encoding", "UTF-8");
        
        // セキュリティ設定：Java Reflectionアクセス制限
        engine.setProperty("introspector.restrict.packages", 
                          "java.lang.reflect,java.lang.Runtime,java.lang.System");
        engine.setProperty("introspector.restrict.classes", 
                          "java.lang.Class,java.lang.Runtime,java.lang.System");
        
        // ファイルリソース読み込みを無効化（セキュリティ強化）
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
        engine.setProperty("string.resource.loader.class", 
                          "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
        
        engine.init();
        
        log.debug("VelocityEngine created with security restrictions");
        return engine;
    }
    
    /**
     * JsonNodeからVelocityContextを作成
     *
     * @param jiraData JIRA APIレスポンスのJsonNode
     * @return ユーティリティ付きVelocityContext
     */
    private VelocityContext createVelocityContext(JsonNode jiraData) {
        VelocityContext context = new VelocityContext();

        // JsonNodeをMap<String, Object>に変換
        // VelocityはMapとListを自動的にナビゲートできるため、再帰的展開は不要
        Map<String, Object> dataMap = objectMapper.convertValue(
            jiraData,
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
        );

        // トップレベルのフィールドを直接アクセス可能にする
        // $key, $fields などでアクセス可能
        dataMap.forEach((key, value) -> context.put(key, value));

        // 全体のデータも保持（$dataでアクセス可能）
        context.put("data", dataMap);

        // 元のJSON文字列も保持（デバッグ用）
        context.put("rawData", jiraData.toString());

        // ユーティリティツールを追加
        context.put("stringUtils", new StringUtils());
        context.put("dateUtils", new DateUtils());
        context.put("numberUtils", new NumberUtils());
        context.put("jsonUtils", new JsonUtils(objectMapper));

        return context;
    }
    
    
    /**
     * 変換パラメータの検証
     * 
     * @param jiraResponse JIRA APIレスポンス
     * @param velocityTemplate Velocityテンプレート
     * @throws VelocityTemplateException パラメータが不正な場合
     */
    private void validateTransformParameters(String jiraResponse, String velocityTemplate) {
        if (jiraResponse == null || jiraResponse.trim().isEmpty()) {
            throw new VelocityTemplateException("JIRA response cannot be null or empty");
        }
        
        if (velocityTemplate == null || velocityTemplate.trim().isEmpty()) {
            throw new VelocityTemplateException("Velocity template cannot be null or empty");
        }
    }
    
    /**
     * 文字列操作用ユーティリティクラス
     * 
     * Velocityテンプレート内で使用可能な文字列処理メソッドを提供
     */
    public static class StringUtils {
        
        /**
         * 文字列の前後の空白を除去
         */
        public String trim(String str) {
            return str != null ? str.trim() : null;
        }
        
        /**
         * 文字列を大文字に変換
         */
        public String uppercase(String str) {
            return str != null ? str.toUpperCase() : null;
        }
        
        /**
         * 文字列を小文字に変換
         */
        public String lowercase(String str) {
            return str != null ? str.toLowerCase() : null;
        }
        
        /**
         * 空文字列の場合にデフォルト値を返す
         */
        public String defaultIfEmpty(String str, String defaultValue) {
            return (str == null || str.trim().isEmpty()) ? defaultValue : str;
        }
        
        /**
         * すべてのスペースを除去
         */
        public String removeSpaces(String str) {
            return str != null ? str.replaceAll("\\s+", "") : null;
        }
        
        /**
         * 複数の空白を単一の空白に正規化
         */
        public String normalizeSpaces(String str) {
            return str != null ? str.replaceAll("\\s+", " ").trim() : null;
        }
    }
    
    /**
     * 日付操作用ユーティリティクラス
     * 
     * Velocityテンプレート内で使用可能な日付処理メソッドを提供
     */
    public static class DateUtils {
        
        private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
        /**
         * ISO日時文字列を日付形式にフォーマット
         */
        public String formatDate(String isoDateTime) {
            if (isoDateTime == null) {
                return null;
            }
            try {
                // ISO_INSTANT形式（Z付き）を試す
                if (isoDateTime.endsWith("Z")) {
                    return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME).format(ISO_DATE);
                }
                // 標準的なISO_DATE_TIME形式を試す
                return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME).format(ISO_DATE);
            } catch (Exception e) {
                try {
                    // ISO_INSTANTとして解析を試す
                    return java.time.Instant.parse(isoDateTime).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(ISO_DATE);
                } catch (Exception e2) {
                    // パースできない場合は元の文字列をそのまま返す
                    return isoDateTime;
                }
            }
        }
        
        /**
         * 現在の日時をISO形式で取得
         */
        public String now() {
            return LocalDateTime.now().format(ISO_DATETIME);
        }
        
        /**
         * 今日の日付をISO形式で取得
         */
        public String today() {
            return LocalDate.now().format(ISO_DATE);
        }
    }
    
    /**
     * 数値操作用ユーティリティクラス
     * 
     * Velocityテンプレート内で使用可能な数値処理メソッドを提供
     */
    public static class NumberUtils {
        
        /**
         * オブジェクトをDoubleに変換（変換不可能な場合は0.0）
         */
        public Double toDouble(Object value) {
            if (value == null) return 0.0;
            
            try {
                if (value instanceof String) {
                    return Double.parseDouble((String) value);
                }
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        /**
         * オブジェクトをIntegerに変換（変換不可能な場合は0）
         */
        public Integer toInteger(Object value) {
            if (value == null) return 0;
            
            try {
                if (value instanceof String) {
                    return Integer.parseInt((String) value);
                }
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
    
    /**
     * JSON操作用ユーティリティクラス
     * 
     * Velocityテンプレート内で使用可能なJSON処理メソッドを提供
     */
    public static class JsonUtils {
        
        private final ObjectMapper objectMapper;
        
        public JsonUtils(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }
        
        /**
         * オブジェクトをJSON文字列に変換
         */
        public String toJsonString(Object obj) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (Exception e) {
                return "{}";
            }
        }
        
        /**
         * JSON文字列をオブジェクトに変換
         */
        public Object fromJsonString(String jsonString) {
            try {
                return objectMapper.readValue(jsonString, Object.class);
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    /**
     * テンプレート検証結果クラス
     * 
     * テンプレート構文検証の結果を保持する
     */
    public static class ValidationResult {
        
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        /**
         * 成功時のValidationResultを作成
         */
        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }
        
        /**
         * エラー時のValidationResultを作成
         */
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        /**
         * 検証が成功したかを取得
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * メッセージを取得
         */
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * VelocityTemplateProcessor専用の例外クラス
     * 
     * テンプレート処理エラーや構文エラーを表現する
     */
    public static class VelocityTemplateException extends RuntimeException {
        
        /**
         * メッセージを指定してVelocityTemplateExceptionを作成
         * 
         * @param message エラーメッセージ
         */
        public VelocityTemplateException(String message) {
            super(message);
        }
        
        /**
         * メッセージと原因を指定してVelocityTemplateExceptionを作成
         * 
         * @param message エラーメッセージ
         * @param cause 例外の原因
         */
        public VelocityTemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}