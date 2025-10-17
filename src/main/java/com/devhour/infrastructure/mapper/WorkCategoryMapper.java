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
import org.apache.ibatis.annotations.Update;

import com.devhour.domain.model.entity.WorkCategory;

/**
 * 作業カテゴリMyBatisマッパー
 * 
 * 作業カテゴリエンティティの永続化操作を提供
 */
@Mapper
public interface WorkCategoryMapper {
    
    /**
     * カテゴリIDでカテゴリを検索
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_at, updated_by, updated_at
        FROM work_categories 
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WorkCategory> findById(@Param("id") String id);
    
    /**
     * カテゴリコードでカテゴリを検索
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_by, created_at, updated_at, updated_by
        FROM work_categories 
        WHERE code = #{code} AND deleted_at IS NULL
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WorkCategory> findByCode(@Param("code") String code);
    
    /**
     * 全カテゴリ取得
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_at, updated_by, updated_at
        FROM work_categories 
        WHERE deleted_at IS NULL
        ORDER BY display_order ASC, code ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkCategory> findAll();
    
    /**
     * アクティブなカテゴリ取得
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_at, updated_by, updated_at
        FROM work_categories 
        WHERE is_active = true AND deleted_at IS NULL
        ORDER BY display_order ASC, code ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkCategory> findActiveCategories();
    
    /**
     * ユーザー定義カテゴリ取得
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_at, updated_by, updated_at
        FROM work_categories 
        WHERE deleted_at IS NULL
        ORDER BY display_order ASC, code ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkCategory> findUserDefinedCategories();
    
    /**
     * 名前での部分一致検索
     */
    @Select("""
        SELECT id, code, name, description, is_active, 
               display_order, created_by, created_at, updated_by, updated_at
        FROM work_categories 
        WHERE name LIKE CONCAT('%', #{namePattern}, '%') AND deleted_at IS NULL
        ORDER BY display_order ASC, code ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryCodeTypeHandler.class),
        @Result(property = "name", column = "name", 
                typeHandler = com.devhour.infrastructure.typehandler.CategoryNameTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "displayOrder", column = "display_order",
                typeHandler = com.devhour.infrastructure.typehandler.DisplayOrderTypeHandler.class),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedBy", column = "updated_by"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<WorkCategory> searchByName(@Param("namePattern") String namePattern);
    
    /**
     * カテゴリ作成
     */
    @Insert("""
        INSERT INTO work_categories (id, code, name, description, 
                                   is_active, display_order, created_by, created_at, updated_by, updated_at)
        VALUES (#{id}, #{code}, #{name}, #{description}, 
                #{isActive}, #{displayOrder}, #{createdBy}, #{createdAt}, #{updatedBy}, #{updatedAt})
        """)
    void insert(@Param("id") String id,
                @Param("code") String code,
                @Param("name") String name,
                @Param("description") String description,
                @Param("isActive") boolean isActive,
                @Param("displayOrder") int displayOrder,
                @Param("createdBy") String createdBy,
                @Param("createdAt") LocalDateTime createdAt,
                @Param("updatedBy") String updatedBy,
                @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * カテゴリ更新
     */
    @Update("""
        UPDATE work_categories 
        SET name = #{name}, description = #{description}, display_order = #{displayOrder}, 
            updated_by = #{updatedBy}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int update(@Param("id") String id,
               @Param("name") String name,
               @Param("description") String description,
               @Param("displayOrder") int displayOrder,
                @Param("updatedBy") String updatedBy,
               @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * アクティブ状態更新
     */
    @Update("""
        UPDATE work_categories 
        SET is_active = #{isActive}, updated_by = #{updatedBy}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int updateActiveStatus(@Param("id") String id,
                          @Param("isActive") boolean isActive,
                          @Param("updatedBy") String updatedBy,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 表示順序更新
     */
    @Update("""
        UPDATE work_categories 
        SET display_order = #{displayOrder}, 
        updated_by = #{updatedBy}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int updateDisplayOrder(@Param("id") String id,
                          @Param("displayOrder") int displayOrder,
                          @Param("updatedBy") String updatedBy,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * カテゴリ論理削除
     */
    @Update("""
        UPDATE work_categories 
        SET deleted_at = #{deletedAt}, 
        updated_by = #{updatedBy}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_at IS NULL
        """)
    int softDelete(@Param("id") String id,
                   @Param("deletedAt") LocalDateTime deletedAt,
                   @Param("updatedBy") String updatedBy,
                   @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * カテゴリコード存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM work_categories 
        WHERE code = #{code} AND deleted_at IS NULL
        """)
    boolean existsByCode(@Param("code") String code);
    
    /**
     * カテゴリ名存在チェック
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM work_categories 
        WHERE name = #{name} AND deleted_at IS NULL
        """)
    boolean existsByName(@Param("name") String name);
    
    /**
     * 表示順序の最大値取得
     */
    @Select("""
        SELECT COALESCE(MAX(display_order), 0)
        FROM work_categories 
        WHERE deleted_at IS NULL
        """)
    int getMaxDisplayOrder();
    
    /**
     * カテゴリ総数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM work_categories 
        WHERE deleted_at IS NULL
        """)
    long count();
    
    /**
     * アクティブカテゴリ数取得
     */
    @Select("""
        SELECT COUNT(1)
        FROM work_categories 
        WHERE is_active = true AND deleted_at IS NULL
        """)
    long countActiveCategories();
}