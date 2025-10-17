package com.devhour.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ApprovalStatusのテストクラス
 */
@DisplayName("ApprovalStatus")
class ApprovalStatusTest {
    
    @Test
    @DisplayName("PENDING status - 正常ケース")
    void pending_Status() {
        // Act
        ApprovalStatus status = ApprovalStatus.PENDING;
        
        // Assert
        assertEquals("PENDING", status.getValue());
        assertEquals("承認待ち", status.getDisplayName());
        assertFalse(status.isApproved());
        assertFalse(status.isRejected());
        assertTrue(status.isPending());
        assertTrue(status.isEditable());
    }
    
    @Test
    @DisplayName("APPROVED status - 正常ケース")
    void approved_Status() {
        // Act
        ApprovalStatus status = ApprovalStatus.APPROVED;
        
        // Assert
        assertEquals("APPROVED", status.getValue());
        assertEquals("承認済み", status.getDisplayName());
        assertTrue(status.isApproved());
        assertFalse(status.isRejected());
        assertFalse(status.isPending());
        assertFalse(status.isEditable());
    }
    
    @Test
    @DisplayName("REJECTED status - 正常ケース")
    void rejected_Status() {
        // Act
        ApprovalStatus status = ApprovalStatus.REJECTED;
        
        // Assert
        assertEquals("REJECTED", status.getValue());
        assertEquals("却下", status.getDisplayName());
        assertFalse(status.isApproved());
        assertTrue(status.isRejected());
        assertFalse(status.isPending());
        assertTrue(status.isEditable());
    }
    
    @Test
    @DisplayName("fromValue - 有効な値")
    void fromValue_ValidValue() {
        // Act & Assert
        assertEquals(ApprovalStatus.PENDING, ApprovalStatus.fromValue("PENDING"));
        assertEquals(ApprovalStatus.APPROVED, ApprovalStatus.fromValue("APPROVED"));
        assertEquals(ApprovalStatus.REJECTED, ApprovalStatus.fromValue("REJECTED"));
    }
    
    @Test
    @DisplayName("fromValue - 無効な値")
    void fromValue_InvalidValue() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ApprovalStatus.fromValue("INVALID")
        );
        
        assertEquals("Invalid approval status value: INVALID", exception.getMessage());
    }
    
    @Test
    @DisplayName("fromValue - null値")
    void fromValue_NullValue() {
        // Act
        ApprovalStatus result = ApprovalStatus.fromValue(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("values - 全ての値が含まれる")
    void values_AllIncluded() {
        // Act
        ApprovalStatus[] values = ApprovalStatus.values();
        
        // Assert
        assertEquals(4, values.length);
        assertNotNull(ApprovalStatus.valueOf("NOT_ENTERED"));
        assertNotNull(ApprovalStatus.valueOf("PENDING"));
        assertNotNull(ApprovalStatus.valueOf("APPROVED"));
        assertNotNull(ApprovalStatus.valueOf("REJECTED"));
    }
}