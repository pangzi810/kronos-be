package com.devhour.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;

@DisplayName("ApprovalHistory エンティティのテスト")
class WorkRecordApprovalHistoryTest {
    
    @Test
    @DisplayName("createFromWorkRecord - 承認アクション履歴の作成")
    void testCreateFromWorkRecord_Approve() {
        // Arrange
        WorkRecord workRecord = createTestWorkRecord();
        String eventId = "test-event-id";
        String action = "APPROVE";
        String previousStatus = "PENDING";
        String approverId = "supervisor-001";
        String rejectionReason = null;
        String workRecordSnapshot = "{\"snapshot\":\"test\"}";
        
        // Act
        WorkRecordApprovalHistory history = WorkRecordApprovalHistory.createFromWorkRecord(
            eventId, workRecord, action, previousStatus, approverId, rejectionReason, workRecordSnapshot);
        
        // Assert
        assertNotNull(history);
        assertEquals(eventId, history.getHistoryId());
        assertEquals(workRecord.getId(), history.getWorkRecordId());
        assertEquals(workRecord.getUserId(), history.getUserId());
        assertEquals(workRecord.getProjectId(), history.getProjectId());
        assertEquals(workRecord.getWorkDate(), history.getWorkDate());
        assertEquals(new BigDecimal("8.0"), history.getTotalHours());
        assertEquals(workRecord.getDescription(), history.getDescription());
        assertEquals(action, history.getAction());
        assertEquals(previousStatus, history.getPreviousStatus());
        assertEquals("PENDING", history.getCurrentStatus());
        assertEquals(approverId, history.getApproverId());
        assertEquals(rejectionReason, history.getRejectionReason());
        assertEquals(workRecordSnapshot, history.getWorkRecordSnapshot());
        assertNotNull(history.getOccurredAt());
        assertNotNull(history.getCreatedAt());
        assertTrue(history.isApprovalAction());
        assertFalse(history.isRejectionAction());
        assertFalse(history.isResubmitAction());
    }
    
    @Test
    @DisplayName("createFromWorkRecord - 却下アクション履歴の作成")
    void testCreateFromWorkRecord_Reject() {
        // Arrange
        WorkRecord workRecord = createTestWorkRecord();
        String eventId = "test-event-id";
        String action = "REJECT";
        String previousStatus = "PENDING";
        String approverId = "supervisor-001";
        String rejectionReason = "工数が不正確です";
        String workRecordSnapshot = "{\"snapshot\":\"test\"}";
        
        // Act
        WorkRecordApprovalHistory history = WorkRecordApprovalHistory.createFromWorkRecord(
            eventId, workRecord, action, previousStatus, approverId, rejectionReason, workRecordSnapshot);
        
        // Assert
        assertNotNull(history);
        assertEquals(rejectionReason, history.getRejectionReason());
        assertEquals(action, history.getAction());
        assertFalse(history.isApprovalAction());
        assertTrue(history.isRejectionAction());
        assertFalse(history.isResubmitAction());
    }
    
    @Test
    @DisplayName("createFromWorkRecord - 再申請アクション履歴の作成")
    void testCreateFromWorkRecord_Resubmit() {
        // Arrange
        WorkRecord workRecord = createTestWorkRecord();
        String eventId = "test-event-id";
        String action = "RESUBMIT";
        String previousStatus = "REJECTED";
        String approverId = null;
        String rejectionReason = null;
        String workRecordSnapshot = "{\"snapshot\":\"test\"}";
        
        // Act
        WorkRecordApprovalHistory history = WorkRecordApprovalHistory.createFromWorkRecord(
            eventId, workRecord, action, previousStatus, approverId, rejectionReason, workRecordSnapshot);
        
        // Assert
        assertNotNull(history);
        assertEquals(action, history.getAction());
        assertEquals(previousStatus, history.getPreviousStatus());
        assertFalse(history.isApprovalAction());
        assertFalse(history.isRejectionAction());
        assertTrue(history.isResubmitAction());
    }
    
    @Test
    @DisplayName("createFromWorkRecord - CategoryHoursがnullの場合")
    void testCreateFromWorkRecord_WithNullCategoryHours() {
        // Arrange
        LocalDate testDate = LocalDate.now().minusDays(1);
        WorkRecord workRecord = WorkRecord.restore(
            "test-id", "user-001", "project-001", testDate,
            null, // CategoryHours is null
            "Test description", "user-001",
            testDate.atStartOfDay(), "user-001", testDate.atStartOfDay()
        );
        
        // Act
        WorkRecordApprovalHistory history = WorkRecordApprovalHistory.createFromWorkRecord(
            "event-001", workRecord, "APPROVE", "PENDING", "supervisor-001", null, "{}");
        
        // Assert
        assertEquals(BigDecimal.ZERO, history.getTotalHours());
        assertEquals("{}", history.getCategoryHours());
    }
    
    @Test
    @DisplayName("restore - 既存の承認履歴の復元")
    void testRestore() {
        // Arrange
        String historyId = "history-001";
        String workRecordId = "work-record-001";
        String userId = "user-001";
        String projectId = "project-001";
        LocalDate workDate = LocalDate.now().minusDays(1);
        String categoryHours = "{\"DEVELOPMENT\":\"8.0\"}";
        BigDecimal totalHours = new BigDecimal("8.0");
        String description = "Test work";
        String action = "APPROVE";
        String previousStatus = "PENDING";
        String currentStatus = "APPROVED";
        String approverId = "supervisor-001";
        String rejectionReason = null;
        String workRecordSnapshot = "{\"snapshot\":\"test\"}";
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        WorkRecordApprovalHistory history = WorkRecordApprovalHistory.restore(
            historyId, workRecordId, userId, projectId, workDate, categoryHours,
            totalHours, description, action, previousStatus, currentStatus,
            approverId, rejectionReason, workRecordSnapshot, occurredAt, createdAt);
        
        // Assert
        assertNotNull(history);
        assertEquals(historyId, history.getHistoryId());
        assertEquals(workRecordId, history.getWorkRecordId());
        assertEquals(userId, history.getUserId());
        assertEquals(projectId, history.getProjectId());
        assertEquals(workDate, history.getWorkDate());
        assertEquals(categoryHours, history.getCategoryHours());
        assertEquals(totalHours, history.getTotalHours());
        assertEquals(description, history.getDescription());
        assertEquals(action, history.getAction());
        assertEquals(previousStatus, history.getPreviousStatus());
        assertEquals(currentStatus, history.getCurrentStatus());
        assertEquals(approverId, history.getApproverId());
        assertEquals(rejectionReason, history.getRejectionReason());
        assertEquals(workRecordSnapshot, history.getWorkRecordSnapshot());
        assertEquals(occurredAt, history.getOccurredAt());
        assertEquals(createdAt, history.getCreatedAt());
    }
    
    @Test
    @DisplayName("isApprovalAction - 承認アクション判定")
    void testIsApprovalAction() {
        // Arrange & Act & Assert
        WorkRecordApprovalHistory approveHistory = createTestApprovalHistory("APPROVE");
        assertTrue(approveHistory.isApprovalAction());
        
        WorkRecordApprovalHistory rejectHistory = createTestApprovalHistory("REJECT");
        assertFalse(rejectHistory.isApprovalAction());
        
        WorkRecordApprovalHistory resubmitHistory = createTestApprovalHistory("RESUBMIT");
        assertFalse(resubmitHistory.isApprovalAction());
    }
    
    @Test
    @DisplayName("isRejectionAction - 却下アクション判定")
    void testIsRejectionAction() {
        // Arrange & Act & Assert
        WorkRecordApprovalHistory approveHistory = createTestApprovalHistory("APPROVE");
        assertFalse(approveHistory.isRejectionAction());
        
        WorkRecordApprovalHistory rejectHistory = createTestApprovalHistory("REJECT");
        assertTrue(rejectHistory.isRejectionAction());
        
        WorkRecordApprovalHistory resubmitHistory = createTestApprovalHistory("RESUBMIT");
        assertFalse(resubmitHistory.isRejectionAction());
    }
    
    @Test
    @DisplayName("isResubmitAction - 再申請アクション判定")
    void testIsResubmitAction() {
        // Arrange & Act & Assert
        WorkRecordApprovalHistory approveHistory = createTestApprovalHistory("APPROVE");
        assertFalse(approveHistory.isResubmitAction());
        
        WorkRecordApprovalHistory rejectHistory = createTestApprovalHistory("REJECT");
        assertFalse(rejectHistory.isResubmitAction());
        
        WorkRecordApprovalHistory resubmitHistory = createTestApprovalHistory("RESUBMIT");
        assertTrue(resubmitHistory.isResubmitAction());
    }
    
    @Test
    @DisplayName("equals - IDベースの等価性判定")
    void testEquals() {
        // Arrange
        WorkRecordApprovalHistory history1 = createTestApprovalHistory("APPROVE");
        WorkRecordApprovalHistory history2 = createTestApprovalHistory("REJECT");
        WorkRecordApprovalHistory history3 = WorkRecordApprovalHistory.restore(
            history1.getHistoryId(), "different-work-record", "different-user", 
            "different-project", LocalDate.now(), "{}", BigDecimal.ZERO, 
            "different description", "DIFFERENT", "DIFFERENT", "DIFFERENT",
            "different-approver", "different reason", "{}", 
            LocalDateTime.now(), LocalDateTime.now());
        
        // Act & Assert
        assertTrue(history1.equals(history1)); // 自己参照
        assertFalse(history1.equals(history2)); // 異なるID
        assertTrue(history1.equals(history3)); // 同じID
        assertFalse(history1.equals(null)); // null
        assertFalse(history1.equals("string")); // 異なるクラス
    }
    
    @Test
    @DisplayName("hashCode - IDベースのハッシュコード")
    void testHashCode() {
        // Arrange
        WorkRecordApprovalHistory history1 = createTestApprovalHistory("APPROVE");
        WorkRecordApprovalHistory history2 = WorkRecordApprovalHistory.restore(
            history1.getHistoryId(), "different-work-record", "different-user", 
            "different-project", LocalDate.now(), "{}", BigDecimal.ZERO, 
            "different description", "DIFFERENT", "DIFFERENT", "DIFFERENT",
            "different-approver", "different reason", "{}", 
            LocalDateTime.now(), LocalDateTime.now());
        
        // Act & Assert
        assertEquals(history1.hashCode(), history2.hashCode()); // 同じID
    }
    
    @Test
    @DisplayName("toString - 文字列表現")
    void testToString() {
        // Arrange
        WorkRecordApprovalHistory history = createTestApprovalHistory("APPROVE");
        
        // Act
        String result = history.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("ApprovalHistory"));
        assertTrue(result.contains(history.getHistoryId()));
        assertTrue(result.contains(history.getWorkRecordId()));
        assertTrue(result.contains(history.getAction()));
        assertTrue(result.contains(history.getCurrentStatus()));
    }
    
    // Helper methods
    
    private WorkRecord createTestWorkRecord() {
        LocalDate testDate = LocalDate.now().minusDays(1);
        Map<CategoryCode, BigDecimal> hours = Map.of(
            CategoryCode.of("DEVELOPMENT"), new BigDecimal("8.0")
        );
        CategoryHours categoryHours = CategoryHours.of(hours);
        
        return WorkRecord.restore(
            "work-record-001",
            "user-001",
            "project-001",
            testDate,
            categoryHours,
            "Test work description",
            "user-001",
            testDate.atStartOfDay(),
            "user-001",
            testDate.atStartOfDay()
        );
    }
    
    private WorkRecordApprovalHistory createTestApprovalHistory(String action) {
        return WorkRecordApprovalHistory.restore(
            "history-" + action.toLowerCase(),
            "work-record-001",
            "user-001",
            "project-001",
            LocalDate.now().minusDays(1),
            "{\"DEVELOPMENT\":\"8.0\"}",
            new BigDecimal("8.0"),
            "Test description",
            action,
            "PENDING",
            "APPROVED",
            "supervisor-001",
            action.equals("REJECT") ? "Test rejection reason" : null,
            "{\"snapshot\":\"test\"}",
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
    }
}