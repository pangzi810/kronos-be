package com.devhour.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;

/**
 * 作業記録承認リポジトリインターフェース
 */
public interface WorkRecordApprovalRepository {
    
    /**
     * 申請者IDと作業日で承認レコードを検索
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     * @return 承認レコード（存在しない場合は空）
     */
    Optional<WorkRecordApproval> findByUserIdAndDate(String userId, LocalDate workDate);
    
    /**
     * 複数の申請者IDと承認ステータスで承認レコードを検索
     * 
     * @param userIds 申請者IDリスト
     * @param statuses 承認ステータスリスト
     * @return 承認レコードリスト
     */
    List<WorkRecordApproval> findByUsersAndStatuses(List<String> userIds, List<ApprovalStatus> statuses);
    
    /**
     * 申請者IDで承認レコードを検索
     * 
     * @param userId 申請者ID
     * @return 承認レコードリスト
     */
    List<WorkRecordApproval> findByUserId(String userId);
    
    /**
     * 承認者IDで承認レコードを検索
     * 
     * @param approverId 承認者ID
     * @return 承認レコードリスト
     */
    List<WorkRecordApproval> findByApproverId(String approverId);
    
    /**
     * 期間内の承認レコードを検索
     * 
     * @param userId 申請者ID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 承認レコードリスト
     */
    List<WorkRecordApproval> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 承認ステータスで承認レコードを検索
     * 
     * @param status 承認ステータス
     * @return 承認レコードリスト
     */
    List<WorkRecordApproval> findByStatus(ApprovalStatus status);
    
    /**
     * 承認レコードを保存（新規作成または更新）
     * 
     * @param approval 承認レコード
     * @return 保存された承認レコード
     */
    WorkRecordApproval save(WorkRecordApproval approval);
    
    /**
     * 承認レコードを削除
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     */
    void delete(String userId, LocalDate workDate);
    
    /**
     * 申請者IDで承認レコードを全て削除
     * 
     * @param userId 申請者ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 承認レコードが存在するかチェック
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     * @return 存在する場合true
     */
    boolean exists(String userId, LocalDate workDate);
}