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

import com.devhour.domain.model.valueobject.DisplayOrder;

@ExtendWith(MockitoExtension.class)
class DisplayOrderTypeHandlerTest {

    private DisplayOrderTypeHandler typeHandler;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        typeHandler = new DisplayOrderTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        DisplayOrder displayOrder = DisplayOrder.of(5);
        
        typeHandler.setNonNullParameter(preparedStatement, 1, displayOrder, null);
        
        verify(preparedStatement).setInt(1, 5);
    }

    @Test
    void testGetNullableResultFromResultSet() throws SQLException {
        when(resultSet.getInt("column")).thenReturn(3);
        when(resultSet.wasNull()).thenReturn(false);
        
        DisplayOrder result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNotNull(result);
        assertEquals(3, result.value());
    }

    @Test
    void testGetNullableResultFromResultSet_NullValue() throws SQLException {
        when(resultSet.getInt("column")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        
        DisplayOrder result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromResultSetByIndex() throws SQLException {
        when(resultSet.getInt(1)).thenReturn(7);
        when(resultSet.wasNull()).thenReturn(false);
        
        DisplayOrder result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNotNull(result);
        assertEquals(7, result.value());
    }

    @Test
    void testGetNullableResultFromResultSetByIndex_NullValue() throws SQLException {
        when(resultSet.getInt(1)).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        
        DisplayOrder result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        when(callableStatement.getInt(1)).thenReturn(2);
        when(callableStatement.wasNull()).thenReturn(false);
        
        DisplayOrder result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNotNull(result);
        assertEquals(2, result.value());
    }

    @Test
    void testGetNullableResultFromCallableStatement_NullValue() throws SQLException {
        when(callableStatement.getInt(1)).thenReturn(0);
        when(callableStatement.wasNull()).thenReturn(true);
        
        DisplayOrder result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNull(result);
    }
}