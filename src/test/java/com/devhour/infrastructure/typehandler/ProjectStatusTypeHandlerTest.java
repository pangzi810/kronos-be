package com.devhour.infrastructure.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void testSetNonNullParameter_Draft() throws SQLException {
        ProjectStatus status = ProjectStatus.DRAFT;

        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);

        verify(preparedStatement).setString(1, "DRAFT");
    }

    @Test
    void testSetNonNullParameter_InProgress() throws SQLException {
        ProjectStatus status = ProjectStatus.IN_PROGRESS;

        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);

        verify(preparedStatement).setString(1, "IN_PROGRESS");
    }

    @Test
    void testSetNonNullParameter_Closed() throws SQLException {
        ProjectStatus status = ProjectStatus.CLOSED;

        typeHandler.setNonNullParameter(preparedStatement, 1, status, null);

        verify(preparedStatement).setString(1, "CLOSED");
    }
    
    @Test
    void testGetNullableResult_ByColumnName_Draft() throws SQLException {
        when(resultSet.getString("status")).thenReturn("DRAFT");

        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");

        assertNotNull(result);
        assertEquals(ProjectStatus.DRAFT, result);
        assertEquals("DRAFT", result.value());
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
    void testGetNullableResult_ByColumnName_Closed() throws SQLException {
        when(resultSet.getString("status")).thenReturn("CLOSED");

        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");

        assertNotNull(result);
        assertEquals(ProjectStatus.CLOSED, result);
        assertEquals("CLOSED", result.value());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_NullValue() throws SQLException {
        when(resultSet.getString("status")).thenReturn(null);
        
        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");
        
        assertNull(result);
    }
    
    @Test
    void testGetNullableResult_ByColumnIndex() throws SQLException {
        when(resultSet.getString(1)).thenReturn("DRAFT");

        ProjectStatus result = typeHandler.getNullableResult(resultSet, 1);

        assertNotNull(result);
        assertEquals(ProjectStatus.DRAFT, result);
    }

    @Test
    void testGetNullableResult_ByColumnIndex_NullValue() throws SQLException {
        when(resultSet.getString(1)).thenReturn(null);

        ProjectStatus result = typeHandler.getNullableResult(resultSet, 1);

        assertNull(result);
    }

    @Test
    void testGetNullableResult_CallableStatement() throws SQLException {
        when(callableStatement.getString(1)).thenReturn("CLOSED");

        ProjectStatus result = typeHandler.getNullableResult(callableStatement, 1);

        assertNotNull(result);
        assertEquals(ProjectStatus.CLOSED, result);
    }
    
    @Test
    void testGetNullableResult_CallableStatement_NullValue() throws SQLException {
        when(callableStatement.getString(1)).thenReturn(null);
        
        ProjectStatus result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNull(result);
    }
    
    @Test
    void testGetNullableResult_LowerCase() throws SQLException {
        when(resultSet.getString("status")).thenReturn("draft");

        ProjectStatus result = typeHandler.getNullableResult(resultSet, "status");

        assertNotNull(result);
        assertEquals(ProjectStatus.DRAFT, result);
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