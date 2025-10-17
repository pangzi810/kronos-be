package com.devhour.infrastructure.typehandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.valueobject.ProjectStatus;

@ExtendWith(MockitoExtension.class)
class ProjectStatusTypeHandlerTest {

    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private CallableStatement callableStatement;
    
    private ProjectStatusTypeHandler typeHandler;
    
    @BeforeEach
    void setUp() {
        typeHandler = new ProjectStatusTypeHandler();
    }
    
    @Test
    void testSetNonNullParameter_Planning() throws SQLException {
        ProjectStatus status = ProjectStatus.PLANNING;
        
        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);
        
        verify(preparedStatement).setString(1, "PLANNING");
    }
    
    @Test
    void testSetNonNullParameter_InProgress() throws SQLException {
        ProjectStatus status = ProjectStatus.IN_PROGRESS;
        
        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);
        
        verify(preparedStatement).setString(1, "IN_PROGRESS");
    }
    
    @Test
    void testSetNonNullParameter_Completed() throws SQLException {
        ProjectStatus status = ProjectStatus.COMPLETED;
        
        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);
        
        verify(preparedStatement).setString(1, "COMPLETED");
    }
    
    @Test
    void testSetNonNullParameter_Cancelled() throws SQLException {
        ProjectStatus status = ProjectStatus.CANCELLED;
        
        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);
        
        verify(preparedStatement).setString(1, "CANCELLED");
    }
    
    @Test
    void testGetNullableResult_ByColumnName_Planning() throws SQLException {
        when(resultSet.getString("status")).thenReturn("PLANNING");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.PLANNING, result);
        assertEquals("PLANNING", result.value());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_InProgress() throws SQLException {
        when(resultSet.getString("status")).thenReturn("IN_PROGRESS");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result);
        assertEquals("IN_PROGRESS", result.value());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_Completed() throws SQLException {
        when(resultSet.getString("status")).thenReturn("COMPLETED");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.COMPLETED, result);
        assertEquals("COMPLETED", result.value());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_Cancelled() throws SQLException {
        when(resultSet.getString("status")).thenReturn("CANCELLED");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.CANCELLED, result);
        assertEquals("CANCELLED", result.value());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_NullValue() throws SQLException {
        when(resultSet.getString("status")).thenReturn(null);
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNull(result);
    }
    
    @Test
    void testGetNullableResult_ByColumnIndex() throws SQLException {
        when(resultSet.getString(1)).thenReturn("PLANNING");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNotNull(result);
        assertEquals(ProjectStatus.PLANNING, result);
    }
    
    @Test
    void testGetNullableResult_ByColumnIndex_NullValue() throws SQLException {
        when(resultSet.getString(1)).thenReturn(null);
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNull(result);
    }
    
    @Test
    void testGetNullableResult_CallableStatement() throws SQLException {
        when(callableStatement.getString(1)).thenReturn("COMPLETED");
        
        ProjectStatus result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNotNull(result);
        assertEquals(ProjectStatus.COMPLETED, result);
    }
    
    @Test
    void testGetNullableResult_CallableStatement_NullValue() throws SQLException {
        when(callableStatement.getString(1)).thenReturn(null);
        
        ProjectStatus result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNull(result);
    }
    
    @Test
    void testGetNullableResult_LowerCase() throws SQLException {
        when(resultSet.getString("status")).thenReturn("planning");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.PLANNING, result);
    }
    
    @Test
    void testGetNullableResult_MixedCase() throws SQLException {
        when(resultSet.getString("status")).thenReturn("In_Progress");
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result);
    }
    
    @Test
    void testGetNullableResult_InvalidStatus() throws SQLException {
        when(resultSet.getString("status")).thenReturn("INVALID_STATUS");
        
        assertThrows(Exception.class, () -> {
            typeHandler.getNullableResult(resultSet, "status");
        });
    }
}