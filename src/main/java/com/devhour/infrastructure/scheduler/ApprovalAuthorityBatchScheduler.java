package com.devhour.infrastructure.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devhour.application.dto.BatchResult;
import com.devhour.application.service.ApprovalAuthorityBatchService;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 承認権限者バッチスケジューラー
 * 
 * 毎日深夜2時に承認権限者のCSVインポート処理を実行するスケジューラーコンポーネント。
 * 分散環境での重複実行防止のためShedLockを使用。
 * 
 * 主な責務:
 * - 定期的な承認権限者バッチ処理の実行（デフォルト: 毎日2時）
 * - ShedLockによる分散環境での重複実行防止
 * - バッチ処理の例外処理とログ記録
 * - 設定による外部CSVファイルパス指定
 * 
 * 設定項目:
 * - app.batch.approval-authority.csv-file-path: CSVファイルパス（デフォルト: employee_master.csv）
 * 
 * アーキテクチャパターン:
 * - Spring Schedulerによる定期実行
 * - ShedLockによる分散ロック
 * - アプリケーションサービスへの委譲パターン
 * - 包括的エラーハンドリング
 */
@Slf4j
@Component
@EnableScheduling
public class ApprovalAuthorityBatchScheduler {
    
    private final ApprovalAuthorityBatchService batchService;
    private final String csvFilePath;
    
    public ApprovalAuthorityBatchScheduler(
        ApprovalAuthorityBatchService batchService,
        @Value("${app.batch.approval-authority.csv-file-path:employee_master.csv}") String csvFilePath
    ) {
        this.batchService = batchService;
        this.csvFilePath = csvFilePath;
    }
    
    /**
     * 定期承認権限者バッチ処理実行
     * 
     * 毎日深夜2時に承認権限者のCSVインポート処理を実行する。
     * ShedLockにより分散環境での重複実行を防止。
     * 
     * 実行フロー:
     * 1. 実行開始ログ出力
     * 2. ApprovalAuthorityBatchService.importApprovalAuthoritiesFromFile()呼び出し
     * 3. バッチ結果の確認とログ出力
     * 4. 例外発生時のエラーハンドリングとログ記録（監視・アラート用に再スロー）
     * 
     * エラー処理:
     * - RuntimeException等: バッチ処理エラーとして記録し例外を再スロー
     * - 例外の再スローにより監視システムでのアラート検知を可能にする
     * 
     * ShedLock設定:
     * - ロック名: "ApprovalAuthorityBatch"
     * - 最大ロック時間: 30分（大量データ処理に対応）
     * - 最小ロック時間: 1分
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(name = "ApprovalAuthorityBatch", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void executeApprovalAuthorityBatch() {
        log.info("Starting approval authority batch processing...");
        
        try {
            // ApprovalAuthorityBatchServiceにバッチ処理を委譲
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFilePath);
            
            // バッチ結果のログ出力
            handleBatchResult(result);
            
        } catch (Exception e) {
            // バッチ処理エラー（監視・アラート用に例外を再スロー）
            log.error("Approval authority batch processing failed", e);
            throw e; // Re-throw for monitoring/alerting
        }
    }
    
    /**
     * 手動実行（テスト・管理用）
     * 
     * 管理者による手動実行やテスト用途での実行に使用。
     * スケジューラーのロック機構を使用せず、直接バッチ処理を実行する。
     * 
     * @return バッチ処理結果
     */
    public BatchResult executeManually() {
        log.info("Manual approval authority batch processing started");
        return batchService.importApprovalAuthoritiesFromFile(csvFilePath);
    }
    
    /**
     * バッチ結果の処理とログ出力
     * 
     * BatchResultの内容に基づいて適切なログメッセージを出力する。
     * 成功時は統計情報、エラーがある場合は警告情報を出力。
     * 
     * @param result バッチ処理結果
     */
    private void handleBatchResult(BatchResult result) {
        if (result == null) {
            log.warn("Batch result is null");
            return;
        }
        
        // 基本統計情報のログ出力
        log.info("Approval authority batch processing completed successfully. " +
                "Processed: {}, Added: {}, Updated: {}, Deleted: {}, " +
                "Approver Relations Added: {}, Approver Relations Deleted: {}, " +
                "Errors: {}",
                result.getProcessed(),
                result.getAdded(), 
                result.getUpdated(),
                result.getDeleted(),
                result.getApproverRelations().getAdded(),
                result.getApproverRelations().getDeleted(),
                result.getErrors().size());
        
        // エラーがある場合は詳細を警告ログで出力
        if (!result.getErrors().isEmpty()) {
            log.warn("Approval authority batch processing completed with {} errors: {}", 
                    result.getErrors().size(), result.getErrors());
        }
    }
}