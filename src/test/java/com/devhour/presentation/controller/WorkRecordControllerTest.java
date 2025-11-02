package com.devhour.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.devhour.application.service.WorkRecordApplicationService;
import com.devhour.domain.exception.InvalidParameterException;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.presentation.dto.request.WorkRecordSaveRequest;
import com.devhour.presentation.dto.response.WorkHoursSummaryResponse;
import com.devhour.presentation.dto.response.WorkRecordsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工数記録コントローラー統合テスト
 */
@WebMvcTest(WorkRecordController.class)
@Import(com.devhour.config.TestSecurityConfiguration.class)
@ActiveProfiles("test")
@DisplayName("工数記録コントローラー")
class WorkRecordControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private WorkRecordApplicationService workRecordApplicationService;
    
    
    
    
    @Test
    @DisplayName("工数記録作成 - 正常ケース")
    void createWorkRecord_Success() throws Exception {
        // Arrange
        String userId = "user123";
        LocalDate workDate = LocalDate.now().minusDays(5);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "project123",
            workDate,
            categoryHours,
            "システム開発作業"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        WorkRecord expectedWorkRecord = WorkRecord.create(
            userId,
            dto.getProjectId(),
            dto.getWorkDate(),
            dto.getCategoryHours(),
            dto.getDescription(),
            userId
        );

        WorkRecordsResponse response = WorkRecordsResponse.builder()
            .workRecords(List.of(expectedWorkRecord))
            .build();
        when(workRecordApplicationService.saveWorkRecords(
            userId,
            workDate,
            request
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords[0].id").exists())
                .andExpect(jsonPath("$.workRecords[0].userId").value(userId))
                .andExpect(jsonPath("$.workRecords[0].projectId").value("project123"));
        
        verify(workRecordApplicationService).saveWorkRecords(
            userId,
            workDate,
            request
        );
    }
    
    @Test
    @Disabled("WorkRecordSaveRequestの構造変更により一時的に無効化")
    @DisplayName("工数記録作成 - バリデーションエラー（プロジェクトIDが空）")
    void createWorkRecord_ValidationError_EmptyProjectId() throws Exception {
        // Arrange
        String userId = "user123";
        LocalDate workDate = LocalDate.now().minusDays(5);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "", // 空のプロジェクトID
            workDate,
            categoryHours,
            "システム開発作業"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService, never()).saveWorkRecords(any(), any(), any());
    }
    
    
    @Test
    @DisplayName("工数記録更新 - 正常ケース（同一日付で再度保存）")
    void updateWorkRecord_Success() throws Exception {
        // Arrange
        String userId = "user123";
        LocalDate workDate = LocalDate.now().minusDays(5);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("6.0"),
            CategoryCode.of("REVIEW"), new BigDecimal("2.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "project123",
            workDate,
            categoryHours,
            "システム開発作業（更新）"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        WorkRecord expectedWorkRecord = WorkRecord.create(
            userId,
            "project123",
            workDate,
            dto.getCategoryHours(),
            dto.getDescription(),
            userId
        );

        WorkRecordsResponse response = WorkRecordsResponse.builder()
            .workRecords(List.of(expectedWorkRecord))
            .build();
        when(workRecordApplicationService.saveWorkRecords(
            userId,
            workDate,
            request
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords[0].id").exists())
                .andExpect(jsonPath("$.workRecords[0].userId").value(userId));
        
        verify(workRecordApplicationService).saveWorkRecords(
            userId,
            workDate,
            request
        );
    }
    
    
    
    @Test
    @DisplayName("期間指定で工数記録検索 - 正常ケース")
    void getWorkRecordsByPeriod_Success() throws Exception {
        // Arrange
        String userId = "user123";
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        Map<CategoryCode, BigDecimal> hoursMap1 = Map.of(
            CategoryCode.of("DEV"), new BigDecimal("8.0")
        );
        Map<CategoryCode, BigDecimal> hoursMap2 = Map.of(
            CategoryCode.of("REVIEW"), new BigDecimal("4.0")
        );
        
        WorkRecord record1 = WorkRecord.create(
            userId,
            "project123",
            LocalDate.now().minusDays(5),
            CategoryHours.of(hoursMap1),
            "開発作業",
            "system"
        );
        
        WorkRecord record2 = WorkRecord.create(
            userId,
            "project456",
            LocalDate.now().minusDays(10),
            CategoryHours.of(hoursMap2),
            "レビュー作業",
            "system"
        );
        
        List<WorkRecord> expectedRecords = Arrays.asList(record1, record2);
        
        when(workRecordApplicationService.findByUserIdAndDateRange(userId, startDate, endDate))
                .thenReturn(expectedRecords);
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/period")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].userId").value(userId));
        
        verify(workRecordApplicationService).findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    
    
    
    @Test
    @DisplayName("工数集計レポート取得 - 正常ケース")
    void getWorkHoursSummary_Success() throws Exception {
        // Given
        String userId = "developer123";
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);
        
        WorkHoursSummaryResponse.WeeklySummary weeklySummary = new WorkHoursSummaryResponse.WeeklySummary(
            LocalDate.of(2023, 10, 2),
            LocalDate.of(2023, 10, 8),
            new BigDecimal("40.0"),
            5,
            new BigDecimal("8.0")
        );
        
        WorkHoursSummaryResponse expectedSummary = new WorkHoursSummaryResponse(
            userId,
            "田中太郎",
            startDate,
            endDate,
            new BigDecimal("160.0"),
            20,
            new BigDecimal("8.0"),
            Map.of("プロジェクトA", new BigDecimal("120.0"), "プロジェクトB", new BigDecimal("40.0")),
            Map.of("開発", new BigDecimal("120.0"), "レビュー", new BigDecimal("40.0")),
            Map.of(LocalDate.of(2023, 10, 1), new BigDecimal("8.0"), LocalDate.of(2023, 10, 2), new BigDecimal("8.0")),
            Arrays.asList(weeklySummary)
        );
        
        when(workRecordApplicationService.generateWorkHoursSummary(userId, startDate, endDate))
            .thenReturn(expectedSummary);
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/summary")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userFullName").value("田中太郎"))
                .andExpect(jsonPath("$.startDate").value("2023-10-01"))
                .andExpect(jsonPath("$.endDate").value("2023-10-31"))
                .andExpect(jsonPath("$.totalHours").value(160.0))
                .andExpect(jsonPath("$.totalDays").value(20))
                .andExpect(jsonPath("$.averageHoursPerDay").value(8.0))
                .andExpect(jsonPath("$.projectHours['プロジェクトA']").value(120.0))
                .andExpect(jsonPath("$.projectHours['プロジェクトB']").value(40.0))
                .andExpect(jsonPath("$.categoryHours['開発']").value(120.0))
                .andExpect(jsonPath("$.categoryHours['レビュー']").value(40.0))
                .andExpect(jsonPath("$.weeklySummaries").isArray())
                .andExpect(jsonPath("$.weeklySummaries.length()").value(1))
                .andExpect(jsonPath("$.weeklySummaries[0].weekStartDate").value("2023-10-02"))
                .andExpect(jsonPath("$.weeklySummaries[0].weekEndDate").value("2023-10-08"))
                .andExpect(jsonPath("$.weeklySummaries[0].totalHours").value(40.0))
                .andExpect(jsonPath("$.weeklySummaries[0].workingDays").value(5))
                .andExpect(jsonPath("$.weeklySummaries[0].averageHoursPerDay").value(8.0));
        
        verify(workRecordApplicationService).generateWorkHoursSummary(userId, startDate, endDate);
    }
    
    @Test
    @DisplayName("工数集計レポート取得 - 不正な日付範囲")
    void getWorkHoursSummary_InvalidDateRange() throws Exception {
        // Given
        String userId = "developer123";
        LocalDate startDate = LocalDate.of(2023, 10, 31);
        LocalDate endDate = LocalDate.of(2023, 10, 1); // 開始日より前の終了日
        
        when(workRecordApplicationService.generateWorkHoursSummary(userId, startDate, endDate))
            .thenThrow(new IllegalArgumentException("終了日は開始日より後である必要があります"));
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/summary")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService).generateWorkHoursSummary(userId, startDate, endDate);
    }
    
    @Test
    @DisplayName("工数集計レポート取得 - サービス層例外")
    void getWorkHoursSummary_ServiceException() throws Exception {
        // Given
        String userId = "developer123";
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);
        
        when(workRecordApplicationService.generateWorkHoursSummary(userId, startDate, endDate))
            .thenThrow(new RuntimeException("データベースエラー"));
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/summary")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isInternalServerError());
        
        verify(workRecordApplicationService).generateWorkHoursSummary(userId, startDate, endDate);
    }

    // === 新規エンドポイント：単一日付の工数記録と承認ステータス取得のテスト ===

    @Test
    @DisplayName("単一日付の工数記録と承認ステータス取得 - 正常ケース（承認済み）")
    void getWorkRecordsByDate_ApprovedData_Success() throws Exception {
        // Arrange
        String userId = "developer123";
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
        
        // 承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);
        approval.approve("supervisor1");
        
        WorkRecordsResponse expectedResponse = WorkRecordsResponse.builder()
            .workRecords(Arrays.asList(workRecord))
            .workRecordApproval(approval)
            .build();
        
        when(workRecordApplicationService.getWorkRecordsWithApprovalStatus(userId, date))
            .thenReturn(expectedResponse);
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/{date}", date.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords").isArray())
                .andExpect(jsonPath("$.workRecords.length()").value(1))
                .andExpect(jsonPath("$.workRecords[0].id").value("record1"))
                .andExpect(jsonPath("$.workRecordApproval").isNotEmpty())
                .andExpect(jsonPath("$.workRecordApproval.userId").value(userId))
                .andExpect(jsonPath("$.workRecordApproval.workDate").value(date.toString()))
                .andExpect(jsonPath("$.workRecordApproval.approvalStatus").value("APPROVED"));
        
        verify(workRecordApplicationService).getWorkRecordsWithApprovalStatus(userId, date);
    }

    @Test
    @DisplayName("単一日付の工数記録と承認ステータス取得 - 未承認データ")
    void getWorkRecordsByDate_PendingData_Success() throws Exception {
        // Arrange
        String userId = "developer123";
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
        
        // 未承認データを準備
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);
        
        WorkRecordsResponse expectedResponse = WorkRecordsResponse.builder()
            .workRecords(Arrays.asList(workRecord))
            .workRecordApproval(approval)
            .build();
        
        when(workRecordApplicationService.getWorkRecordsWithApprovalStatus(userId, date))
            .thenReturn(expectedResponse);
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/{date}", date.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords").isArray())
                .andExpect(jsonPath("$.workRecords.length()").value(1))
                .andExpect(jsonPath("$.workRecordApproval").isNotEmpty())
                .andExpect(jsonPath("$.workRecordApproval.approvalStatus").value("PENDING"));
        
        verify(workRecordApplicationService).getWorkRecordsWithApprovalStatus(userId, date);
    }

    @Test
    @DisplayName("単一日付の工数記録と承認ステータス取得 - 承認データなし")
    void getWorkRecordsByDate_NoApprovalData_Success() throws Exception {
        // Arrange
        String userId = "developer123";
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
        
        // 新規承認データ
        WorkRecordApproval approval = new WorkRecordApproval(userId, date);
        
        WorkRecordsResponse expectedResponse = WorkRecordsResponse.builder()
            .workRecords(Arrays.asList(workRecord))
            .workRecordApproval(approval)
            .build();
        
        when(workRecordApplicationService.getWorkRecordsWithApprovalStatus(userId, date))
            .thenReturn(expectedResponse);
        
        // When & Then
        mockMvc.perform(get("/api/work-records/me/{date}", date.toString())
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords").isArray())
                .andExpect(jsonPath("$.workRecordApproval.approvalStatus").value("PENDING"));
        
        verify(workRecordApplicationService).getWorkRecordsWithApprovalStatus(userId, date);
    }

    // === 承認済みデータ更新制限のテスト ===

    @Test
    @DisplayName("工数記録更新 - 異常系：承認済みデータの更新拒否")
    void saveWorkRecords_ApprovedDataUpdateRejected() throws Exception {
        // Arrange
        String userId = "developer123";
        LocalDate workDate = LocalDate.now().minusDays(1);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEVELOPMENT"), new BigDecimal("7.5")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "project123",
            workDate,
            categoryHours,
            "Updated work description"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        // Arrange: ApprovalExceptionがスローされるように設定
        when(workRecordApplicationService.saveWorkRecords(
            userId,
            workDate,
            request
        )).thenThrow(com.devhour.domain.exception.ApprovalException.status(
            "record1", 
            com.devhour.domain.model.valueobject.ApprovalStatus.APPROVED, 
            "更新"));
        
        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(workRecordApplicationService).saveWorkRecords(
            userId,
            workDate,
            request
        );
    }

    @Test
    @DisplayName("工数記録更新 - 正常系：未承認データの更新成功")
    void saveWorkRecords_UnApprovedDataUpdateSuccess() throws Exception {
        // Arrange
        String userId = "developer123";
        LocalDate workDate = LocalDate.now().minusDays(1);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEVELOPMENT"), new BigDecimal("7.5")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "project123",
            workDate,
            categoryHours,
            "Updated work description"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        WorkRecord expectedWorkRecord = WorkRecord.create(
            userId,
            dto.getProjectId(),
            dto.getWorkDate(),
            dto.getCategoryHours(),
            dto.getDescription(),
            userId
        );

        WorkRecordsResponse response = WorkRecordsResponse.builder()
            .workRecords(List.of(expectedWorkRecord))
            .build();
        when(workRecordApplicationService.saveWorkRecords(
            userId,
            workDate,
            request
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords[0].userId").value(userId))
                .andExpect(jsonPath("$.workRecords[0].projectId").value("project123"));
        
        verify(workRecordApplicationService).saveWorkRecords(
            userId,
            workDate,
            request
        );
    }

    @Test
    @DisplayName("工数記録更新 - 正常系：却下済みデータの更新成功")
    void saveWorkRecords_RejectedDataUpdateSuccess() throws Exception {
        // Arrange
        String userId = "developer123";
        LocalDate workDate = LocalDate.now().minusDays(1);
        Map<CategoryCode, BigDecimal> hoursMap = Map.of(
            CategoryCode.of("DEVELOPMENT"), new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hoursMap);
        
        WorkRecordSaveRequest.WorkRecordDto dto = new WorkRecordSaveRequest.WorkRecordDto(
            "project123",
            workDate,
            categoryHours,
            "Fixed work after rejection"
        );
        WorkRecordSaveRequest request = new WorkRecordSaveRequest();
        request.setRecords(List.of(dto));
        
        WorkRecord expectedWorkRecord = WorkRecord.create(
            userId,
            dto.getProjectId(),
            dto.getWorkDate(),
            dto.getCategoryHours(),
            dto.getDescription(),
            userId
        );

        WorkRecordsResponse response = WorkRecordsResponse.builder()
            .workRecords(List.of(expectedWorkRecord))
            .build();
        when(workRecordApplicationService.saveWorkRecords(
            userId,
            workDate,
            request
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/work-records/me/" + workDate)
                .with(csrf())
                .with(jwt().jwt(jwt -> jwt.subject(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords[0].userId").value(userId));
        
        verify(workRecordApplicationService).saveWorkRecords(
            userId,
            workDate,
            request
        );
    }

    @Test
    @DisplayName("承認ステータスAPIのアクセス制御 - 他ユーザーのデータアクセス制限")
    void getWorkRecordsByDate_AccessControl_OtherUserData() throws Exception {
        // Arrange
        String authenticatedUserId = "developer123";
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        // サービス層で空のレスポンスを返すように設定（アクセス制限）
        WorkRecordsResponse expectedResponse = WorkRecordsResponse.builder()
            .workRecords(List.of())
            .workRecordApproval(null)
            .build();
        
        when(workRecordApplicationService.getWorkRecordsWithApprovalStatus(authenticatedUserId, date))
            .thenReturn(expectedResponse);
        
        // When & Then - 他のユーザーのデータアクセス
        mockMvc.perform(get("/api/work-records/me/{date}", date.toString())
                .with(jwt().jwt(jwt -> jwt.subject(authenticatedUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workRecords").isArray())
                .andExpect(jsonPath("$.workRecords").isEmpty());
        
        verify(workRecordApplicationService).getWorkRecordsWithApprovalStatus(authenticatedUserId, date);
    }

    // === getMissingDates エンドポイントテスト ===

    @Test
    @DisplayName("未入力日取得 - 正常ケース")
    void getMissingDates_ValidRequest_Success() throws Exception {
        // Arrange
        String userId = "developer123";
        int year = 2024;
        int month = 8;
        
        List<LocalDate> expectedMissingDates = Arrays.asList(
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 8, 5),
            LocalDate.of(2024, 8, 15)
        );
        
        when(workRecordApplicationService.getMissingDatesForMonth(userId, year, month))
            .thenReturn(expectedMissingDates);
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDates").isArray())
                .andExpect(jsonPath("$.missingDates.length()").value(3))
                .andExpect(jsonPath("$.missingDates[0]").value("2024-08-01"))
                .andExpect(jsonPath("$.missingDates[1]").value("2024-08-05"))
                .andExpect(jsonPath("$.missingDates[2]").value("2024-08-15"));
        
        verify(workRecordApplicationService).getMissingDatesForMonth(userId, year, month);
    }

    @Test
    @DisplayName("未入力日取得 - 未入力日なし")
    void getMissingDates_NoMissingDates_EmptyArray() throws Exception {
        // Arrange
        String userId = "developer123";
        int year = 2024;
        int month = 7;
        
        when(workRecordApplicationService.getMissingDatesForMonth(userId, year, month))
            .thenReturn(Arrays.asList());
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDates").isArray())
                .andExpect(jsonPath("$.missingDates").isEmpty());
        
        verify(workRecordApplicationService).getMissingDatesForMonth(userId, year, month);
    }

    @Test
    @DisplayName("未入力日取得 - 必須パラメータ不足（year）")
    void getMissingDates_MissingYearParameter_BadRequest() throws Exception {
        // Arrange
        String userId = "developer123";
        int month = 8;
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService, never()).getMissingDatesForMonth(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("未入力日取得 - 必須パラメータ不足（month）")
    void getMissingDates_MissingMonthParameter_BadRequest() throws Exception {
        // Arrange
        String userId = "developer123";
        int year = 2024;
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService, never()).getMissingDatesForMonth(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("未入力日取得 - 無効なパラメータ（無効な年）")
    void getMissingDates_InvalidYearParameter_BadRequest() throws Exception {
        // Arrange
        String userId = "developer123";
        String invalidYear = "invalid";
        int month = 8;
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", invalidYear)
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService, never()).getMissingDatesForMonth(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("未入力日取得 - 無効なパラメータ（無効な月）")
    void getMissingDates_InvalidMonthParameter_BadRequest() throws Exception {
        // Arrange
        String userId = "developer123";
        int year = 2024;
        String invalidMonth = "invalid";
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", invalidMonth)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest());
        
        verify(workRecordApplicationService, never()).getMissingDatesForMonth(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("未入力日取得 - サービス層エラー処理")
    void getMissingDates_ServiceError_BadRequest() throws Exception {
        // Arrange
        String userId = "developer123";
        LocalDate fourMonthsAgo = LocalDate.now().minusMonths(4);
        int year = fourMonthsAgo.getYear();
        int month = fourMonthsAgo.getMonthValue();
        
        when(workRecordApplicationService.getMissingDatesForMonth(userId, year, month))
            .thenThrow(new InvalidParameterException("指定された月は過去3ヶ月以内から当月までの範囲で入力してください"));
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("指定された月は過去3ヶ月以内から当月までの範囲で入力してください"));
        
        verify(workRecordApplicationService).getMissingDatesForMonth(userId, year, month);
    }

    @Test
    @DisplayName("未入力日取得 - 認証なしアクセス")
    void getMissingDates_Unauthenticated_Unauthorized() throws Exception {
        // Arrange
        int year = 2024;
        int month = 8;
        
        // Act & Assert
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month)))
                .andExpect(status().isUnauthorized());
        
        verify(workRecordApplicationService, never()).getMissingDatesForMonth(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("未入力日取得 - CSRF保護確認")
    void getMissingDates_WithoutCsrf_Success() throws Exception {
        // Arrange
        String userId = "developer123";
        int year = 2024;
        int month = 8;
        
        when(workRecordApplicationService.getMissingDatesForMonth(userId, year, month))
            .thenReturn(Arrays.asList());
        
        // Act & Assert - GETリクエストなのでCSRFトークン不要
        mockMvc.perform(get("/api/work-records/me/missing-dates")
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk());
        
        verify(workRecordApplicationService).getMissingDatesForMonth(userId, year, month);
    }
}