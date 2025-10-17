package com.devhour.presentation.controller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.JiraSyncApplicationService;
import com.devhour.application.service.JiraSyncHistoryApplicationService;
import com.devhour.config.JiraConfiguration;
import com.devhour.domain.exception.JiraAuthenticationException;
import com.devhour.domain.exception.JiraConnectionException;
import com.devhour.domain.exception.JiraSyncAlreadyRunningException;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.presentation.dto.response.ConnectionTestResponse;
import com.devhour.presentation.dto.response.JiraConnectionResponse;
import com.devhour.presentation.dto.response.JiraSyncHistoryDetailResponse;
import com.devhour.presentation.dto.response.JiraSyncHistoryResponse;
import com.devhour.presentation.dto.response.JiraSyncResponse;
import com.devhour.presentation.dto.response.JiraSyncStatusResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA同期REST APIコントローラー
 * 
 * JIRA統合機能の接続設定、接続テスト、手動同期実行、同期ステータス監視
 * 機能を提供するREST APIエンドポイント
 * 
 * エンドポイント:
 * - GET /api/jira/connection: JIRA接続設定情報取得
 * - POST /api/jira/connection/test: JIRA接続テスト実行
 * - POST /api/jira/sync/manual: 手動同期実行
 * - GET /api/jira/sync/status: 同期ステータス取得
 * - GET /api/jira/sync/history: 同期履歴一覧取得（ページネーション対応）
 * - GET /api/jira/sync/history/{id}: 同期履歴詳細取得
 * 
 * セキュリティ:
 * - 管理者権限: JIRA接続設定・テスト
 * - PMO/管理者権限: 同期管理・監視
 * - 開発者権限: アクセス不可
 * 
 * 要件対応:
 * - REQ-1.1: 管理者がJIRA接続設定画面にアクセス可能
 * - REQ-1.4: 管理者が接続テストボタンで接続確認可能
 * - REQ-4.1: 管理者が手動同期ボタンで即座に同期実行可能
 * - REQ-4.2: 管理者が手動同期実行を確認可能
 * - REQ-6.1: 管理者が過去30日間の同期履歴を参照可能
 * - REQ-6.2: 管理者が特定同期の詳細情報を参照可能
 * - REQ-6.3: 管理者が期間フィルタで同期履歴を絞り込み可能
 */
@RestController
@RequestMapping("/api/jira")
@Validated
@Slf4j
public class JiraSyncController {
    
    private final JiraSyncApplicationService jiraSyncApplicationService;
    private final JiraSyncHistoryApplicationService syncHistoryApplicationService;
    private final JiraConfiguration jiraConfiguration;
    
    public JiraSyncController(
            JiraSyncApplicationService jiraSyncApplicationService,
            JiraSyncHistoryApplicationService syncHistoryApplicationService,
            JiraConfiguration jiraConfiguration) {
        this.jiraSyncApplicationService = jiraSyncApplicationService;
        this.syncHistoryApplicationService = syncHistoryApplicationService;
        this.jiraConfiguration = jiraConfiguration;
    }
    
    /**
     * JIRA接続設定取得
     * 
     * 現在のJIRA接続設定情報を取得します。
     * セキュリティのため、実際の認証情報は返さず、環境変数キー名と設定状態のみを返します。
     * 
     * @return JIRA接続設定レスポンス
     */
    @GetMapping("/connection")
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<JiraConnectionResponse> getConnection() {
        JiraConnectionResponse response = JiraConnectionResponse.of(
            jiraConfiguration.getBaseUrl(),
            jiraConfiguration.getAuth().getTokenEnvKey(),
            jiraConfiguration.getAuth().getUsernameEnvKey(),
            jiraConfiguration.isAuthenticationConfigured()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * JIRA接続テスト実行
     * 
     * 現在の設定でJIRAサーバーへの接続をテストします。
     * 認証情報、ネットワーク接続、JIRA API応答を検証します。
     * 
     * @return 接続テスト結果レスポンス
     */
    @PostMapping("/connection/test")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<ConnectionTestResponse> testConnection() {
        try {
            boolean connectionSuccess = jiraSyncApplicationService.testConnection();
            
            if (connectionSuccess) {
                return ResponseEntity.ok(ConnectionTestResponse.success());
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ConnectionTestResponse.failure("JIRA接続テストが失敗しました"));
            }
            
        } catch (JiraAuthenticationException e) {
            log.error("JIRA接続テスト中に認証エラーが発生: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ConnectionTestResponse.authenticationError(e.getMessage(), e.getStatusCode()));
            
        } catch (JiraConnectionException e) {
            log.error("JIRA接続テスト中に接続エラーが発生: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ConnectionTestResponse.connectionError(e.getMessage()));
            
        } catch (Exception e) {
            log.error("JIRA接続テスト中に予期しないエラーが発生", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ConnectionTestResponse.failure("接続テスト中に予期しないエラーが発生しました"));
        }
    }
    
    /**
     * 手動同期実行
     * 
     * 管理者またはPMOが手動でJIRA同期処理を実行します。
     * 同期が既に実行中の場合は競合エラーを返します。
     * 
     * @return 手動同期実行結果レスポンス
     */
    @PostMapping("/sync/manual")
    @PreAuthorize("hasAuthority('SCOPE_jira:write')")
    public ResponseEntity<JiraSyncResponse> executeManualSync() {
        try {
            JiraSyncHistory syncHistory = jiraSyncApplicationService.executeSync();
            
            log.info("手動同期実行を開始: syncId={}", syncHistory.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(JiraSyncResponse.success(syncHistory));
            
        } catch (JiraSyncAlreadyRunningException e) {
            log.warn("手動同期実行時に競合が発生: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(JiraSyncResponse.conflict(e.getMessage()));
            
        } catch (Exception e) {
            log.error("手動同期実行中に予期しないエラーが発生", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JiraSyncResponse.failure("同期処理中にエラーが発生しました"));
        }
    }
    
    /**
     * 同期ステータス取得
     * 
     * 現在の同期実行状況と最後の同期結果を取得します。
     * 実行中の同期、最後の完了同期の情報を提供します。
     * 
     * @return 同期ステータスレスポンス
     */
    @GetMapping("/sync/status")
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<JiraSyncStatusResponse> getSyncStatus() {
        // 最近の同期ステータスを取得
        JiraSyncHistoryResponse recentSyncStatus = syncHistoryApplicationService.getRecentSyncStatus();
        
        // レスポンス形式に変換
        JiraSyncStatusResponse response = convertToSyncStatusResponse(recentSyncStatus);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 同期履歴取得（ページネーション対応）
     * 
     * 管理者またはPMOが過去の同期履歴を一覧形式で取得します。
     * ページネーションと日付範囲フィルタに対応しており、効率的な履歴検索を提供します。
     * 
     * @param page ページ番号（0から開始、デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20）
     * @param from 検索開始日付（YYYY-MM-DD形式、オプション）
     * @param to 検索終了日付（YYYY-MM-DD形式、オプション）
     * @return ページネーション情報付き同期履歴レスポンス
     */
    @GetMapping("/sync/history")
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<JiraSyncHistoryResponse> getSyncHistory(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        
        // パラメータ検証
        if (page < 0) {
            throw new IllegalArgumentException("ページ番号は0以上である必要があります");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("ページサイズは1以上である必要があります");
        }
        
        // 同期履歴を取得
        JiraSyncHistoryResponse response = syncHistoryApplicationService.getSyncHistory(page, size, from, to, status);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 同期履歴詳細取得
     * 
     * 管理者またはPMOが特定の同期実行の詳細情報を取得します。
     * 処理されたプロジェクト詳細、エラー情報、実行時間等の詳細データを提供します。
     * 
     * @param id 同期履歴ID
     * @return 同期履歴詳細レスポンス
     */
    @GetMapping("/sync/history/{id}")
    @PreAuthorize("hasAuthority('SCOPE_jira:read')")
    public ResponseEntity<JiraSyncHistoryDetailResponse> getSyncHistoryDetails(@PathVariable String id) {
        // UUIDフォーマット検証
        try {
            java.util.UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.error("不正な同期履歴IDフォーマット: id={}", id);
            throw new IllegalArgumentException("同期履歴IDのフォーマットが不正です: " + id);
        }
        
        // 同期履歴詳細を取得
        JiraSyncHistoryDetailResponse response = syncHistoryApplicationService.getSyncHistoryDetails(id);
        
        return ResponseEntity.ok(response);
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * SyncHistoryResponseをSyncStatusResponseに変換
     * 
     * @param syncHistoryResponse 同期履歴レスポンス
     * @return 同期ステータスレスポンス
     */
    private JiraSyncStatusResponse convertToSyncStatusResponse(JiraSyncHistoryResponse syncHistoryResponse) {
        boolean hasInProgress = syncHistoryResponse.isHasInProgress();
        
        if (hasInProgress && !syncHistoryResponse.getSyncHistories().isEmpty()) {
            // 実行中の同期がある場合
            JiraSyncHistoryResponse.SyncHistorySummary runningSyncSummary = syncHistoryResponse.getSyncHistories()
                    .stream()
                    .filter(summary -> summary.getSyncStatus().name().equals("IN_PROGRESS"))
                    .findFirst()
                    .orElse(null);
            
            if (runningSyncSummary != null) {
                // 完了した同期を検索
                JiraSyncHistoryResponse.SyncHistorySummary completedSyncSummary = syncHistoryResponse.getSyncHistories()
                        .stream()
                        .filter(summary -> !summary.getSyncStatus().name().equals("IN_PROGRESS"))
                        .findFirst()
                        .orElse(null);
                
                JiraSyncStatusResponse.LastCompletedSyncInfo lastCompletedInfo = null;
                if (completedSyncSummary != null) {
                    lastCompletedInfo = JiraSyncStatusResponse.LastCompletedSyncInfo.builder()
                            .syncId(completedSyncSummary.getSyncHistoryId())
                            .syncType(completedSyncSummary.getSyncType().name())
                            .completedAt(completedSyncSummary.getCompletedAt())
                            .status(completedSyncSummary.getSyncStatus().name())
                            .processedCount(completedSyncSummary.getTotalProjectsProcessed())
                            .errorMessage(completedSyncSummary.getErrorDetails())
                            .executedBy(completedSyncSummary.getTriggeredBy())
                            .build();
                }
                
                return JiraSyncStatusResponse.builder()
                        .isRunning(true)
                        .currentSyncId(runningSyncSummary.getSyncHistoryId())
                        .syncType(runningSyncSummary.getSyncType().name())
                        .startedAt(runningSyncSummary.getStartedAt())
                        .executedBy(runningSyncSummary.getTriggeredBy())
                        .lastCompletedSync(lastCompletedInfo)
                        .build();
            }
        }
        
        // 実行中の同期がない場合
        JiraSyncStatusResponse.LastCompletedSyncInfo lastCompletedInfo = null;
        
        // 最後の完了同期があれば追加
        if (!syncHistoryResponse.getSyncHistories().isEmpty()) {
            JiraSyncHistoryResponse.SyncHistorySummary lastCompletedSummary = syncHistoryResponse.getSyncHistories()
                    .get(0); // 最初の要素が最新
            
            lastCompletedInfo = JiraSyncStatusResponse.LastCompletedSyncInfo.builder()
                    .syncId(lastCompletedSummary.getSyncHistoryId())
                    .syncType(lastCompletedSummary.getSyncType().name())
                    .completedAt(lastCompletedSummary.getCompletedAt())
                    .status(lastCompletedSummary.getSyncStatus().name())
                    .processedCount(lastCompletedSummary.getTotalProjectsProcessed())
                    .errorMessage(lastCompletedSummary.getErrorDetails())
                    .executedBy(lastCompletedSummary.getTriggeredBy())
                    .build();
        }
        
        return JiraSyncStatusResponse.builder()
                .isRunning(false)
                .lastCompletedSync(lastCompletedInfo)
                .build();
    }
}
