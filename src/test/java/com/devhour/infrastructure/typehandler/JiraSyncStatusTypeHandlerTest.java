package com.devhour.infrastructure.typehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.JiraSyncStatus;

/**
 * SyncStatusTypeHandlerのユニットテスト
 */
@DisplayName("SyncStatusTypeHandler単体テスト")
class JiraSyncStatusTypeHandlerTest {

    private JiraSyncStatusTypeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JiraSyncStatusTypeHandler();
    }

    @Test
    @DisplayName("setNonNullParameter - 正常ケース")
    void setNonNullParameter_Success() throws SQLException {
        // Arrange
        PreparedStatement ps = mock(PreparedStatement.class);
        JiraSyncStatus syncStatus = JiraSyncStatus.IN_PROGRESS;

        // Act
        handler.setNonNullParameter(ps, 1, syncStatus, JdbcType.VARCHAR);

        // Assert - 実際のSQLの設定はmockなので、例外が発生しなければ成功
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - IN_PROGRESS")
    void getNullableResult_ResultSetString_InProgress_ReturnsInProgress() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_status")).thenReturn("IN_PROGRESS");

        // Act
        JiraSyncStatus result = handler.getNullableResult(rs, "sync_status");

        // Assert
        assertThat(result).isEqualTo(JiraSyncStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - COMPLETED")
    void getNullableResult_ResultSetString_Completed_ReturnsCompleted() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_status")).thenReturn("COMPLETED");

        // Act
        JiraSyncStatus result = handler.getNullableResult(rs, "sync_status");

        // Assert
        assertThat(result).isEqualTo(JiraSyncStatus.COMPLETED);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - FAILED")
    void getNullableResult_ResultSetString_Failed_ReturnsFailed() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_status")).thenReturn("FAILED");

        // Act
        JiraSyncStatus result = handler.getNullableResult(rs, "sync_status");

        // Assert
        assertThat(result).isEqualTo(JiraSyncStatus.FAILED);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - null値")
    void getNullableResult_ResultSetString_Null_ReturnsNull() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_status")).thenReturn(null);

        // Act
        JiraSyncStatus result = handler.getNullableResult(rs, "sync_status");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, int) - 正常ケース")
    void getNullableResult_ResultSetInt_Success() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("COMPLETED");

        // Act
        JiraSyncStatus result = handler.getNullableResult(rs, 1);

        // Assert
        assertThat(result).isEqualTo(JiraSyncStatus.COMPLETED);
    }

    @Test
    @DisplayName("getNullableResult(CallableStatement, int) - 正常ケース")
    void getNullableResult_CallableStatement_Success() throws SQLException {
        // Arrange
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn("FAILED");

        // Act
        JiraSyncStatus result = handler.getNullableResult(cs, 1);

        // Assert
        assertThat(result).isEqualTo(JiraSyncStatus.FAILED);
    }

    @Test
    @DisplayName("getNullableResult - 不正な値でエラー")
    void getNullableResult_InvalidValue_ThrowsException() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_status")).thenReturn("INVALID_STATUS");

        // Act & Assert
        assertThatThrownBy(() -> handler.getNullableResult(rs, "sync_status"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不正な同期ステータスです: INVALID_STATUS");
    }
}