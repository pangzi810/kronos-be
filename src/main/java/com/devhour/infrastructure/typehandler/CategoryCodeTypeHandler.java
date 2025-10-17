package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.CategoryCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CategoryCode用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とCategoryCode recordの相互変換を行う
 */
public class CategoryCodeTypeHandler extends BaseTypeHandler<CategoryCode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CategoryCode parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public CategoryCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? CategoryCode.of(code) : null;
    }

    @Override
    public CategoryCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? CategoryCode.of(code) : null;
    }

    @Override
    public CategoryCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? CategoryCode.of(code) : null;
    }
}