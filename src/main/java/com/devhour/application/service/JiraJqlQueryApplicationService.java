package com.devhour.application.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.infrastructure.jira.JiraClient;
import com.devhour.infrastructure.jira.JiraClient.JiraClientException;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;

/**
 * JQLクエリアプリケーションサービス
 * 
 * JIRA同期機能におけるJQLクエリ管理のユースケースを実装する
 * ドメイン駆動設計におけるアプリケーションサービス層の責務を担う
 * 
 * 責務:
 * - JQLクエリのCRUD操作の調整 (REQ-2.1, REQ-2.2, REQ-2.8)
 * - JQL構文検証による事前バリデーション (REQ-2.2, REQ-2.3)
 * - 実行優先度管理とクエリ順序制御 (REQ-2.4)
 * - トランザクション境界の管理
 * - ドメインエンティティとインフラストラクチャサービスの協調
 * - ビジネスルール違反の検出と例外処理
 * - 監査証跡のためのユーザー情報管理
 * 
 * 設計方針:
 * - ドメインロジックはエンティティに委譲
 * - インフラストラクチャサービスへの依存を抽象化
 * - 包括的なパラメータ検証
 * - 適切なエラーハンドリングとログ出力
 * - テスト容易性を考慮した単一責任原則
 */
@Service
@Transactional
@ConditionalOnProperty(name = "jira.integration.enabled", havingValue = "true", matchIfMissing = false)
public class JiraJqlQueryApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraJqlQueryApplicationService.class);
    
    private final JiraJqlQueryRepository jqlQueryRepository;
    private final JiraResponseTemplateRepository responseTemplateRepository;
    private final JiraClient jiraClient;
    
    public JiraJqlQueryApplicationService(
            JiraJqlQueryRepository jqlQueryRepository,
            JiraResponseTemplateRepository responseTemplateRepository,
            JiraClient jiraClient) {
        this.jqlQueryRepository = jqlQueryRepository;
        this.responseTemplateRepository = responseTemplateRepository;
        this.jiraClient = jiraClient;
    }
    
    /**
     * 新しいJQLクエリを作成 (REQ-2.2)
     * 
     * 管理者が新しいJQLクエリを作成する際の処理。
     * テンプレート存在確認、クエリ名重複チェック、ドメインエンティティ生成を行う。
     * 
     * @param queryName JQLクエリ名（1-100文字）
     * @param jqlString JQL式（必須、空白不可）
     * @param templateId レスポンステンプレートID（存在必須）
     * @param priority 実行優先度（0以上の整数）
     * @param createdBy 作成者のユーザーID（必須）
     * @return 作成されたJQLクエリエンティティ
     * @throws IllegalArgumentException パラメータ検証違反の場合
     */
    public JiraJqlQuery createJqlQuery(String queryName, String jqlString, String templateId, 
                                  Integer priority, String createdBy) {
        logger.info("JQLクエリ作成開始: queryName={}, priority={}, createdBy={}", 
                   queryName, priority, createdBy);
        
        // 入力パラメータの基本検証
        validateCreateParameters(queryName, jqlString, templateId, priority, createdBy);
        
        // テンプレート存在確認
        if (!responseTemplateRepository.existsById(templateId)) {
            logger.warn("存在しないテンプレートIDが指定されました: {}", templateId);
            throw new IllegalArgumentException("指定されたテンプレートIDが存在しません: " + templateId);
        }
        
        // クエリ名重複チェック
        if (jqlQueryRepository.existsByQueryName(queryName)) {
            logger.warn("重複するクエリ名が指定されました: {}", queryName);
            throw new IllegalArgumentException("同名のJQLクエリが既に存在します: " + queryName);
        }
        
        // ドメインエンティティ作成
        JiraJqlQuery newQuery = JiraJqlQuery.createNew(queryName, jqlString, templateId, priority, createdBy);
        
        // 永続化
        JiraJqlQuery savedQuery = jqlQueryRepository.save(newQuery);
        
        logger.info("JQLクエリ作成完了: id={}, queryName={}", savedQuery.getId(), savedQuery.getQueryName());
        return savedQuery;
    }
    
    /**
     * 既存JQLクエリを更新 (REQ-2.2)
     * 
     * 管理者が既存のJQLクエリを更新する際の処理。
     * 存在確認、重複チェック、ドメインエンティティの更新を行う。
     * 
     * @param id 更新対象のJQLクエリID
     * @param queryName 新しいクエリ名
     * @param jqlString 新しいJQL式
     * @param templateId 新しいテンプレートID
     * @param priority 新しい実行優先度
     * @param updatedBy 更新者のユーザーID
     * @return 更新されたJQLクエリエンティティ
     * @throws IllegalArgumentException パラメータ検証違反またはエンティティが見つからない場合
     */
    public JiraJqlQuery updateJqlQuery(String id, String queryName, String jqlString, String templateId, 
                                  Integer priority, String updatedBy) {
        logger.info("JQLクエリ更新開始: id={}, queryName={}, updatedBy={}", id, queryName, updatedBy);
        
        // 入力パラメータの基本検証
        validateUpdateParameters(id, queryName, jqlString, templateId, priority, updatedBy);
        
        // 既存クエリの取得
        JiraJqlQuery existingQuery = jqlQueryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("JQLクエリが見つかりません: " + id));
        
        // テンプレート存在確認
        if (!responseTemplateRepository.existsById(templateId)) {
            logger.warn("存在しないテンプレートIDが指定されました: {}", templateId);
            throw new IllegalArgumentException("指定されたテンプレートIDが存在しません: " + templateId);
        }
        
        // クエリ名重複チェック（自分以外で同名が存在する場合）
        Optional<JiraJqlQuery> duplicateQuery = jqlQueryRepository.findByQueryName(queryName);
        if (duplicateQuery.isPresent() && !duplicateQuery.get().getId().equals(id)) {
            logger.warn("重複するクエリ名が指定されました: {} (既存ID: {})", queryName, duplicateQuery.get().getId());
            throw new IllegalArgumentException("同名のJQLクエリが既に存在します: " + queryName);
        }
        
        // ドメインエンティティ更新（個別メソッドで段階的更新）
        existingQuery.updateQueryName(queryName, updatedBy);
        existingQuery.updateQuery(jqlString, updatedBy);
        existingQuery.updateTemplate(templateId, updatedBy);
        existingQuery.updatePriority(priority, updatedBy);
        
        // 永続化
        JiraJqlQuery updatedQuery = jqlQueryRepository.save(existingQuery);
        
        logger.info("JQLクエリ更新完了: id={}, queryName={}", updatedQuery.getId(), updatedQuery.getQueryName());
        return updatedQuery;
    }
    
    /**
     * JQLクエリを論理削除 (REQ-2.8)
     * 
     * 管理者がJQLクエリを削除する際の処理。
     * 物理削除ではなく非アクティブ化による論理削除を実行し、
     * 削除者と削除日時の監査証跡を記録する。
     * 
     * @param id 削除対象のJQLクエリID
     * @param deletedBy 削除実行者のユーザーID
     * @throws IllegalArgumentException パラメータ検証違反またはエンティティが見つからない場合
     */
    public void deleteJqlQuery(String id, String deletedBy) {
        logger.info("JQLクエリ論理削除開始: id={}, deletedBy={}", id, deletedBy);
        
        // 入力パラメータの基本検証
        validateDeleteParameters(id, deletedBy);
        
        // 既存クエリの取得
        JiraJqlQuery existingQuery = jqlQueryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("JQLクエリが見つかりません: " + id));
        
        // 論理削除の実行（非アクティブ化）
        existingQuery.deactivate();
        
        // 永続化
        jqlQueryRepository.save(existingQuery);
        
        logger.info("JQLクエリ論理削除完了: id={}", id);
    }
    
    /**
     * JQL構文を検証 (REQ-2.2, REQ-2.3)
     * 
     * 管理者がJQLクエリを入力した際の構文検証処理。
     * JIRA APIを使用してクエリ構文の有効性と、マッチするプロジェクト数を確認する。
     * 
     * @param jqlString 検証対象のJQL式
     * @return JQL検証結果（有効性、マッチング数、エラーメッセージ）
     * @throws IllegalArgumentException jqlStringが無効な場合
     */
    @Transactional(readOnly = true)
    public JqlValidationResult validateJql(String jqlString) {
        logger.info("JQL構文検証開始: jql={}", jqlString);
        
        // 入力パラメータの基本検証
        if (jqlString == null || jqlString.trim().isEmpty()) {
            throw new IllegalArgumentException("JQL式は必須です");
        }
        
        try {
            // JIRA APIを使用した構文検証
            // maxResults=0, startAt=0 でカウントのみ取得
            JiraIssueSearchResponse response = jiraClient.searchIssues(
                jqlString.trim(),
                1,  // カウントのみ取得
                0   // 開始位置
            );
            
            // 総件数を使用（maxResults=0の場合でも正確な件数を取得）
            int matchingCount = response.getTotal() != null ? response.getTotal() :
                               (response.getIssues() != null ? response.getIssues().size() : 0);
            logger.info("JQL構文検証完了: 有効なクエリ、マッチング数={}", matchingCount);
            return JqlValidationResult.valid(matchingCount);
            
        } catch (JiraClientException e) {
            logger.warn("JQL構文検証エラー: jql={}, error={}", jqlString, e.getMessage());
            return JqlValidationResult.invalid(e.getMessage());
            
        } catch (Exception e) {
            logger.error("JQL構文検証中に予期しないエラー: jql={}", jqlString, e);
            return JqlValidationResult.invalid("構文検証中にエラーが発生しました: " + e.getMessage());
        }
    }
    
    /**
     * アクティブなJQLクエリを優先度順で取得 (REQ-2.4)
     * 
     * JIRA同期処理で実行されるアクティブなJQLクエリ一覧を
     * 実行優先度の昇順（数値の小さい方が高優先度）で取得する。
     * 
     * @return 優先度順のアクティブJQLクエリリスト
     */
    @Transactional(readOnly = true)
    public List<JiraJqlQuery> getQueriesByPriority() {
        logger.debug("優先度順JQLクエリ一覧取得開始");
        
        List<JiraJqlQuery> queries = jqlQueryRepository.findActiveQueriesOrderByPriority();
        
        logger.info("優先度順JQLクエリ一覧取得完了: {} 件", queries.size());
        return queries;
    }
    
    /**
     * IDでJQLクエリを取得
     * 
     * 管理画面での個別クエリ表示や編集時に使用する。
     * 
     * @param id JQLクエリID
     * @return JQLクエリエンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException idが無効な場合
     */
    @Transactional(readOnly = true)
    public Optional<JiraJqlQuery> findById(String id) {
        validateId(id);
        
        return jqlQueryRepository.findById(id);
    }
    
    /**
     * 全JQLクエリ一覧を取得
     * 
     * 管理画面でのクエリ一覧表示に使用する。
     * アクティブ・非アクティブを問わず全てのクエリを取得する。
     * 
     * @return 全JQLクエリのリスト
     */
    @Transactional(readOnly = true)
    public List<JiraJqlQuery> findAll() {
        logger.debug("全JQLクエリ一覧取得開始");
        
        List<JiraJqlQuery> queries = jqlQueryRepository.findAll();
        
        logger.info("全JQLクエリ一覧取得完了: {} 件", queries.size());
        return queries;
    }
    
    
    /**
     * JQLクエリをアクティブ化
     * 
     * 非アクティブなJQLクエリを再びアクティブ状態に変更する。
     * 
     * @param id アクティブ化対象のJQLクエリID
     * @return アクティブ化されたJQLクエリエンティティ
     * @throws IllegalArgumentException パラメータ検証違反またはエンティティが見つからない場合
     */
    public JiraJqlQuery activateQuery(String id) {
        logger.info("JQLクエリアクティブ化開始: id={}", id);
        
        validateId(id);
        
        // 既存クエリの取得
        JiraJqlQuery existingQuery = jqlQueryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("JQLクエリが見つかりません: " + id));
        
        // アクティブ化
        existingQuery.activate();
        
        // 永続化
        JiraJqlQuery updatedQuery = jqlQueryRepository.save(existingQuery);
        
        logger.info("JQLクエリアクティブ化完了: id={}", id);
        return updatedQuery;
    }
    
    /**
     * JQLクエリを非アクティブ化
     * 
     * アクティブなJQLクエリを一時的に非アクティブ状態に変更する。
     * 論理削除とは異なり、再アクティブ化が可能。
     * 
     * @param id 非アクティブ化対象のJQLクエリID
     * @return 非アクティブ化されたJQLクエリエンティティ
     * @throws IllegalArgumentException パラメータ検証違反またはエンティティが見つからない場合
     */
    public JiraJqlQuery deactivateQuery(String id) {
        logger.info("JQLクエリ非アクティブ化開始: id={}", id);
        
        validateId(id);
        
        // 既存クエリの取得
        JiraJqlQuery existingQuery = jqlQueryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("JQLクエリが見つかりません: " + id));
        
        // 非アクティブ化
        existingQuery.deactivate();
        
        // 永続化
        JiraJqlQuery updatedQuery = jqlQueryRepository.save(existingQuery);
        
        logger.info("JQLクエリ非アクティブ化完了: id={}", id);
        return updatedQuery;
    }
    
    // ========== パラメータ検証メソッド ==========
    
    /**
     * JQLクエリ作成パラメータの検証
     */
    private void validateCreateParameters(String queryName, String jqlString, String templateId, 
                                        Integer priority, String createdBy) {
        if (queryName == null || queryName.trim().isEmpty()) {
            throw new IllegalArgumentException("クエリ名は必須です");
        }
        
        if (jqlString == null || jqlString.trim().isEmpty()) {
            throw new IllegalArgumentException("JQL式は必須です");
        }
        
        if (templateId == null || templateId.trim().isEmpty()) {
            throw new IllegalArgumentException("テンプレートIDは必須です");
        }
        
        if (priority == null || priority < 0) {
            throw new IllegalArgumentException("優先度は0以上である必要があります");
        }
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("作成者は必須です");
        }
    }
    
    /**
     * JQLクエリ更新パラメータの検証
     */
    private void validateUpdateParameters(String id, String queryName, String jqlString, 
                                        String templateId, Integer priority, String updatedBy) {
        validateId(id);
        validateCreateParameters(queryName, jqlString, templateId, priority, updatedBy);
    }
    
    /**
     * JQLクエリ削除パラメータの検証
     */
    private void validateDeleteParameters(String id, String deletedBy) {
        validateId(id);
        if (deletedBy == null || deletedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("削除実行者は必須です");
        }
    }
    
    /**
     * ID共通検証
     */
    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("JQLクエリIDは必須です");
        }
    }
    
    // ========== 内部クラス ==========
    
    /**
     * JQL検証結果を表す値オブジェクト
     * 
     * JQL構文検証の結果（有効性、マッチング数、エラーメッセージ）を保持する。
     * REQ-2.2, REQ-2.3で要求される検証結果の表現に使用される。
     */
    public static class JqlValidationResult {
        
        private final boolean valid;
        private final int matchingProjectCount;
        private final String errorMessage;
        
        private JqlValidationResult(boolean valid, int matchingProjectCount, String errorMessage) {
            this.valid = valid;
            this.matchingProjectCount = matchingProjectCount;
            this.errorMessage = errorMessage;
        }
        
        /**
         * 有効なJQL検証結果を作成
         * 
         * @param matchingCount マッチしたプロジェクト数
         * @return 有効な検証結果
         */
        public static JqlValidationResult valid(int matchingCount) {
            return new JqlValidationResult(true, matchingCount, null);
        }
        
        /**
         * 無効なJQL検証結果を作成
         * 
         * @param errorMessage エラーメッセージ
         * @return 無効な検証結果
         */
        public static JqlValidationResult invalid(String errorMessage) {
            return new JqlValidationResult(false, 0, errorMessage);
        }
        
        /**
         * JQLクエリが有効かどうかを判定
         * 
         * @return 有効な場合true
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * マッチするプロジェクト数を取得
         * 
         * @return マッチしたプロジェクト数（無効な場合は0）
         */
        public int getMatchingProjectCount() {
            return matchingProjectCount;
        }
        
        /**
         * エラーメッセージを取得
         * 
         * @return エラーメッセージ（有効な場合はnull）
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}