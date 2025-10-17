package com.devhour.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * 同期履歴リポジトリインターフェース
 * 
 * JIRA同期機能で使用する同期履歴エンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepositoryパターンの実装
 * 
 * 責務:
 * - 同期履歴エンティティのCRUD操作
 * - 日付範囲に基づく同期履歴検索
 * - ステータス・トリガータイプでのフィルタリング
 * - 実行中同期の管理機能
 * - ページネーション対応の履歴取得
 * - 詳細履歴との関連付け管理
 * - 統計情報の取得（カウント、失敗率分析）
 * 
 * ビジネス要件:
 * - REQ-6.1: 管理者が過去30日間の同期履歴を参照可能（実行時間、トリガータイプ、ステータス、処理数）
 * - REQ-6.2: 管理者が特定同期の詳細情報を参照可能（処理プロジェクト、エラー詳細、実行JQLクエリ）
 * 
 * アーキテクチャ:
 * - Domain Repository Pattern
 * - 永続化技術に依存しない抽象化レイヤー
 * - ドメインロジックとインフラストラクチャの分離
 */
public interface JiraSyncHistoryRepository {
    
    /**
     * 同期履歴IDで同期履歴を検索
     * 
     * @param id 同期履歴ID
     * @return 同期履歴エンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException idがnullの場合
     */
    Optional<JiraSyncHistory> findById(String id);
    
    /**
     * 詳細履歴付きで同期履歴を検索
     * 
     * 管理画面での詳細表示や同期実行状況の詳細分析に使用
     * 同期履歴と関連する詳細履歴を一括で取得する
     * 
     * @param id 同期履歴ID
     * @return 詳細履歴を含む同期履歴エンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException idがnullの場合
     */
    Optional<JiraSyncHistory> findWithDetails(String id);
    
    /**
     * 最近30日間の同期履歴を取得
     * 
     * REQ-6.1対応: 管理者向けの同期履歴一覧表示に使用
     * 開始日時の降順（最新から順）でソート
     * 
     * @return 最近30日間の同期履歴リスト（開始日時降順）
     */
    List<JiraSyncHistory> findRecent();
    
    /**
     * 指定日付範囲の同期履歴を取得
     * 
     * 特定期間の同期実行状況分析に使用
     * 開始日時の降順でソート
     * 
     * @param startDate 検索開始日時（この日時以降）
     * @param endDate 検索終了日時（この日時以前）
     * @return 期間内の同期履歴リスト（開始日時降順）
     * @throws IllegalArgumentException startDate または endDate がnullの場合
     * @throws IllegalArgumentException startDate が endDate より後の場合
     */
    List<JiraSyncHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, String status);
    
    /**
     * 指定ステータスの同期履歴を取得
     * 
     * 同期実行状況の分析や失敗したジョブの確認に使用
     * 開始日時の降順でソート
     * 
     * @param status 同期ステータス
     * @return 指定ステータスの同期履歴リスト（開始日時降順）
     * @throws IllegalArgumentException statusがnullの場合
     */
    List<JiraSyncHistory> findByStatus(JiraSyncStatus status);
    
    /**
     * 指定トリガータイプの同期履歴を取得
     * 
     * 手動実行・自動実行の実行状況分析に使用
     * 開始日時の降順でソート
     * 
     * @param triggerType トリガータイプ（MANUAL, SCHEDULED）
     * @return 指定トリガータイプの同期履歴リスト（開始日時降順）
     * @throws IllegalArgumentException triggerTypeがnullの場合
     */
    List<JiraSyncHistory> findByTriggerType(JiraSyncType triggerType);
    
    /**
     * 実行中の同期履歴を取得
     * 
     * 同期の重複実行防止や進行状況確認に使用
     * 開始日時の昇順でソート（古い実行から順）
     * 
     * @return 実行中の同期履歴リスト（開始日時昇順）
     */
    List<JiraSyncHistory> findInProgress();
    
    /**
     * ページネーション対応で同期履歴を取得
     * 
     * 管理画面でのページング表示に使用（REQ-6.1対応）
     * 開始日時の降順（最新から順）でソート
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return 同期履歴リスト（開始日時降順）
     * @throws IllegalArgumentException limit < 0 または offset < 0の場合
     */
    List<JiraSyncHistory> findWithPagination(int limit, int offset, String status);
    
    /**
     * 同期履歴を保存
     * 
     * 新規作成・更新の両方で使用
     * 同期実行の開始・進行・完了状況の永続化に使用
     * 
     * @param syncHistory 保存対象の同期履歴エンティティ
     * @return 保存された同期履歴エンティティ
     * @throws IllegalArgumentException syncHistoryがnullの場合
     */
    JiraSyncHistory save(JiraSyncHistory syncHistory);
    
    /**
     * 複数同期履歴を一括保存
     * 
     * バッチ処理や大量データの同期処理時に使用
     * 
     * @param syncHistories 保存対象の同期履歴エンティティのリスト
     * @return 保存された同期履歴エンティティのリスト
     * @throws IllegalArgumentException syncHistoriesがnullの場合
     */
    List<JiraSyncHistory> saveAll(List<JiraSyncHistory> syncHistories);
    
    /**
     * 同期履歴を削除
     * 
     * 物理削除を実行（古い履歴のクリーンアップに使用）
     * 通常の運用では削除は推奨されない
     * 
     * @param id 削除対象の同期履歴ID
     * @throws IllegalArgumentException idがnullの場合
     */
    void deleteById(String id);
    
    /**
     * 同期履歴の存在チェック
     * 
     * @param id 同期履歴ID
     * @return 存在する場合true
     * @throws IllegalArgumentException idがnullの場合
     */
    boolean existsById(String id);
    
    // ========================================
    // 統計情報取得メソッド
    // ========================================
    
    /**
     * 指定ステータスの同期履歴数をカウント
     * 
     * 同期実行成功率や失敗率の統計情報取得に使用
     * 管理画面でのメトリクス表示に活用
     * 
     * @param status 同期ステータス
     * @return 指定ステータスの同期履歴数
     * @throws IllegalArgumentException statusがnullの場合
     */
    long countByStatus(JiraSyncStatus status);
    
    /**
     * 最近N時間以内の失敗数をカウント
     * 
     * 同期実行の安定性監視や障害検知に使用
     * アラート機能での閾値判定に活用
     * 
     * @param hours 過去何時間分を対象とするか
     * @return 最近N時間以内の失敗数
     * @throws IllegalArgumentException hours < 0の場合
     */
    long countRecentFailures(int hours);
}