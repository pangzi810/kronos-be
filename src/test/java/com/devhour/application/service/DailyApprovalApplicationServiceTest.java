package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.application.service.DailyApprovalApplicationService.*;
import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.exception.UnauthorizedException;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.repository.DomainEventRepository;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.repository.WorkRecordApprovalRepository;
import com.devhour.domain.repository.WorkRecordRepository;
import com.devhour.domain.service.ApprovalAuthorityValidationService;
import com.devhour.domain.service.ListApproverDomainService;

/**
 * DailyApprovalApplicationServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DailyApprovalApplicationService")
class DailyApprovalApplicationServiceTest {
    
    @Mock
    private WorkRecordApprovalRepository workRecordApprovalRepository;
    
    @Mock
    private ApprovalAuthorityValidationService approvalAuthorityService;
    
    @Mock
    private ListApproverDomainService listApproverService;
    
    @Mock
    private DomainEventRepository domainEventRepository;
    
    @Mock
    private WorkRecordRepository workRecordRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private DailyApprovalApplicationService service;
    
    private User testUser;
    private User approverUser;
    private WorkRecordApproval testApproval;
    private WorkRecord testWorkRecord;
    private Project testProject;
    private LocalDate testWorkDate;
    
    @BeforeEach
    void setUp() {
        // テストユーザーの作成
        testUser = User.create("test_user", "test@example.com", 
            "テストユーザー");
        
        // 承認者ユーザーの作成
        approverUser = User.create("approver_user", "approver@example.com",
            "承認者");
        
        // テスト日付
        testWorkDate = LocalDate.now();
        
        // テスト用承認レコード
        testApproval = new WorkRecordApproval(testUser.getId(), testWorkDate);
        
        // テストプロジェクト
        testProject = Project.create("テストプロジェクト", "説明",
            LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), "pmo_user");
        
        // テスト用工数記録
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        hoursMap.put(new CategoryCode("DEV"), new BigDecimal("8.0"));
        CategoryHours categoryHours = new CategoryHours(hoursMap);
        
        testWorkRecord = WorkRecord.create(
            testUser.getId(),
            testProject.getId(),
            testWorkDate,
            categoryHours,
            "テスト作業",
            testUser.getId()
        );
        testWorkRecord.setProject(testProject);
        testWorkRecord.setUser(testUser);
    }
    
    @Test
    @DisplayName("日次承認 - 正常ケース（新規承認）")
    void approveDaily_Success_NewApproval() {
        // Arrange
        String userId = testUser.getId();
        String approverId = approverUser.getId();
        
        when(approvalAuthorityService.validateAuthorityForDate(approverId, userId, testWorkDate))
            .thenReturn(true);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Optional.empty());
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(workRecordRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        doNothing().when(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
        
        // Act
        ApprovalResult result = service.approveDaily(userId, testWorkDate, approverId);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        assertEquals("APPROVED", result.getStatus());
        assertEquals(approverId, result.getApproverId());
        assertNull(result.getError());
        
        verify(approvalAuthorityService).validateAuthorityForDate(approverId, userId, testWorkDate);
        verify(workRecordApprovalRepository).save(any(WorkRecordApproval.class));
        verify(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
    }
    
    @Test
    @DisplayName("日次承認 - 正常ケース（既存承認更新）")
    void approveDaily_Success_ExistingApproval() {
        // Arrange
        String userId = testUser.getId();
        String approverId = approverUser.getId();
        
        when(approvalAuthorityService.validateAuthorityForDate(approverId, userId, testWorkDate))
            .thenReturn(true);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Optional.of(testApproval));
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(workRecordRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        doNothing().when(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
        
        // Act
        ApprovalResult result = service.approveDaily(userId, testWorkDate, approverId);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, testWorkDate);
        verify(workRecordApprovalRepository).save(testApproval);
    }
    
    @Test
    @DisplayName("日次承認 - 権限なしエラー")
    void approveDaily_UnauthorizedError() {
        // Arrange
        String userId = testUser.getId();
        String approverId = "unauthorized_user";
        
        when(approvalAuthorityService.validateAuthorityForDate(approverId, userId, testWorkDate))
            .thenReturn(false);
        
        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> service.approveDaily(userId, testWorkDate, approverId)
        );
        
        assertEquals("承認権限がありません", exception.getMessage());
        verify(approvalAuthorityService).validateAuthorityForDate(approverId, userId, testWorkDate);
        verifyNoInteractions(workRecordApprovalRepository);
    }
    
    @Test
    @DisplayName("日次差し戻し - 正常ケース")
    void rejectDaily_Success() {
        // Arrange
        String userId = testUser.getId();
        String approverId = approverUser.getId();
        String reason = "修正が必要です";
        
        when(approvalAuthorityService.validateAuthorityForDate(approverId, userId, testWorkDate))
            .thenReturn(true);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Optional.of(testApproval));
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(workRecordRepository.findByUserIdAndDate(userId, testWorkDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        doNothing().when(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
        
        // Act
        ApprovalResult result = service.rejectDaily(userId, testWorkDate, approverId, reason);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        
        verify(approvalAuthorityService).validateAuthorityForDate(approverId, userId, testWorkDate);
        verify(workRecordApprovalRepository).save(testApproval);
        verify(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
    }
    
    @Test
    @DisplayName("日次差し戻し - 権限なしエラー")
    void rejectDaily_UnauthorizedError() {
        // Arrange
        String userId = testUser.getId();
        String approverId = "unauthorized_user";
        String reason = "修正が必要です";
        
        when(approvalAuthorityService.validateAuthorityForDate(approverId, userId, testWorkDate))
            .thenReturn(false);
        
        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> service.rejectDaily(userId, testWorkDate, approverId, reason)
        );
        
        assertEquals("承認権限がありません", exception.getMessage());
        verify(approvalAuthorityService).validateAuthorityForDate(approverId, userId, testWorkDate);
        verifyNoInteractions(workRecordApprovalRepository);
    }
    
    @Test
    @DisplayName("一括承認処理 - 正常ケース")
    void approveBatch_Success() {
        // Arrange
        String approverId = approverUser.getId();
        List<ApprovalRequest> requests = Arrays.asList(
            new ApprovalRequest("user1", LocalDate.now()),
            new ApprovalRequest("user2", LocalDate.now().minusDays(1))
        );
        
        when(approvalAuthorityService.validateAuthorityForDate(eq(approverId), anyString(), any(LocalDate.class)))
            .thenReturn(true);
        when(workRecordApprovalRepository.findByUserIdAndDate(anyString(), any(LocalDate.class)))
            .thenReturn(Optional.empty());
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(workRecordRepository.findByUserIdAndDate(anyString(), any(LocalDate.class)))
            .thenReturn(Arrays.asList(testWorkRecord));
        doNothing().when(domainEventRepository).save(any(WorkRecordApprovalEvent.class));
        
        // Act
        List<ApprovalResult> results = service.approveBatch(requests, approverId);
        
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("user1", results.get(0).getUserId());
        assertEquals("user2", results.get(1).getUserId());
        
        verify(approvalAuthorityService, times(2)).validateAuthorityForDate(eq(approverId), anyString(), any(LocalDate.class));
        verify(workRecordApprovalRepository, times(2)).save(any(WorkRecordApproval.class));
    }
    
    @Test
    @DisplayName("一括承認処理 - 部分的エラー")
    void approveBatch_PartialError() {
        // Arrange
        String approverId = approverUser.getId();
        List<ApprovalRequest> requests = Arrays.asList(
            new ApprovalRequest("user1", LocalDate.now()),
            new ApprovalRequest("user2", LocalDate.now().minusDays(1))
        );
        
        // 最初のリクエストは成功、2番目は権限エラー
        when(approvalAuthorityService.validateAuthorityForDate(approverId, "user1", requests.get(0).getWorkDate()))
            .thenReturn(true);
        when(approvalAuthorityService.validateAuthorityForDate(approverId, "user2", requests.get(1).getWorkDate()))
            .thenReturn(false);
        when(workRecordApprovalRepository.findByUserIdAndDate("user1", requests.get(0).getWorkDate()))
            .thenReturn(Optional.empty());
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(workRecordRepository.findByUserIdAndDate("user1", requests.get(0).getWorkDate()))
            .thenReturn(Arrays.asList(testWorkRecord));
        
        // Act
        List<ApprovalResult> results = service.approveBatch(requests, approverId);
        
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertNull(results.get(0).getError());
        assertEquals("ERROR", results.get(1).getStatus());
        assertNotNull(results.get(1).getError());
    }
    
    @Test
    @DisplayName("承認待ち一覧を取得 - 正常ケース")
    void getPendingAggregatedApprovals_Success() {
        // Arrange
        String approverId = approverUser.getId();
        List<String> subordinateIds = Arrays.asList(testUser.getId());
        List<ApprovalStatus> targetStatuses = Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.REJECTED);
        
        when(userRepository.findById(approverId))
            .thenReturn(Optional.of(approverUser));
        when(listApproverService.findApprovalTargetsByApprover(approverUser))
            .thenReturn(Arrays.asList(testUser));
        when(workRecordApprovalRepository.findByUsersAndStatuses(subordinateIds, targetStatuses))
            .thenReturn(Arrays.asList(testApproval));
        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDate(testUser.getId(), testWorkDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        
        // Act
        List<AggregatedApproval> result = service.getPendingAggregatedApprovals(approverId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(testWorkDate, result.get(0).getWorkDate());
        assertNotNull(result.get(0).getCategoryHours());
        assertNotNull(result.get(0).getProjectBreakdowns());
        
        verify(listApproverService).findApprovalTargetsByApprover(approverUser);
        verify(workRecordApprovalRepository).findByUsersAndStatuses(subordinateIds, targetStatuses);
    }
    
    @Test
    @DisplayName("承認待ち一覧を取得 - 部下がいない場合")
    void getPendingAggregatedApprovals_NoSubordinates() {
        // Arrange
        String approverId = approverUser.getId();
        
        when(userRepository.findById(approverId))
            .thenReturn(Optional.of(approverUser));
        when(listApproverService.findApprovalTargetsByApprover(approverUser))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<AggregatedApproval> result = service.getPendingAggregatedApprovals(approverId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(listApproverService).findApprovalTargetsByApprover(approverUser);
        verifyNoInteractions(workRecordApprovalRepository);
    }
    
    @Test
    @DisplayName("ApprovalResultクラス - 成功時のコンストラクタ")
    void approvalResult_SuccessConstructor() {
        // Arrange
        testApproval.approve(approverUser.getId());
        
        // Act
        ApprovalResult result = new ApprovalResult(testApproval);
        
        // Assert
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        assertEquals("APPROVED", result.getStatus());
        assertEquals(approverUser.getId(), result.getApproverId());
        assertNotNull(result.getApprovedAt());
        assertNull(result.getError());
    }
    
    @Test
    @DisplayName("ApprovalResultクラス - エラー時のコンストラクタ")
    void approvalResult_ErrorConstructor() {
        // Arrange
        String errorMessage = "承認エラー";
        
        // Act
        ApprovalResult result = new ApprovalResult(testUser.getId(), testWorkDate, errorMessage);
        
        // Assert
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getApproverId());
        assertNull(result.getApprovedAt());
        assertEquals(errorMessage, result.getError());
    }
    
    @Test
    @DisplayName("AggregatedApproval.create - 正常ケース")
    void aggregatedApproval_Create_Success() {
        // Arrange
        List<WorkRecord> workRecords = Arrays.asList(testWorkRecord);
        
        // Act
        AggregatedApproval result = AggregatedApproval.create(testApproval, workRecords, testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testWorkDate, result.getWorkDate());
        assertEquals("PENDING", result.getApprovalStatus());
        assertNotNull(result.getCategoryHours());
        assertEquals(new BigDecimal("8.0"), result.getTotalHours());
        assertEquals(1, result.getProjectBreakdowns().size());
    }
    
    @Test
    @DisplayName("AggregatedApproval.create - 工数記録が空の場合")
    void aggregatedApproval_Create_EmptyWorkRecords() {
        // Arrange
        List<WorkRecord> emptyWorkRecords = Collections.emptyList();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AggregatedApproval.create(testApproval, emptyWorkRecords, testUser)
        );
        
        assertEquals("工数記録リストが空です", exception.getMessage());
    }
    
    @Test
    @DisplayName("AggregatedApproval.create - 承認レコードがnullの場合")
    void aggregatedApproval_Create_NullApproval() {
        // Arrange
        List<WorkRecord> workRecords = Arrays.asList(testWorkRecord);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AggregatedApproval.create(null, workRecords, testUser)
        );
        
        assertEquals("承認レコードがnullです", exception.getMessage());
    }
    
    @Test
    @DisplayName("ProjectBreakdownクラスのテスト")
    void projectBreakdown_Test() {
        // Arrange
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        hoursMap.put(new CategoryCode("DEV"), new BigDecimal("4.0"));
        hoursMap.put(new CategoryCode("TEST"), new BigDecimal("2.0"));
        CategoryHours categoryHours = new CategoryHours(hoursMap);
        
        // Act
        ProjectBreakdown breakdown = new ProjectBreakdown(
            testProject.getId(),
            testProject,
            categoryHours,
            new BigDecimal("6.0"),
            "作業内容"
        );
        
        // Assert
        assertEquals(testProject.getId(), breakdown.getProjectId());
        assertEquals(testProject, breakdown.getProject());
        assertEquals(categoryHours, breakdown.getCategoryHours());
        assertEquals(new BigDecimal("6.0"), breakdown.getTotalHours());
        assertEquals("作業内容", breakdown.getDescription());
    }
}