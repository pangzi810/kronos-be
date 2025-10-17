package com.devhour.domain.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;

/**
 * WorkRecordApprovalEventのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkRecordApprovalEvent")
class WorkRecordApprovalEventTest {
    
    @Mock
    private WorkRecordApproval approval;
    
    @Mock
    private WorkRecord workRecord1;
    
    @Mock
    private WorkRecord workRecord2;
    
    private CategoryHours categoryHours;
    
    @BeforeEach
    void setUp() {
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("DESIGN"), new BigDecimal("2.5"),
            CategoryCode.of("CODING"), new BigDecimal("3.5")
        );
        categoryHours = new CategoryHours(hours);
    }
    
    @Test
    @DisplayName("承認イベント生成 - 正常ケース")
    void createForApproval_Success() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.now();
        String approverId = "approver1";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        
        when(workRecord1.getId()).thenReturn("wr1");
        when(workRecord1.getProjectId()).thenReturn("proj1");
        when(workRecord1.getCategoryHours()).thenReturn(categoryHours);
        when(workRecord1.getDescription()).thenReturn("作業内容1");
        
        when(workRecord2.getId()).thenReturn("wr2");
        when(workRecord2.getProjectId()).thenReturn("proj2");
        when(workRecord2.getCategoryHours()).thenReturn(null);
        when(workRecord2.getDescription()).thenReturn("作業内容2");
        
        List<WorkRecord> workRecords = List.of(workRecord1, workRecord2);
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        
        // Assert
        assertNotNull(event);
        assertEquals(userId, event.userId());
        assertEquals(workDate, event.workDate());
        assertEquals("APPROVE", event.action());
        assertEquals(approverId, event.approverId());
        assertNull(event.rejectionReason());
        assertEquals("APPROVED", event.currentStatus());
        assertNotNull(event.targetWorkRecords());
        assertEquals(2, event.targetWorkRecords().size());
        
        // First work record snapshot
        var snapshot1 = event.targetWorkRecords().get(0);
        assertEquals("wr1", snapshot1.workRecordId());
        assertEquals("proj1", snapshot1.projectId());
        assertNotNull(snapshot1.categoryHoursJson());
        assertEquals(new BigDecimal("6.0"), snapshot1.totalHours());
        assertEquals("作業内容1", snapshot1.description());
        
        // Second work record snapshot (with null categoryHours)
        var snapshot2 = event.targetWorkRecords().get(1);
        assertEquals("wr2", snapshot2.workRecordId());
        assertEquals("proj2", snapshot2.projectId());
        assertEquals("{}", snapshot2.categoryHoursJson());
        assertEquals(BigDecimal.ZERO, snapshot2.totalHours());
        assertEquals("作業内容2", snapshot2.description());
    }
    
    @Test
    @DisplayName("却下イベント生成 - 正常ケース")
    void createForRejection_Success() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.now();
        String approverId = "approver1";
        String rejectionReason = "工数が不正確です";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        when(approval.getRejectionReason()).thenReturn(rejectionReason);
        
        when(workRecord1.getId()).thenReturn("wr1");
        when(workRecord1.getProjectId()).thenReturn("proj1");
        when(workRecord1.getCategoryHours()).thenReturn(categoryHours);
        when(workRecord1.getDescription()).thenReturn("作業内容1");
        
        List<WorkRecord> workRecords = List.of(workRecord1);
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForRejection(approval, workRecords);
        
        // Assert
        assertNotNull(event);
        assertEquals(userId, event.userId());
        assertEquals(workDate, event.workDate());
        assertEquals("REJECT", event.action());
        assertEquals(approverId, event.approverId());
        assertEquals(rejectionReason, event.rejectionReason());
        assertEquals("REJECTED", event.currentStatus());
        assertNotNull(event.targetWorkRecords());
        assertEquals(1, event.targetWorkRecords().size());
    }
    
    @Test
    @DisplayName("イベントID生成の確認")
    void eventId_Format() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.of(2024, 1, 15);
        String approverId = "approver1";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        
        List<WorkRecord> workRecords = List.of();
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        
        // Assert
        assertNotNull(event.eventId());
        assertTrue(event.eventId().startsWith("user1-2024-01-15-"));
    }
    
    @Test
    @DisplayName("WorkRecordSnapshot生成 - CategoryHoursがnullの場合")
    void workRecordSnapshot_NullCategoryHours() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.now();
        String approverId = "approver1";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        
        when(workRecord1.getId()).thenReturn("wr1");
        when(workRecord1.getProjectId()).thenReturn("proj1");
        when(workRecord1.getCategoryHours()).thenReturn(null);
        when(workRecord1.getDescription()).thenReturn("作業内容");
        
        List<WorkRecord> workRecords = List.of(workRecord1);
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        
        // Assert
        var snapshot = event.targetWorkRecords().get(0);
        assertEquals("{}", snapshot.categoryHoursJson());
        assertEquals(BigDecimal.ZERO, snapshot.totalHours());
        assertEquals("PENDING", snapshot.approvalStatus());
        assertEquals(0, snapshot.version());
    }
    
    @Test
    @DisplayName("occurredAtの確認")
    void occurredAt_IsRecent() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.now();
        String approverId = "approver1";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        
        List<WorkRecord> workRecords = List.of();
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        // Assert
        assertNotNull(event.occurredAt());
        assertTrue(event.occurredAt().isAfter(beforeCreation));
        assertTrue(event.occurredAt().isBefore(afterCreation));
    }
    
    @Test
    @DisplayName("複数の工数記録のスナップショット作成")
    void multipleWorkRecordSnapshots() {
        // Arrange
        String userId = "user1";
        LocalDate workDate = LocalDate.now();
        String approverId = "approver1";
        
        when(approval.getUserId()).thenReturn(userId);
        when(approval.getWorkDate()).thenReturn(workDate);
        when(approval.getApproverId()).thenReturn(approverId);
        
        // Setup multiple work records
        WorkRecord wr1 = mock(WorkRecord.class);
        WorkRecord wr2 = mock(WorkRecord.class);
        WorkRecord wr3 = mock(WorkRecord.class);
        
        when(wr1.getId()).thenReturn("wr1");
        when(wr1.getProjectId()).thenReturn("proj1");
        when(wr1.getCategoryHours()).thenReturn(categoryHours);
        when(wr1.getDescription()).thenReturn("作業1");
        
        when(wr2.getId()).thenReturn("wr2");
        when(wr2.getProjectId()).thenReturn("proj2");
        when(wr2.getCategoryHours()).thenReturn(categoryHours);
        when(wr2.getDescription()).thenReturn("作業2");
        
        when(wr3.getId()).thenReturn("wr3");
        when(wr3.getProjectId()).thenReturn("proj3");
        when(wr3.getCategoryHours()).thenReturn(null);
        when(wr3.getDescription()).thenReturn("作業3");
        
        List<WorkRecord> workRecords = List.of(wr1, wr2, wr3);
        
        // Act
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForRejection(approval, workRecords);
        
        // Assert
        assertEquals(3, event.targetWorkRecords().size());
        assertEquals("REJECT", event.action());
        assertEquals("REJECTED", event.currentStatus());
        
        // Verify each snapshot
        assertEquals("wr1", event.targetWorkRecords().get(0).workRecordId());
        assertEquals("wr2", event.targetWorkRecords().get(1).workRecordId());
        assertEquals("wr3", event.targetWorkRecords().get(2).workRecordId());
    }
}