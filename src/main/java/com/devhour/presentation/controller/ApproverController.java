package com.devhour.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.ApproverApplicationService;
import com.devhour.domain.model.entity.User;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 承認者管理コントローラー
 * 
 * 承認者関係の参照APIエンドポイントを提供
 * 
 * 主な機能:
 * - 承認者一覧取得（ログインユーザーの承認者）
 * - 承認対象者一覧取得（ログインユーザーが承認できるユーザー）
 * 
 * 特徴:
 * - 読み取り専用操作のみ提供
 * - ApproverApplicationServiceに処理を委譲
 * - JWT認証によるアクセス制御
 */
@RestController
@RequestMapping("/api/approver")
@Tag(name = "Approver", description = "承認者管理API")
public class ApproverController {

    private final ApproverApplicationService approverApplicationService;

    public ApproverController(ApproverApplicationService approverApplicationService) {
        this.approverApplicationService = approverApplicationService;
    }

    /**
     * 承認者一覧取得
     */
    @GetMapping("/approvers")
    @PreAuthorize("hasAuthority('SCOPE_approver:read')")
    @Operation(summary = "承認者一覧取得", description = "ログインユーザーの承認者一覧を取得します")
    public ResponseEntity<List<UserResponse>> getApprovers() {
        
        String currentUserId = SecurityUtils.requireCurrentUserId();
        
        List<User> approvers = approverApplicationService.getApprovers(currentUserId);
        
        List<UserResponse> responses = approvers.stream()
            .map(UserResponse::from)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }

    /**
     * 承認対象者一覧を取得
     */
    @GetMapping("/targets")
    @PreAuthorize("hasAuthority('SCOPE_approver:read')")
    @Operation(summary = "承認対象者一覧取得", description = "ログインユーザーの承認対象者一覧を取得します")
    public ResponseEntity<List<UserResponse>> getApprovalTargets() {

        String currentUserId = SecurityUtils.requireCurrentUserId();

        List<User> approvalTargets = approverApplicationService.getApprovalTargets(currentUserId);

        List<UserResponse> responses =
                approvalTargets.stream().map(UserResponse::from).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}