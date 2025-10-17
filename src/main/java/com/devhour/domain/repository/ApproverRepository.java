package com.devhour.domain.repository;

import com.devhour.domain.model.entity.Approver;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 承認者関係リポジトリインターフェース
 * 
 * 承認者関係エンティティの永続化操作を定義
 * V44マイグレーション対応版：メールアドレスベース
 * 
 * 責務:
 * - 承認者関係エンティティのCRUD操作（メールアドレスベース）
 * - 日付による有効性チェック機能
 * - 承認権限の検証機能
 * - バッチ処理に対応した検索機能
 */
public interface ApproverRepository {
    
    // ===== Core CRUD Operations =====
    
    /**
     * 承認者関係を保存
     * 新規作成・更新の両方で使用
     * 
     * @param approver 保存対象の承認者関係
     * @return 保存された承認者関係エンティティ
     */
    Approver save(Approver approver);
    
    /**
     * IDで承認者関係を取得
     * 
     * @param id 承認者関係ID
     * @return 承認者関係（存在しない場合は空のOptional）
     */
    Optional<Approver> findById(String id);
    
    /**
     * 承認者関係をIDで削除
     * 
     * @param id 削除対象の承認者関係ID
     */
    void deleteById(String id);
    
    // ===== Email-based Search Operations =====
    
    /**
     * 対象者メールアドレスで承認者関係を検索
     * 指定された対象者に対する全ての承認者関係を取得
     * 
     * @param targetEmail 対象者メールアドレス（承認を受ける側）
     * @return 承認者関係のリスト
     */
    List<Approver> findByTargetEmail(String targetEmail);
    
    /**
     * 承認者メールアドレスで承認者関係を検索
     * 指定された承認者が承認できる全ての対象者関係を取得
     * 
     * @param approverEmail 承認者メールアドレス（承認する側）
     * @return 承認者関係のリスト
     */
    List<Approver> findByApproverEmail(String approverEmail);
    
    /**
     * 有効な承認者関係の存在チェック
     * 指定日において承認者-対象者の関係が有効かチェック
     * 
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     * @param date 対象日
     * @return 有効な関係が存在する場合true
     */
    boolean isValidApprover(String targetEmail, String approverEmail, LocalDate date);
    
    /**
     * 指定日の有効な承認者を取得
     * 対象者に対してその日に承認権限を持つ承認者のリストを取得
     * 
     * @param targetEmail 対象者メールアドレス
     * @param date 対象日
     * @return 有効な承認者関係のリスト
     */
    List<Approver> findValidApproversForDate(String targetEmail, LocalDate date);
    
    // ===== Bulk and Batch Operations =====
    
    /**
     * 全承認者関係を対象者でグループ化して取得
     * CSVバッチ処理などで使用、効率的な一括処理を可能にする
     * 
     * @return 対象者メールアドレスをキーとし、承認者メールアドレスのSetを値とするMap
     */
    Map<String, Set<String>> findAllGroupedByTarget();
    
    
    // ===== Maintenance Operations =====
    
    /**
     * 対象者と承認者の組み合わせで関係を削除
     * 特定の承認者関係を削除する際に使用
     * 
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     */
    void deleteByTargetAndApprover(String targetEmail, String approverEmail);
    
}