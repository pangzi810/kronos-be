package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.JiraJqlQuery;

/**
 * JQLクエリリポジトリインターフェース
 * 
 * JIRA同期機能で使用するJQLクエリエンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepositoryパターンの実装
 * 
 * 責務:
 * - JQLクエリエンティティのCRUD操作
 * - 優先度に基づくアクティブクエリの取得（実行スケジューリング用）
 * - テンプレートIDに基づくクエリ検索
 * - クエリ名の重複チェック機能
 * - ページネーション対応の一覧取得
 * - アクティブクエリ統計情報の取得
 * 
 * ビジネス要件:
 * - REQ-2.1: 管理者がJQLクエリ一覧を参照可能
 * - REQ-2.4: JQLクエリの実行優先度設定による処理順序制御
 * - REQ-3.1: アクティブなJQLクエリの優先度順実行
 */
public interface JiraJqlQueryRepository {
    
    /**
     * JQLクエリIDでクエリを検索
     * 
     * @param id JQLクエリID
     * @return JQLクエリエンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException idがnullの場合
     */
    Optional<JiraJqlQuery> findById(String id);
    
    /**
     * クエリ名でJQLクエリを検索
     * 
     * JQLクエリ名の重複チェックや特定クエリの検索に使用
     * 
     * @param queryName クエリ名
     * @return JQLクエリエンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException queryNameがnullの場合
     */
    Optional<JiraJqlQuery> findByQueryName(String queryName);
    
    /**
     * 全JQLクエリ一覧を取得
     * 
     * 管理画面でのクエリ一覧表示に使用
     * 優先度昇順、同一優先度では作成日時降順でソート
     * 
     * @return 全JQLクエリのリスト
     */
    List<JiraJqlQuery> findAll();
    
    /**
     * アクティブなJQLクエリを優先度順で取得
     * 
     * JIRA同期実行時の処理順序決定に使用（REQ-2.4対応）
     * 優先度の昇順（数値の小さい方が高優先度）でソート
     * 同一優先度の場合は作成日時の昇順
     * 
     * @return アクティブなJQLクエリリスト（優先度昇順）
     */
    List<JiraJqlQuery> findActiveQueriesOrderByPriority();
    
    /**
     * 指定テンプレートIDのJQLクエリ一覧を取得
     * 
     * 特定のVelocityテンプレートに関連するクエリを取得
     * テンプレート変更時の影響範囲確認に使用
     * 
     * @param templateId テンプレートID
     * @return テンプレートに関連するJQLクエリリスト（優先度昇順）
     * @throws IllegalArgumentException templateIdがnullの場合
     */
    List<JiraJqlQuery> findByTemplateId(String templateId);
    
    /**
     * JQLクエリ名の存在チェック
     * 
     * クエリ作成時の重複チェックに使用
     * 
     * @param queryName クエリ名
     * @return 存在する場合true
     * @throws IllegalArgumentException queryNameがnullの場合
     */
    boolean existsByQueryName(String queryName);
    
    /**
     * JQLクエリの存在チェック
     * 
     * @param id JQLクエリID
     * @return 存在する場合true
     * @throws IllegalArgumentException idがnullの場合
     */
    boolean existsById(String id);
    
    /**
     * JQLクエリを保存
     * 
     * 新規作成・更新の両方で使用
     * 
     * @param jqlQuery 保存対象のJQLクエリエンティティ
     * @return 保存されたJQLクエリエンティティ
     * @throws IllegalArgumentException jqlQueryがnullの場合
     */
    JiraJqlQuery save(JiraJqlQuery jqlQuery);
    
    /**
     * 複数JQLクエリを一括保存
     * 
     * 大量データの同期処理や初期データ投入に使用
     * 
     * @param jqlQueries 保存対象のJQLクエリエンティティのリスト
     * @return 保存されたJQLクエリエンティティのリスト
     * @throws IllegalArgumentException jqlQueriesがnullの場合
     */
    List<JiraJqlQuery> saveAll(List<JiraJqlQuery> jqlQueries);
    
    /**
     * JQLクエリを削除
     * 
     * 物理削除を実行（JQLクエリは論理削除ではなく物理削除）
     * 関連する実行履歴は別途管理される
     * 
     * @param id 削除対象のJQLクエリID
     * @throws IllegalArgumentException idがnullの場合
     */
    void deleteById(String id);
    
    /**
     * ページネーション対応でJQLクエリ一覧を取得
     * 
     * 管理画面でのページング表示に使用（REQ-2.1対応）
     * 優先度昇順、同一優先度では作成日時降順でソート
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return JQLクエリリスト
     * @throws IllegalArgumentException limit < 0 または offset < 0の場合
     */
    List<JiraJqlQuery> findAllWithPagination(int limit, int offset);
    
    /**
     * アクティブなJQLクエリ数をカウント
     * 
     * 同期処理の負荷見積もりや統計情報取得に使用
     * 
     * @return アクティブなJQLクエリ数
     */
    long countActiveQueries();
}