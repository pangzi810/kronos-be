package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.exception.InvalidParameterException;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.model.valueobject.ProjectStatus;
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
 * WorkRecordApplicationServiceのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkRecordApplicationService")
class WorkRecordApplicationServiceTest {

    @Mock
    private WorkRecordRepository workRecordRepository;
    
    @Mock
    private WorkRecordApprovalRepository workRecordApprovalRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private CategoryHoursValidationService categoryHoursValidationService;
    
    @Mock
    private WorkRecordStatusService workRecordStatusService;
    
    @InjectMocks
    private WorkRecordApplicationService service;
    
    private User testUser;
    private Project testProject;
    private WorkRecord testWorkRecord;
    private CategoryHours testCategoryHours;
    private LocalDate testWorkDate;

    @BeforeEach
    void setUp() {
        // テストユーザーの作成
        testUser = User.create("test_user", "test@example.com", "テストユーザー");
        
        // テストプロジェクトの作成
        testProject = Project.create("テストプロジェクト", "説明", 
            LocalDate.now(), LocalDate.now().plusDays(30), "pmo_user");
        
        // テスト用の作業日
        testWorkDate = LocalDate.now();
        
        // テスト用のカテゴリ別工数
        Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
        hoursMap.put(new CategoryCode("DEV"), new BigDecimal("8.0"));
        testCategoryHours = new CategoryHours(hoursMap);
        
        // テスト工数記録の作成
        testWorkRecord = WorkRecord.create(
            testUser.getId(),
            testProject.getId(),
            testWorkDate,
            testCategoryHours,
            "テスト作業",
            testUser.getId()
        );
    }

    @Test
    @DisplayName("工数記録作成 - 正常ケース（新規作成）")
    void saveWorkRecord_NewRecord_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(testUser.getId(), testWorkDate, testProject.getId()))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenReturn(testWorkRecord);

        // Act
        WorkRecord result = service.saveWorkRecord(
            testUser.getId(),
            testProject.getId(),
            testWorkDate,
            testCategoryHours,
            "テスト作業",
            testUser.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testProject.getId(), result.getProjectId());
        assertEquals(testWorkDate, result.getWorkDate());
        
        verify(userRepository).findById(testUser.getId());
        verify(projectRepository).findById(testProject.getId());
        verify(workRecordRepository).findByUserIdAndDateAndProjectId(testUser.getId(), testWorkDate, testProject.getId());
        verify(workRecordRepository).save(any(WorkRecord.class));
    }

    @Test
    @DisplayName("工数記録作成 - 正常ケース（既存記録更新）")
    void saveWorkRecord_UpdateExisting_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(testUser.getId(), testWorkDate, testProject.getId()))
            .thenReturn(Optional.of(testWorkRecord));
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenReturn(testWorkRecord);

        // Act
        WorkRecord result = service.saveWorkRecord(
            testUser.getId(),
            testProject.getId(),
            testWorkDate,
            testCategoryHours,
            "更新された作業",
            testUser.getId()
        );

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(projectRepository).findById(testProject.getId());
        verify(workRecordRepository).findByUserIdAndDateAndProjectId(testUser.getId(), testWorkDate, testProject.getId());
        verify(workRecordRepository).save(testWorkRecord);
    }

    @Test
    @DisplayName("工数記録作成 - ユーザーが存在しない")
    void saveWorkRecord_UserNotFound() {
        // Arrange
        String nonExistentUserId = "nonexistent";
        when(userRepository.findById(nonExistentUserId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.saveWorkRecord(
                nonExistentUserId,
                testProject.getId(),
                testWorkDate,
                testCategoryHours,
                "テスト作業",
                testUser.getId()
            )
        );

        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verifyNoInteractions(projectRepository, workRecordRepository);
    }

    @Test
    @DisplayName("工数記録作成 - プロジェクトが存在しない")
    void saveWorkRecord_ProjectNotFound() {
        // Arrange
        String nonExistentProjectId = "nonexistent";
        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(projectRepository.findById(nonExistentProjectId))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.saveWorkRecord(
                testUser.getId(),
                nonExistentProjectId,
                testWorkDate,
                testCategoryHours,
                "テスト作業",
                testUser.getId()
            )
        );

        assertEquals("Project not found with identifier: " + nonExistentProjectId, exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(projectRepository).findById(nonExistentProjectId);
        verifyNoInteractions(workRecordRepository);
    }

    @Test
    @DisplayName("工数記録作成 - プロジェクトが工数記録不可状態")
    void saveWorkRecord_ProjectCannotRecordWorkHours() {
        // Arrange
        // プロジェクトを完了状態に変更（工数記録不可）
        Project completedProject = Project.create("完了プロジェクト", "説明",
            LocalDate.now().minusDays(30), LocalDate.now().minusDays(1), "pmo_user");
        completedProject.start(); // まず開始状態にする
        completedProject.complete(LocalDate.now().minusDays(1)); // 完了状態に変更

        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(projectRepository.findById(completedProject.getId()))
            .thenReturn(Optional.of(completedProject));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.saveWorkRecord(
                testUser.getId(),
                completedProject.getId(),
                testWorkDate,
                testCategoryHours,
                "テスト作業",
                testUser.getId()
            )
        );

        assertTrue(exception.getMessage().contains("プロジェクトは工数記録できない状態です"));
        verify(userRepository).findById(testUser.getId());
        verify(projectRepository).findById(completedProject.getId());
        verifyNoInteractions(workRecordRepository);
    }




    @Test
    @DisplayName("ユーザーIDと日付で工数記録検索 - 正常ケース")
    void findByUserIdAndDate_Success() {
        // Arrange
        List<WorkRecord> workRecords = Arrays.asList(testWorkRecord);
        when(workRecordRepository.findByUserIdAndDate(testUser.getId(), testWorkDate))
            .thenReturn(workRecords);

        // Act
        List<WorkRecord> result = service.findByUserIdAndDate(testUser.getId(), testWorkDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkRecord, result.get(0));
        verify(workRecordRepository).findByUserIdAndDate(testUser.getId(), testWorkDate);
    }



    @Test
    @DisplayName("ユーザーの期間指定工数記録取得 - 正常ケース")
    void findByUserIdAndDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<WorkRecord> workRecords = Arrays.asList(testWorkRecord);
        when(workRecordRepository.findByUserIdAndDateRange(testUser.getId(), startDate, endDate))
            .thenReturn(workRecords);

        // Act
        List<WorkRecord> result = service.findByUserIdAndDateRange(testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkRecord, result.get(0));
        verify(workRecordRepository).findByUserIdAndDateRange(testUser.getId(), startDate, endDate);
    }



    @Test
    @DisplayName("ユーザーIDと日付で工数記録検索 - 記録が存在しない場合")
    void findByUserIdAndDate_NoRecords() {
        // Arrange
        when(workRecordRepository.findByUserIdAndDate(testUser.getId(), testWorkDate))
            .thenReturn(Collections.emptyList());

        // Act
        List<WorkRecord> result = service.findByUserIdAndDate(testUser.getId(), testWorkDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workRecordRepository).findByUserIdAndDate(testUser.getId(), testWorkDate);
    }



    @Test
    @DisplayName("ユーザーの期間指定工数記録取得 - 記録が存在しない場合")
    void findByUserIdAndDateRange_NoRecords() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(workRecordRepository.findByUserIdAndDateRange(testUser.getId(), startDate, endDate))
            .thenReturn(Collections.emptyList());

        // Act
        List<WorkRecord> result = service.findByUserIdAndDateRange(testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workRecordRepository).findByUserIdAndDateRange(testUser.getId(), startDate, endDate);
    }
    
    @Test
    @DisplayName("工数集計レポート取得 - 正常ケース")
    void generateWorkHoursSummary_Success() {
        // Arrange
        String userId = testUser.getId();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 7);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        
        // Act
        WorkHoursSummaryResponse result = service.generateWorkHoursSummary(userId, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testUser.getFullName(), result.getUserFullName());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        
        verify(userRepository).findById(userId);
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Test
    @DisplayName("工数集計レポート取得 - 工数記録が存在しない場合")
    void generateWorkHoursSummary_NoRecords() {
        // Arrange
        String userId = testUser.getId();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 7);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        
        // Act
        WorkHoursSummaryResponse result = service.generateWorkHoursSummary(userId, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testUser.getFullName(), result.getUserFullName());
        assertEquals(BigDecimal.ZERO, result.getTotalHours());
        assertEquals(0, result.getTotalDays());
        assertTrue(result.getProjectHours().isEmpty());
        assertTrue(result.getCategoryHours().isEmpty());
        assertTrue(result.getDailyHours().isEmpty());
        assertTrue(result.getWeeklySummaries().isEmpty());
    }
    
    @Test
    @DisplayName("工数集計レポート取得 - ユーザーが存在しない場合")
    void generateWorkHoursSummary_UserNotFound() {
        // Arrange
        String nonExistentUserId = "nonexistent";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 7);
        
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.generateWorkHoursSummary(nonExistentUserId, startDate, endDate)
        );
        
        assertEquals("User not found with identifier: " + nonExistentUserId, exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verify(workRecordRepository, never()).findByUserIdAndDateRange(any(), any(), any());
    }

    @Test
    @DisplayName("複数工数記録作成 - 正常ケース")
    void saveWorkRecords_Success() {
        // Arrange
        List<WorkRecordSaveRequest.WorkRecordDto> records = Arrays.asList(
            createWorkRecordDto(testProject.getId(), testWorkDate, testCategoryHours, "作業1"),
            createWorkRecordDto(testProject.getId(), testWorkDate.minusDays(1), testCategoryHours, "作業2")
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(records);
        
        when(userRepository.findById(testUser.getId()))
            .thenReturn(Optional.of(testUser));
        when(projectRepository.findById(testProject.getId()))
            .thenReturn(Optional.of(testProject));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // WorkRecordApprovalのモック設定
        LocalDate workDate = LocalDate.now();
        when(workRecordApprovalRepository.findByUserIdAndDate(testUser.getId(), workDate))
            .thenReturn(Optional.empty());
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        WorkRecordsResponse result = service.saveWorkRecords(testUser.getId(), workDate, request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWorkRecords());
        assertEquals(2, result.getWorkRecords().size());
        verify(userRepository, times(2)).findById(testUser.getId());
        verify(projectRepository, times(2)).findById(testProject.getId());
        verify(workRecordRepository, times(2)).save(any(WorkRecord.class));
    }


    @Test
    @DisplayName("複数工数記録作成 - 承認済みエラー")
    void saveWorkRecords_AlreadyApproved() {
        // Arrange
        List<WorkRecordSaveRequest.WorkRecordDto> records = Arrays.asList(
            createWorkRecordDto(testProject.getId(), testWorkDate, testCategoryHours, "作業1")
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(records);
        
        LocalDate workDate = LocalDate.now();
        WorkRecordApproval approvedRecord = new WorkRecordApproval(testUser.getId(), workDate);
        approvedRecord.approve("supervisor1");
        
        when(workRecordApprovalRepository.findByUserIdAndDate(testUser.getId(), workDate))
            .thenReturn(Optional.of(approvedRecord));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.saveWorkRecords(testUser.getId(), workDate, request)
        );
        
        assertEquals("承認済みの工数記録は更新できません", exception.getMessage());
        verify(workRecordApprovalRepository).findByUserIdAndDate(testUser.getId(), workDate);
        verifyNoInteractions(userRepository, projectRepository, workRecordRepository);
    }

    @Test
    @DisplayName("複数工数記録作成 - 空のリスト")
    void saveWorkRecords_EmptyList() {
        // Arrange
        List<WorkRecordSaveRequest.WorkRecordDto> emptyRecords = Collections.emptyList();
        WorkRecordSaveRequest emptyRequest = new WorkRecordSaveRequest();
        emptyRequest.setRecords(emptyRecords);
        
        // Act & Assert
        LocalDate workDate = LocalDate.now();
        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> service.saveWorkRecords(testUser.getId(), workDate, emptyRequest)
        );
        
        assertEquals("工数記録が空です", exception.getMessage());
        verifyNoInteractions(userRepository, projectRepository, workRecordRepository);
    }

    @Test
    @DisplayName("工数集計レポート取得 - 週別集計あり")
    void generateWorkHoursSummary_WithWeeklySummary() {
        // Arrange
        String userId = testUser.getId();
        LocalDate startDate = LocalDate.now().minusDays(14); // 2 weeks ago
        LocalDate endDate = LocalDate.now(); // Today
        
        // Create multiple work records across weeks
        WorkRecord record1 = WorkRecord.create(userId, testProject.getId(), 
            LocalDate.now().minusDays(10), testCategoryHours, "Monday work", userId);
        WorkRecord record2 = WorkRecord.create(userId, testProject.getId(), 
            LocalDate.now().minusDays(8), testCategoryHours, "Wednesday work", userId);
        WorkRecord record3 = WorkRecord.create(userId, testProject.getId(), 
            LocalDate.now().minusDays(3), testCategoryHours, "Next Monday work", userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(record1, record2, record3));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        
        // Act
        WorkHoursSummaryResponse result = service.generateWorkHoursSummary(userId, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(3, result.getTotalDays());
        assertNotNull(result.getWeeklySummaries());
        assertFalse(result.getWeeklySummaries().isEmpty());
        
        // Verify weekly summaries
        List<WorkHoursSummaryResponse.WeeklySummary> weeklySummaries = result.getWeeklySummaries();
        assertTrue(weeklySummaries.size() >= 1);
        
        verify(userRepository).findById(userId);
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    @DisplayName("工数集計レポート取得 - プロジェクトが存在しない場合のフォールバック")
    void generateWorkHoursSummary_ProjectNotFound() {
        // Arrange
        String userId = testUser.getId();
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(testWorkRecord));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.empty());
        
        // Act
        WorkHoursSummaryResponse result = service.generateWorkHoursSummary(userId, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertTrue(result.getProjectHours().containsKey("Unknown Project"));
        
        verify(userRepository).findById(userId);
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
        verify(projectRepository).findById(testProject.getId());
    }

    @Test
    @DisplayName("工数集計レポート取得 - 複数プロジェクト・複数カテゴリ")
    void generateWorkHoursSummary_MultipleProjectsAndCategories() {
        // Arrange
        String userId = testUser.getId();
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // Create second project
        Project project2 = Project.create("プロジェクト2", "説明2", 
            LocalDate.now(), LocalDate.now().plusDays(30), "pmo_user");
        
        // Create category hours with multiple categories
        Map<CategoryCode, BigDecimal> hours1 = new HashMap<>();
        hours1.put(new CategoryCode("DEV"), new BigDecimal("4.0"));
        hours1.put(new CategoryCode("TEST"), new BigDecimal("4.0"));
        CategoryHours categoryHours1 = new CategoryHours(hours1);
        
        Map<CategoryCode, BigDecimal> hours2 = new HashMap<>();
        hours2.put(new CategoryCode("DEV"), new BigDecimal("6.0"));
        hours2.put(new CategoryCode("DOC"), new BigDecimal("2.0"));
        CategoryHours categoryHours2 = new CategoryHours(hours2);
        
        WorkRecord record1 = WorkRecord.create(userId, testProject.getId(), 
            LocalDate.now().minusDays(2), categoryHours1, "Work 1", userId);
        WorkRecord record2 = WorkRecord.create(userId, project2.getId(), 
            LocalDate.now().minusDays(1), categoryHours2, "Work 2", userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(record1, record2));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(projectRepository.findById(project2.getId())).thenReturn(Optional.of(project2));
        
        // Act
        WorkHoursSummaryResponse result = service.generateWorkHoursSummary(userId, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalDays());
        assertEquals(new BigDecimal("16.0"), result.getTotalHours());
        
        // Verify project hours
        assertEquals(2, result.getProjectHours().size());
        assertTrue(result.getProjectHours().containsKey(testProject.getName()));
        assertTrue(result.getProjectHours().containsKey(project2.getName()));
        
        // Verify category hours
        assertEquals(3, result.getCategoryHours().size());
        assertTrue(result.getCategoryHours().containsKey("DEV"));
        assertTrue(result.getCategoryHours().containsKey("TEST"));
        assertTrue(result.getCategoryHours().containsKey("DOC"));
        assertEquals(new BigDecimal("10.0"), result.getCategoryHours().get("DEV"));
        assertEquals(new BigDecimal("4.0"), result.getCategoryHours().get("TEST"));
        assertEquals(new BigDecimal("2.0"), result.getCategoryHours().get("DOC"));
        
        // Verify daily hours
        assertEquals(2, result.getDailyHours().size());
        
        verify(userRepository).findById(userId);
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    private WorkRecordSaveRequest.WorkRecordDto createWorkRecordDto(
            String projectId, LocalDate workDate, CategoryHours categoryHours, String description) {
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto();
        dto.setProjectId(projectId);
        dto.setWorkDate(workDate);
        dto.setCategoryHours(categoryHours);
        dto.setDescription(description);
        return dto;
    }

    // === getWorkRecordsWithApprovalStatusメソッドのテスト ===

    @Test
    @DisplayName("getWorkRecordsWithApprovalStatus - 承認済みデータを正しく取得する")
    void getWorkRecordsWithApprovalStatus_ApprovedData_Success() {
        // Arrange
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);

        // 工数記録を準備
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        WorkRecord workRecord = WorkRecord.restore(
            "record1", userId, "project1", date,
            categoryHours, "作業内容", userId, date.atStartOfDay(), userId, date.atStartOfDay()
        );
        List<WorkRecord> workRecords = Arrays.asList(workRecord);

        // 承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);
        approval.approve("supervisor1");

        // プロジェクトを準備
        Project project = Project.restore(
            "project1", "テストプロジェクト", "説明",
            date, date.plusDays(30), null,
            ProjectStatus.IN_PROGRESS,
            userId, date.atStartOfDay(), date.atStartOfDay()
        );

        // Mockの設定
        when(workRecordRepository.findByUserIdAndDate(userId, date)).thenReturn(workRecords);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(approval));
        when(projectRepository.findById("project1")).thenReturn(Optional.of(project));

        // Act
        WorkRecordsResponse result = service.getWorkRecordsWithApprovalStatus(userId, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getWorkRecords().size());
        assertEquals(workRecord, result.getWorkRecords().get(0));
        assertNotNull(result.getWorkRecordApproval());
        assertEquals(approval, result.getWorkRecordApproval());
        assertTrue(result.getWorkRecordApproval().isApproved());
        
        verify(workRecordRepository).findByUserIdAndDate(userId, date);
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, date);
    }

    @Test
    @DisplayName("getWorkRecordsWithApprovalStatus - 未承認データを正しく取得する")
    void getWorkRecordsWithApprovalStatus_PendingData_Success() {
        // Arrange
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);

        // 工数記録を準備
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        WorkRecord workRecord = WorkRecord.restore(
            "record1", userId, "project1", date,
            categoryHours, "作業内容", userId, date.atStartOfDay(), userId, date.atStartOfDay()
        );
        List<WorkRecord> workRecords = Arrays.asList(workRecord);

        // 未承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);

        // プロジェクトを準備
        Project project = Project.restore(
            "project1", "テストプロジェクト", "説明",
            date, date.plusDays(30), null,
            ProjectStatus.IN_PROGRESS,
            userId, date.atStartOfDay(), date.atStartOfDay()
        );

        // Mockの設定
        when(workRecordRepository.findByUserIdAndDate(userId, date)).thenReturn(workRecords);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(approval));
        when(projectRepository.findById("project1")).thenReturn(Optional.of(project));

        // Act
        WorkRecordsResponse result = service.getWorkRecordsWithApprovalStatus(userId, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getWorkRecords().size());
        assertEquals(workRecord, result.getWorkRecords().get(0));
        assertNotNull(result.getWorkRecordApproval());
        assertEquals(approval, result.getWorkRecordApproval());
        assertFalse(result.getWorkRecordApproval().isApproved());
        
        verify(workRecordRepository).findByUserIdAndDate(userId, date);
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, date);
    }

    @Test
    @DisplayName("getWorkRecordsWithApprovalStatus - 承認データが存在しない場合は新規作成される")
    void getWorkRecordsWithApprovalStatus_NoApprovalData_CreatesNew() {
        // Arrange
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);

        // 工数記録を準備
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        WorkRecord workRecord = WorkRecord.restore(
            "record1", userId, "project1", date,
            categoryHours, "作業内容", userId, date.atStartOfDay(), userId, date.atStartOfDay()
        );
        List<WorkRecord> workRecords = Arrays.asList(workRecord);

        // プロジェクトを準備
        Project project = Project.restore(
            "project1", "テストプロジェクト", "説明",
            date, date.plusDays(30), null,
            ProjectStatus.IN_PROGRESS,
            userId, date.atStartOfDay(), date.atStartOfDay()
        );

        // Mockの設定
        when(workRecordRepository.findByUserIdAndDate(userId, date)).thenReturn(workRecords);
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.empty());
        when(projectRepository.findById("project1")).thenReturn(Optional.of(project));

        // Act
        WorkRecordsResponse result = service.getWorkRecordsWithApprovalStatus(userId, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getWorkRecords().size());
        assertEquals(workRecord, result.getWorkRecords().get(0));
        assertNotNull(result.getWorkRecordApproval());
        assertEquals(userId, result.getWorkRecordApproval().getUserId());
        assertEquals(date, result.getWorkRecordApproval().getWorkDate());
        assertFalse(result.getWorkRecordApproval().isApproved());
        
        verify(workRecordRepository).findByUserIdAndDate(userId, date);
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, date);
    }

    // === 承認済みデータ更新制限のテスト ===

    @Test
    @DisplayName("saveWorkRecords - 承認済みデータの更新は拒否される")
    void saveWorkRecords_ApprovedData_ThrowsException() {
        // Arrange
        String userId = "user1";
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        // 承認済みの承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);
        approval.approve("supervisor1");
        
        // リクエストデータを準備
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        WorkRecordSaveRequest.WorkRecordDto dto = createWorkRecordDto(
            "project1", date, categoryHours, "作業内容"
        );
        List<WorkRecordSaveRequest.WorkRecordDto> records = Arrays.asList(dto);
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(records);

        // Mockの設定
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, date))
            .thenReturn(Optional.of(approval));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.saveWorkRecords(userId, date, request)
        );
        
        assertEquals("承認済みの工数記録は更新できません", exception.getMessage());
        
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, date);
        verify(workRecordRepository, never()).save(any(WorkRecord.class));
        verify(workRecordApprovalRepository, never()).save(any(WorkRecordApproval.class));
    }

    @Test
    @DisplayName("saveWorkRecord - 承認済みデータの更新は拒否される")
    void saveWorkRecord_ApprovedData_ThrowsException() {
        // Arrange
        String userId = "user1";
        String projectId = "project1";
        LocalDate workDate = LocalDate.now().minusDays(1); // 昨日の日付を使用
        
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        String description = "作業内容";
        
        // ユーザーとプロジェクトのMockデータ
        User user = User.restore(userId, userId, "test@example.com", "Test User", true, null, null);
        Project project = Project.restore(projectId, "テストプロジェクト", "説明", 
            LocalDate.now(), LocalDate.now().plusMonths(3), null, ProjectStatus.IN_PROGRESS, 
            "creator", LocalDateTime.now(), LocalDateTime.now());

        // 承認済みの承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, workDate);
        approval.approve("supervisor1");

        // Mockの設定
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, workDate))
            .thenReturn(Optional.of(approval));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.saveWorkRecord(userId, projectId, workDate, categoryHours, description, userId)
        );
        
        assertEquals("承認済みの工数記録は更新できません", exception.getMessage());
        
        verify(userRepository).findById(userId);
        verify(projectRepository).findById(projectId);
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, workDate);
        verify(workRecordRepository, never()).findByUserIdAndDateAndProjectId(any(), any(), any());
        verify(workRecordRepository, never()).save(any(WorkRecord.class));
    }

    @Test
    @DisplayName("saveWorkRecord - 未承認データは正常に更新される")
    void saveWorkRecord_PendingData_Success() {
        // Arrange
        String userId = "user1";
        String projectId = "project1";
        LocalDate workDate = LocalDate.now().minusDays(1); // 昨日の日付を使用
        
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("4.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        String description = "作業内容";
        
        // ユーザーとプロジェクトのMockデータ
        User user = User.restore(userId, userId, "test@example.com", "Test User", true, null, null);
        Project project = Project.restore(projectId, "テストプロジェクト", "説明", 
            LocalDate.now(), LocalDate.now().plusMonths(3), null, ProjectStatus.IN_PROGRESS, 
            "creator", LocalDateTime.now(), LocalDateTime.now());

        // 未承認の承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, workDate);

        // 作成される工数記録（Mockオブジェクトとして作成）
        WorkRecord newRecord = WorkRecord.restore(
            "newRecord", userId, projectId, workDate, categoryHours, description, 
            userId, workDate.atStartOfDay(), userId, workDate.atStartOfDay()
        );

        // Mockの設定
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, workDate))
            .thenReturn(Optional.of(approval));
        doNothing().when(categoryHoursValidationService).validate(categoryHours);
        when(workRecordRepository.findByUserIdAndDateAndProjectId(userId, workDate, projectId))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class))).thenReturn(newRecord);

        // Act
        WorkRecord result = service.saveWorkRecord(userId, projectId, workDate, categoryHours, description, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(workDate, result.getWorkDate());
        
        verify(userRepository).findById(userId);
        verify(projectRepository).findById(projectId);
        verify(workRecordApprovalRepository).findByUserIdAndDate(userId, workDate);
        verify(categoryHoursValidationService).validate(categoryHours);
        verify(workRecordRepository).findByUserIdAndDateAndProjectId(userId, workDate, projectId);
        verify(workRecordRepository).save(any(WorkRecord.class));
    }

    // === getMissingDatesForMonth テスト ===

    @Test
    @DisplayName("月の未入力日取得 - 正常ケース")
    void getMissingDatesForMonth_ValidInput_Success() {
        // Arrange
        String userId = "user1";
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);
        int year = twoMonthsAgo.getYear();
        int month = twoMonthsAgo.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<LocalDate> expectedMissingDates = Arrays.asList(
            startDate,
            startDate.plusDays(1),
            startDate.plusDays(4)
        );

        when(workRecordStatusService.findMissingDates(userId, startDate, endDate))
            .thenReturn(expectedMissingDates);

        // Act
        List<LocalDate> result = service.getMissingDatesForMonth(userId, year, month);

        // Assert
        assertNotNull(result);
        assertEquals(expectedMissingDates.size(), result.size());
        assertEquals(expectedMissingDates, result);
        
        verify(workRecordStatusService).findMissingDates(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の未入力日取得 - 未入力日なし")
    void getMissingDatesForMonth_NoMissingDates_EmptyList() {
        // Arrange
        String userId = "user1";
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        int year = oneMonthAgo.getYear();
        int month = oneMonthAgo.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(workRecordStatusService.findMissingDates(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());

        // Act
        List<LocalDate> result = service.getMissingDatesForMonth(userId, year, month);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(workRecordStatusService).findMissingDates(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の未入力日取得 - 無効なユーザーID")
    void getMissingDatesForMonth_NullUserId_ThrowsException() {
        // Arrange
        String userId = null;
        int year = 2024;
        int month = 8;

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getMissingDatesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordStatusService);
    }

    @Test
    @DisplayName("月の未入力日取得 - 3ヶ月より前の月")
    void getMissingDatesForMonth_TooOldMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        LocalDate fourMonthsAgo = LocalDate.now().minusMonths(4);
        int year = fourMonthsAgo.getYear();
        int month = fourMonthsAgo.getMonthValue();

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getMissingDatesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordStatusService);
    }

    @Test
    @DisplayName("月の未入力日取得 - 無効な月")
    void getMissingDatesForMonth_InvalidMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        int year = 2024;
        int month = 13; // 無効な月

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getMissingDatesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordStatusService);
    }

    @Test
    @DisplayName("月の未入力日取得 - 来月以降の未来月")
    void getMissingDatesForMonth_FutureMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        int year = nextMonth.getYear();
        int month = nextMonth.getMonthValue();

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getMissingDatesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordStatusService);
    }

    @Test
    @DisplayName("月の未入力日取得 - 3ヶ月前の境界値")
    void getMissingDatesForMonth_ThreeMonthsAgoBoundary_Success() {
        // Arrange
        String userId = "user1";
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        int year = threeMonthsAgo.getYear();
        int month = threeMonthsAgo.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<LocalDate> expectedMissingDates = Arrays.asList(
            LocalDate.of(year, month, 1)
        );

        when(workRecordStatusService.findMissingDates(userId, startDate, endDate))
            .thenReturn(expectedMissingDates);

        // Act
        List<LocalDate> result = service.getMissingDatesForMonth(userId, year, month);

        // Assert
        assertNotNull(result);
        assertEquals(expectedMissingDates, result);
        
        verify(workRecordStatusService).findMissingDates(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の未入力日取得 - 当月の境界値テスト")
    void getMissingDatesForMonth_CurrentMonthBoundary_Success() {
        // Arrange
        String userId = "user1";
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<LocalDate> expectedMissingDates = Arrays.asList(
            startDate,  // 月初
            endDate     // 月末
        );

        when(workRecordStatusService.findMissingDates(userId, startDate, endDate))
            .thenReturn(expectedMissingDates);

        // Act
        List<LocalDate> result = service.getMissingDatesForMonth(userId, year, month);

        // Assert
        assertNotNull(result);
        assertEquals(expectedMissingDates, result);
        
        verify(workRecordStatusService).findMissingDates(userId, startDate, endDate);
    }

    // === getDateStatusesForMonth テスト ===

    @Test
    @DisplayName("月の日付ステータス取得 - 正常ケース（工数記録あり・承認済み）")
    void getDateStatusesForMonth_WithRecordsAndApproved_Success() {
        // Arrange
        String userId = "user1";
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);
        int year = twoMonthsAgo.getYear();
        int month = twoMonthsAgo.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // 工数記録を作成
        LocalDate workDate1 = startDate.plusDays(5);
        LocalDate workDate2 = startDate.plusDays(10);
        
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("8.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        
        WorkRecord record1 = WorkRecord.restore(
            "record1", userId, "project1", workDate1,
            categoryHours, "作業1", userId, workDate1.atStartOfDay(), userId, workDate1.atStartOfDay()
        );
        WorkRecord record2 = WorkRecord.restore(
            "record2", userId, "project1", workDate2,
            categoryHours, "作業2", userId, workDate2.atStartOfDay(), userId, workDate2.atStartOfDay()
        );
        
        // 承認データを作成
        WorkRecordApproval approval1 = new WorkRecordApproval(userId, workDate1);
        approval1.approve("supervisor1");
        WorkRecordApproval approval2 = new WorkRecordApproval(userId, workDate2);
        // approval2は未承認のまま
        
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(record1, record2));
        when(workRecordApprovalRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(approval1, approval2));
        
        // Act
        Map<LocalDate, DateStatusResponse.DateStatus> result = service.getDateStatusesForMonth(userId, year, month);
        
        // Assert
        assertNotNull(result);
        assertEquals(startDate.lengthOfMonth(), result.size());
        
        // 工数記録がある日の検証
        DateStatusResponse.DateStatus status1 = result.get(workDate1);
        assertNotNull(status1);
        assertTrue(status1.isHasWorkRecord());
        assertEquals(ApprovalStatus.APPROVED, status1.getApprovalStatus());
        assertEquals(8.0, status1.getTotalHours(), 0.01);
        
        DateStatusResponse.DateStatus status2 = result.get(workDate2);
        assertNotNull(status2);
        assertTrue(status2.isHasWorkRecord());
        assertEquals(ApprovalStatus.PENDING, status2.getApprovalStatus());
        assertEquals(8.0, status2.getTotalHours(), 0.01);
        
        // 工数記録がない日の検証
        DateStatusResponse.DateStatus emptyStatus = result.get(startDate);
        assertNotNull(emptyStatus);
        assertFalse(emptyStatus.isHasWorkRecord());
        assertEquals(ApprovalStatus.NOT_ENTERED, emptyStatus.getApprovalStatus());
        assertEquals(0.0, emptyStatus.getTotalHours(), 0.01);
        
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
        verify(workRecordApprovalRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 工数記録なし")
    void getDateStatusesForMonth_NoRecords_AllEmpty() {
        // Arrange
        String userId = "user1";
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        int year = oneMonthAgo.getYear();
        int month = oneMonthAgo.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        when(workRecordApprovalRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        
        // Act
        Map<LocalDate, DateStatusResponse.DateStatus> result = service.getDateStatusesForMonth(userId, year, month);
        
        // Assert
        assertNotNull(result);
        assertEquals(startDate.lengthOfMonth(), result.size());
        
        // すべての日が空であることを確認
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DateStatusResponse.DateStatus status = result.get(date);
            assertNotNull(status);
            assertFalse(status.isHasWorkRecord());
            assertEquals(ApprovalStatus.NOT_ENTERED, status.getApprovalStatus());
            assertEquals(0.0, status.getTotalHours(), 0.01);
        }
        
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
        verify(workRecordApprovalRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 無効なユーザーID")
    void getDateStatusesForMonth_NullUserId_ThrowsException() {
        // Arrange
        String userId = null;
        int year = 2024;
        int month = 8;

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getDateStatusesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordRepository, workRecordApprovalRepository);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 空のユーザーID")
    void getDateStatusesForMonth_EmptyUserId_ThrowsException() {
        // Arrange
        String userId = "   ";
        int year = 2024;
        int month = 8;

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getDateStatusesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordRepository, workRecordApprovalRepository);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 無効な月")
    void getDateStatusesForMonth_InvalidMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        int year = 2024;
        int month = 13; // 無効な月

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getDateStatusesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordRepository, workRecordApprovalRepository);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 3ヶ月より前の月")
    void getDateStatusesForMonth_TooOldMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        LocalDate fourMonthsAgo = LocalDate.now().minusMonths(4);
        int year = fourMonthsAgo.getYear();
        int month = fourMonthsAgo.getMonthValue();

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getDateStatusesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordRepository, workRecordApprovalRepository);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 来月以降の未来月")
    void getDateStatusesForMonth_FutureMonth_ThrowsException() {
        // Arrange
        String userId = "user1";
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        int year = nextMonth.getYear();
        int month = nextMonth.getMonthValue();

        // Act & Assert
        assertThrows(InvalidParameterException.class, () -> {
            service.getDateStatusesForMonth(userId, year, month);
        });
        
        verifyNoInteractions(workRecordRepository, workRecordApprovalRepository);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - 複数プロジェクトの工数記録")
    void getDateStatusesForMonth_MultipleProjects_Success() {
        // Arrange
        String userId = "user1";
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        LocalDate workDate = startDate.plusDays(7);
        
        // 同じ日に複数プロジェクトの工数記録
        Map<CategoryCode, BigDecimal> hours1 = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("4.0")
        );
        Map<CategoryCode, BigDecimal> hours2 = Map.of(
            CategoryCode.of("TEST"), new BigDecimal("3.0")
        );
        
        WorkRecord record1 = WorkRecord.restore(
            "record1", userId, "project1", workDate,
            new CategoryHours(hours1), "プロジェクト1作業", userId, 
            workDate.atStartOfDay(), userId, workDate.atStartOfDay()
        );
        WorkRecord record2 = WorkRecord.restore(
            "record2", userId, "project2", workDate,
            new CategoryHours(hours2), "プロジェクト2作業", userId, 
            workDate.atStartOfDay(), userId, workDate.atStartOfDay()
        );
        
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(record1, record2));
        when(workRecordApprovalRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        
        // Act
        Map<LocalDate, DateStatusResponse.DateStatus> result = service.getDateStatusesForMonth(userId, year, month);
        
        // Assert
        assertNotNull(result);
        
        DateStatusResponse.DateStatus status = result.get(workDate);
        assertNotNull(status);
        assertTrue(status.isHasWorkRecord());
        assertEquals(7.0, status.getTotalHours(), 0.01); // 4.0 + 3.0
        assertEquals(ApprovalStatus.NOT_ENTERED, status.getApprovalStatus());
        
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
        verify(workRecordApprovalRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    @DisplayName("月の日付ステータス取得 - CategoryHoursがnullの場合")
    void getDateStatusesForMonth_NullCategoryHours_HandledGracefully() {
        // Arrange
        String userId = "user1";
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        LocalDate workDate = startDate.plusDays(3);
        
        // CategoryHoursがnullの工数記録
        WorkRecord recordWithNullHours = WorkRecord.restore(
            "record1", userId, "project1", workDate,
            null, "作業内容", userId, 
            workDate.atStartOfDay(), userId, workDate.atStartOfDay()
        );
        
        when(workRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(recordWithNullHours));
        when(workRecordApprovalRepository.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        
        // Act
        Map<LocalDate, DateStatusResponse.DateStatus> result = service.getDateStatusesForMonth(userId, year, month);
        
        // Assert
        assertNotNull(result);
        
        DateStatusResponse.DateStatus status = result.get(workDate);
        assertNotNull(status);
        assertTrue(status.isHasWorkRecord());
        assertEquals(0.0, status.getTotalHours(), 0.01); // nullの場合は0.0として扱われる
        assertEquals(ApprovalStatus.NOT_ENTERED, status.getApprovalStatus());
        
        verify(workRecordRepository).findByUserIdAndDateRange(userId, startDate, endDate);
        verify(workRecordApprovalRepository).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    // === 工数記録テスト ===

    @Test
    @DisplayName("工数記録作成 - 任意のユーザーが有効なプロジェクトに工数入力可能")
    void saveWorkRecord_AnyUserCanRecordToActiveProject_Success() {
        // Arrange
        String userId = "any_user";
        String projectId = "any_active_project";
        
        // 任意のユーザーとプロジェクトを作成
        User anyUser = User.create(userId, "any@example.com", "任意ユーザー");
        Project activeProject = Project.create("任意プロジェクト", "説明", 
            LocalDate.now(), LocalDate.now().plusDays(30), "pmo_user");
        activeProject.start(); // 進行中状態にする
        
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(anyUser));
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.of(activeProject));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(userId, testWorkDate, projectId))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WorkRecord result = service.saveWorkRecord(
            userId,
            projectId,
            testWorkDate,
            testCategoryHours,
            "任意プロジェクトでの作業",
            userId
        );

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(testWorkDate, result.getWorkDate());
        assertEquals("任意プロジェクトでの作業", result.getDescription());
        
        // アサインチェックが行われていないことを確認（プロジェクトと有効性チェックのみ）
        verify(userRepository).findById(userId);
        verify(projectRepository).findById(projectId);
        verify(workRecordRepository).findByUserIdAndDateAndProjectId(userId, testWorkDate, projectId);
        verify(workRecordRepository).save(any(WorkRecord.class));
    }

    @Test
    @DisplayName("工数記録作成 - 制限なし工数記録")
    void saveWorkRecord_NoRestriction_Success() {
        // Arrange
        String developerId = "developer1";
        String projectId = "unassigned_project";
        LocalDate workDate = LocalDate.now().minusDays(1);
        
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("CODING"), new BigDecimal("6.0"),
            CategoryCode.of("REVIEW"), new BigDecimal("2.0")
        );
        CategoryHours categoryHours = new CategoryHours(hours);
        
        User developer = User.create(developerId, "dev@example.com", "開発者");
        Project project = Project.create("未アサインプロジェクト", "説明", 
            LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), "pmo_user");
        project.start();
        
        when(userRepository.findById(developerId))
            .thenReturn(Optional.of(developer));
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.of(project));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(developerId, workDate, projectId))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> {
            WorkRecord result = service.saveWorkRecord(
                developerId, projectId, workDate, categoryHours, 
                "プロジェクトアサインに依存しない工数記録", developerId
            );
            
            assertNotNull(result);
            assertEquals(developerId, result.getUserId());
            assertEquals(projectId, result.getProjectId());
            assertEquals(workDate, result.getWorkDate());
            assertEquals(new BigDecimal("8.0"), result.getCategoryHours().getTotalHours());
        });
        
        verify(userRepository).findById(developerId);
        verify(projectRepository).findById(projectId);
        verify(workRecordRepository).save(any(WorkRecord.class));
    }

    @Test
    @DisplayName("複数工数記録作成 - 全ユーザーが全プロジェクトにアクセス可能")
    void saveWorkRecords_AllUsersCanAccessAllActiveProjects_Success() {
        // Arrange
        String userId = "any_developer";
        LocalDate workDate = LocalDate.now();
        
        // 異なるプロジェクトの工数記録リスト
        List<WorkRecordSaveRequest.WorkRecordDto> records = Arrays.asList(
            createWorkRecordDto("project_a", workDate, testCategoryHours, "プロジェクトA作業"),
            createWorkRecordDto("project_b", workDate, testCategoryHours, "プロジェクトB作業")
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(records);
        
        User anyDeveloper = User.create(userId, "any.dev@example.com", "任意開発者");
        Project projectA = Project.create("プロジェクトA", "説明A", 
            LocalDate.now(), LocalDate.now().plusDays(30), "pmo1");
        projectA.start();
        Project projectB = Project.create("プロジェクトB", "説明B", 
            LocalDate.now(), LocalDate.now().plusDays(30), "pmo2");
        projectB.start();
        
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(anyDeveloper));
        when(projectRepository.findById("project_a"))
            .thenReturn(Optional.of(projectA));
        when(projectRepository.findById("project_b"))
            .thenReturn(Optional.of(projectB));
        when(workRecordRepository.findByUserIdAndDateAndProjectId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(workRecordRepository.save(any(WorkRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // WorkRecordApprovalのモック設定
        when(workRecordApprovalRepository.findByUserIdAndDate(userId, workDate))
            .thenReturn(Optional.empty());
        when(workRecordApprovalRepository.save(any(WorkRecordApproval.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        WorkRecordsResponse result = service.saveWorkRecords(userId, workDate, request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWorkRecords());
        assertEquals(2, result.getWorkRecords().size());

        // 各プロジェクトに工数記録ができていることを確認
        assertTrue(result.getWorkRecords().stream().anyMatch(r -> "project_a".equals(r.getProjectId())));
        assertTrue(result.getWorkRecords().stream().anyMatch(r -> "project_b".equals(r.getProjectId())));
        
        verify(userRepository, times(2)).findById(userId);
        verify(projectRepository).findById("project_a");
        verify(projectRepository).findById("project_b");
        verify(workRecordRepository, times(2)).save(any(WorkRecord.class));
    }
}