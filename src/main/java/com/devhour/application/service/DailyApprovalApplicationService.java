package com.devhour.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.exception.UnauthorizedException;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.repository.DomainEventRepository;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.repository.WorkRecordApprovalRepository;
import com.devhour.domain.repository.WorkRecordRepository;
import com.devhour.domain.service.ApprovalAuthorityValidationService;
import com.devhour.domain.service.ListApproverDomainService;
import lombok.RequiredArgsConstructor;

/**
 * 日次承認アプリケーションサービス
 * 申請者/日付単位での承認処理を管理
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DailyApprovalApplicationService {
    
    private final WorkRecordApprovalRepository workRecordApprovalRepository;
    private final ApprovalAuthorityValidationService approvalAuthorityService;
    private final ListApproverDomainService listApproverService;
    private final DomainEventRepository domainEventRepository;
    private final WorkRecordRepository workRecordRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    
    /**
     * 申請者/日付単位で承認
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     * @param approverId 承認者ID
     * @return 承認結果
     */
    public ApprovalResult approveDaily(String userId, LocalDate workDate, String approverId) {
        // 権限検証
        if (!approvalAuthorityService.validateAuthorityForDate(approverId, userId, workDate)) {
            throw new UnauthorizedException("承認権限がありません");
        }
        
        // 承認レコード取得または作成
        WorkRecordApproval approval = workRecordApprovalRepository
            .findByUserIdAndDate(userId, workDate)
            .orElseGet(() -> new WorkRecordApproval(userId, workDate));
        
        // 承認実行
        approval.approve(approverId);
        WorkRecordApproval saved = workRecordApprovalRepository.save(approval);
        
        // 対象工数記録を取得
        List<WorkRecord> targetWorkRecords = workRecordRepository.findByUserIdAndDate(userId, workDate);
        
        // ドメインイベント発行
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(
            saved,
            targetWorkRecords
        );
        domainEventRepository.save(event);
        
        return new ApprovalResult(saved);
    }
    
    /**
     * 申請者/日付単位で差し戻し
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     * @param approverId 承認者ID
     * @param reason 却下理由
     * @return 承認結果
     */
    public ApprovalResult rejectDaily(String userId, LocalDate workDate, String approverId, String reason) {
        // 権限検証
        if (!approvalAuthorityService.validateAuthorityForDate(approverId, userId, workDate)) {
            throw new UnauthorizedException("承認権限がありません");
        }
        
        // 承認レコード取得または作成
        WorkRecordApproval approval = workRecordApprovalRepository
            .findByUserIdAndDate(userId, workDate)
            .orElseGet(() -> new WorkRecordApproval(userId, workDate));
        
        // 差し戻し実行
        approval.reject(approverId, reason);
        WorkRecordApproval saved = workRecordApprovalRepository.save(approval);
        
        // 対象工数記録を取得
        List<WorkRecord> targetWorkRecords = workRecordRepository.findByUserIdAndDate(userId, workDate);
        
        // ドメインイベント発行
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForRejection(
            saved,
            targetWorkRecords
        );
        domainEventRepository.save(event);
        
        return new ApprovalResult(saved);
    }
    
    /**
     * 一括承認処理
     * 
     * @param requests 承認リクエストリスト
     * @param approverId 承認者ID
     * @return 承認結果リスト
     */
    public List<ApprovalResult> approveBatch(List<ApprovalRequest> requests, String approverId) {
        List<ApprovalResult> results = new ArrayList<>();
        
        for (ApprovalRequest request : requests) {
            try {
                ApprovalResult result = approveDaily(
                    request.getUserId(),
                    request.getWorkDate(),
                    approverId
                );
                results.add(result);
            } catch (Exception e) {
                // エラーの場合はスキップして続行
                results.add(new ApprovalResult(request.getUserId(), request.getWorkDate(), e.getMessage()));
            }
        }
        
        return results;
    }
    
        /**
     * 一括承認処理
     * 
     * @param requests 承認リクエストリスト
     * @param approverId 承認者ID
     * @return 承認結果リスト
     */
    public List<ApprovalResult> rejectBatch(String reason, List<ApprovalRequest> requests, String approverId) {
        List<ApprovalResult> results = new ArrayList<>();
        
        for (ApprovalRequest request : requests) {
            try {
                ApprovalResult result = rejectDaily(
                    request.getUserId(),
                    request.getWorkDate(),
                    approverId,
                    reason
                );
                results.add(result);
            } catch (Exception e) {
                // エラーの場合はスキップして続行
                results.add(new ApprovalResult(request.getUserId(), request.getWorkDate(), e.getMessage()));
            }
        }
        
        return results;
    }
    
    /**
     * 承認待ち一覧を取得（集計済み）
     * 
     * @param approverId 承認者ID
     * @return 集計済み承認待ち一覧
     */
    @Transactional(readOnly = true)
    public List<AggregatedApproval> getPendingAggregatedApprovals(String approverId) {
        // 承認者のユーザー情報を取得
        User approverUser = userRepository.findById(approverId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(approverId));
        
        // 承認対象者リストを取得
        List<User> approvalTargets = listApproverService.findApprovalTargetsByApprover(approverUser);
        
        if (approvalTargets.isEmpty()) {
            return Collections.emptyList();
        }
        
        // ユーザーIDのリストに変換
        List<String> approvalTargetIds = approvalTargets.stream()
            .map(User::getId)
            .collect(Collectors.toList());
        
        // 承認待ち・差し戻しステータスのレコードを取得
        List<ApprovalStatus> targetStatuses = Arrays.asList(
            ApprovalStatus.PENDING,
            ApprovalStatus.REJECTED
        );
        
        List<WorkRecordApproval> approvals = workRecordApprovalRepository
            .findByUsersAndStatuses(approvalTargetIds, targetStatuses);
        
        // 申請者/日付ごとに集計
        List<AggregatedApproval> result = approvals.stream()
            .map(approval -> {
                // ユーザー情報を取得
                User applicantUser = userRepository.findById(approval.getUserId())
                    .orElseThrow(() -> EntityNotFoundException.userNotFound(approval.getUserId()));

                // 対象の工数記録を取得
                List<WorkRecord> workRecords = workRecordRepository.findByUserIdAndDate(
                    approval.getUserId(),
                    approval.getWorkDate()
                );                
                    
                workRecords.forEach(workRecord -> {
                    // プロジェクトとユーザー情報を設定
                    Project project = projectRepository.findById(workRecord.getProjectId())
                        .orElseThrow(() -> EntityNotFoundException.projectNotFound(workRecord.getProjectId())  );
                    workRecord.setProject(project);
                    workRecord.setUser(applicantUser);
                });
                
                // ファクトリメソッドを使用してAggregatedApprovalを作成
                return AggregatedApproval.create(approval, workRecords, applicantUser);
            })
            .collect(Collectors.toList());
        
        // 日付の降順でソート
        result.sort((a, b) -> b.getWorkDate().compareTo(a.getWorkDate()));
        
        return result;
    }
    
    /**
     * 承認結果クラス
     */
    public static class ApprovalResult {
        private final String userId;
        private final LocalDate workDate;
        private final String status;
        private final String approverId;
        private final LocalDateTime approvedAt;
        private final String error;
        
        public ApprovalResult(WorkRecordApproval approval) {
            this.userId = approval.getUserId();
            this.workDate = approval.getWorkDate();
            this.status = approval.getApprovalStatus().getValue();
            this.approverId = approval.getApproverId();
            this.approvedAt = approval.getApprovedAt();
            this.error = null;
        }
        
        public ApprovalResult(String userId, LocalDate workDate, String error) {
            this.userId = userId;
            this.workDate = workDate;
            this.status = "ERROR";
            this.approverId = null;
            this.approvedAt = null;
            this.error = error;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public LocalDate getWorkDate() { return workDate; }
        public String getStatus() { return status; }
        public String getApproverId() { return approverId; }
        public LocalDateTime getApprovedAt() { return approvedAt; }
        public String getError() { return error; }
    }
    
    /**
     * 承認リクエストクラス
     */
    public static class ApprovalRequest {
        private final String userId;
        private final LocalDate workDate;

        @JsonCreator
        public ApprovalRequest(@JsonProperty("userId") String userId,
                              @JsonProperty("workDate") LocalDate workDate) {
            this.userId = userId;
            this.workDate = workDate;
        }

        public String getUserId() { return userId; }
        public LocalDate getWorkDate() { return workDate; }
    }
    
    /**
     * 集計済み承認情報
     */
    public static class AggregatedApproval {
        private final String userId;
        private final User user;
        private final LocalDate workDate;
        private final String approvalStatus;
        private final Map<String, BigDecimal> categoryHours;
        private final List<ProjectBreakdown> projectBreakdowns;
        private final String rejectionReason;
        
        public AggregatedApproval(User applicantUser, LocalDate workDate, String approvalStatus,
                                 Map<String, BigDecimal> categoryHours,
                                 List<ProjectBreakdown> projectBreakdowns,
                                 String rejectionReason) {
            this.userId = applicantUser.getId();
            this.user = applicantUser;
            this.workDate = workDate;
            this.approvalStatus = approvalStatus;
            this.categoryHours = categoryHours;
            this.projectBreakdowns = projectBreakdowns;
            this.rejectionReason = rejectionReason;
        }
        
        /**
         * WorkRecordApprovalとWorkRecordのリストからAggregatedApprovalを作成するファクトリメソッド
         * 
         * @param approval 承認レコード
         * @param workRecords 工数記録リスト
         * @return 集計済み承認情報
         */
        public static AggregatedApproval create(WorkRecordApproval approval, 
                                               List<WorkRecord> workRecords,
                                               User applicantUser) {
            if (workRecords.isEmpty()) {
                throw new IllegalArgumentException("工数記録リストが空です");
            }
            
            if (approval == null) {
                throw new IllegalArgumentException("承認レコードがnullです");
            }


            // カテゴリ別工数を集計
            Map<String, BigDecimal> categoryHours = aggregateByCategory(workRecords);
            
            // 案件ごとの内訳を取得
            List<ProjectBreakdown> breakdowns = getProjectBreakdown(workRecords);
            
            return new AggregatedApproval(
                applicantUser,
                approval.getWorkDate(),
                approval.getApprovalStatus().getValue(),
                categoryHours,
                breakdowns,
                approval.getRejectionReason()
            );
        }
        
        /**
         * カテゴリ別工数を集計
         * 
         * @param workRecords 工数記録リスト
         * @return カテゴリ別工数マップ
         */
        private static Map<String, BigDecimal> aggregateByCategory(List<WorkRecord> workRecords) {
            List<CategoryHours> categoryHoursList = workRecords.stream()
                .map(WorkRecord::getCategoryHours)
                .collect(Collectors.toList());
            
            return CategoryHours.aggregateByCategory(categoryHoursList);
        }
        
        /**
         * 案件ごとの内訳を取得
         * 
         * @param workRecords 工数記録リスト
         * @return 案件内訳リスト
         */
        private static List<ProjectBreakdown> getProjectBreakdown(List<WorkRecord> workRecords) {
            return workRecords.stream()
                .map(record -> new ProjectBreakdown(
                    record.getProject().getId(),
                    record.getProject(),
                    record.getCategoryHours(),
                    record.getCategoryHours() != null ? record.getCategoryHours().getTotalHours() : BigDecimal.ZERO,
                    record.getDescription()
                ))
                .collect(Collectors.toList());
        }
        
        // Getters
        public String getUserId() { return userId; }
        public LocalDate getWorkDate() { return workDate; }
        public String getApprovalStatus() { return approvalStatus; }
        public Map<String, BigDecimal> getCategoryHours() { return categoryHours; }
        public List<ProjectBreakdown> getProjectBreakdowns() { return projectBreakdowns; }
        public String getRejectionReason() { return rejectionReason; }
        
        public BigDecimal getTotalHours() {
            return categoryHours.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public User getUser() { return user; }
    }
    
    /**
     * 案件内訳
     */
    public static class ProjectBreakdown {
        private final String projectId;
        private final Project project;
        private final CategoryHours categoryHours;
        private final BigDecimal totalHours;
        private final String description;
        
        public ProjectBreakdown(String projectId, Project project, CategoryHours categoryHours,
                               BigDecimal totalHours, String description) {
            this.projectId = projectId;
            this.project = project;
            this.categoryHours = categoryHours;
            this.totalHours = totalHours;
            this.description = description;
        }
        
        // Getters
        public String getProjectId() { return projectId; }
        public CategoryHours getCategoryHours() { return categoryHours; }
        public BigDecimal getTotalHours() { return totalHours; }
        public String getDescription() { return description; }

        public Project getProject() { return project; }
    }
    
}