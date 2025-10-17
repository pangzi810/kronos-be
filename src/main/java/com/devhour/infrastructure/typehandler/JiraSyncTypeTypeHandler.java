package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.JiraSyncType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SyncType用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値とSyncType enumの相互変換を行う
 * JIRA同期履歴の同期タイプ（MANUAL/SCHEDULED）の変換を担当
 */
public class JiraSyncTypeTypeHandler extends BaseTypeHandler<JiraSyncType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JiraSyncType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public JiraSyncType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? JiraSyncType.fromValue(value) : null;
    }

    @Override
    public JiraSyncType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? JiraSyncType.fromValue(value) : null;
    }

    @Override
    public JiraSyncType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? JiraSyncType.fromValue(value) : null;
    }
}