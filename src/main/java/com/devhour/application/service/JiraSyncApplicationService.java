package com.devhour.application.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.application.service.JsonTransformService.JsonTransformException;
import com.devhour.config.RetryConfiguration;
import com.devhour.domain.exception.JiraAuthenticationException;
import com.devhour.domain.exception.JiraRateLimitException;
import com.devhour.domain.exception.JiraSyncException;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.domain.repository.JiraSyncHistoryRepository;
import com.devhour.domain.service.JiraSyncDomainService;
import com.devhour.infrastructure.jira.JiraClient;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA同期アプリケーションサービス
 * 
 * JIRA統合におけるユースケースを実装し、JQLクエリの実行、
 * プロジェクト同期、同期履歴管理を担当するアプリケーションサービス
 * 
 * 責務:
 * - 全アクティブJQLクエリの一括実行 (REQ-3.1, REQ-3.2)
 * - 個別JQLクエリの実行 (REQ-4.2)
 * - JIRA接続状態テスト
 * - 同期処理のオーケストレーション
 * - エラーハンドリングと同期履歴記録
 * - トランザクション境界の管理
 * - リトライ機能によるレジリエンス向上 (REQ-8.1, REQ-8.2, REQ-8.3)
 * 
 * アーキテクチャパターン:
 * - ドメインサービスとインフラストラクチャサービスの協調
 * - エラー境界での適切な例外処理
 * - 同期履歴による監査証跡の確保
 * - トランザクション管理による整合性保証
 * - Spring Retryによる回復力のある外部API通信
 */
@Service
@Transactional
@Slf4j
@ConditionalOnProperty(name = "jira.integration.enabled", havingValue = "true", matchIfMissing = false)
public class JiraSyncApplicationService {
    
    private final JiraJqlQueryRepository jqlQueryRepository;
    private final JiraResponseTemplateRepository responseTemplateRepository;
    private final JiraSyncHistoryRepository syncHistoryRepository;
    private final JiraClient jiraClient;
    private final JsonTransformService jsonTransformService;
    private final JiraSyncDomainService jiraSyncDomainService;
    private final ObjectMapper objectMapper;
    private final RetryTemplate jiraSyncRetryTemplate;
    private final AdminNotificationService adminNotificationService;
    
    // Batch processing configuration (Task 5.2.1)
    @Value("${jira.sync.batch-size:100}")
    private int batchSize;
    
    @Value("${jira.sync.memory-efficient-processing:true}")
    private boolean memoryEfficientProcessing;
    
    @Value("${jira.sync.progress-logging.enabled:true}")
    private boolean progressLoggingEnabled;
    
    @Value("${jira.sync.progress-logging.interval:10}")
    private int progressLoggingInterval;
    
    @Value("${jira.sync.streaming.chunk-size:50}")
    private int streamingChunkSize;
    
    @Value("${jira.sync.performance-monitoring.enabled:true}")
    private boolean performanceMonitoringEnabled;
    
    public JiraSyncApplicationService(
            JiraJqlQueryRepository jqlQueryRepository,
            JiraResponseTemplateRepository responseTemplateRepository,
            JiraSyncHistoryRepository syncHistoryRepository,
            JiraClient jiraClient,
            JsonTransformService jsonTransformService,
            JiraSyncDomainService jiraSyncDomainService,
            ObjectMapper objectMapper,
            @Qualifier("jiraSyncRetryTemplate") RetryTemplate jiraSyncRetryTemplate,
            AdminNotificationService adminNotificationService) {
        this.jqlQueryRepository = jqlQueryRepository;
        this.responseTemplateRepository = responseTemplateRepository;
        this.syncHistoryRepository = syncHistoryRepository;
        this.jiraClient = jiraClient;
        this.jsonTransformService = jsonTransformService;
        this.jiraSyncDomainService = jiraSyncDomainService;
        this.objectMapper = objectMapper;
        this.jiraSyncRetryTemplate = jiraSyncRetryTemplate;
        this.adminNotificationService = adminNotificationService;
    }
    
    /**
     * 全アクティブJQLクエリの同期実行 (REQ-3.1, REQ-3.2)
     * 
     * スケジュールされた同期処理として、すべてのアクティブなJQLクエリを
     * 優先度順に実行し、JIRA統合プロジェクトの同期を行う。
     * 
     * 処理フロー:
     * 1. 同期履歴作成・開始
     * 2. アクティブJQLクエリ取得（優先度順）
     * 3. 各クエリの順次実行
     * 4. 同期結果の集約・履歴更新
     * 5. トランザクション確定
     * 
     * @return 実行結果を含む同期履歴エンティティ
     */
    public JiraSyncHistory executeSync() {
        log.info("JIRA全同期処理を開始");
        
        // 同期履歴の作成と開始
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");
        syncHistory = syncHistoryRepository.save(syncHistory);
        
        try {
            // アクティブJQLクエリを優先度順で取得
            List<JiraJqlQuery> activeQueries = jqlQueryRepository.findActiveQueriesOrderByPriority();
            
            log.info("アクティブJQLクエリ {} 件を取得", activeQueries.size());
            
            if (activeQueries.isEmpty()) {
                log.info("実行対象のアクティブJQLクエリが存在しません");

                syncHistory.addDetail("Fetch Active JQL Queries", DetailStatus.SUCCESS, "No active JQL queries found");
                syncHistory.completeSync();
                return syncHistoryRepository.save(syncHistory);
            }
            
            syncHistory.addDetail("Fetch Active JQL Queries", DetailStatus.SUCCESS, String.format("Found %d active JQL queries", activeQueries.size()));
            
            // 各JQLクエリを順次実行
            for (JiraJqlQuery query : activeQueries) {
                try {
                    log.info("JQLクエリを実行中: {} (優先度: {})", query.getQueryName(), query.getPriority());
                    executeJqlQuery(query, syncHistory);

                    syncHistory.addDetail("Completed JQL Query", DetailStatus.SUCCESS, "Success: " + query.getQueryName());
                } catch (Exception e) {
                    log.error("JQLクエリ実行中にエラーが発生: {} - {}", query.getQueryName(), e.getMessage(), e);
        
                    syncHistory.addDetail("Execute JQL Query", DetailStatus.ERROR, String.format("JQLクエリ実行エラー [%s]: %s", query.getQueryName(), e.getMessage()));
                }
            }
            
            // 同期履歴の完了処理
            if (syncHistory.getDetails().stream().anyMatch(detail -> detail.getStatus() == DetailStatus.ERROR)) {
                syncHistory.failSync("一部のクエリ実行でエラーが発生");
                log.warn("同期処理が部分的にエラーで完了: {} 件の詳細", syncHistory.getDetails().size());
            } else {
                syncHistory.completeSync();
                log.info("同期処理が正常に完了: {} 件の詳細", syncHistory.getDetails().size());
            }
            
            return syncHistoryRepository.save(syncHistory);
            
        } catch (Exception e) {
            log.error("JIRA全同期処理中に予期しないエラーが発生", e);
            syncHistory.failSync("予期しないエラー: " + e.getMessage());
            return syncHistoryRepository.save(syncHistory);
        }
    }
    
    /**
     * JIRA接続テスト
     * 
     * JIRA APIへの接続状態を確認し、設定が正しく動作するかテストする。
     * 管理画面での接続確認機能に使用される。
     * 
     * @return 接続可能な場合true、不可能またはエラーの場合false
     */
    @Transactional(readOnly = true)
    public boolean testConnection() {
        log.info("JIRA接続テストを開始");
        
        try {
            boolean connectionResult = jiraClient.testConnection();
            log.info("JIRA接続テスト結果: {}", connectionResult ? "成功" : "失敗");
            return connectionResult;
            
        } catch (Exception e) {
            log.warn("JIRA接続テスト中に例外が発生: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * 単一JQLクエリの実行処理（バッチ処理最適化版 - Task 5.2.1）
     * 
     * JQLクエリを実行し、取得したJIRAイシューをバッチサイズごとに分割して
     * メモリ効率的に処理し、プロジェクトエンティティとして保存する。
     * 
     * バッチ処理最適化機能:
     * - 大量データの分割処理によるメモリ効率化
     * - 進捗状況の定期的なログ出力
     * - パフォーマンス監視機能
     * - ストリーミング処理による安定性向上
     * 
     * REQ-8.1, REQ-8.2, REQ-8.3に対応:
     * - タイムアウトエラーの自動リトライ（30秒間隔、最大3回）
     * - ネットワークエラーのエクスポネンシャルバックオフ
     * - レート制限エラーの適応的リトライ
     * 
     * @param query 実行するJQLクエリエンティティ
     * @param syncHistory 同期履歴エンティティ
     * @return 処理結果の同期履歴詳細リスト
     */
    private void executeJqlQuery(JiraJqlQuery query, JiraSyncHistory syncHistory) {
        JiraIssueSearchResponse response = null;
        do{
            try {
                long startTime = performanceMonitoringEnabled ? System.currentTimeMillis() : 0;
                // リトライ可能なJQLクエリ実行
                response = executeJqlQueryWithRetry(query, response != null ? response.getStartAt() : null);
                
                int totalIssues = response.getIssues().size();
                log.info("JQLクエリ実行結果: {} 件のイシューを取得 (クエリ: {})", totalIssues, query.getQueryName());
                
                syncHistory.addDetail("Execute JQL Query", DetailStatus.SUCCESS, 
                                    String.format("JQL Execution Succeeded [%s]: %d issues found\n%s", query.getQueryName(), totalIssues, query.getJqlExpression()));

                if (response.getIssues().isEmpty()) {
                    log.info("JQLクエリ結果が空です: {}", query.getQueryName());
                    return ;
                }
                
                // レスポンステンプレート取得
                JiraResponseTemplate template = responseTemplateRepository.findById(query.getTemplateId())
                    .orElseThrow(() -> new IllegalStateException("レスポンステンプレートが見つかりません: " + query.getTemplateId()));
                
                // バッチ処理最適化: メモリ効率的な処理
                if (memoryEfficientProcessing && totalIssues > batchSize) {
                    processBatchedIssues(response.getIssues(), template, syncHistory, query);
                } else {
                    // 少量データは従来通り一括処理
                    processIssuesTraditional(response.getIssues(), template, syncHistory);
                }
                
                // パフォーマンス監視ログ
                if (performanceMonitoringEnabled) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    double itemsPerSecond = totalIssues > 0 ? (totalIssues * 1000.0) / executionTime : 0;
                    log.info("JQLクエリ処理完了 - 処理時間: {} ms, 処理速度: {:.2f} 件/秒 (クエリ: {})", 
                            executionTime, itemsPerSecond, query.getQueryName());
                }
            } catch (JiraAuthenticationException e) {
                log.error("JQLクエリ実行中に認証エラーが発生: {} - {}", query.getQueryName(), e.getMessage(), e);
                
                adminNotificationService.notifyAuthenticationError(
                    e.getMessage(), e.getStatusCode(), query.getQueryName());
                
                throw new JiraSyncException("認証エラー: " + e.getMessage(), e);
                
            } catch (JiraRateLimitException e) {
                log.error("JQLクエリ実行中にレート制限エラーが発生: {} - {}", query.getQueryName(), e.getMessage(), e);
                throw new JiraSyncException("レート制限エラー: " + e.getMessage(), e);
                
            } catch (JiraClient.JiraClientException e) {
                log.error("JQLクエリ実行中にJIRAクライアントエラーが発生: {} - {}", query.getQueryName(), e.getMessage(), e);
                throw new JiraSyncException("JQLクエリ実行エラー: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("JQLクエリ実行中に予期しないエラーが発生: {} - {}", query.getQueryName(), e.getMessage(), e);
                throw new JiraSyncException("JQLクエリ実行中のエラー: " + e.getMessage(), e);
            }
        } while (response.getStartAt() + batchSize < response.getTotal());
    }
    
    /**
     * 個別JIRAイシューの処理
     * 
     * JIRAイシューを共通フォーマットJSONに変換し、
     * コンフリクト解決を経てプロジェクトエンティティとして保存する。
     * 
     * @param issue 処理対象のJIRAイシュー（JsonNode）
     * @param template 変換用レスポンステンプレート
     * @param syncHistory 同期履歴エンティティ
     * @return 処理結果の同期履歴詳細
     */
    private void processJiraIssue(JsonNode issue, JiraResponseTemplate template, JiraSyncHistory syncHistory) {
        String issueKey = issue.has("key") ? issue.get("key").asText() : "unknown";
        
        try {
            // JIRAイシューをJSON文字列に変換
            String jiraResponseJson = objectMapper.writeValueAsString(issue);
            
            // Velocityテンプレートで共通フォーマットJSONに変換
            String commonFormatJson = jsonTransformService.transformResponse(
                jiraResponseJson, 
                template.getTemplateName()
            );
            
            syncHistory.addDetail("Convert Response by Velocity Template", DetailStatus.SUCCESS, commonFormatJson);
            log.debug("イシュー {} を共通フォーマットJSONに変換完了", issueKey);
            
            // プロジェクト情報を反映
            jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);
        } catch (JsonTransformException e) {
            log.error("JSON変換エラー: issueKey={} - {}", issueKey, e.getMessage(), e);

            // エラー詳細を同期履歴に追加
            syncHistory.addDetail("SYNC_ERROR", DetailStatus.ERROR, "JSON変換エラー: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("JIRAレスポンスのシリアライゼーションエラー: issueKey={} - {}", issueKey, e.getMessage(), e);

            // エラー詳細を同期履歴に追加
            syncHistory.addDetail("SYNC_ERROR", DetailStatus.ERROR, "JIRAレスポンス変換エラー: " + e.getMessage());
        } catch (Exception e) {
            log.error("イシュー処理中に予期しないエラーが発生: issueKey={} - {}", issueKey, e.getMessage(), e);

            // エラー詳細を同期履歴に追加
            syncHistory.addDetail("SYNC_ERROR", DetailStatus.ERROR, "イシュー処理エラー: " + e.getMessage());
        }
    }
    
    /**
     * リトライ機能付きJQLクエリ実行
     * 
     * REQ-8.1, REQ-8.2, REQ-8.3に対応した包括的なリトライロジック：
     * - タイムアウトエラー: 30秒間隔で最大3回リトライ
     * - ネットワークエラー: エクスポネンシャルバックオフでリトライ
     * - レート制限エラー: APIヘッダーから取得した時間だけ待機してリトライ
     * - 認証エラー: リトライしない（即座に管理者通知）
     * 
     * @param query 実行するJQLクエリエンティティ
     * @return JIRA検索結果
     * @throws JiraAuthenticationException 認証エラーの場合（REQ-8.4）
     * @throws JiraRateLimitException レート制限エラーの場合（REQ-8.3）
     * @throws JiraSyncException その他のエラーの場合
     */
    private JiraIssueSearchResponse executeJqlQueryWithRetry(JiraJqlQuery query, Integer startAt) {
        return jiraSyncRetryTemplate.execute(new RetryCallback<JiraIssueSearchResponse, RuntimeException>() {
            @Override
            public JiraIssueSearchResponse doWithRetry(RetryContext context) throws RuntimeException {
                int attemptCount = context.getRetryCount() + 1;
                log.debug("JQLクエリ実行試行 {}/{}: {}", attemptCount, 
                         RetryConfiguration.MAX_ATTEMPTS, query.getQueryName());
                
                try {
                    // 基本的なJQLクエリ実行
                    return jiraClient.searchIssues(
                        query.getJqlExpression(), 
                        50, // maxResults
                        startAt   // startAt
                    );
                    
                } catch (JiraRateLimitException e) {
                    // REQ-8.3: レート制限エラーの特別処理
                    handleRateLimitError(e, query, attemptCount);
                    throw e; // 再スローしてリトライ処理に委ねる
                    
                } catch (JiraAuthenticationException e) {
                    // REQ-8.4: 認証エラーはリトライしない
                    log.error("認証エラー発生のためリトライ終了: {}", query.getQueryName());
                    throw e; // 即座に外側のcatch節に伝播
                    
                } catch (Exception e) {
                    // その他のエラー: リトライ可能かを判定
                    if (RetryConfiguration.isRetryableException(e)) {
                        log.warn("リトライ可能なエラー発生 (試行 {}/{}): {} - {}", 
                                attemptCount, RetryConfiguration.MAX_ATTEMPTS, 
                                query.getQueryName(), e.getMessage());
                        throw e; // リトライ処理に委ねる
                    } else {
                        log.error("リトライ不可能なエラー発生: {} - {}", query.getQueryName(), e.getMessage());
                        throw new JiraSyncException("リトライ不可能なエラー: " + e.getMessage(), e);
                    }
                }
            }
        }, new RecoveryCallback<JiraIssueSearchResponse>() {
            @Override
            public JiraIssueSearchResponse recover(RetryContext context) throws Exception {
                // REQ-8.6: 全リトライ失敗時の管理者通知
                Throwable lastException = context.getLastThrowable();
                int attemptCount = context.getRetryCount();
                
                log.error("JQLクエリの全リトライが失敗: {} (試行回数: {})", 
                         query.getQueryName(), attemptCount);
                
                adminNotificationService.notifyRetryExhausted(
                    "JQLクエリの実行が全て失敗しました", 
                    query.getQueryName(), 
                    attemptCount, 
                    lastException
                );
                
                // 最後の例外を再スロー
                if (lastException instanceof RuntimeException) {
                    throw (RuntimeException) lastException;
                } else {
                    throw new JiraSyncException("全リトライ失敗: " + lastException.getMessage(), lastException);
                }
            }
        });
    }
    
    /**
     * レート制限エラーの処理
     * 
     * JIRA APIから取得したRetry-Afterヘッダーの値に基づいて
     * 適切な待機時間を設定する（REQ-8.3）。
     * 
     * @param rateLimitException レート制限例外
     * @param query 実行中のJQLクエリ
     * @param attemptCount 現在の試行回数
     */
    private void handleRateLimitError(JiraRateLimitException rateLimitException, JiraJqlQuery query, int attemptCount) {
        long waitTimeSeconds = rateLimitException.getRetryAfterSeconds();
        
        log.warn("JIRA APIレート制限発生: {} 秒待機後にリトライ (クエリ: {}, 試行: {}/{})",
                waitTimeSeconds, query.getQueryName(), attemptCount, RetryConfiguration.MAX_ATTEMPTS);
        
        try {
            // APIが指定した時間だけ待機
            Thread.sleep(rateLimitException.getRetryAfterMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("レート制限待機中に割り込み発生: {}", query.getQueryName());
            throw new JiraSyncException("レート制限待機中断: " + e.getMessage(), e);
        }
    }
    
    /**
     * バッチ処理によるイシュー処理（Task 5.2.1）
     * 
     * 大量のJIRAイシューをバッチサイズごとに分割してメモリ効率的に処理する。
     * 進捗ログを定期的に出力し、処理の可視性を向上させる。
     * 
     * @param issues 処理対象のJIRAイシューリスト
     * @param template レスポンステンプレート
     * @param syncHistory 同期履歴
     * @param query JQLクエリ（ログ出力用）
     * @return 処理結果の同期履歴詳細リスト
     */
    private void processBatchedIssues(List<JsonNode> issues, JiraResponseTemplate template, 
                                                        JiraSyncHistory syncHistory, JiraJqlQuery query) {
        int totalIssues = issues.size();
        int processedCount = 0;
        
        log.info("バッチ処理開始: {} 件のイシューを {} 件ずつ処理 (クエリ: {})", 
                totalIssues, batchSize, query.getQueryName());
        
        // バッチごとの処理
        for (int i = 0; i < totalIssues; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalIssues);
            List<JsonNode> batch = issues.subList(i, endIndex);
            
            // バッチ処理実行
            processIssueBatch(batch, template, syncHistory);
            
            processedCount += batch.size();
            
            // 進捗ログ出力
            if (progressLoggingEnabled && (processedCount % (batchSize * progressLoggingInterval) == 0 || processedCount >= totalIssues)) {
                double progressPercentage = (processedCount * 100.0) / totalIssues;
                log.info("処理進捗: {}/{} 件完了 ({:.1f}%) - クエリ: {}", 
                        processedCount, totalIssues, progressPercentage, query.getQueryName());
            }
            
            // メモリ効率化: バッチ間でGC推奨
            if (memoryEfficientProcessing && processedCount % (batchSize * 5) == 0) {
                System.gc();
            }
        }
        
        log.info("バッチ処理完了: {} 件のイシューを処理 (クエリ: {})", totalIssues, query.getQueryName());
    }
    
    /**
     * 単一バッチのイシュー処理
     * 
     * @param batch 処理対象のバッチ
     * @param template レスポンステンプレート
     * @param syncHistory 同期履歴
     * @return バッチ処理結果の同期履歴詳細リスト
     */
    private void processIssueBatch(List<JsonNode> batch, JiraResponseTemplate template, 
                                                     JiraSyncHistory syncHistory) {
        for (JsonNode issue : batch) {
            try {
                processJiraIssue(issue, template, syncHistory);
            } catch (Exception e) {
                String issueKey = issue.has("key") ? issue.get("key").asText() : "unknown";
                log.error("バッチ内イシュー処理エラー: issueKey={} - {}", issueKey, e.getMessage(), e);
                syncHistory.addDetail("Sync Error", DetailStatus.ERROR, String.format("バッチ処理エラー [%s]: %s", issueKey, e.getMessage()));
            }
        }
    }
    
    /**
     * 従来の一括イシュー処理
     * 
     * 少量データに対して従来通りの処理を実行する。
     * バッチ処理のオーバーヘッドを避けるための最適化。
     * 
     * @param issues 処理対象のJIRAイシューリスト
     * @param template レスポンステンプレート
     * @param syncHistory 同期履歴
     * @return 処理結果の同期履歴詳細リスト
     */
    private void processIssuesTraditional(List<JsonNode> issues, JiraResponseTemplate template, 
                                                           JiraSyncHistory syncHistory) {
        for (JsonNode issue : issues) {
            try {
                processJiraIssue(issue, template, syncHistory);
            } catch (Exception e) {
                String issueKey = issue.has("key") ? issue.get("key").asText() : "unknown";
                log.error("JIRAイシュー処理中にエラーが発生: issueKey={} - {}", issueKey, e.getMessage(), e);
                syncHistory.addDetail("Sync Error", DetailStatus.ERROR, String.format("バッチ処理エラー [%s]: %s", issueKey, e.getMessage()));
            }
        }
    }
    
    /**
     * クエリID入力値検証
     * 
     * @param queryId 検証対象のクエリID
     * @throws IllegalArgumentException queryIdが無効な場合
     */
}