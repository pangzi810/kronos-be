package com.devhour.presentation.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.JiraJqlQueryApplicationService;
import com.devhour.application.service.JiraJqlQueryApplicationService.JqlValidationResult;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.ValidationResult;
import com.devhour.presentation.dto.request.JiraJqlQueryRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * JQLクエリ管理REST APIコントローラー
 * 
 * JIRA同期機能におけるJQLクエリの管理機能を提供する
 * クエリの作成・更新・削除・検索・JQL構文検証機能をサポート
 * 
 * エンドポイント:
 * - GET /api/jira/queries: JQLクエリ一覧取得（ページネーション対応）
 * - POST /api/jira/queries: JQLクエリ作成
 * - PUT /api/jira/queries/{id}: JQLクエリ更新
 * - DELETE /api/jira/queries/{id}: JQLクエリ削除
 * - POST /api/jira/queries/{id}/validate: JQL構文検証
 * 
 * セキュリティ:
 * - PMOロール以上でのみアクセス可能
 * - 管理者権限による包括的な管理機能
 * 
 * 要件対応:
 * - REQ-2.2: JQLクエリ登録・管理機能
 * - REQ-2.3: JQL構文検証機能
 * - REQ-2.4: 実行優先度管理機能
 * - REQ-2.6: クエリ一覧表示機能
 * - REQ-2.7: クエリ編集・削除機能
 * - REQ-2.8: JQLクエリ論理削除機能
 */
@RestController
@RequestMapping("/api/jira/queries")
@Validated
@Slf4j
@ConditionalOnProperty(name = "jira.integration.enabled", havingValue = "true", matchIfMissing = false)
public class JiraJqlQueryController {
    
    private final JiraJqlQueryApplicationService jqlQueryApplicationService;
    
    public JiraJqlQueryController(JiraJqlQueryApplicationService jqlQueryApplicationService) {
        this.jqlQueryApplicationService = jqlQueryApplicationService;
    }
    
    /**
     * JQLクエリ一覧取得（ページネーション対応）
     * 
     * 管理者またはPMOがJQLクエリの一覧を取得します。
     * ページネーション機能により大量データの効率的な表示をサポートします。
     * 
     * @param page ページ番号（0から開始、デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20、最大: 100）
     * @param activeOnly アクティブなクエリのみ取得するかフラグ（デフォルト: false）
     * @return JQLクエリ一覧レスポンス
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<List<JiraJqlQuery>> listQueries(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "false") Boolean activeOnly
            ) {
        
        // パラメータ検証
        validatePaginationParameters(page, size);
        
        List<JiraJqlQuery> queries;
        if (activeOnly) {
            // アクティブなクエリのみを優先度順で取得
            queries = jqlQueryApplicationService.getQueriesByPriority();
        } else {
            // 全クエリを取得
            queries = jqlQueryApplicationService.findAll();
        }
        
        // 簡易ページネーション（実際のプロダクションでは適切なページネーション実装が必要）
        List<JiraJqlQuery> paginatedQueries = applyPagination(queries, page, size);
        
        return ResponseEntity.ok(paginatedQueries);
    }
    
    /**
     * JQLクエリ作成
     * 
     * 管理者またはPMOが新しいJQLクエリを作成します。
     * JQL構文の事前検証、テンプレート存在確認、クエリ名重複チェックを実行します。
     * 
     * @param request JQLクエリ作成リクエスト
     * @return 作成されたJQLクエリエンティティ
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<JiraJqlQuery> createQuery(
            @Valid @RequestBody JiraJqlQueryRequest request
            ) {
        
        JiraJqlQuery createdQuery = jqlQueryApplicationService.createJqlQuery(
                request.getQueryName(),
                request.getJqlExpression(),
                request.getTemplateId(),
                request.getPriority(),
                SecurityUtils.requireCurrentUserId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuery);
    }
    
    /**
     * JQLクエリ更新
     * 
     * 管理者またはPMOが既存のJQLクエリを更新します。
     * 存在確認、重複チェック、ドメインルール検証を実行します。
     * 
     * @param id 更新対象のJQLクエリID
     * @param request JQLクエリ更新リクエスト
     * @return 更新されたJQLクエリエンティティ
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<JiraJqlQuery> updateQuery(
            @PathVariable String id,
            @Valid @RequestBody JiraJqlQueryRequest request
            ) {
        
        // IDフォーマット検証
        validateQueryId(id);
        
        JiraJqlQuery updatedQuery = jqlQueryApplicationService.updateJqlQuery(
                id,
                request.getQueryName(),
                request.getJqlExpression(),
                request.getTemplateId(),
                request.getPriority(),
                SecurityUtils.requireCurrentUserId()
        );
        
        // アクティブ状態の更新（必要に応じて）
        if (!updatedQuery.isActive().equals(request.getIsActive())) {
            if (request.getIsActive()) {
                updatedQuery = jqlQueryApplicationService.activateQuery(id);
            } else {
                updatedQuery = jqlQueryApplicationService.deactivateQuery(id);
            }
        }
        
        return ResponseEntity.ok(updatedQuery);
    }
    
    /**
     * JQLクエリ削除
     * 
     * 管理者またはPMOがJQLクエリを論理削除します。
     * 物理削除ではなく非アクティブ化による論理削除を実行します。
     * 
     * @param id 削除対象のJQLクエリID
     * @return 削除完了レスポンス
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<Void> deleteQuery(
            @PathVariable String id
            ) {
        
        // IDフォーマット検証
        validateQueryId(id);
        
        // 論理削除実行
        jqlQueryApplicationService.deleteJqlQuery(id, SecurityUtils.requireCurrentUserId());
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * JQL構文検証
     * 
     * 管理者またはPMOがJQLクエリの構文を検証します。
     * JIRA APIを使用してクエリの有効性とマッチするイシュー数を確認します。
     * 
     * @param id 検証対象のJQLクエリID
     * @return JQL検証結果
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<ValidationResult> validateQuery(
            @PathVariable String id
            ) {
        
        // IDフォーマット検証
        validateQueryId(id);
        
        // JQLクエリ存在確認
        Optional<JiraJqlQuery> queryOpt = jqlQueryApplicationService.findById(id);
        if (!queryOpt.isPresent()) {
            log.error("JQL構文検証対象のクエリが見つかりません: id={}", id);
            return ResponseEntity.notFound().build();
        }
        
        JiraJqlQuery query = queryOpt.get();
        
        // JQL構文検証実行
        JqlValidationResult validationResult = jqlQueryApplicationService.validateJql(query.getJqlExpression());
        
        // プレゼンテーション層のDTOに変換
        ValidationResult result;
        if (validationResult.isValid()) {
            result = ValidationResult.valid(validationResult.getMatchingProjectCount());
            log.info("JQL構文検証成功: id={}, matchingCount={}, user={}", 
                    id, validationResult.getMatchingProjectCount(), SecurityUtils.getCurrentUsername().orElse("anonymous"));
        } else {
            result = ValidationResult.invalid(validationResult.getErrorMessage());
            log.warn("JQL構文検証失敗: id={}, error={}, user={}", 
                    id, validationResult.getErrorMessage(), SecurityUtils.getCurrentUsername().orElse("anonymous"));
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * ページネーションパラメータの検証
     * 
     * @param page ページ番号
     * @param size ページサイズ
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    private void validatePaginationParameters(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("ページ番号は0以上である必要があります");
        }
        if (size == null || size <= 0) {
            throw new IllegalArgumentException("ページサイズは1以上である必要があります");
        }
        if (size > 100) {
            throw new IllegalArgumentException("ページサイズは100以下である必要があります");
        }
    }
    
    /**
     * JQLクエリIDの検証
     * 
     * @param id JQLクエリID
     * @throws IllegalArgumentException IDが無効な場合
     */
    private void validateQueryId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("JQLクエリIDは必須です");
        }
    }
    
    /**
     * 簡易ページネーション適用
     * 
     * 注意: 実際のプロダクション環境では、データベースレベルでのページネーション
     * （LIMIT/OFFSET）を使用することを強く推奨します。
     * 
     * @param queries 全クエリリスト
     * @param page ページ番号（0から開始）
     * @param size ページサイズ
     * @return ページネーション適用後のクエリリスト
     */
    private List<JiraJqlQuery> applyPagination(List<JiraJqlQuery> queries, Integer page, Integer size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, queries.size());
        
        if (startIndex >= queries.size()) {
            return List.of(); // 空のリストを返す
        }
        
        return queries.subList(startIndex, endIndex);
    }
}
