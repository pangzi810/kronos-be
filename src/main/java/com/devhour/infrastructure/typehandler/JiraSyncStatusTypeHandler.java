package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.JiraSyncStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SyncStatus用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とSyncStatus enumの相互変換を行う
 * JIRA同期履歴の同期ステータス（IN_PROGRESS/COMPLETED/FAILED）の変換を担当
 */
public class JiraSyncStatusTypeHandler extends BaseTypeHandler<JiraSyncStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JiraSyncStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public JiraSyncStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? JiraSyncStatus.fromValue(value) : null;
    }

    @Override
    public JiraSyncStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? JiraSyncStatus.fromValue(value) : null;
    }

    @Override
    public JiraSyncStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? JiraSyncStatus.fromValue(value) : null;
    }
}