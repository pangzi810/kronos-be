package com.devhour.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.WorkRecordApprovalHistory;

/**
 * 承認履歴リポジトリインターフェース
 * 
 * 承認履歴エンティティの永続化操作を定義
 */
public interface WorkRecordApprovalHistoryRepository {
    
    /**
     * 承認履歴を保存
     * 
     * @param history 承認履歴
     */
    void save(WorkRecordApprovalHistory history);
    
    /**
     * 履歴IDで承認履歴を取得
     * 
     * @param historyId 履歴ID
     * @return 承認履歴（オプショナル）
     */
    Optional<WorkRecordApprovalHistory> findById(String historyId);
    
    /**
     * 工数記録IDで承認履歴を取得
     * 
     * @param workRecordId 工数記録ID
     * @return 承認履歴のリスト（発生順）
     */
    List<WorkRecordApprovalHistory> findByWorkRecordId(String workRecordId);
    
    /**
     * 承認者IDと期間で承認履歴を取得
     * 
     * @param approverId 承認者ID
     * @param from 開始日時
     * @param to 終了日時
     * @return 承認履歴のリスト
     */
    List<WorkRecordApprovalHistory> findByApproverIdAndPeriod(String approverId,
                                                    LocalDateTime from,
                                                    LocalDateTime to);
    
    /**
     * ユーザーIDと期間で承認履歴を取得
     * 
     * @param userId ユーザーID
     * @param from 開始日時
     * @param to 終了日時
     * @return 承認履歴のリスト
     */
    List<WorkRecordApprovalHistory> findByUserIdAndPeriod(String userId,
                                               LocalDateTime from,
                                               LocalDateTime to);
    
    /**
     * プロジェクトIDと期間で承認履歴を取得
     * 
     * @param projectId プロジェクトID
     * @param from 開始日時
     * @param to 終了日時
     * @return 承認履歴のリスト
     */
    List<WorkRecordApprovalHistory> findByProjectIdAndPeriod(String projectId,
                                                  LocalDateTime from,
                                                  LocalDateTime to);
    
    /**
     * 指定期間の承認アクション数を取得
     * 
     * @param approverId 承認者ID
     * @param from 開始日時
     * @param to 終了日時
     * @return 承認アクション数
     */
    int countApprovalsByApproverAndPeriod(String approverId,
                                          LocalDateTime from,
                                          LocalDateTime to);
    
    /**
     * 指定期間の却下アクション数を取得
     * 
     * @param approverId 承認者ID
     * @param from 開始日時
     * @param to 終了日時
     * @return 却下アクション数
     */
    int countRejectionsByApproverAndPeriod(String approverId,
                                           LocalDateTime from,
                                           LocalDateTime to);
    
    /**
     * 最新の承認履歴を取得
     * 
     * @param workRecordId 工数記録ID
     * @return 最新の承認履歴（オプショナル）
     */
    Optional<WorkRecordApprovalHistory> findLatestByWorkRecordId(String workRecordId);
    
    /**
     * 履歴IDの存在チェック
     * 
     * @param historyId 履歴ID
     * @return 存在する場合true
     */
    boolean existsById(String historyId);
}