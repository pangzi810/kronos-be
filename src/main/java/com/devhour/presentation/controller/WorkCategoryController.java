package com.devhour.presentation.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.WorkCategoryApplicationService;
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.request.WorkCategoryCreateRequest;
import com.devhour.presentation.dto.request.WorkCategoryUpdateRequest;
import jakarta.validation.Valid;

/**
 * 作業カテゴリ管理REST APIコントローラー
 * 
 * 作業カテゴリの作成・更新・削除・検索機能を提供
 * ControllerLoggingAspectによる自動ログ出力対応
 * 
 * エンドポイント: 
 * - POST /api/work-categories: 作業カテゴリ作成 
 * - PUT /api/work-categories/{id}: 作業カテゴリ更新 
 * - GET /api/work-categories/{id}: 作業カテゴリ詳細取得 
 * - GET /api/work-categories: 作業カテゴリ一覧取得 
 * - GET /api/work-categories/active: アクティブな作業カテゴリ一覧 
 * - PATCH /api/work-categories/{id}/activate: 作業カテゴリ有効化
 * - PATCH /api/work-categories/{id}/deactivate: 作業カテゴリ無効化
 */
@RestController
@RequestMapping("/api/work-categories")
@Validated
public class WorkCategoryController {

    private final WorkCategoryApplicationService workCategoryApplicationService;

    public WorkCategoryController(WorkCategoryApplicationService workCategoryApplicationService) {
        this.workCategoryApplicationService = workCategoryApplicationService;
    }

    /**
     * 作業カテゴリ作成
     * 
     * @param request 作業カテゴリ作成リクエスト
     * @return 作成された作業カテゴリ情報
     */
    @PostMapping
    public ResponseEntity<WorkCategory> createWorkCategory(
            @Valid @RequestBody WorkCategoryCreateRequest request) {
        
        WorkCategory workCategory = workCategoryApplicationService.createWorkCategory(
                CategoryCode.of(request.getCode()),
                CategoryName.of(request.getName()),
                request.getDescription(), request.getColorCode(),
                SecurityUtils.requireCurrentUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(workCategory);
    }

    /**
     * 作業カテゴリ更新
     * 
     * @param categoryId 作業カテゴリID
     * @param request 作業カテゴリ更新リクエスト
     * @return 更新された作業カテゴリ情報
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<WorkCategory> updateWorkCategory(@PathVariable String categoryId,
            @Valid @RequestBody WorkCategoryUpdateRequest request) {
        
        WorkCategory workCategory = workCategoryApplicationService.updateWorkCategory(
                categoryId,
                CategoryName.of(request.getName()),
                request.getDescription(),
                request.getColorCode(),
                SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(workCategory);
    }

    /**
     * 作業カテゴリの有効化
     * 
     * @param categoryId 作業カテゴリID
     * @return 有効化された作業カテゴリ情報
     */
    @PatchMapping("/{categoryId}/activate")
    public ResponseEntity<WorkCategory> activateWorkCategory(@PathVariable String categoryId) {
        WorkCategory workCategory = workCategoryApplicationService.activateWorkCategory(categoryId,
                SecurityUtils.requireCurrentUserId());
        return ResponseEntity.ok(workCategory);
    }

    /**
     * 作業カテゴリの無効化
     * 
     * @param categoryId 作業カテゴリID
     * @return 無効化された作業カテゴリ情報
     */
    @PatchMapping("/{categoryId}/deactivate")
    public ResponseEntity<WorkCategory> deactivateWorkCategory(@PathVariable String categoryId) {
        WorkCategory workCategory = workCategoryApplicationService
                .deactivateWorkCategory(categoryId, SecurityUtils.requireCurrentUserId());
        return ResponseEntity.ok(workCategory);
    }

    /**
     * 作業カテゴリ詳細取得
     * 
     * @param categoryId 作業カテゴリID
     * @return 作業カテゴリ詳細情報
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<WorkCategory> getWorkCategory(@PathVariable String categoryId) {
        Optional<WorkCategory> workCategory = workCategoryApplicationService.findById(categoryId);
        return workCategory.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 全作業カテゴリ一覧取得
     * 
     * @return 全作業カテゴリ一覧
     */
    @GetMapping
    public ResponseEntity<List<WorkCategory>> getAllWorkCategories() {
        List<WorkCategory> workCategories = workCategoryApplicationService.findAllWorkCategories();
        return ResponseEntity.ok(workCategories);
    }

    /**
     * アクティブな作業カテゴリ一覧取得
     * 
     * @return アクティブな作業カテゴリ一覧
     */
    @GetMapping("/active")
    public ResponseEntity<List<WorkCategory>> getActiveWorkCategories() {
        List<WorkCategory> workCategories = workCategoryApplicationService.findActiveWorkCategories();
        return ResponseEntity.ok(workCategories);
    }

}
