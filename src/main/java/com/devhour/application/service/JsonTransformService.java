package com.devhour.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.domain.service.DataMappingDomainService.CommonFormatProject;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor.ValidationResult;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor.VelocityTemplateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON変換サービス実装クラス
 * 
 * JIRA同期機能におけるJSON変換とレスポンステンプレート管理を担当するサービス
 * VelocityTemplateProcessorを使用してJIRA APIレスポンスを共通フォーマットJSONに変換し、
 * レスポンステンプレートのCRUD操作とバリデーション機能を提供する。
 * 
 * 主な機能:
 * - JIRA APIレスポンスの変換処理（REQ-5.3対応）
 * - レスポンステンプレート管理（REQ-5.2対応）
 * - テンプレート検証機能（REQ-5.4対応）
 * - エラーハンドリングとトランザクション管理
 * 
 * セキュリティ考慮事項:
 * - テンプレート名の一意性制約チェック
 * - Velocityテンプレート構文検証
 * - JSON処理エラーの適切なハンドリング
 * - データベーストランザクション管理
 */
@Service
@Slf4j
public class JsonTransformService {
    
    private final VelocityTemplateProcessor velocityTemplateProcessor;
    private final JiraResponseTemplateRepository responseTemplateRepository;
    private final ObjectMapper objectMapper;

    /**
     * JsonTransformServiceのコンストラクタ
     *
     * @param velocityTemplateProcessor Velocityテンプレート処理サービス
     * @param responseTemplateRepository レスポンステンプレートリポジトリ
     * @param objectMapper Jackson ObjectMapper（エラーハンドリング用）
     */
    public JsonTransformService(
            VelocityTemplateProcessor velocityTemplateProcessor,
            JiraResponseTemplateRepository responseTemplateRepository,
            ObjectMapper objectMapper) {
        this.velocityTemplateProcessor = velocityTemplateProcessor;
        this.responseTemplateRepository = responseTemplateRepository;
        this.objectMapper = objectMapper;

        log.info("JsonTransformService initialized with template management capabilities");
    }
    
    /**
     * JIRAレスポンスを共通フォーマットJSONに変換
     * 
     * @param jiraResponse JIRA APIから取得したレスポンス（JSON文字列）
     * @param templateName 変換に使用するレスポンステンプレート名
     * @return 共通フォーマットJSON文字列
     * @throws JsonTransformException 変換処理でエラーが発生した場合
     */
    public String transformResponse(String jiraResponse, String templateName) {
        log.debug("Starting JIRA response transformation with template: {}", templateName);
        
        // パラメータ検証
        validateTransformParameters(jiraResponse, templateName);
        
        try {
            // テンプレート名からレスポンステンプレートを取得（最初にチェック）
            JiraResponseTemplate customFieldTemplate = responseTemplateRepository.findByTemplateName(templateName)
                .orElseThrow(() -> new JsonTransformException("Template not found: " + templateName));

            // テンプレートが存在する場合のみVelocity処理を実行
            String commonFieldJsonString = velocityTemplateProcessor.transformResponse(jiraResponse, CommonFormatProject.COMMON_FORMAT_VM_TEMPLATE);

            // VelocityTemplateProcessorを使用して変換実行
            String customFieldJsonString = velocityTemplateProcessor.transformResponse(
                jiraResponse, customFieldTemplate.getVelocityTemplate());

            // 共通フィールドとカスタムフィールドをマージ
            String transformedJson = mergeJsonFields(commonFieldJsonString, customFieldJsonString);

            log.debug("JIRA response transformation completed successfully, result length: {}",
                transformedJson.length());

            return transformedJson;
            
        } catch (VelocityTemplateException e) {
            log.error("Velocity template processing failed for template '{}': {}", templateName, e.getMessage(), e);
            throw new JsonTransformException("Failed to transform JIRA response with template: " + templateName, e);
        } catch (Exception e) {
            log.error("Unexpected error during JIRA response transformation: {}", e.getMessage(), e);
            throw new JsonTransformException("Failed to transform JIRA response: " + e.getMessage(), e);
        }
    }
    
    /**
     * 新規レスポンステンプレートを作成
     * 
     * @param templateName テンプレート名（一意性制約）
     * @param templateDescription テンプレート説明（nullable）
     * @param velocityTemplate Velocityテンプレート
     * @return 作成されたResponseTemplateエンティティ
     * @throws JsonTransformException 作成処理でエラーが発生した場合
     */
    @Transactional
    public JiraResponseTemplate createTemplate(String templateName, String templateDescription, String velocityTemplate) {
        log.debug("Creating new response template: {}", templateName);
        
        // パラメータ検証
        validateCreateTemplateParameters(templateName, velocityTemplate);
        
        try {
            // テンプレート名の重複チェック
            if (responseTemplateRepository.existsByTemplateName(templateName)) {
                throw new JsonTransformException("Template name already exists: " + templateName);
            }
            
            // Velocityテンプレートの構文検証
            ValidationResult validationResult = velocityTemplateProcessor.validateTemplate(velocityTemplate);
            if (!validationResult.isValid()) {
                throw new JsonTransformException("Template validation failed: " + validationResult.getMessage());
            }
            
            // 新しいResponseTemplateエンティティ作成
            JiraResponseTemplate newTemplate = JiraResponseTemplate.createNew(templateName, velocityTemplate, templateDescription);
            
            // データベースに保存
            JiraResponseTemplate savedTemplate = responseTemplateRepository.save(newTemplate);
            
            log.info("Response template created successfully: {} (ID: {})", templateName, savedTemplate.getId());
            
            return savedTemplate;
            
        } catch (JsonTransformException e) {
            throw e; // 既にJsonTransformExceptionの場合はそのまま再スロー
        } catch (Exception e) {
            log.error("Failed to create response template '{}': {}", templateName, e.getMessage(), e);
            throw new JsonTransformException("Failed to create template: " + templateName, e);
        }
    }
    
    /**
     * 既存レスポンステンプレートを更新
     * 
     * @param templateName 更新対象のテンプレート名
     * @param templateDescription 新しいテンプレート説明（nullable）
     * @param velocityTemplate 新しいVelocityテンプレート
     * @return 更新されたResponseTemplateエンティティ
     * @throws JsonTransformException 更新処理でエラーが発生した場合
     */
    @Transactional
    public JiraResponseTemplate updateTemplate(String templateName, String templateDescription, String velocityTemplate) {
        log.debug("Updating response template: {}", templateName);
        
        // パラメータ検証
        validateUpdateTemplateParameters(templateName, velocityTemplate);
        
        try {
            // 既存テンプレートを取得
            JiraResponseTemplate existingTemplate = responseTemplateRepository.findByTemplateName(templateName)
                .orElseThrow(() -> new JsonTransformException("Template not found: " + templateName));
            
            // 新しいVelocityテンプレートの構文検証
            ValidationResult validationResult = velocityTemplateProcessor.validateTemplate(velocityTemplate);
            if (!validationResult.isValid()) {
                throw new JsonTransformException("Template validation failed: " + validationResult.getMessage());
            }
            
            // エンティティの更新
            existingTemplate.updateTemplate(velocityTemplate);
            existingTemplate.updateDescription(templateDescription);
            
            // データベースに保存
            JiraResponseTemplate updatedTemplate = responseTemplateRepository.save(existingTemplate);
            
            log.info("Response template updated successfully: {} (ID: {})", templateName, updatedTemplate.getId());
            
            return updatedTemplate;
            
        } catch (JsonTransformException e) {
            throw e; // 既にJsonTransformExceptionの場合はそのまま再スロー
        } catch (Exception e) {
            log.error("Failed to update response template '{}': {}", templateName, e.getMessage(), e);
            throw new JsonTransformException("Failed to update template: " + templateName, e);
        }
    }
    
    /**
     * 利用可能なレスポンステンプレート一覧を取得
     * 
     * @return ResponseTemplateエンティティのリスト
     */
    @Transactional(readOnly = true)
    public List<JiraResponseTemplate> listTemplates() {
        log.debug("Retrieving all response templates");
        
        try {
            List<JiraResponseTemplate> templates = responseTemplateRepository.findAll();
            
            log.debug("Retrieved {} response templates", templates.size());
            
            return templates;
            
        } catch (Exception e) {
            log.error("Failed to retrieve response templates: {}", e.getMessage(), e);
            throw new JsonTransformException("Failed to retrieve template list", e);
        }
    }
    
    
    /**
     * 変換パラメータの検証
     * 
     * @param jiraResponse JIRA APIレスポンス
     * @param templateName テンプレート名
     * @throws JsonTransformException パラメータが不正な場合
     */
    private void validateTransformParameters(String jiraResponse, String templateName) {
        if (jiraResponse == null || jiraResponse.trim().isEmpty()) {
            throw new JsonTransformException("JIRA response cannot be null or empty");
        }
        
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new JsonTransformException("Template name cannot be null or empty");
        }
    }
    
    /**
     * テンプレート作成パラメータの検証
     * 
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @throws JsonTransformException パラメータが不正な場合
     */
    private void validateCreateTemplateParameters(String templateName, String velocityTemplate) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new JsonTransformException("Template name cannot be null or empty");
        }
        
        if (velocityTemplate == null || velocityTemplate.trim().isEmpty()) {
            throw new JsonTransformException("Velocity template cannot be null or empty");
        }
    }
    
    /**
     * テンプレート更新パラメータの検証
     *
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @throws JsonTransformException パラメータが不正な場合
     */
    private void validateUpdateTemplateParameters(String templateName, String velocityTemplate) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new JsonTransformException("Template name cannot be null or empty");
        }

        if (velocityTemplate == null || velocityTemplate.trim().isEmpty()) {
            throw new JsonTransformException("Velocity template cannot be null or empty");
        }
    }

    /**
     * 共通フィールドとカスタムフィールドのJSONをマージ
     *
     * @param commonFieldJsonString 共通フィールドのJSON文字列
     * @param customFieldJsonString カスタムフィールドのJSON文字列
     * @return マージされたJSON文字列
     * @throws JsonTransformException マージ処理でエラーが発生した場合
     */
    private String mergeJsonFields(String commonFieldJsonString, String customFieldJsonString) {
        try {
            // JSONパースしてマップに変換
            java.util.Map<String, Object> commonFields = objectMapper.readValue(
                commonFieldJsonString,
                objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)
            );

            java.util.Map<String, Object> customFields = objectMapper.readValue(
                customFieldJsonString,
                objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)
            );

            commonFields.put("customFields", customFields);

            // JSON文字列に変換して返す
            return objectMapper.writeValueAsString(commonFields);

        } catch (Exception e) {
            log.error("Failed to merge JSON fields: {}", e.getMessage(), e);
            throw new JsonTransformException("Failed to merge common and custom fields: " + e.getMessage(), e);
        }
    }

    /**
     * JSON変換処理における例外クラス
     *
     * JIRA APIレスポンスの変換処理やレスポンステンプレート管理において
     * 発生する業務例外を表現する。
     */
    public static class JsonTransformException extends RuntimeException {

        /**
         * メッセージ付きでJsonTransformExceptionを作成
         *
         * @param message エラーメッセージ
         */
        public JsonTransformException(String message) {
            super(message);
        }

        /**
         * メッセージと原因例外付きでJsonTransformExceptionを作成
         *
         * @param message エラーメッセージ
         * @param cause 原因例外
         */
        public JsonTransformException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}