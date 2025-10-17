package com.devhour.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.exception.InvalidParameterException;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.repository.WorkRecordApprovalRepository;
import com.devhour.domain.repository.WorkRecordRepository;
import com.devhour.domain.service.CategoryHoursValidationService;
import com.devhour.domain.service.WorkRecordStatusService;
import com.devhour.presentation.dto.request.WorkRecordSaveRequest;
import com.devhour.presentation.dto.response.DateStatusResponse;
import com.devhour.presentation.dto.response.WorkHoursSummaryResponse;
import com.devhour.presentation.dto.response.WorkRecordsResponse;

/**
 * 工数記録アプリケーションサービス
 * 
 * 工数記録に関するユースケースを実装
 * 
 * 責務:
 * - 工数記録の作成・更新・削除
 * - 1日1レコード制約の管理

 * - ビジネスルールの適用とトランザクション管理
 */
@Service
@Transactional
public class WorkRecordApplicationService {
    
    private final WorkRecordRepository workRecordRepository;
    private final WorkRecordApprovalRepository workRecordApprovalRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CategoryHoursValidationService categoryHoursValidationService;
    private final WorkRecordStatusService workRecordStatusService;
    
    public WorkRecordApplicationService(WorkRecordRepository workRecordRepository,
                                        WorkRecordApprovalRepository workRecordApprovalRepository,  
                                      UserRepository userRepository,
                                      ProjectRepository projectRepository,
                                      CategoryHoursValidationService categoryHoursValidationService,
                                      WorkRecordStatusService workRecordStatusService) {
        this.workRecordRepository = workRecordRepository;
        this.workRecordApprovalRepository = workRecordApprovalRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.categoryHoursValidationService = categoryHoursValidationService;
        this.workRecordStatusService = workRecordStatusService;
    }
    
    /**
     * 工数記録を作成
     * 
     * @param userId ユーザーID
     * @param projectId プロジェクトID
     * @param workDate 作業日
     * @param categoryHours カテゴリ別工数
     * @param description 作業内容・備考
     * @return 作成された工数記録
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException ビジネスルール違反の場合
     */
    public WorkRecord saveWorkRecord(String userId, String projectId, LocalDate workDate,
                                     CategoryHours categoryHours, String description, String createdBy) {
        // ユーザー存在チェック
        userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        // プロジェクト存在チェック
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> EntityNotFoundException.projectNotFound(projectId));
        
        // プロジェクトが工数記録可能な状態かチェック
        if (!project.canRecordWorkHours()) {
            throw new IllegalStateException("プロジェクトは工数記録できない状態です: " + project.getStatus());
        }

        // 承認済みデータの更新制限チェック
        WorkRecordApproval approval = workRecordApprovalRepository
            .findByUserIdAndDate(userId, workDate)
            .orElse(null);
        if (approval != null && approval.isApproved()) {
            throw new IllegalStateException("承認済みの工数記録は更新できません");
        }

        categoryHoursValidationService.validate(categoryHours);
        
        // 既存の工数記録を取得、なければ新規作成
        Optional<WorkRecord> existingRecord = workRecordRepository.findByUserIdAndDateAndProjectId(userId, workDate, projectId);
        WorkRecord workRecord;
        if (existingRecord.isPresent()) {
            workRecord = existingRecord.get();
            // 承認状態チェックは日次承認レベルで管理されるため、ここでは期間チェックのみ
            workRecord.updateWorkRecord(categoryHours, description, createdBy);
        } else {
            workRecord = WorkRecord.create(userId, projectId, workDate, categoryHours, description, createdBy);
        }
        
        workRecord.setProject(project);

        // 保存
        return workRecordRepository.save(workRecord);
    }
    
    public void removeWorkRecords(String userId, LocalDate date , List<String> recordIds) {
        
        // 承認済みデータの更新制限チェック
        WorkRecordApproval approval = workRecordApprovalRepository
            .findByUserIdAndDate(userId, date)
            .orElse(null);
        if (approval != null && approval.isApproved()) {
            throw new IllegalStateException("承認済みの工数記録は削除できません");
        }
        
        // 削除
        workRecordRepository.deleteByUserIdAndDateAndRecordIds(userId, date, recordIds);
        workRecordApprovalRepository.delete(userId, date);
    }

    /**
     * 工数記録を作成
     * 
     * @param userId ユーザーID
     * @param projectId プロジェクトID
     * @param workDate 作業日
     * @param categoryHours カテゴリ別工数
     * @param description 作業内容・備考
     * @return 作成された工数記録
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException ビジネスルール違反の場合
     */
    @Transactional
    public WorkRecordsResponse saveWorkRecords(String userId, LocalDate date, WorkRecordSaveRequest request) {
        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new InvalidParameterException("工数記録が空です");
        }

        if (request.getDeletedRecordIds() != null && !request.getDeletedRecordIds().isEmpty()) {
            removeWorkRecords(userId, date, request.getDeletedRecordIds());
        }
        
        // 承認済みデータの更新制限チェック
        WorkRecordApproval workRecordApproval = 
            workRecordApprovalRepository.findByUserIdAndDate(userId, date)
                                        .orElse(new WorkRecordApproval(userId, date));
        if (workRecordApproval.isApproved()) {
            throw new IllegalStateException("承認済みの工数記録は更新できません");
        }
        workRecordApproval.makePending();
        workRecordApprovalRepository.save(workRecordApproval);

        List<WorkRecord> results = new ArrayList<>();
        request.getRecords().forEach(record -> {
            String projectId = record.getProjectId();
            CategoryHours categoryHours = record.getCategoryHours();
            String description = record.getDescription();

            // 工数記録を保存
            results.add(saveWorkRecord(userId, projectId, date, categoryHours, description, userId));
        });

        return WorkRecordsResponse.builder()
            .workRecords(results)
            .workRecordApproval(workRecordApproval)
            .build();
    }
    

    
    // === 検索メソッド ===
    
    /**
     * ユーザーIDと日付で工数記録を検索
     * 
     * @param userId ユーザーID
     * @param workDate 作業日
     * @return 工数記録リスト
     */
    @Transactional(readOnly = true)
    public List<WorkRecord> findByUserIdAndDate(String userId, LocalDate workDate) {
        return workRecordRepository.findByUserIdAndDate(userId, workDate);
    }
    
    
    
    /**
     * ユーザーの期間指定工数記録を取得
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 工数記録リスト
     */
    @Transactional(readOnly = true)
    public List<WorkRecord> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    
    /**
     * 単一日付の工数記録と承認ステータスを取得
     * 
     * @param userId ユーザーID
     * @param date 対象日付
     * @return 工数記録と承認ステータスを含むレスポンス
     */
    @Transactional(readOnly = true)
    public WorkRecordsResponse getWorkRecordsWithApprovalStatus(String userId, LocalDate date) {
        // 工数記録取得
        List<WorkRecord> workRecords = findByUserIdAndDate(userId, date);
        
        // 承認ステータス取得（存在しない場合は新規作成）
        WorkRecordApproval approval = workRecordApprovalRepository
            .findByUserIdAndDate(userId, date)
            .orElseGet(() -> new WorkRecordApproval(userId, date));
        
        workRecords.stream().forEach(workrecord -> {
            Project project = projectRepository.findById(workrecord.getProjectId())
                .orElseThrow(() -> EntityNotFoundException.projectNotFound(workrecord.getProjectId()));
            workrecord.setProject(project);
        });
        
        // レスポンスオブジェクト構築
        return WorkRecordsResponse.builder()
            .workRecords(workRecords)
            .workRecordApproval(approval)
            .build();
    }
    
    /**
     * 開発者の工数集計レポート取得
     * 
     * 指定期間の開発者の工数を日別・プロジェクト別・カテゴリ別で集計
     * 開発者ダッシュボードで使用
     * 
     * @param userId 開発者のユーザーID
     * @param startDate 集計開始日
     * @param endDate 集計終了日
     * @return 工数集計レポート
     */
    @Transactional(readOnly = true)
    public WorkHoursSummaryResponse generateWorkHoursSummary(String userId, LocalDate startDate, LocalDate endDate) {
        // ユーザー情報取得
        User user = userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
            
        // 指定期間の工数記録を取得
        List<WorkRecord> workRecords = workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        
        if (workRecords.isEmpty()) {
            return createEmptySummary(userId, user.getFullName(), startDate, endDate);
        }
        
        // 総工数計算
        BigDecimal totalHours = calculateTotalHours(workRecords);
        
        // 稼働日数計算（工数記録がある日の数）
        int totalDays = (int) workRecords.stream()
            .map(WorkRecord::getWorkDate)
            .distinct()
            .count();
            
        // 日平均工数計算
        BigDecimal averageHoursPerDay = totalDays > 0 
            ? totalHours.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
            
        // プロジェクト別集計
        Map<String, BigDecimal> projectHours = calculateProjectHours(workRecords);
        
        // カテゴリ別集計
        Map<String, BigDecimal> categoryHours = calculateCategoryHours(workRecords);
        
        // 日別集計
        Map<LocalDate, BigDecimal> dailyHours = calculateDailyHours(workRecords);
        
        // 週別集計
        List<WorkHoursSummaryResponse.WeeklySummary> weeklySummaries = calculateWeeklySummaries(workRecords, startDate, endDate);
        
        return new WorkHoursSummaryResponse(
            userId,
            user.getFullName(),
            startDate,
            endDate,
            totalHours,
            totalDays,
            averageHoursPerDay,
            projectHours,
            categoryHours,
            dailyHours,
            weeklySummaries
        );
    }
    
    private WorkHoursSummaryResponse createEmptySummary(String userId, String userFullName, 
                                                       LocalDate startDate, LocalDate endDate) {
        return new WorkHoursSummaryResponse(
            userId,
            userFullName,
            startDate,
            endDate,
            BigDecimal.ZERO,
            0,
            BigDecimal.ZERO,
            Map.of(),
            Map.of(),
            Map.of(),
            new ArrayList<>()
        );
    }
    
    private BigDecimal calculateTotalHours(List<WorkRecord> workRecords) {
        return workRecords.stream()
            .map(record -> record.getCategoryHours().getTotalHours())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Map<String, BigDecimal> calculateProjectHours(List<WorkRecord> workRecords) {
        Map<String, BigDecimal> projectHoursMap = new LinkedHashMap<>();
        
        for (WorkRecord record : workRecords) {
            // プロジェクト情報を取得
            Optional<Project> project = projectRepository.findById(record.getProjectId());
            String projectName = project.map(Project::getName).orElse("Unknown Project");
            
            BigDecimal hours = record.getCategoryHours().getTotalHours();
            projectHoursMap.merge(projectName, hours, BigDecimal::add);
        }
        
        return projectHoursMap;
    }
    
    private Map<String, BigDecimal> calculateCategoryHours(List<WorkRecord> workRecords) {
        Map<String, BigDecimal> categoryHoursMap = new LinkedHashMap<>();
        
        for (WorkRecord record : workRecords) {
            CategoryHours categoryHours = record.getCategoryHours();
            categoryHours.hours().forEach((categoryCode, hours) -> {
                categoryHoursMap.merge(categoryCode.value(), hours, BigDecimal::add);
            });
        }
        
        return categoryHoursMap;
    }
    
    private Map<LocalDate, BigDecimal> calculateDailyHours(List<WorkRecord> workRecords) {
        return workRecords.stream()
            .collect(Collectors.groupingBy(
                WorkRecord::getWorkDate,
                LinkedHashMap::new,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    record -> record.getCategoryHours().getTotalHours(),
                    BigDecimal::add
                )
            ));
    }
    
    private List<WorkHoursSummaryResponse.WeeklySummary> calculateWeeklySummaries(
            List<WorkRecord> workRecords, LocalDate startDate, LocalDate endDate) {
        
        List<WorkHoursSummaryResponse.WeeklySummary> weeklySummaries = new ArrayList<>();
        
        // 週の開始日（月曜日）を取得
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        while (!weekStart.isAfter(endDate)) {
            final LocalDate finalWeekStart = weekStart;
            final LocalDate weekEnd = weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            
            // この週の工数記録を抽出
            List<WorkRecord> weeklyRecords = workRecords.stream()
                .filter(record -> {
                    LocalDate workDate = record.getWorkDate();
                    return !workDate.isBefore(finalWeekStart) && !workDate.isAfter(weekEnd) &&
                           !workDate.isBefore(startDate) && !workDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
            
            if (!weeklyRecords.isEmpty()) {
                BigDecimal weeklyTotal = calculateTotalHours(weeklyRecords);
                int workingDays = (int) weeklyRecords.stream()
                    .map(WorkRecord::getWorkDate)
                    .distinct()
                    .count();
                BigDecimal weeklyAverage = workingDays > 0 
                    ? weeklyTotal.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                    
                weeklySummaries.add(new WorkHoursSummaryResponse.WeeklySummary(
                    finalWeekStart,
                    weekEnd,
                    weeklyTotal,
                    workingDays,
                    weeklyAverage
                ));
            }
            
            weekStart = weekStart.plusWeeks(1);
        }
        
        return weeklySummaries;
    }

    /**
     * 指定された月の未入力日を取得
     * 
     * @param userId ユーザーID
     * @param year 年
     * @param month 月（1-12）
     * @return 未入力日のリスト
     * @throws InvalidParameterException パラメータが不正な場合
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getMissingDatesForMonth(String userId, int year, int month) {
        // パラメータ検証
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidParameterException("ユーザーIDは必須です");
        }
        
        // LocalDateで日付の妥当性をチェック
        LocalDate startDate;
        try {
            startDate = LocalDate.of(year, month, 1);
        } catch (Exception e) {
            throw new InvalidParameterException("無効な年月が指定されました: " + year + "年" + month + "月", e);
        }
        
        // 日付の期間制限（3ヶ月以内）
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsAgo = now.minusMonths(3);
        
        if (startDate.isBefore(threeMonthsAgo.withDayOfMonth(1)) || startDate.isAfter(now.withDayOfMonth(1))) {
            throw new InvalidParameterException("指定された月は過去3ヶ月以内から当月までの範囲で入力してください");
        }

        // 指定月の終了日を計算
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // WorkRecordStatusServiceを使用して未入力日を取得
        return workRecordStatusService.findMissingDates(userId, startDate, endDate);
    }
    
    /**
     * 指定された月の各日付のステータス情報を取得
     * 
     * @param userId ユーザーID
     * @param year 年
     * @param month 月（1-12）
     * @return 日付毎のステータス情報
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, DateStatusResponse.DateStatus> getDateStatusesForMonth(String userId, int year, int month) {
        // パラメータ検証
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidParameterException("ユーザーIDは必須です");
        }
        
        // LocalDateで日付の妥当性をチェック
        LocalDate startDate;
        try {
            startDate = LocalDate.of(year, month, 1);
        } catch (Exception e) {
            throw new InvalidParameterException("無効な年月が指定されました: " + year + "年" + month + "月", e);
        }
        
        // 日付の期間制限（3ヶ月以内）
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsAgo = now.minusMonths(3);
        
        if (startDate.isBefore(threeMonthsAgo.withDayOfMonth(1)) || startDate.isAfter(now.withDayOfMonth(1))) {
            throw new InvalidParameterException("指定された月は過去3ヶ月以内から当月までの範囲で入力してください");
        }

        // 指定月の終了日を計算
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // 指定期間の全ての工数記録を取得
        List<WorkRecord> workRecords = workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // 承認情報を取得
        List<WorkRecordApproval> approvals = workRecordApprovalRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        Map<LocalDate, WorkRecordApproval> approvalMap = approvals.stream()
            .collect(Collectors.toMap(WorkRecordApproval::getWorkDate, approval -> approval));
        
        Map<LocalDate, DateStatusResponse.DateStatus> result = new LinkedHashMap<>();
        
        // 月の各日をループ
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            
            // その日の工数記録があるかチェック
            List<WorkRecord> dayRecords = workRecords.stream()
                .filter(record -> record.getWorkDate().equals(currentDate))
                .collect(Collectors.toList());
            
            if (dayRecords.isEmpty()) {
                // 工数記録がない場合
                result.put(currentDate, DateStatusResponse.DateStatus.empty());
            } else {
                // 工数記録がある場合
                double totalHours = dayRecords.stream()
                    .mapToDouble(record -> {
                        CategoryHours categoryHours = record.getCategoryHours();
                        return categoryHours != null ? categoryHours.getTotalHours().doubleValue() : 0.0;
                    })
                    .sum();
                
                WorkRecordApproval approval = approvalMap.get(currentDate);
                ApprovalStatus approvalStatus = approval != null ? approval.getApprovalStatus() : ApprovalStatus.NOT_ENTERED;
                
                result.put(currentDate, DateStatusResponse.DateStatus.withRecord(approvalStatus, totalHours));
            }
        }
        
        return result;
    }
}