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

import com.devhour.domain.model.valueobject.DetailStatus;

/**
 * DetailStatusTypeHandlerのユニットテスト
 */
@DisplayName("DetailStatusTypeHandler単体テスト")
class DetailStatusTypeHandlerTest {

    private DetailStatusTypeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DetailStatusTypeHandler();
    }

    @Test
    @DisplayName("setNonNullParameter - 正常ケース")
    void setNonNullParameter_Success() throws SQLException {
        // Arrange
        PreparedStatement ps = mock(PreparedStatement.class);
        DetailStatus detailStatus = DetailStatus.SUCCESS;

        // Act
        handler.setNonNullParameter(ps, 1, detailStatus, JdbcType.VARCHAR);

        // Assert - 実際のSQLの設定はmockなので、例外が発生しなければ成功
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - SUCCESS")
    void getNullableResult_ResultSetString_Success_ReturnsSuccess() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("status")).thenReturn("SUCCESS");

        // Act
        DetailStatus result = handler.getNullableResult(rs, "status");

        // Assert
        assertThat(result).isEqualTo(DetailStatus.SUCCESS);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - ERROR")
    void getNullableResult_ResultSetString_Error_ReturnsError() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("status")).thenReturn("ERROR");

        // Act
        DetailStatus result = handler.getNullableResult(rs, "status");

        // Assert
        assertThat(result).isEqualTo(DetailStatus.ERROR);
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, String) - null値")
    void getNullableResult_ResultSetString_Null_ReturnsNull() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("status")).thenReturn(null);

        // Act
        DetailStatus result = handler.getNullableResult(rs, "status");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult(ResultSet, int) - 正常ケース")
    void getNullableResult_ResultSetInt_Success() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("ERROR");

        // Act
        DetailStatus result = handler.getNullableResult(rs, 1);

        // Assert
        assertThat(result).isEqualTo(DetailStatus.ERROR);
    }

    @Test
    @DisplayName("getNullableResult(CallableStatement, int) - 正常ケース")
    void getNullableResult_CallableStatement_Success() throws SQLException {
        // Arrange
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn("SUCCESS");

        // Act
        DetailStatus result = handler.getNullableResult(cs, 1);

        // Assert
        assertThat(result).isEqualTo(DetailStatus.SUCCESS);
    }

    @Test
    @DisplayName("getNullableResult - 不正な値でエラー")
    void getNullableResult_InvalidValue_ThrowsException() throws SQLException {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("status")).thenReturn("INVALID_STATUS");

        // Act & Assert
        assertThatThrownBy(() -> handler.getNullableResult(rs, "status"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不正な詳細ステータスです: INVALID_STATUS");
    }
}