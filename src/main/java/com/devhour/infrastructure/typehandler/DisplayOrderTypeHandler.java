package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.DisplayOrder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DisplayOrder用MyBatis TypeHandler
 * 
 * データベースのINTEGER値とDisplayOrder recordの相互変換を行う
 */
public class DisplayOrderTypeHandler extends BaseTypeHandler<DisplayOrder> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DisplayOrder parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.value());
    }

    @Override
    public DisplayOrder getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : DisplayOrder.of(value);
    }

    @Override
    public DisplayOrder getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : DisplayOrder.of(value);
    }

    @Override
    public DisplayOrder getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : DisplayOrder.of(value);
    }
}