package com.devhour.infrastructure.typehandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryHoursTypeHandlerTest {

    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private CallableStatement callableStatement;
    
    private CategoryHoursTypeHandler typeHandler;
    
    @BeforeEach
    void setUp() {
        typeHandler = new CategoryHoursTypeHandler();
    }
    
    @Test
    void testSetNonNullParameter() throws SQLException {
        Map<String, BigDecimal> categoryHours = new HashMap<>();
        categoryHours.put("DEV", BigDecimal.valueOf(8.0));
        categoryHours.put("MEETING", BigDecimal.valueOf(1.5));
        
        typeHandler.setNonNullParameter(preparedStatement, 1, categoryHours, null);
        
        verify(preparedStatement).setString(eq(1), contains("DEV"));
        verify(preparedStatement).setString(eq(1), contains("MEETING"));
        verify(preparedStatement).setString(eq(1), contains("8.0"));
        verify(preparedStatement).setString(eq(1), contains("1.5"));
    }
    
    @Test
    void testSetNonNullParameter_EmptyMap() throws SQLException {
        Map<String, BigDecimal> categoryHours = new HashMap<>();
        
        typeHandler.setNonNullParameter(preparedStatement, 1, categoryHours, null);
        
        verify(preparedStatement).setString(1, "{}");
    }
    
    @Test
    void testGetNullableResult_ByColumnName_ValidJson() throws SQLException {
        String json = "{\"DEV\": 8.0, \"MEETING\": 1.5}";
        when(resultSet.getString("category_hours")).thenReturn(json);
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(resultSet, "category_hours");
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(8.0), result.get("DEV"));
        assertEquals(BigDecimal.valueOf(1.5), result.get("MEETING"));
    }
    
    @Test
    void testGetNullableResult_ByColumnName_EmptyJson() throws SQLException {
        when(resultSet.getString("category_hours")).thenReturn("{}");
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(resultSet, "category_hours");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_NullValue() throws SQLException {
        when(resultSet.getString("category_hours")).thenReturn(null);
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(resultSet, "category_hours");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetNullableResult_ByColumnName_EmptyString() throws SQLException {
        when(resultSet.getString("category_hours")).thenReturn("");
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(resultSet, "category_hours");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetNullableResult_ByColumnIndex() throws SQLException {
        String json = "{\"BRD\": 1.5, \"DEV\": 6.0}";
        when(resultSet.getString(1)).thenReturn(json);
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(resultSet, 1);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(1.5), result.get("BRD"));
        assertEquals(BigDecimal.valueOf(6.0), result.get("DEV"));
    }
    
    @Test
    void testGetNullableResult_CallableStatement() throws SQLException {
        String json = "{\"MEETING\": 0.5}";
        when(callableStatement.getString(1)).thenReturn(json);
        
        Map<String, BigDecimal> result = typeHandler.getNullableResult(callableStatement, 1);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(0.5), result.get("MEETING"));
    }
    
    @Test
    void testGetNullableResult_InvalidJson() throws SQLException {
        when(resultSet.getString("category_hours")).thenReturn("invalid json");
        
        assertThrows(SQLException.class, () -> {
            typeHandler.getNullableResult(resultSet, "category_hours");
        });
    }
    
    @Test
    void testMapToJson_ValidMap() throws Exception {
        Map<String, BigDecimal> categoryHours = new HashMap<>();
        categoryHours.put("DEV", BigDecimal.valueOf(8.0));
        categoryHours.put("MEETING", BigDecimal.valueOf(1.5));
        
        String result = typeHandler.mapToJson(categoryHours);
        
        assertNotNull(result);
        assertTrue(result.contains("DEV"));
        assertTrue(result.contains("MEETING"));
        assertTrue(result.contains("8.0"));
        assertTrue(result.contains("1.5"));
    }
    
    @Test
    void testMapToJson_EmptyMap() throws Exception {
        Map<String, BigDecimal> categoryHours = new HashMap<>();
        
        String result = typeHandler.mapToJson(categoryHours);
        
        assertEquals("{}", result);
    }
    
    @Test
    void testMapToJson_NullMap() throws Exception {
        String result = typeHandler.mapToJson(null);
        
        assertEquals("{}", result);
    }
}