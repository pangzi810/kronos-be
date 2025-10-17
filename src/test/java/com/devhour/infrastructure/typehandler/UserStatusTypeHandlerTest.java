package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.entity.User;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserStatusTypeHandlerのユニットテスト
 */
@DisplayName("UserStatusTypeHandler")
class UserStatusTypeHandlerTest {

    private UserStatusTypeHandler handler;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new UserStatusTypeHandler();
    }

    @Test
    @DisplayName("setNonNullParameter - ACTIVE値の設定")
    void setNonNullParameter_Active_Success() throws SQLException {
        // Act
        handler.setNonNullParameter(preparedStatement, 1, User.UserStatus.ACTIVE, JdbcType.VARCHAR);

        // Assert
        verify(preparedStatement).setString(1, "ACTIVE");
    }

    @Test
    @DisplayName("setNonNullParameter - INACTIVE値の設定")
    void setNonNullParameter_Inactive_Success() throws SQLException {
        // Act
        handler.setNonNullParameter(preparedStatement, 1, User.UserStatus.INACTIVE, JdbcType.VARCHAR);

        // Assert
        verify(preparedStatement).setString(1, "INACTIVE");
    }

    @Test
    @DisplayName("setNonNullParameter - SUSPENDED値の設定")
    void setNonNullParameter_Suspended_Success() throws SQLException {
        // Act
        handler.setNonNullParameter(preparedStatement, 1, User.UserStatus.SUSPENDED, JdbcType.VARCHAR);

        // Assert
        verify(preparedStatement).setString(1, "SUSPENDED");
    }

    @Test
    @DisplayName("getNullableResult (column name) - ACTIVE値の取得")
    void getNullableResult_ByColumnName_Active_ReturnsActive() throws SQLException {
        // Arrange
        when(resultSet.getString("user_status")).thenReturn("ACTIVE");

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, "user_status");

        // Assert
        assertThat(result).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("getNullableResult (column name) - INACTIVE値の取得")
    void getNullableResult_ByColumnName_Inactive_ReturnsInactive() throws SQLException {
        // Arrange
        when(resultSet.getString("user_status")).thenReturn("INACTIVE");

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, "user_status");

        // Assert
        assertThat(result).isEqualTo(User.UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("getNullableResult (column name) - SUSPENDED値の取得")
    void getNullableResult_ByColumnName_Suspended_ReturnsSuspended() throws SQLException {
        // Arrange
        when(resultSet.getString("user_status")).thenReturn("SUSPENDED");

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, "user_status");

        // Assert
        assertThat(result).isEqualTo(User.UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("getNullableResult (column name) - null値の場合")
    void getNullableResult_ByColumnName_Null_ReturnsNull() throws SQLException {
        // Arrange
        when(resultSet.getString("user_status")).thenReturn(null);

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, "user_status");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult (column index) - ACTIVE値の取得")
    void getNullableResult_ByColumnIndex_Active_ReturnsActive() throws SQLException {
        // Arrange
        when(resultSet.getString(1)).thenReturn("ACTIVE");

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, 1);

        // Assert
        assertThat(result).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("getNullableResult (column index) - null値の場合")
    void getNullableResult_ByColumnIndex_Null_ReturnsNull() throws SQLException {
        // Arrange
        when(resultSet.getString(1)).thenReturn(null);

        // Act
        User.UserStatus result = handler.getNullableResult(resultSet, 1);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult (callable statement) - SUSPENDED値の取得")
    void getNullableResult_CallableStatement_Suspended_ReturnsSuspended() throws SQLException {
        // Arrange
        when(callableStatement.getString(1)).thenReturn("SUSPENDED");

        // Act
        User.UserStatus result = handler.getNullableResult(callableStatement, 1);

        // Assert
        assertThat(result).isEqualTo(User.UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("getNullableResult (callable statement) - null値の場合")
    void getNullableResult_CallableStatement_Null_ReturnsNull() throws SQLException {
        // Arrange
        when(callableStatement.getString(1)).thenReturn(null);

        // Act
        User.UserStatus result = handler.getNullableResult(callableStatement, 1);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNullableResult - 不正な値の場合は例外")
    void getNullableResult_InvalidValue_ThrowsException() throws SQLException {
        // Arrange
        when(resultSet.getString("user_status")).thenReturn("INVALID_STATUS");

        // Act & Assert
        assertThatThrownBy(() -> handler.getNullableResult(resultSet, "user_status"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不正なステータス値: INVALID_STATUS");
    }
}