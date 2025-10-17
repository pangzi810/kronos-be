package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.CategoryName;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CategoryName用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とCategoryName recordの相互変換を行う
 */
public class CategoryNameTypeHandler extends BaseTypeHandler<CategoryName> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CategoryName parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public CategoryName getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String name = rs.getString(columnName);
        return name != null ? CategoryName.of(name) : null;
    }

    @Override
    public CategoryName getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String name = rs.getString(columnIndex);
        return name != null ? CategoryName.of(name) : null;
    }

    @Override
    public CategoryName getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String name = cs.getString(columnIndex);
        return name != null ? CategoryName.of(name) : null;
    }
}