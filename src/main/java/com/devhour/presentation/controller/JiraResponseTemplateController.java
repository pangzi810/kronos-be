package com.devhour.presentation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.JsonTransformService;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.infrastructure.velocity.VelocityTemplateProcessor;
import com.devhour.presentation.dto.request.JiraResponseTemplateRequest;
import com.devhour.presentation.dto.request.JiraTemplateTestRequest;
import com.devhour.presentation.dto.response.ErrorResponse;
import com.devhour.presentation.dto.response.TemplateTestResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * レスポンステンプレート管理コントローラー
 * 
 * JIRA同期機能で使用するレスポンステンプレートのCRUD操作を提供するRESTful API VelocityテンプレートエンジンによるJSON変換テンプレートの管理機能を実装
 * 
 * 提供機能: - レスポンステンプレート一覧取得 - レスポンステンプレート詳細取得 - レスポンステンプレート作成 - レスポンステンプレート更新 - レスポンステンプレート削除 -
 * テンプレートテスト実行
 * 
 * 認可制御: - システム統合管理権限を持つユーザーのみアクセス可能 - @PreAuthorize("hasAuthority('SCOPE_system:manage:integration')")による制御
 * 
 * エラーハンドリング: - バリデーションエラー: 400 Bad Request - テンプレート重複: 409 Conflict - テンプレート不存在: 404 Not Found -
 * Velocity構文エラー: 400 Bad Request - システムエラー: 500 Internal Server Error
 * 
 * 要件対応: - REQ-6.3.1: レスポンステンプレート管理API実装 - REQ-5.2: テンプレート管理メソッド（作成、更新、一覧） - REQ-5.3:
 * JsonPath形式バリデーションとVelocityテンプレート処理 - REQ-5.7: テンプレート検証・テスト機能
 */
@RestController
@RequestMapping("/api/jira/templates")
@Slf4j
public class JiraResponseTemplateController {

    private final JsonTransformService jsonTransformService;
    private final JiraResponseTemplateRepository responseTemplateRepository;
    private final VelocityTemplateProcessor velocityTemplateProcessor;

    /**
     * ResponseTemplateControllerのコンストラクタ
     * 
     * @param jsonTransformService JSON変換サービス
     * @param responseTemplateRepository レスポンステンプレートリポジトリ
     * @param velocityTemplateProcessor Velocityテンプレートプロセッサ
     */
    public JiraResponseTemplateController(JsonTransformService jsonTransformService,
            JiraResponseTemplateRepository responseTemplateRepository,
            VelocityTemplateProcessor velocityTemplateProcessor) {
        this.jsonTransformService = jsonTransformService;
        this.responseTemplateRepository = responseTemplateRepository;
        this.velocityTemplateProcessor = velocityTemplateProcessor;
    }

    /**
     * レスポンステンプレート一覧取得API
     * 
     * 利用可能な全レスポンステンプレート一覧を取得する テンプレート名昇順でソートされた結果を返す
     * 
     * @return レスポンステンプレート一覧
     * 
     *         成功レスポンス: 200 OK エラーレスポンス: - 401 Unauthorized: 認証が必要 - 403 Forbidden: PMO/管理者権限が必要 -
     *         500 Internal Server Error: システムエラー
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<List<JiraResponseTemplate>> listTemplates() {

        List<JiraResponseTemplate> templates = jsonTransformService.listTemplates();

        return ResponseEntity.ok(templates);
    }

    /**
     * レスポンステンプレート詳細取得API
     * 
     * 指定されたIDのレスポンステンプレート詳細情報を取得する
     * 
     * @param id レスポンステンプレートID
     * @return レスポンステンプレート詳細
     * 
     *         成功レスポンス: 200 OK エラーレスポンス: - 400 Bad Request: IDフォーマット不正 - 401 Unauthorized: 認証が必要 -
     *         403 Forbidden: PMO/管理者権限が必要 - 404 Not Found: テンプレートが見つからない - 500 Internal Server
     *         Error: システムエラー
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<?> getTemplateById(@PathVariable String id) {
        // IDフォーマット検証
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_ID", "テンプレートIDが無効です"));
        }

        try {
            // すべてのテンプレートを取得してIDで検索（簡単な実装）
            List<JiraResponseTemplate> templates = jsonTransformService.listTemplates();
            JiraResponseTemplate template =
                    templates.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);

            if (template == null) {
                log.debug("Response template not found: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of("TEMPLATE_NOT_FOUND", "レスポンステンプレートが見つかりません"));
            }

            return ResponseEntity.ok(template);

        } catch (Exception e) {
            log.error("Failed to retrieve response template by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("INTERNAL_ERROR", "レスポンステンプレートの取得に失敗しました"));
        }
    }

    /**
     * レスポンステンプレート作成API
     * 
     * 新しいレスポンステンプレートを作成する テンプレート名の一意性制約とVelocity構文検証を実行
     * 
     * @param request テンプレート作成リクエスト
     * @return 作成されたレスポンステンプレート
     * 
     *         成功レスポンス: 201 Created エラーレスポンス: - 400 Bad Request: バリデーションエラー、Velocity構文エラー - 401
     *         Unauthorized: 認証が必要 - 403 Forbidden: PMO/管理者権限が必要 - 409 Conflict: テンプレート名重複 - 500
     *         Internal Server Error: システムエラー
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<?> createTemplate(@Valid @RequestBody JiraResponseTemplateRequest request) {

        try {
            JiraResponseTemplate createdTemplate =
                    jsonTransformService.createTemplate(request.templateName(),
                            request.templateDescription(), request.velocityTemplate());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);

        } catch (JsonTransformService.JsonTransformException e) {
            log.debug("Template creation failed: {}", e.getMessage());

            String errorMessage = e.getMessage();
            if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.of("TEMPLATE_NAME_CONFLICT", "テンプレート名が重複しています"));
            } else if (errorMessage.contains("validation failed")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("VELOCITY_SYNTAX_ERROR", "Velocityテンプレートの構文が無効です"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("VALIDATION_ERROR", errorMessage));
            }

        } catch (Exception e) {
            log.error("Failed to create response template '{}': {}", request.templateName(),
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("INTERNAL_ERROR", "レスポンステンプレートの作成に失敗しました"));
        }
    }

    /**
     * レスポンステンプレート更新API
     * 
     * 既存のレスポンステンプレートを更新する Velocity構文検証を実行し、テンプレート名変更時は重複チェックを行う
     * 
     * @param id 更新対象のテンプレートID
     * @param request テンプレート更新リクエスト
     * @return 更新されたレスポンステンプレート
     * 
     *         成功レスポンス: 200 OK エラーレスポンス: - 400 Bad Request: IDフォーマット不正、バリデーションエラー、Velocity構文エラー -
     *         401 Unauthorized: 認証が必要 - 403 Forbidden: PMO/管理者権限が必要 - 404 Not Found: テンプレートが見つからない
     *         - 409 Conflict: テンプレート名重複 - 500 Internal Server Error: システムエラー
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<?> updateTemplate(@PathVariable String id,
            @Valid @RequestBody JiraResponseTemplateRequest request) {

        // IDフォーマット検証
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_ID", "テンプレートIDが無効です"));
        }

        try {
            // まず存在するテンプレートを検索
            List<JiraResponseTemplate> templates = jsonTransformService.listTemplates();
            JiraResponseTemplate existingTemplate =
                    templates.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);

            if (existingTemplate == null) {
                log.debug("Response template not found for update: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of("TEMPLATE_NOT_FOUND", "レスポンステンプレートが見つかりません"));
            }

            // JsonTransformServiceは名前ベースで更新するため、既存テンプレート名を使用
            JiraResponseTemplate updatedTemplate =
                    jsonTransformService.updateTemplate(existingTemplate.getTemplateName(), // 既存の名前を使用
                            request.templateDescription(), request.velocityTemplate());

            // テンプレート名が変更された場合は更新
            if (!request.templateName().equals(existingTemplate.getTemplateName())) {
                updatedTemplate.updateName(request.templateName());
                responseTemplateRepository.save(updatedTemplate);
            }

            log.info("Response template updated successfully: {} (ID: {})",
                    updatedTemplate.getTemplateName(), updatedTemplate.getId());

            return ResponseEntity.ok(updatedTemplate);

        } catch (JsonTransformService.JsonTransformException e) {
            log.debug("Template update failed: {}", e.getMessage());

            String errorMessage = e.getMessage();
            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of("TEMPLATE_NOT_FOUND", "レスポンステンプレートが見つかりません"));
            } else if (errorMessage.contains("validation failed")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("VELOCITY_SYNTAX_ERROR", "Velocityテンプレートの構文が無効です"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("VALIDATION_ERROR", errorMessage));
            }

        } catch (Exception e) {
            log.error("Failed to update response template '{}' (ID: {}): {}",
                    request.templateName(), id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("INTERNAL_ERROR", "レスポンステンプレートの更新に失敗しました"));
        }
    }

    /**
     * レスポンステンプレート削除API
     * 
     * 指定されたIDのレスポンステンプレートを削除する 物理削除を実行し、関連するJQLクエリとの整合性確認は事前に必要
     * 
     * @param id 削除対象のテンプレートID
     * @return 削除完了レスポンス
     * 
     *         成功レスポンス: 204 No Content エラーレスポンス: - 400 Bad Request: IDフォーマット不正 - 401 Unauthorized:
     *         認証が必要 - 403 Forbidden: PMO/管理者権限が必要 - 404 Not Found: テンプレートが見つからない - 500 Internal
     *         Server Error: システムエラー
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<?> deleteTemplate(@PathVariable String id) {
        // IDフォーマット検証
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_ID", "テンプレートIDが無効です"));
        }

        try {
            // 存在確認
            if (!responseTemplateRepository.existsById(id)) {
                log.debug("Response template not found for deletion: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of("TEMPLATE_NOT_FOUND", "レスポンステンプレートが見つかりません"));
            }

            // 削除実行
            responseTemplateRepository.deleteById(id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Failed to delete response template {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("INTERNAL_ERROR", "レスポンステンプレートの削除に失敗しました"));
        }
    }

    /**
     * テンプレートテスト実行API
     * 
     * 指定されたテンプレートにサンプルJSONデータを適用してテスト実行を行う Velocityテンプレートの動作確認と出力結果の確認に使用
     * 
     * @param id テスト対象のテンプレートID
     * @param request テストリクエスト（サンプルデータを含む）
     * @return テスト実行結果
     * 
     *         成功レスポンス: 200 OK エラーレスポンス: - 400 Bad Request: IDフォーマット不正、テストデータ不正 - 401 Unauthorized:
     *         認証が必要 - 403 Forbidden: PMO/管理者権限が必要 - 404 Not Found: テンプレートが見つからない - 500 Internal
     *         Server Error: システムエラー
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<?> testTemplate(@PathVariable String id,
            @Valid @RequestBody JiraTemplateTestRequest request) {
        log.debug("Testing response template: {}", id);

        // IDフォーマット検証
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_ID", "テンプレートIDが無効です"));
        }

        try {
            // テンプレート検索
            List<JiraResponseTemplate> templates = jsonTransformService.listTemplates();
            JiraResponseTemplate template =
                    templates.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);

            if (template == null) {
                log.debug("Response template not found for testing: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.of("TEMPLATE_NOT_FOUND", "レスポンステンプレートが見つかりません"));
            }

            // テストデータのJSON検証
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                objectMapper.readTree(request.testData()); // JSON構文チェック
            } catch (Exception e) {
                log.debug("Invalid test data format: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("INVALID_TEST_DATA", "テストデータのJSON形式が無効です"));
            }

            // テスト実行
            long startTime = System.currentTimeMillis();

            try {
                String result = velocityTemplateProcessor
                        .testTemplate(template.getVelocityTemplate(), request.testData());
                long executionTime = System.currentTimeMillis() - startTime;

                log.debug("Template test completed successfully for template {} in {}ms", id,
                        executionTime);

                return ResponseEntity.ok(TemplateTestResponse.success(result, executionTime));

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                log.debug("Template test failed for template {}: {}", id, e.getMessage());

                return ResponseEntity.ok(TemplateTestResponse
                        .failure("テンプレートテストが失敗しました: " + e.getMessage(), executionTime));
            }

        } catch (Exception e) {
            log.error("Failed to test response template {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("INTERNAL_ERROR", "テンプレートテストの実行に失敗しました"));
        }
    }
}
