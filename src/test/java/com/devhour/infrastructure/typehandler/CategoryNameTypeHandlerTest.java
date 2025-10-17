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

import com.devhour.domain.model.valueobject.CategoryName;

@ExtendWith(MockitoExtension.class)
class CategoryNameTypeHandlerTest {

    private CategoryNameTypeHandler typeHandler;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        typeHandler = new CategoryNameTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        CategoryName categoryName = CategoryName.of("Development Work");
        
        typeHandler.setNonNullParameter(preparedStatement, 1, categoryName, null);
        
        verify(preparedStatement).setString(1, "Development Work");
    }

    @Test
    void testGetNullableResultFromResultSet() throws SQLException {
        when(resultSet.getString("column")).thenReturn("Meeting");
        
        CategoryName result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNotNull(result);
        assertEquals("Meeting", result.getValue());
    }

    @Test
    void testGetNullableResultFromResultSet_NullValue() throws SQLException {
        when(resultSet.getString("column")).thenReturn(null);
        
        CategoryName result = typeHandler.getNullableResult(resultSet, "column");
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromResultSetByIndex() throws SQLException {
        when(resultSet.getString(1)).thenReturn("Architecture Design");
        
        CategoryName result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNotNull(result);
        assertEquals("Architecture Design", result.getValue());
    }

    @Test
    void testGetNullableResultFromResultSetByIndex_NullValue() throws SQLException {
        when(resultSet.getString(1)).thenReturn(null);
        
        CategoryName result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNull(result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        when(callableStatement.getString(1)).thenReturn("Business Requirements");
        
        CategoryName result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNotNull(result);
        assertEquals("Business Requirements", result.getValue());
    }

    @Test
    void testGetNullableResultFromCallableStatement_NullValue() throws SQLException {
        when(callableStatement.getString(1)).thenReturn(null);
        
        CategoryName result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNull(result);
    }
}