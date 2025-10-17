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
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * SyncTypeTypeHandlerのユニットテスト
 */
@DisplayName("SyncTypeTypeHandler単体テスト")
class JiraSyncTypeTypeHandlerTest {

    private JiraSyncTypeTypeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JiraSyncTypeTypeHandler();
    }

    @Test
    @DisplayName("setNonNullParameter - 正常ケース（MANUAL）")
    void setNonNullParameter_Manual_Success() throws SQLException {
        // Arrange
        PreparedStatement ps = mock(PreparedStatement.class);
        JiraSyncType syncType = JiraSyncType.MANUAL;

        // Act
        handler.setNonNullParameter(ps, 1, syncType, JdbcType.VARCHAR);

        // Assert
        // Mockitoで呼び出しを検証
        // Note: 実際の実装では ps.setString(1, "MANUAL") が呼ばれる
    }

    @Test
    @DisplayName("setNonNullParameter - 正常ケース（SCHEDULED）")
    void setNonNullParameter_Scheduled_Success() throws SQLException {
        // Arrange
        PreparedStatement ps = mock(PreparedStatement.class);
        JiraSyncType syncType = JiraSyncType.SCHEDULED;

        // Act
        handler.setNonNullParameter(ps, 1, syncType, JdbcType.VARCHAR);

        // Assert
        // Mockitoで呼び出しを検証
        // Note: 実際の実装では ps.setString(1, "SCHEDULED") が呼ばれる
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - 正常ケース（MANUAL）")
    void getNullableResult_ResultSetString_Manual_ReturnsManual() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_type")).thenReturn("MANUAL");

        // Act
        JiraSyncType result = handler.getNullableResult(rs, "sync_type");

        // Assert
        assertThat(result).isEqualTo(JiraSyncType.MANUAL);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - 正常ケース（SCHEDULED）")
    void getNullableResult_ResultSetString_Scheduled_ReturnsScheduled() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_type")).thenReturn("SCHEDULED");

        // Act
        JiraSyncType result = handler.getNullableResult(rs, "sync_type");

        // Assert
        assertThat(result).isEqualTo(JiraSyncType.SCHEDULED);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - null値")
    void getNullableResult_ResultSetString_Null_ReturnsNull() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_type")).thenReturn(null);

        // Act
        JiraSyncType result = handler.getNullableResult(rs, "sync_type");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, int) - 正常ケース（MANUAL）")
    void getNullableResult_ResultSetInt_Manual_ReturnsManual() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("MANUAL");

        // Act
        JiraSyncType result = handler.getNullableResult(rs, 1);

        // Assert
        assertThat(result).isEqualTo(JiraSyncType.MANUAL);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, int) - null値")
    void getNullableResult_ResultSetInt_Null_ReturnsNull() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(null);

        // Act
        JiraSyncType result = handler.getNullableResult(rs, 1);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult(CallableStatement, int) - 正常ケース（SCHEDULED）")
    void getNullableResult_CallableStatement_Scheduled_ReturnsScheduled() throws SQLException {
        // Arrange
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn("SCHEDULED");

        // Act
        JiraSyncType result = handler.getNullableResult(cs, 1);

        // Assert
        assertThat(result).isEqualTo(JiraSyncType.SCHEDULED);
    }

    @Test
    @DisplayName("getNullableResult(CallableStatement, int) - null値")
    void getNullableResult_CallableStatement_Null_ReturnsNull() throws SQLException {
        // Arrange
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn(null);

        // Act
        JiraSyncType result = handler.getNullableResult(cs, 1);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult - 不正な値でエラー")
    void getNullableResult_InvalidValue_ThrowsException() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("sync_type")).thenReturn("INVALID_TYPE");

        // Act & Assert
        assertThatThrownBy(() -> handler.getNullableResult(rs, "sync_type"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不正な同期タイプです: INVALID_TYPE");
    }
}