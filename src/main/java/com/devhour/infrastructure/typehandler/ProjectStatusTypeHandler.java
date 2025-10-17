package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.ProjectStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ProjectStatus用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とProjectStatus enumの相互変換を行う
 */
public class ProjectStatusTypeHandler extends BaseTypeHandler<ProjectStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProjectStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public ProjectStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String status = rs.getString(columnName);
        return status != null ? ProjectStatus.of(status) : null;
    }

    @Override
    public ProjectStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String status = rs.getString(columnIndex);
        return status != null ? ProjectStatus.of(status) : null;
    }

    @Override
    public ProjectStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String status = cs.getString(columnIndex);
        return status != null ? ProjectStatus.of(status) : null;
    }
}