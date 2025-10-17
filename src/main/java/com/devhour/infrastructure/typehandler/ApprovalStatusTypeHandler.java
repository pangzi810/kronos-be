package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.ApprovalStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ApprovalStatus の MyBatis TypeHandler
 */
public class ApprovalStatusTypeHandler extends BaseTypeHandler<ApprovalStatus> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ApprovalStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }
    
    @Override
    public ApprovalStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return ApprovalStatus.fromValue(value);
    }
    
    @Override
    public ApprovalStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return ApprovalStatus.fromValue(value);
    }
    
    @Override
    public ApprovalStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return ApprovalStatus.fromValue(value);
    }
}