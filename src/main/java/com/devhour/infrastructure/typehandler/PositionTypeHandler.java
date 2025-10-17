package com.devhour.infrastructure.typehandler;

import com.devhour.domain.model.valueobject.Position;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Position用MyBatis TypeHandler
 * 
 * データベースのVARCHAR値(日本語役職名)とPosition enumの相互変換を行う
 * 
 * データベースには日本語名("マネージャー", "部長", "本部長", "統括本部長")が格納され、
 * Javaエンティティでは英語のenum値(MANAGER, DEPARTMENT_MANAGER等)で管理される
 */
public class PositionTypeHandler extends BaseTypeHandler<Position> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Position parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getJapaneseName());
    }

    @Override
    public Position getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String japaneseName = rs.getString(columnName);
        return japaneseName != null ? Position.fromJapaneseName(japaneseName) : null;
    }

    @Override
    public Position getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String japaneseName = rs.getString(columnIndex);
        return japaneseName != null ? Position.fromJapaneseName(japaneseName) : null;
    }

    @Override
    public Position getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String japaneseName = cs.getString(columnIndex);
        return japaneseName != null ? Position.fromJapaneseName(japaneseName) : null;
    }
}