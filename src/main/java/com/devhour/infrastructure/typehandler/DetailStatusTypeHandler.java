package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.DetailStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DetailStatus用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とDetailStatus enumの相互変換を行う
 * JIRA同期履歴詳細のステータス（SUCCESS/ERROR）の変換を担当
 */
public class DetailStatusTypeHandler extends BaseTypeHandler<DetailStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DetailStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public DetailStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? DetailStatus.fromValue(value) : null;
    }

    @Override
    public DetailStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? DetailStatus.fromValue(value) : null;
    }

    @Override
    public DetailStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? DetailStatus.fromValue(value) : null;
    }
}