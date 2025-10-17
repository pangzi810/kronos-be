package com.devhour.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.WorkRecord;

/**
 * 工数記録リポジトリインターフェース
 * 
 * 工数記録エンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepository パターンの実装
 * 
 * 責務:
 * - 工数記録エンティティの CRUD 操作
 * - ユーザー・プロジェクト・日付に基づく検索機能
 */
public interface WorkRecordRepository {
    
    /**
     * 工数記録IDで工数記録を検索
     * 
     * @param workRecordId 工数記録ID
     * @return 工数記録エンティティ（存在しない場合は空のOptional）
     */
    Optional<WorkRecord> findById(String workRecordId);
    
    /**
     * ユーザーIDと日付で工数記録を検索
     * 
     * @param userId ユーザーID
     * @param workDate 作業日
     * @return 工数記録のリスト
     */
    List<WorkRecord> findByUserIdAndDate(String userId, LocalDate workDate);
    
    /**
     * ユーザーID、日付、プロジェクトIDで工数記録を検索
     * 
     * @param userId ユーザーID
     * @param workDate 作業日
     * @param projectId プロジェクトID
     * @return 工数記録エンティティ
     */
    Optional<WorkRecord> findByUserIdAndDateAndProjectId(String userId, LocalDate workDate, String projectId);
    
    /**
     * ユーザーの全工数記録を取得
     * 
     * @param userId ユーザーID
     * @return ユーザーの全工数記録リスト（作業日降順）
     */
    List<WorkRecord> findByUser(String userId);
    
    /**
     * プロジェクトの全工数記録を取得
     * 
     * @param projectId プロジェクトID
     * @return プロジェクトの全工数記録リスト（作業日降順）
     */
    List<WorkRecord> findByProject(String projectId);
    
    /**
     * 指定期間の工数記録を取得
     * 
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 指定期間の工数記録リスト
     */
    List<WorkRecord> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * ユーザーIDの指定期間の工数記録を取得
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return ユーザーの指定期間の工数記録リスト
     */
    List<WorkRecord> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    
    /**
     * ユーザーの最新工数記録を取得
     * 
     * @param userId ユーザーID
     * @param limit 取得件数
     * @return 最新の工数記録リスト
     */
    List<WorkRecord> findLatestByUser(String userId, int limit);
    
    /**
     * 工数記録を保存
     * 新規作成・更新の両方で使用
     * 
     * @param workRecord 保存対象の工数記録エンティティ
     * @return 保存された工数記録エンティティ
     */
    WorkRecord save(WorkRecord workRecord);
    
    /**
     * 複数の工数記録を一括保存
     * 
     * @param workRecords 保存対象の工数記録エンティティのリスト
     * @return 保存された工数記録エンティティのリスト
     */
    List<WorkRecord> saveAll(List<WorkRecord> workRecords);
    
    /**
     * 工数記録を削除
     * 
     * @param workRecordId 削除対象の工数記録ID
     */
    void deleteById(String workRecordId);

    /**
     * 指定日のユーザーの工数記録を全て削除
     * @param userId
     * @param workDate
     */
    void deleteByUserIdAndDate(String userId, LocalDate workDate);

    /**
     * 指定日のユーザーの工数記録をIDリストで削除
     * @param userId
     * @param recordIds
     */
    void deleteByUserIdAndDateAndRecordIds(String userId, LocalDate workDate, List<String> recordIds);
}