package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.entity.User;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserStatus用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とUserStatus enumの相互変換を行う
 */
public class UserStatusTypeHandler extends BaseTypeHandler<User.UserStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, User.UserStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public User.UserStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String status = rs.getString(columnName);
        return status != null ? User.UserStatus.fromValue(status) : null;
    }

    @Override
    public User.UserStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String status = rs.getString(columnIndex);
        return status != null ? User.UserStatus.fromValue(status) : null;
    }

    @Override
    public User.UserStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String status = cs.getString(columnIndex);
        return status != null ? User.UserStatus.fromValue(status) : null;
    }
}