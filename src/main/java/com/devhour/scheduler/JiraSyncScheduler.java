package com.devhour.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.devhour.application.service.JiraSyncApplicationService;
import com.devhour.domain.exception.JiraAuthenticationException;
import com.devhour.domain.exception.JiraSyncException;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * JIRA同期スケジューラー
 * 
 * 毎時0分にJIRA同期処理を実行するスケジューラーコンポーネント。
 * 分散環境での重複実行防止のためShedLockを使用。
 * 
 * 主な責務:
 * - 定期的なJIRA同期処理の実行（デフォルト: 毎時0分）
 * - ShedLockによる分散環境での重複実行防止
 * - 同期処理の例外処理とログ記録
 * - 設定による有効/無効制御
 * 
 * 設定項目:
 * - jira.integration.enabled: JIRA統合機能有効/無効（デフォルト: false）
 * - jira.sync.scheduler.cron: 実行スケジュール（デフォルト: 0 0 * * * *）
 * - jira.sync.scheduler.lock-at-most-for: 最大ロック時間（デフォルト: PT10M）
 * - jira.sync.scheduler.lock-at-least-for: 最小ロック時間（デフォルト: PT1M）
 *
 * アーキテクチャパターン:
 * - Spring Schedulerによる定期実行
 * - ShedLockによる分散ロック
 * - アプリケーションサービスへの委譲パターン
 * - 包括的エラーハンドリング
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "jira.integration.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class JiraSyncScheduler {
    
    private final JiraSyncApplicationService jiraSyncApplicationService;
    
    public JiraSyncScheduler(JiraSyncApplicationService jiraSyncApplicationService) {
        this.jiraSyncApplicationService = jiraSyncApplicationService;
    }
    
    /**
     * 定期JIRA同期処理実行
     * 
     * 毎時0分にJIRA同期処理を実行する。
     * ShedLockにより分散環境での重複実行を防止。
     * 
     * 実行フロー:
     * 1. 実行開始ログ出力
     * 2. JiraSyncApplicationService.executeSync()呼び出し
     * 3. 同期結果の確認とログ出力
     * 4. 例外発生時のエラーハンドリングとログ記録
     * 
     * エラー処理:
     * - JiraAuthenticationException: 認証エラーとして記録
     * - JiraSyncException: 同期エラーとして記録
     * - その他の例外: 予期しないエラーとして記録
     * - スケジューラー自体は停止せずに継続実行
     * 
     * ShedLock設定:
     * - ロック名: "JiraSyncScheduler.executeSync"
     * - 最大ロック時間: application.propertiesで設定可能（デフォルト10分）
     * - 最小ロック時間: application.propertiesで設定可能（デフォルト1分）
     */
    @Scheduled(cron = "${jira.sync.scheduler.cron}")
    @SchedulerLock(
        name = "JiraSyncScheduler.executeSync",
        lockAtMostFor = "${jira.sync.scheduler.lock-at-most-for}",
        lockAtLeastFor = "${jira.sync.scheduler.lock-at-least-for}"
    )
    public void executeSync() {
        log.info("JIRA同期スケジューラー実行開始");
        
        try {
            // JiraSyncApplicationServiceに同期処理を委譲
            JiraSyncHistory syncHistory = jiraSyncApplicationService.executeSync();
            
            // 同期結果のログ出力
            handleSyncResult(syncHistory);
            
        } catch (JiraAuthenticationException e) {
            // REQ-8.4: 認証エラーの特別処理
            log.error("JIRA認証エラーが発生: {} (ステータスコード: {})", 
                     e.getMessage(), e.getStatusCode(), e);
            
        } catch (JiraSyncException e) {
            // 同期処理固有エラー
            log.error("JIRA同期処理でエラーが発生: {}", e.getMessage(), e);
            
        } catch (Exception e) {
            // 予期しないエラー（スケジューラー継続のため例外は再スローしない）
            log.error("JIRA同期スケジューラーで予期しないエラーが発生: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 同期結果の処理とログ出力
     * 
     * SyncHistoryの内容に基づいて適切なログメッセージを出力する。
     * 
     * @param syncHistory 同期履歴エンティティ
     */
    private void handleSyncResult(JiraSyncHistory syncHistory) {
        if (syncHistory == null) {
            log.warn("同期履歴がnullです");
            return;
        }
        
        JiraSyncStatus status = syncHistory.getSyncStatus();
        Integer processedCount = syncHistory.getTotalProjectsProcessed();
        int count = processedCount != null ? processedCount : 0;
        
        switch (status) {
            case COMPLETED:
                log.info("JIRA同期スケジューラー実行完了: {} 件のプロジェクトを処理 (同期ステータス: {})", 
                        count, status);
                break;
                
            case FAILED:
                String errorMessage = syncHistory.getErrorDetails() != null 
                    ? syncHistory.getErrorDetails() 
                    : "詳細不明";
                log.error("同期処理が失敗で完了: {} 件のプロジェクトを処理 (同期ステータス: {}, エラー: {})", 
                         count, status, errorMessage);
                break;
                
            case IN_PROGRESS:
                log.info("同期処理が進行中で完了: {} 件のプロジェクトを処理 (同期ステータス: {})", 
                        count, status);
                break;
                
            default:
                log.warn("未知の同期ステータス: {} (処理件数: {})", status, count);
                break;
        }
    }
}