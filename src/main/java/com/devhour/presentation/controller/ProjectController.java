package com.devhour.presentation.controller;

import java.util.List;
import java.util.Optional;
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
import com.devhour.application.service.ProjectApplicationService;
import com.devhour.domain.model.entity.Project;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.request.ProjectCreateRequest;
import com.devhour.presentation.dto.request.ProjectUpdateRequest;
import jakarta.validation.Valid;

/**
 * プロジェクト管理REST APIコントローラー
 * 
 * プロジェクトの作成・更新・削除・検索機能を提供
 * 
 * エンドポイント: - POST /api/projects: プロジェクト作成 - PUT /api/projects/{id}: プロジェクト更新 - GET
 * /api/projects/{id}: プロジェクト詳細取得 - GET /api/projects: プロジェクト一覧取得 - GET /api/projects/active:
 * アクティブなプロジェクト一覧 - GET /api/projects/status/{status}: 指定ステータスのプロジェクト一覧 - DELETE /api/projects/{id}:
 * プロジェクト削除
 */
@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

    private final ProjectApplicationService projectApplicationService;

    public ProjectController(ProjectApplicationService projectApplicationService) {
        this.projectApplicationService = projectApplicationService;
    }

    /**
     * プロジェクト作成
     * 
     * @param request プロジェクト作成リクエスト
     * @param userId 実行者のユーザーID（ヘッダーから取得）
     * @return 作成されたプロジェクト情報
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_projects:write')")
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        Project project = projectApplicationService.createProject(
                SecurityUtils.requireCurrentUserId(), 
                request.getName(),
                request.getStartDate(), 
                request.getPlannedEndDate(), 
                request.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /**
     * プロジェクト更新
     * 
     * @param projectId プロジェクトID
     * @param request プロジェクト更新リクエスト
     * @param userId 実行者のユーザーID（ヘッダーから取得）
     * @return 更新されたプロジェクト情報
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("hasAuthority('SCOPE_projects:write')")
    public ResponseEntity<Project> updateProject(@PathVariable String projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        Project project = projectApplicationService.updateProject(
            projectId,
            SecurityUtils.requireCurrentUserId(), 
            request.getName(),
            request.getStartDate(),
            request.getPlannedEndDate(),
            request.getDescription());

        return ResponseEntity.ok(project);
    }

    /**
     * プロジェクト詳細取得
     * 
     * @param projectId プロジェクトID
     * @return プロジェクト詳細情報
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<Project> getProject(@PathVariable String projectId) {
        Optional<Project> project = projectApplicationService.findById(projectId);

        return project.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * プロジェクト一覧取得
     * 
     * @return 全プロジェクト一覧
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectApplicationService.findAllProjects();

        return ResponseEntity.ok(projects);
    }

    /**
     * プロジェクト一覧取得
     * 
     * @return 全プロジェクト一覧
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<List<Project>> searchProjects(
        @Valid @RequestParam("q") String query
    ) {
        List<Project> projects = projectApplicationService.searchProjects(query);

        return ResponseEntity.ok(projects);
    }

    /**
     * 工数記録可能プロジェクト一覧取得（ユーザーアサインメント考慮）
     * 
     * ユーザーがアサインされている工数記録可能なプロジェクトの一覧を取得 認証されたユーザーのみアクセス可能
     * 
     * @param userId ユーザーID（ヘッダーから取得）
     * @return ユーザーがアサインされている工数記録可能なプロジェクト一覧
     */
    @GetMapping("/workrecordable")
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<List<Project>> getWorkRecordableProjectForUsers() {
        List<Project> projects = projectApplicationService
                .findWorkRecordableProjectsForUser(SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(projects);
    }

    /**
     * 工数記録可能プロジェクト一覧取得
     * 
     * 工数記録可能なプロジェクトの一覧を取得(進行中) 認証されたユーザーのみアクセス可能
     * 
     * @return 工数記録可能なプロジェクト一覧
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<List<Project>> getAllWorkRecordableProjects() {
        List<Project> projects = projectApplicationService.findWorkRecordableProjects();

        return ResponseEntity.ok(projects);
    }

    /**
     * 直近工数記録しているプロジェクト一覧取得
     * 
     * ユーザーが直近で工数記録しているプロジェクトの一覧を取得
     * 
     * @return 工数記録したプロジェクト一覧
     */
    @GetMapping("/recent-recorded")
    @PreAuthorize("hasAuthority('SCOPE_projects:read')")
    public ResponseEntity<List<Project>> getRecentRecordedProjects() {
        List<Project> projects = projectApplicationService.findRecentWorkRecordedProjects();

        return ResponseEntity.ok(projects);
    }

    /**
     * プロジェクト削除（論理削除）
     * 
     * @param projectId プロジェクトID
     * @param userId 実行者のユーザーID（ヘッダーから取得）
     * @return 削除結果
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAuthority('SCOPE_projects:write')")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        projectApplicationService.deleteProject(projectId,
                SecurityUtils.requireCurrentUserId());

        return ResponseEntity.noContent().build();
    }
}
