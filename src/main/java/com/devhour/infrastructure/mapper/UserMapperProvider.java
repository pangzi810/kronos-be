package com.devhour.infrastructure.mapper;

import org.apache.ibatis.jdbc.SQL;

/**
 * UserMapper用の動的SQLプロバイダー
 */
public class UserMapperProvider {

    /**
     * 複数条件検索用の動的SQL生成
     */
    public String searchUsers(String username, String email, String userStatus) {
        return new SQL() {
            {
                SELECT("id, username, email, full_name");
                SELECT("user_status, last_login_at, created_at, updated_at, okta_user_id");
                FROM("users");
                
                if (username != null) {
                    WHERE("username LIKE CONCAT('%', #{username}, '%')");
                }
                if (email != null) {
                    WHERE("email LIKE CONCAT('%', #{email}, '%')");
                }
                if (userStatus != null) {
                    WHERE("user_status = #{userStatus}");
                }
                
                ORDER_BY("full_name ASC");
            }
        }.toString();
    }
}