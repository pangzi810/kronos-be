package com.devhour.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.WorkRecordApplicationService;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.infrastructure.security.SecurityUtils;
import com.devhour.presentation.dto.request.WorkRecordSaveRequest;
import com.devhour.presentation.dto.response.DateStatusResponse;
import com.devhour.presentation.dto.response.MissingDatesResponse;
import com.devhour.presentation.dto.response.WorkHoursSummaryResponse;
import com.devhour.presentation.dto.response.WorkRecordsResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 工数記録管理REST APIコントローラー
 * 
 * 工数記録の作成・更新・削除・検索機能を提供
 * 
 * エンドポイント: - POST /api/work-records: 工数記録作成 - PUT /api/work-records/{id}: 工数記録更新 - GET
 * /api/work-records/{id}: 工数記録詳細取得 - GET /api/work-records: 工数記録検索 - GET
 * /api/work-records/user/{userId}: ユーザーの工数記録一覧 - GET /api/work-records/project/{projectId}:
 * プロジェクトの工数記録一覧 - GET /api/work-records/missing-dates: 未入力日取得 - DELETE /api/work-records/{id}:
 * 工数記録削除
 */
@RestController
@RequestMapping("/api/work-records")
@Validated
public class WorkRecordController {

    private final WorkRecordApplicationService workRecordApplicationService;

    public WorkRecordController(WorkRecordApplicationService workRecordApplicationService) {
        this.workRecordApplicationService = workRecordApplicationService;
    }

    /**
     * 工数記録作成または更新（アップサート）
     * 
     * 同一ユーザー・同一日付の記録が存在する場合は更新、存在しない場合は新規作成
     * 
     * @param request 工数記録作成リクエスト
     * @param userId 実行者のユーザーID（ヘッダーから取得）
     * @return 作成または更新された工数記録情報
     */
    @PutMapping("/me/{date}")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:write')")
    public ResponseEntity<WorkRecordsResponse> saveWorkRecord(@Valid @PathVariable LocalDate date,
            @Valid @RequestBody WorkRecordSaveRequest request) {

        WorkRecordsResponse response = workRecordApplicationService.saveWorkRecords(
                SecurityUtils.requireCurrentUserId(), date, request);

        // 新規作成でも更新でも200 OKを返す（upsert操作として統一）
        return ResponseEntity.ok(response);
    }

    /**
     * 単一日付の工数記録と承認ステータス取得
     * 
     * @param userId ユーザーID
     * @param date 対象日付
     * @param userDetails 認証済みユーザー情報
     * @return 工数記録と承認ステータスを含むレスポンス
     */
    @GetMapping("/me/{date}")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:read')")
    public ResponseEntity<WorkRecordsResponse> getWorkRecordsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        WorkRecordsResponse response =
                workRecordApplicationService.getWorkRecordsWithApprovalStatus(
                    SecurityUtils.requireCurrentUserId(), date);

        return ResponseEntity.ok(response);
    }

    /**
     * 期間指定で工数記録検索
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 指定期間の工数記録一覧
     */
    @GetMapping("/me/period")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:read')")
    public ResponseEntity<List<WorkRecord>> getWorkRecordsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                
        List<WorkRecord> workRecords =
                workRecordApplicationService.findByUserIdAndDateRange(
                    SecurityUtils.requireCurrentUserId(), startDate, endDate);

        return ResponseEntity.ok(workRecords);
    }

    /**
     * 開発者の工数集計レポート取得
     * 
     * 指定期間の開発者の工数を日別・プロジェクト別・カテゴリ別で集計 開発者ダッシュボードで使用
     * 
     * @param userId 開発者のユーザーID
     * @param startDate 集計開始日
     * @param endDate 集計終了日
     * @return 工数集計レポート
     */
    @GetMapping("/me/summary")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:read')")
    public ResponseEntity<WorkHoursSummaryResponse> getWorkHoursSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        WorkHoursSummaryResponse summary =
                workRecordApplicationService.generateWorkHoursSummary(
                    SecurityUtils.requireCurrentUserId(), startDate, endDate);

        return ResponseEntity.ok(summary);
    }

    /**
     * 指定された月の未入力日を取得
     * 
     * @param year 年
     * @param month 月（1-12）
     * @param userDetails 認証済みユーザー情報
     * @return 未入力日のリスト
     */
    @GetMapping("/me/missing-dates")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:read')")
    public ResponseEntity<MissingDatesResponse> getMissingDates(
            @RequestParam @Min(2000) @Max(3000) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month) {

        List<LocalDate> missingDates =
                workRecordApplicationService.getMissingDatesForMonth(
                    SecurityUtils.requireCurrentUserId(), year, month);

        return ResponseEntity.ok(MissingDatesResponse.of(missingDates));
    }

    /**
     * 指定された月の各日付のステータス情報を取得
     * 
     * @param year 年
     * @param month 月（1-12）
     * @param userDetails 認証済みユーザー情報
     * @return 日付毎のステータス情報（工数記録の有無、承認状況、総工数）
     */
    @GetMapping("/me/date-statuses")
    @PreAuthorize("hasAuthority('SCOPE_work-hours:read')")
    public ResponseEntity<DateStatusResponse> getDateStatuses(
            @RequestParam @Min(2000) @Max(3000) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month) {

        Map<LocalDate, DateStatusResponse.DateStatus> statuses =
                workRecordApplicationService.getDateStatusesForMonth(
                    SecurityUtils.requireCurrentUserId(), year, month);

        return ResponseEntity.ok(DateStatusResponse.of(statuses));
    }

}
