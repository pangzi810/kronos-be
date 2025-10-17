package com.devhour.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;

/**
 * 工数記録承認イベント
 * 
 * 申請者/日付単位での承認・却下処理を表現するイベント
 * Kafkaを通じて他のサービスに配信される
 */
public record WorkRecordApprovalEvent(
    String eventId,                    // イベントID（userId-workDate-timestamp形式）
    String userId,                     // 申請者ID
    LocalDate workDate,                // 作業日
    String action,                     // アクション（APPROVE/REJECT）
    String approverId,                 // 承認者ID
    String rejectionReason,            // 却下理由（却下の場合のみ）
    String currentStatus,              // 変更後ステータス
    List<WorkRecordSnapshot> targetWorkRecords, // 対象工数記録の内容
    LocalDateTime occurredAt           // イベント発生日時
) {
    
    /**
     * 工数記録のスナップショット
     * イベント時点での工数記録の内容を保持
     */
    public record WorkRecordSnapshot(
        String workRecordId,           // 工数記録ID
        String projectId,              // プロジェクトID
        String categoryHoursJson,      // カテゴリ別工数（JSON形式）
        BigDecimal totalHours,         // 合計工数
        String description,            // 作業内容
        String approvalStatus,         // 承認ステータス
        int version                    // バージョン
    ) {}
    
    /**
     * 日次承認イベントを生成
     */
    public static WorkRecordApprovalEvent createForApproval(WorkRecordApproval approval,
                                                           List<WorkRecord> targetWorkRecords) {
        String eventId = String.format("%s-%s-%d", approval.getUserId(), approval.getWorkDate().toString(), System.currentTimeMillis());
        
        List<WorkRecordSnapshot> snapshots = targetWorkRecords.stream()
            .map(WorkRecordApprovalEvent::createWorkRecordSnapshot)
            .toList();
        
        return new WorkRecordApprovalEvent(
            eventId,
            approval.getUserId(),
            approval.getWorkDate(),
            "APPROVE",
            approval.getApproverId(),
            null, // 承認時は却下理由なし
            "APPROVED",
            snapshots,
            LocalDateTime.now()
        );
    }
    
    /**
     * 日次却下イベントを生成
     */
    public static WorkRecordApprovalEvent createForRejection(WorkRecordApproval approval,
                                                            List<WorkRecord> targetWorkRecords) {
        String eventId = String.format("%s-%s-%d", approval.getUserId(), approval.getWorkDate().toString(), System.currentTimeMillis());
        
        List<WorkRecordSnapshot> snapshots = targetWorkRecords.stream()
            .map(WorkRecordApprovalEvent::createWorkRecordSnapshot)
            .toList();
        
        return new WorkRecordApprovalEvent(
            eventId,
            approval.getUserId(),
            approval.getWorkDate(),
            "REJECT",
            approval.getApproverId(),
            approval.getRejectionReason(),
            "REJECTED",
            snapshots,
            LocalDateTime.now()
        );
    }
    
    /**
     * WorkRecordからWorkRecordSnapshotを作成
     */
    private static WorkRecordSnapshot createWorkRecordSnapshot(WorkRecord workRecord) {
        String categoryHoursJson = workRecord.getCategoryHours() != null ?
            workRecord.getCategoryHours().toJsonString() : "{}";
        
        BigDecimal totalHours = workRecord.getCategoryHours() != null ?
            workRecord.getCategoryHours().getTotalHours() : BigDecimal.ZERO;
        
        return new WorkRecordSnapshot(
            workRecord.getId(),
            workRecord.getProjectId(),
            categoryHoursJson,
            totalHours,
            workRecord.getDescription(),
            "PENDING", // 工数記録レベルでは承認状態は管理されない
            0 // バージョン番号も工数記録レベルでは管理されない
        );
    }
}