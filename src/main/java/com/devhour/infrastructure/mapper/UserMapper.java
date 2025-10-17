package com.devhour.infrastructure.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import com.devhour.domain.model.entity.User;

/**
 * ユーザーMyBatisマッパー
 * 
 * ユーザーエンティティの永続化操作を提供
 */
@Mapper
public interface UserMapper {
    
    /**
     * ユーザーIDでユーザーを検索
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE id = #{id}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    Optional<User> findById(@Param("id") String id);
    
    /**
     * ユーザー名でユーザーを検索
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE username = #{username}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * メールアドレスでユーザーを検索
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE email = #{email}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * アクティブユーザーをメールアドレスで検索
     * 削除済み（deleted_at != NULL）のユーザーは除外
     * Oktaユーザープロビジョニング時の存在確認で使用
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE email = #{email} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    Optional<User> findByEmailAndDeletedAtIsNull(@Param("email") String email);
    
    /**
     * Okta User IDでユーザーを検索
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE okta_user_id = #{oktaUserId}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    Optional<User> findByOktaUserId(@Param("oktaUserId") String oktaUserId);
    
    /**
     * 全ユーザー取得
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        ORDER BY created_at DESC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    List<User> findAll();
    
    /**
     * アクティブユーザー取得
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE user_status = 'ACTIVE'
        ORDER BY full_name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    List<User> findAllActive();
    
    /**
     * ユーザー作成（Okta対応版）
     */
    @Insert("""
        INSERT INTO users (id, username, email, full_name, 
                          user_status, last_login_at, created_at, updated_at, okta_user_id)
        VALUES (#{id}, #{username}, #{email}, #{fullName}, 
                #{userStatus, typeHandler=com.devhour.infrastructure.typehandler.UserStatusTypeHandler}, #{lastLoginAt}, #{createdAt}, #{updatedAt}, #{oktaUserId})
        """)
    void insertWithOkta(@Param("id") String id,
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("fullName") String fullName,
                        @Param("userStatus") User.UserStatus userStatus,
                        @Param("lastLoginAt") LocalDateTime lastLoginAt,
                        @Param("createdAt") LocalDateTime createdAt,
                        @Param("updatedAt") LocalDateTime updatedAt,
                        @Param("oktaUserId") String oktaUserId);
    
    /**
     * ユーザー作成
     */
    @Insert("""
        INSERT INTO users (id, username, email, full_name, 
                          user_status, last_login_at, created_at, updated_at)
        VALUES (#{id}, #{username}, #{email}, #{fullName}, 
                #{userStatus, typeHandler=com.devhour.infrastructure.typehandler.UserStatusTypeHandler}, #{lastLoginAt}, #{createdAt}, #{updatedAt})
        """)
    void insert(@Param("id") String id,
                @Param("username") String username,
                @Param("email") String email,
                @Param("fullName") String fullName,
                @Param("userStatus") User.UserStatus userStatus,
                @Param("lastLoginAt") LocalDateTime lastLoginAt,
                @Param("createdAt") LocalDateTime createdAt,
                @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * ユーザー更新（Okta対応版）
     */
    @Update("""
        UPDATE users 
        SET username = #{username}, email = #{email}, full_name = #{fullName}, 
            updated_at = #{updatedAt}, okta_user_id = #{oktaUserId}
        WHERE id = #{id}
        """)
    int updateWithOkta(@Param("id") String id,
                       @Param("username") String username,
                       @Param("email") String email,
                       @Param("fullName") String fullName,
                       @Param("updatedAt") LocalDateTime updatedAt,
                       @Param("oktaUserId") String oktaUserId);
    
    /**
     * ユーザー更新
     */
    @Update("""
        UPDATE users 
        SET username = #{username}, email = #{email}, full_name = #{fullName}, 
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(@Param("id") String id,
               @Param("username") String username,
               @Param("email") String email,
               @Param("fullName") String fullName,
               @Param("updatedAt") LocalDateTime updatedAt);
    
    
    /**
     * アクティブ状態更新（後方互換性のため保持）
     */
    @Update("""
        UPDATE users 
        SET user_status = #{userStatus, typeHandler=com.devhour.infrastructure.typehandler.UserStatusTypeHandler}, updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int updateActiveStatus(@Param("id") String id,
                          @Param("userStatus") User.UserStatus userStatus,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * ユーザーステータス更新
     */
    @Update("""
        UPDATE users 
        SET user_status = #{userStatus, typeHandler=com.devhour.infrastructure.typehandler.UserStatusTypeHandler}, updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int updateUserStatus(@Param("id") String id,
                         @Param("userStatus") User.UserStatus userStatus,
                         @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 最終ログイン時刻更新
     */
    @Update("""
        UPDATE users 
        SET last_login_at = #{lastLoginAt}, updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int updateLastLoginAt(@Param("id") String id,
                          @Param("lastLoginAt") LocalDateTime lastLoginAt,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * ユーザー名存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM users 
        WHERE username = #{username}
        """)
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * メールアドレス存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM users 
        WHERE email = #{email}
        """)
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * ユーザー総数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM users 
        """)
    long count();
    
    /**
     * アクティブユーザー数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM users 
        WHERE user_status = 'ACTIVE'
        """)
    long countActiveUsers();
    
    /**
     * フルネーム部分一致検索
     */
    @Select("""
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE full_name LIKE CONCAT('%', #{fullNamePattern}, '%')
        ORDER BY full_name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    List<User> searchByFullName(@Param("fullNamePattern") String fullNamePattern);

    /**
     * 複数条件検索
     */
    @SelectProvider(type = UserMapperProvider.class, method = "searchUsers")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    List<User> searchUsers(@Param("username") String username,
                          @Param("email") String email,
                          @Param("userStatus") String userStatus);
    
    /**
     * ページネーション対応のユーザー検索
     */
    @Select("""
        <script>
        SELECT id, username, email, full_name, 
               user_status, last_login_at, created_at, updated_at, okta_user_id
        FROM users 
        WHERE 1=1
        <if test="search != null">
            AND (
                username LIKE CONCAT('%', #{search}, '%')
                OR email LIKE CONCAT('%', #{search}, '%')
                OR full_name LIKE CONCAT('%', #{search}, '%')
            )
        </if>
        <if test="userStatus != null">
            AND user_status = #{userStatus}
        </if>
        ORDER BY 
        <choose>
            <when test="sortBy == 'username'">username</when>
            <when test="sortBy == 'email'">email</when>
            <when test="sortBy == 'fullName'">full_name</when>
            <when test="sortBy == 'userStatus'">user_status</when>
            <when test="sortBy == 'updatedAt'">updated_at</when>
            <otherwise>created_at</otherwise>
        </choose>
        <if test="sortOrder == 'ASC'">ASC</if>
        <if test="sortOrder == 'DESC'">DESC</if>
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"), 
        @Result(property = "fullName", column = "full_name"),
        @Result(property = "userStatus", column = "user_status", typeHandler = com.devhour.infrastructure.typehandler.UserStatusTypeHandler.class),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "oktaUserId", column = "okta_user_id")
    })
    List<User> selectUsersWithPagination(@Param("search") String search,
                                        @Param("userStatus") String userStatus,
                                        @Param("sortBy") String sortBy,
                                        @Param("sortOrder") String sortOrder,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);
    
    /**
     * ユーザー数をカウント
     */
    @Select("""
        <script>
        SELECT COUNT(1)
        FROM users 
        WHERE 1=1
        <if test="search != null">
            AND (
                username LIKE CONCAT('%', #{search}, '%')
                OR email LIKE CONCAT('%', #{search}, '%')
                OR full_name LIKE CONCAT('%', #{search}, '%')
            )
        </if>
        <if test="userStatus != null">
            AND user_status = #{userStatus}
        </if>
        </script>
        """)
    long countUsers(@Param("search") String search,
                   @Param("userStatus") String userStatus);


    /**
     * Okta User IDの存在チェック
     * Okta認証時の重複チェックに使用
     * 
     * @param oktaUserId Okta User ID
     * @return 存在する場合true
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM users 
        WHERE okta_user_id = #{oktaUserId}
        """)
    boolean existsByOktaUserId(@Param("oktaUserId") String oktaUserId);
}