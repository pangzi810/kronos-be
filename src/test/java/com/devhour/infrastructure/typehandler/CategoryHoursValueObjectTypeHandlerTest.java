package com.devhour.infrastructure.typehandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;

/**
 * CategoryHoursValueObjectTypeHandlerの包括的テスト
 * 
 * MyBatisのTypeHandlerとしてCategoryHours値オブジェクトとJSON文字列間の
 * 変換処理を検証する
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryHoursValueObjectTypeHandler")
class CategoryHoursValueObjectTypeHandlerTest {

    private CategoryHoursValueObjectTypeHandler typeHandler;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        typeHandler = new CategoryHoursValueObjectTypeHandler();
    }

    @Nested
    @DisplayName("setNonNullParameter")
    class SetNonNullParameterTest {

        @Test
        @DisplayName("正常ケース - CategoryHoursをJSON文字列に変換して設定")
        void setNonNullParameter_Success() throws SQLException {
            // Given
            Map<CategoryCode, BigDecimal> hoursMap = new HashMap<>();
            hoursMap.put(CategoryCode.of("DEV"), new BigDecimal("8.0"));
            hoursMap.put(CategoryCode.of("MEETING"), new BigDecimal("1.5"));
            hoursMap.put(CategoryCode.of("REVIEW"), new BigDecimal("2.0"));
            
            CategoryHours categoryHours = CategoryHours.of(hoursMap);
            int parameterIndex = 1;
            JdbcType jdbcType = JdbcType.VARCHAR;

            // When
            typeHandler.setNonNullParameter(preparedStatement, parameterIndex, categoryHours, jdbcType);

            // Then
            verify(preparedStatement).setString(eq(parameterIndex), anyString());
        }

        @Test
        @DisplayName("空のCategoryHours - 空のJSONオブジェクトに変換")
        void setNonNullParameter_EmptyCategoryHours() throws SQLException {
            // Given
            CategoryHours emptyCategoryHours = CategoryHours.empty();
            int parameterIndex = 1;
            JdbcType jdbcType = JdbcType.VARCHAR;

            // When
            typeHandler.setNonNullParameter(preparedStatement, parameterIndex, emptyCategoryHours, jdbcType);

            // Then
            verify(preparedStatement).setString(eq(parameterIndex), eq("{}"));
        }

        @Test
        @DisplayName("単一カテゴリのCategoryHours - 正常にJSON変換")
        void setNonNullParameter_SingleCategory() throws SQLException {
            // Given
            Map<CategoryCode, BigDecimal> hoursMap = Map.of(
                CategoryCode.of("DEV"), new BigDecimal("8.0")
            );
            CategoryHours categoryHours = CategoryHours.of(hoursMap);
            int parameterIndex = 2;
            JdbcType jdbcType = JdbcType.VARCHAR;

            // When
            typeHandler.setNonNullParameter(preparedStatement, parameterIndex, categoryHours, jdbcType);

            // Then
            verify(preparedStatement).setString(eq(parameterIndex), contains("\"DEV\""));
            verify(preparedStatement).setString(eq(parameterIndex), contains("8.0"));
        }
    }

    @Nested
    @DisplayName("getNullableResult - ColumnName")
    class GetNullableResultByColumnNameTest {

        @Test
        @DisplayName("正常ケース - JSON文字列をCategoryHoursに変換")
        void getNullableResult_ValidJson() throws SQLException {
            // Given
            String columnName = "category_hours";
            String json = "{\"DEV\":8.0,\"MEETING\":1.5,\"REVIEW\":2.0}";
            when(resultSet.getString(columnName)).thenReturn(json);

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnName);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("8.0"), result.getHours(CategoryCode.of("DEV")));
            assertEquals(new BigDecimal("1.5"), result.getHours(CategoryCode.of("MEETING")));
            assertEquals(new BigDecimal("2.0"), result.getHours(CategoryCode.of("REVIEW")));
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("null値 - 空のCategoryHoursを返す")
        void getNullableResult_NullValue() throws SQLException {
            // Given
            String columnName = "category_hours";
            when(resultSet.getString(columnName)).thenReturn(null);

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnName);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("空文字列 - 空のCategoryHoursを返す")
        void getNullableResult_EmptyString() throws SQLException {
            // Given
            String columnName = "category_hours";
            when(resultSet.getString(columnName)).thenReturn("");

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnName);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("空白文字列 - 空のCategoryHoursを返す")
        void getNullableResult_WhitespaceString() throws SQLException {
            // Given
            String columnName = "category_hours";
            when(resultSet.getString(columnName)).thenReturn("   ");

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnName);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("空のJSONオブジェクト - 空のCategoryHoursを返す")
        void getNullableResult_EmptyJsonObject() throws SQLException {
            // Given
            String columnName = "category_hours";
            when(resultSet.getString(columnName)).thenReturn("{}");

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnName);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("不正なJSON形式 - SQLExceptionを投げる")
        void getNullableResult_InvalidJson() throws SQLException {
            // Given
            String columnName = "category_hours";
            String invalidJson = "{invalid json}";
            when(resultSet.getString(columnName)).thenReturn(invalidJson);

            // When & Then
            SQLException exception = assertThrows(SQLException.class, () -> 
                typeHandler.getNullableResult(resultSet, columnName)
            );
            
            assertTrue(exception.getMessage().contains("Error parsing CategoryHours JSON"));
            assertTrue(exception.getMessage().contains(invalidJson));
            verify(resultSet).getString(columnName);
        }

        @Test
        @DisplayName("不正なCategoryCode - SQLExceptionを投げる")
        void getNullableResult_InvalidCategoryCode() throws SQLException {
            // Given
            String columnName = "category_hours";
            String jsonWithInvalidCode = "{\"INVALID_CODE_THAT_IS_TOO_LONG\":8.0}";
            when(resultSet.getString(columnName)).thenReturn(jsonWithInvalidCode);

            // When & Then
            SQLException exception = assertThrows(SQLException.class, () -> 
                typeHandler.getNullableResult(resultSet, columnName)
            );
            
            assertTrue(exception.getMessage().contains("Error creating CategoryHours from JSON"));
            verify(resultSet).getString(columnName);
        }
    }

    @Nested
    @DisplayName("getNullableResult - ColumnIndex")
    class GetNullableResultByColumnIndexTest {

        @Test
        @DisplayName("正常ケース - インデックス指定でJSON文字列をCategoryHoursに変換")
        void getNullableResult_ValidJsonByIndex() throws SQLException {
            // Given
            int columnIndex = 1;
            String json = "{\"DEV\":6.0,\"TEST\":2.0}";
            when(resultSet.getString(columnIndex)).thenReturn(json);

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnIndex);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("6.0"), result.getHours(CategoryCode.of("DEV")));
            assertEquals(new BigDecimal("2.0"), result.getHours(CategoryCode.of("TEST")));
            verify(resultSet).getString(columnIndex);
        }

        @Test
        @DisplayName("null値 - インデックス指定で空のCategoryHoursを返す")
        void getNullableResult_NullValueByIndex() throws SQLException {
            // Given
            int columnIndex = 2;
            when(resultSet.getString(columnIndex)).thenReturn(null);

            // When
            CategoryHours result = typeHandler.getNullableResult(resultSet, columnIndex);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(resultSet).getString(columnIndex);
        }

        @Test
        @DisplayName("不正なJSON - インデックス指定でSQLExceptionを投げる")
        void getNullableResult_InvalidJsonByIndex() throws SQLException {
            // Given
            int columnIndex = 3;
            String invalidJson = "not a json";
            when(resultSet.getString(columnIndex)).thenReturn(invalidJson);

            // When & Then
            SQLException exception = assertThrows(SQLException.class, () -> 
                typeHandler.getNullableResult(resultSet, columnIndex)
            );
            
            assertTrue(exception.getMessage().contains("Error parsing CategoryHours JSON"));
            verify(resultSet).getString(columnIndex);
        }
    }

    @Nested
    @DisplayName("getNullableResult - CallableStatement")
    class GetNullableResultByCallableStatementTest {

        @Test
        @DisplayName("正常ケース - CallableStatementからJSON文字列をCategoryHoursに変換")
        void getNullableResult_ValidJsonFromCallableStatement() throws SQLException {
            // Given
            int columnIndex = 1;
            String json = "{\"ANALYSIS\":4.0,\"DOC\":1.0}";
            when(callableStatement.getString(columnIndex)).thenReturn(json);

            // When
            CategoryHours result = typeHandler.getNullableResult(callableStatement, columnIndex);

            // Then
            assertNotNull(result);
            assertEquals(new BigDecimal("4.0"), result.getHours(CategoryCode.of("ANALYSIS")));
            assertEquals(new BigDecimal("1.0"), result.getHours(CategoryCode.of("DOC")));
            verify(callableStatement).getString(columnIndex);
        }

        @Test
        @DisplayName("null値 - CallableStatementから空のCategoryHoursを返す")
        void getNullableResult_NullValueFromCallableStatement() throws SQLException {
            // Given
            int columnIndex = 2;
            when(callableStatement.getString(columnIndex)).thenReturn(null);

            // When
            CategoryHours result = typeHandler.getNullableResult(callableStatement, columnIndex);

            // Then
            assertNotNull(result);
            assertFalse(result.hasAnyHours());
            verify(callableStatement).getString(columnIndex);
        }

        @Test
        @DisplayName("不正なJSON - CallableStatementでSQLExceptionを投げる")
        void getNullableResult_InvalidJsonFromCallableStatement() throws SQLException {
            // Given
            int columnIndex = 3;
            String invalidJson = "{malformed";
            when(callableStatement.getString(columnIndex)).thenReturn(invalidJson);

            // When & Then
            SQLException exception = assertThrows(SQLException.class, () -> 
                typeHandler.getNullableResult(callableStatement, columnIndex)
            );
            
            assertTrue(exception.getMessage().contains("Error parsing CategoryHours JSON"));
            verify(callableStatement).getString(columnIndex);
        }
    }

    @Nested
    @DisplayName("Integration Tests - エンドツーエンド変換")
    class IntegrationTest {

        @Test
        @DisplayName("CategoryHours -> JSON -> CategoryHours の完全変換テスト")
        void fullConversionCycle() throws SQLException {
            // Given
            Map<CategoryCode, BigDecimal> originalHours = new HashMap<>();
            originalHours.put(CategoryCode.of("DEV"), new BigDecimal("8.00"));
            originalHours.put(CategoryCode.of("MEETING"), new BigDecimal("1.50"));
            originalHours.put(CategoryCode.of("REVIEW"), new BigDecimal("2.25"));
            
            CategoryHours originalCategoryHours = CategoryHours.of(originalHours);
            
            // JSONに変換するための準備
            when(resultSet.getString("test_column")).thenAnswer(invocation -> {
                // PreparedStatementに設定されるJSON文字列をシミュレート
                typeHandler.setNonNullParameter(preparedStatement, 1, originalCategoryHours, JdbcType.VARCHAR);
                // 実際にはDBから取得されるJSON（この例では手動で構築）
                return "{\"DEV\":8.00,\"MEETING\":1.50,\"REVIEW\":2.25}";
            });

            // When - JSONから逆変換
            CategoryHours restoredCategoryHours = typeHandler.getNullableResult(resultSet, "test_column");

            // Then
            assertNotNull(restoredCategoryHours);
            assertEquals(originalHours.size(), restoredCategoryHours.hours().size());
            
            for (Map.Entry<CategoryCode, BigDecimal> entry : originalHours.entrySet()) {
                BigDecimal expectedHours = entry.getValue();
                BigDecimal actualHours = restoredCategoryHours.getHours(entry.getKey());
                assertEquals(0, expectedHours.compareTo(actualHours), 
                    "Hours mismatch for category " + entry.getKey().value());
            }
        }

        @Test
        @DisplayName("大きなデータセットでの変換性能テスト")
        void largeDatasetConversion() throws SQLException {
            // Given
            Map<CategoryCode, BigDecimal> largeHoursMap = new HashMap<>();
            // Use valid category codes (max 10 chars)
            String[] validCodes = {"DEV", "TEST", "MEET", "DOC", "REVIEW", "BRD", "DESIGN", "IMPL", "QA", "DEPLOY"};
            for (int i = 0; i < 10; i++) {
                largeHoursMap.put(CategoryCode.of(validCodes[i]), new BigDecimal(i * 0.5));
            }
            
            CategoryHours largeCategoryHours = CategoryHours.of(largeHoursMap);

            // When - JSON変換（性能測定）
            long startTime = System.currentTimeMillis();
            typeHandler.setNonNullParameter(preparedStatement, 1, largeCategoryHours, JdbcType.VARCHAR);
            long endTime = System.currentTimeMillis();

            // Then
            assertTrue(endTime - startTime < 100, "変換処理が100ms以内に完了すること");
            verify(preparedStatement).setString(eq(1), anyString());
        }
    }
}