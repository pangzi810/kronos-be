package com.devhour.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ドメイン例外クラスのテスト
 */
@DisplayName("Domain Exceptions")
class ExceptionTest {
    
    @Test
    @DisplayName("EntityNotFoundException - userNotFound")
    void entityNotFoundException_UserNotFound() {
        // Act
        EntityNotFoundException exception = EntityNotFoundException.userNotFound("user123");
        
        // Assert
        assertEquals("User not found with identifier: user123", exception.getMessage());
        assertEquals("user123", exception.getIdentifier());
        assertEquals("User", exception.getEntityType());
    }
    
    @Test
    @DisplayName("EntityNotFoundException - projectNotFound")
    void entityNotFoundException_ProjectNotFound() {
        // Act
        EntityNotFoundException exception = EntityNotFoundException.projectNotFound("project123");
        
        // Assert
        assertEquals("Project not found with identifier: project123", exception.getMessage());
        assertEquals("project123", exception.getIdentifier());
        assertEquals("Project", exception.getEntityType());
    }
    
    @Test
    @DisplayName("EntityNotFoundException - workRecordNotFound")
    void entityNotFoundException_WorkRecordNotFound() {
        // Act
        EntityNotFoundException exception = EntityNotFoundException.workRecordNotFound("record123");
        
        // Assert
        assertEquals("WorkRecord not found with identifier: record123", exception.getMessage());
        assertEquals("record123", exception.getIdentifier());
        assertEquals("WorkRecord", exception.getEntityType());
    }
    
    @Test
    @DisplayName("EntityNotFoundException - approverNotFound")
    void entityNotFoundException_ApproverNotFound() {
        // Act
        EntityNotFoundException exception = EntityNotFoundException.approverNotFound("rel123");
        
        // Assert
        assertEquals("Approver not found with identifier: rel123", exception.getMessage());
        assertEquals("rel123", exception.getIdentifier());
        assertEquals("Approver", exception.getEntityType());
    }
    
    @Test
    @DisplayName("EntityNotFoundException - workCategoryNotFound")
    void entityNotFoundException_WorkCategoryNotFound() {
        // Act
        EntityNotFoundException exception = EntityNotFoundException.workCategoryNotFound("DEV");
        
        // Assert
        assertEquals("WorkCategory not found with identifier: DEV", exception.getMessage());
        assertEquals("DEV", exception.getIdentifier());
        assertEquals("WorkCategory", exception.getEntityType());
    }
    
    
    @Test
    @DisplayName("EntityNotFoundException - Constructor with all parameters")
    void entityNotFoundException_FullConstructor() {
        // Act
        EntityNotFoundException exception = new EntityNotFoundException("Custom", "custom123");
        
        // Assert
        assertEquals("Custom not found with identifier: custom123", exception.getMessage());
        assertEquals("custom123", exception.getIdentifier());
        assertEquals("Custom", exception.getEntityType());
    }
    
    
    
}