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

import com.devhour.domain.model.valueobject.CategoryCode;

@ExtendWith(MockitoExtension.class)
class CategoryCodeTypeHandlerTest {

    private CategoryCodeTypeHandler typeHandler;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        typeHandler = new CategoryCodeTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        CategoryCode categoryCode = CategoryCode.of("DEV");
        
        typeHandler.setNonNullParameter(preparedStatement, 1, categoryCode, null);
        
        verify(preparedStatement).setString(1, "DEV");
    }

    @Test
    void testGetNullableResultFromResultSet() throws SQLException {
        when(resultSet.getString("column")).thenReturn("MEETING");
        
        CategoryCode result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNotNull(result);
        assertEquals("MEETING", result.value());
    }

    @Test
    void testGetNullableResultFromResultSet_NullValue() throws SQLException {
        when(resultSet.getString("column")).thenReturn(null);
        
        CategoryCode result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromResultSetByIndex() throws SQLException {
        when(resultSet.getString(1)).thenReturn("ARCHITECTURE");
        
        CategoryCode result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNotNull(result);
        assertEquals("ARCHITECTURE", result.value());
    }

    @Test
    void testGetNullableResultFromResultSetByIndex_NullValue() throws SQLException {
        when(resultSet.getString(1)).thenReturn(null);
        
        CategoryCode result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        when(callableStatement.getString(1)).thenReturn("BRD");
        
        CategoryCode result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNotNull(result);
        assertEquals("BRD", result.value());
    }

    @Test
    void testGetNullableResultFromCallableStatement_NullValue() throws SQLException {
        when(callableStatement.getString(1)).thenReturn(null);
        
        CategoryCode result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNull(result);
    }
}