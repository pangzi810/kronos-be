package com.devhour.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.DailyApprovalApplicationService;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.request.BatchApprovalRequest;
import com.devhour.presentation.dto.request.BatchRejectRequest;
import com.devhour.presentation.dto.request.DailyApprovalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 工数記録承認管理コントローラー
 * 
 * 工数記録の承認プロセスを管理するAPIエンドポイントを提供
 * 日次単位での承認ワークフローを実装
 * 
 * 主な機能:
 * - 承認待ち工数記録の集計表示
 * - 申請者/日付単位での一括承認
 * - 申請者/日付単位での一括差し戻し（コメント付き）
 * - 複数の申請者/日付の一括処理
 * 
 * 承認フロー:
 * 1. 承認者が承認待ち一覧を取得
 * 2. 申請者と日付の組み合わせで承認/差し戻しを実行
 * 3. 承認結果がレスポンスとして返却
 * 
 * 特徴:
 * - DailyApprovalApplicationServiceに処理を委譲
 * - 承認者権限チェック（SCOPE_work-hours:approve:team）
 * - JWT認証によるアクセス制御
 */
@RestController
@RequestMapping("/api/approvals")
@Tag(name = "Approval", description = "工数記録承認管理API")
public class WorkRecordApprovalController {

    private final DailyApprovalApplicationService dailyApprovalApplicationService;

    public WorkRecordApprovalController(DailyApprovalApplicationService dailyApprovalApplicationService) {
        this.dailyApprovalApplicationService = dailyApprovalApplicationService;
    }

    /**
     * 承認待ち工数記録一覧を取得（集計済み）
     * 
     * ログインユーザーが承認権限を持つ申請者の承認待ち工数記録を、
     * 申請者と日付の組み合わせで集計した一覧を取得。
     * 
     * 集計情報:
     * - 申請者ごとの日別サマリー
     * - 合計工数、プロジェクト数などの集計情報
     * - 承認可能な状態の記録のみ返却
     * 
     * @return 集計済み承認待ち一覧
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:approve')")
    @Operation(summary = "承認待ち工数記録一覧取得", description = "ログインユーザーが承認可能な承認待ち工数記録の集計済み一覧を取得します")
    public ResponseEntity<List<DailyApprovalApplicationService.AggregatedApproval>> getPendingApprovals() {

        List<DailyApprovalApplicationService.AggregatedApproval> aggregatedApprovals =
                dailyApprovalApplicationService.getPendingAggregatedApprovals(
                        SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(aggregatedApprovals);
    }

    /**
     * 申請者/日付単位で承認（日次承認）
     * 
     * 指定した申請者の特定日における全ての工数記録を一括で承認。
     * 日次承認フローの核となる機能。
     * 
     * 処理内容:
     * - 承認権限の確認（申請者と日付でチェック）
     * - 対象日の全工数記録を承認状態に更新
     * - 承認履歴の記録
     * - 処理結果の返却（成功/失敗件数など）
     * 
     * @param request 承認リクエスト（ユーザーID、工数日）
     * @return 承認処理結果
     */
    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:approve')")
    @Operation(summary = "日次承認", description = "申請者の特定日の全案件を一括承認します")
    public ResponseEntity<DailyApprovalApplicationService.ApprovalResult> approveDaily(
            @Valid @RequestBody DailyApprovalRequest request) {

        DailyApprovalApplicationService.ApprovalResult result =
                dailyApprovalApplicationService.approveDaily(
                    request.getUserId(),
                    request.getWorkDate(),
                     SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(result);
    }

    /**
     * 申請者/日付単位で差し戻し（日次差し戻し）
     * 
     * 指定した申請者の特定日における全ての工数記録を一括で差し戻し。
     * 差し戻し理由のコメントを含めて処理。
     * 
     * 処理内容:
     * - 承認権限の確認（申請者と日付でチェック）
     * - 対象日の全工数記録を差し戻し状態に更新
     * - 差し戻し理由を履歴として記録
     * - 申請者への通知処理（必要に応じて）
     * 
     * @param request 差し戻しリクエスト（ユーザーID、工数日、理由）
     * @return 差し戻し処理結果
     */
    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:approve')")
    @Operation(summary = "日次差し戻し", description = "申請者の特定日の全案件を一括差し戻します")
    public ResponseEntity<DailyApprovalApplicationService.ApprovalResult> rejectDaily(
            @Valid @RequestBody DailyApprovalRequest request) {

        DailyApprovalApplicationService.ApprovalResult result =
                dailyApprovalApplicationService.rejectDaily(
                    request.getUserId(),
                    request.getWorkDate(),
                    SecurityUtils.requireCurrentUserId(),
                    request.getRejectionReason());

        return ResponseEntity.ok(result);
    }

    /**
     * 複数の申請者/日付を一括承認（バッチ承認）
     * 
     * 複数の申請者と日付の組み合わせをまとめて一括承認。
     * 効率的な承認作業を実現するバッチ処理機能。
     * 
     * 処理内容:
     * - 各申請者/日付の組み合わせで個別に承認権限をチェック
     * - 成功したものは承認、失敗したものはスキップ
     * - 各処理の結果を個別に返却（エラー情報含む）
     * - 部分的な失敗があっても全体をロールバックしない
     * 
     * @param request 一括承認リクエスト（複数の申請者/日付の組み合わせ）
     * @return 各処理の結果リスト
     */
    @PostMapping("/approve-batch")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:approve')")
    @Operation(summary = "一括承認（日次）", description = "複数の申請者/日付の組み合わせを一括承認します")
    public ResponseEntity<List<DailyApprovalApplicationService.ApprovalResult>> approveBatch(
            @Valid @RequestBody BatchApprovalRequest request) {

        List<DailyApprovalApplicationService.ApprovalResult> results =
                dailyApprovalApplicationService.approveBatch(
                    request.getRequests(),
                    SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(results);
    }

     /**
     * 複数の申請者/日付を一括承認（バッチ承認）
     * 
     * 複数の申請者と日付の組み合わせをまとめて一括承認。
     * 効率的な承認作業を実現するバッチ処理機能。
     * 
     * 処理内容:
     * - 各申請者/日付の組み合わせで個別に承認権限をチェック
     * - 成功したものは承認、失敗したものはスキップ
     * - 各処理の結果を個別に返却（エラー情報含む）
     * - 部分的な失敗があっても全体をロールバックしない
     * 
     * @param request 一括承認リクエスト（複数の申請者/日付の組み合わせ）
     * @return 各処理の結果リスト
     */
    @PostMapping("/reject-batch")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:approve')")
    @Operation(summary = "一括否決（日次）", description = "複数の申請者/日付の組み合わせを一括否決します")
    public ResponseEntity<List<DailyApprovalApplicationService.ApprovalResult>> rejectBatch(
            @Valid @RequestBody BatchRejectRequest request) {

        List<DailyApprovalApplicationService.ApprovalResult> results =
                dailyApprovalApplicationService.rejectBatch(
                    request.getReason(),
                    request.getRequests(),
                    SecurityUtils.requireCurrentUserId());

        return ResponseEntity.ok(results);
    }
}
