package com.devhour.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.repository.WorkRecordRepository;

import lombok.RequiredArgsConstructor;

/**
 * 工数記録ステータスドメインサービス
 * 
 * 工数記録の状態判定と未入力日の特定を管理
 * 
 * 責務:
 * - 工数記録とApprovalからステータス計算
 * - 指定期間内の未入力日特定
 * - 営業日（平日のみ）判定
 */
@Service
@RequiredArgsConstructor
public class WorkRecordStatusService {
    
    private final WorkRecordRepository workRecordRepository;
    
    
    /**
     * 工数記録と承認情報からステータスを計算
     * 
     * @param workRecord 工数記録エンティティ（null可能）
     * @param approval 承認情報エンティティ（null可能）
     * @return 工数記録ステータス
     */
    public ApprovalStatus calculateStatus(WorkRecord workRecord, WorkRecordApproval approval) {
        // 工数記録がない場合は未入力
        if (workRecord == null) {
            return ApprovalStatus.NOT_ENTERED;
        }
        
        // 承認情報がない場合も未入力扱い
        if (approval == null) {
            return ApprovalStatus.NOT_ENTERED;
        }
        
        // 承認ステータスをそのまま返す
        return approval.getApprovalStatus();
    }
    
    /**
     * 指定期間内の未入力日を特定
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 未入力日のリスト（営業日のみ）
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    public List<LocalDate> findMissingDates(String userId, LocalDate startDate, LocalDate endDate) {
        // パラメータ検証
        validateParameters(userId, startDate, endDate);
        
        // 指定期間の工数記録済み日付を取得
        List<WorkRecord> existingRecords = workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        Set<LocalDate> existingDates = existingRecords.stream()
            .map(WorkRecord::getWorkDate)
            .collect(Collectors.toSet());
        
        // 指定期間の全営業日を生成
        List<LocalDate> allBusinessDays = generateBusinessDays(startDate, endDate);
        
        // 営業日から既存の工数記録日を除いて未入力日を特定
        return allBusinessDays.stream()
            .filter(date -> !existingDates.contains(date))
            .collect(Collectors.toList());
    }
    
    /**
     * 指定期間内の工数記録済み日付リストを取得
     * （テスト用にアクセス性を向上）
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 工数記録済み日付のリスト
     */
    public List<LocalDate> findWorkDatesInRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<WorkRecord> records = workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return records.stream()
            .map(WorkRecord::getWorkDate)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    // === プライベートメソッド ===
    
    /**
     * パラメータの検証
     */
    private void validateParameters(String userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null) {
            throw new IllegalArgumentException("ユーザーIDは必須です");
        }
        
        if (startDate == null) {
            throw new IllegalArgumentException("開始日は必須です");
        }
        
        if (endDate == null) {
            throw new IllegalArgumentException("終了日は必須です");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日は終了日以前である必要があります");
        }
    }
    
    /**
     * 指定期間の営業日リストを生成
     * （平日のみ）
     */
    private List<LocalDate> generateBusinessDays(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> businessDays = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isBusinessDay(current)) {
                businessDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return businessDays;
    }
    
    /**
     * 営業日判定（平日のみ）
     */
    private boolean isBusinessDay(LocalDate date) {
        // 土曜日・日曜日は営業日でない
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}

