package com.devhour.infrastructure.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;

/**
 * 承認権限MyBatisマッパー
 * 
 * ApprovalAuthorityエンティティの永続化操作を提供
 * MyBatisアノテーションベースのマッピング（XMLファイル不使用）
 * 
 * 特徴:
 * - Position enum用TypeHandlerを使用してデータベース⇔Java間の変換を自動化
 * - 複雑な検索クエリのサポート（組織階層、役職、LIKE検索）
 * - パフォーマンス最適化されたインデックスを活用
 */
@Mapper
public interface ApprovalAuthorityMapper {

    /**
     * 全承認権限一覧を取得
     * 管理画面での承認権限者一覧表示に使用
     * 
     * @return 全承認権限のリスト（作成日昇順）
     */
    @Select("""
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        ORDER BY created_at ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<ApprovalAuthority> findAll();

    /**
     * メールアドレスで承認権限を検索
     * 承認権限者の特定・重複チェックに使用
     * 
     * @param email メールアドレス
     * @return 承認権限エンティティ（存在しない場合は空のOptional）
     */
    @Select("""
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        WHERE email = #{email}
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<ApprovalAuthority> findByEmail(@Param("email") String email);

    /**
     * 名前またはメールアドレスでの部分一致検索
     * 承認者選択UI（ApprovalSelectionModal）での検索機能に使用
     * 承認者を検索・選択する際のユーザビリティ向上のため
     * 
     * @param query 検索クエリ（名前またはメールアドレスの部分一致）
     * @return マッチした承認権限のリスト（階層レベル降順、名前昇順）
     */
    @Select("""
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        WHERE name LIKE CONCAT('%', #{query}, '%') 
           OR email LIKE CONCAT('%', #{query}, '%')
        ORDER BY 
            CASE position
                WHEN '統括本部長' THEN 4
                WHEN '本部長' THEN 3
                WHEN '部長' THEN 2
                WHEN 'マネージャー' THEN 1
                ELSE 0
            END DESC, name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<ApprovalAuthority> searchByNameOrEmail(@Param("query") String query);

    /**
     * 承認権限を挿入
     * 新規承認権限者の作成に使用
     * 
     * @param approvalAuthority 挿入対象の承認権限エンティティ
     */
    @Insert("""
        INSERT INTO approval_authorities (
            id, email, name, position,
            level_1_code, level_1_name, level_2_code, level_2_name,
            level_3_code, level_3_name, level_4_code, level_4_name,
            created_at, updated_at
        ) VALUES (
            #{id}, #{email}, #{name}, 
            #{position, typeHandler=com.devhour.infrastructure.typehandler.PositionTypeHandler},
            #{level1Code}, #{level1Name}, #{level2Code}, #{level2Name},
            #{level3Code}, #{level3Name}, #{level4Code}, #{level4Name},
            #{createdAt}, #{updatedAt}
        )
        """)
    void insert(ApprovalAuthority approvalAuthority);

    /**
     * 承認権限を更新
     * 既存承認権限者の情報変更に使用
     * 
     * @param approvalAuthority 更新対象の承認権限エンティティ
     */
    @Update("""
        UPDATE approval_authorities SET
            email = #{email},
            name = #{name},
            position = #{position, typeHandler=com.devhour.infrastructure.typehandler.PositionTypeHandler},
            level_1_code = #{level1Code},
            level_1_name = #{level1Name},
            level_2_code = #{level2Code},
            level_2_name = #{level2Name},
            level_3_code = #{level3Code},
            level_3_name = #{level3Name},
            level_4_code = #{level4Code},
            level_4_name = #{level4Name},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void update(ApprovalAuthority approvalAuthority);

    /**
     * メールアドレスで承認権限を削除
     * 承認権限者の削除・無効化に使用
     * 
     * @param email 削除対象のメールアドレス
     */
    @Delete("""
        DELETE FROM approval_authorities
        WHERE email = #{email}
        """)
    void deleteByEmail(@Param("email") String email);

    /**
     * 役職レベルで承認権限一覧を取得
     * 特定の役職レベルの承認者の一覧表示に使用
     * 
     * @param position 役職レベル
     * @return 指定役職の承認権限のリスト（名前昇順）
     */
    @Select("""
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        WHERE position = #{position, typeHandler=com.devhour.infrastructure.typehandler.PositionTypeHandler}
        ORDER BY name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<ApprovalAuthority> findByPosition(@Param("position") Position position);

    /**
     * 組織レベルコードで承認権限一覧を取得
     * 組織階層に基づく承認権限の管理・検索に使用
     * Level1-4の任意の組織レベルで検索可能
     * 
     * @param levelCode 組織レベルコード（Level1-4）
     * @param level 組織レベル（1-4）
     * @return 指定組織レベルの承認権限のリスト（階層レベル降順、名前昇順）
     */
    @Select("""
        <script>
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        WHERE 
        <choose>
            <when test="level == 1">level_1_code = #{levelCode}</when>
            <when test="level == 2">level_2_code = #{levelCode}</when>
            <when test="level == 3">level_3_code = #{levelCode}</when>
            <when test="level == 4">level_4_code = #{levelCode}</when>
        </choose>
        ORDER BY 
            CASE position
                WHEN '統括本部長' THEN 4
                WHEN '本部長' THEN 3
                WHEN '部長' THEN 2
                WHEN 'マネージャー' THEN 1
                ELSE 0
            END DESC, name ASC
        </script>
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<ApprovalAuthority> findByLevelCode(@Param("levelCode") String levelCode, @Param("level") int level);

    /**
     * 承認権限の存在チェック
     * 承認権限者の重複チェック・存在確認に使用
     * 
     * @param email メールアドレス
     * @return 存在する場合true
     */
    @Select("""
        SELECT COUNT(1) > 0
        FROM approval_authorities
        WHERE email = #{email}
        """)
    boolean existsByEmail(@Param("email") String email);

    /**
     * 組織内の承認権限者数をカウント
     * 組織管理・統計情報の表示に使用
     * 
     * @param levelCode 組織レベルコード
     * @param level 組織レベル（1-4）
     * @return 指定組織内の承認権限者数
     */
    @Select("""
        <script>
        SELECT COUNT(1)
        FROM approval_authorities
        WHERE 
        <choose>
            <when test="level == 1">level_1_code = #{levelCode}</when>
            <when test="level == 2">level_2_code = #{levelCode}</when>
            <when test="level == 3">level_3_code = #{levelCode}</when>
            <when test="level == 4">level_4_code = #{levelCode}</when>
        </choose>
        </script>
        """)
    long countByLevelCode(@Param("levelCode") String levelCode, @Param("level") int level);

    /**
     * 承認権限を持つユーザーの一覧を取得
     * 一般社員以外の全ての承認権限者を取得
     * 承認フローの構築・承認者候補の表示に使用
     * 
     * @return 承認権限を持つユーザーのリスト（階層レベル降順、名前昇順）
     */
    @Select("""
        SELECT id, email, name, position,
               level_1_code, level_1_name, level_2_code, level_2_name,
               level_3_code, level_3_name, level_4_code, level_4_name,
               created_at, updated_at
        FROM approval_authorities
        WHERE position IN ('マネージャー', '部長', '本部長', '統括本部長')
        ORDER BY 
            CASE position
                WHEN '統括本部長' THEN 4
                WHEN '本部長' THEN 3
                WHEN '部長' THEN 2
                WHEN 'マネージャー' THEN 1
                ELSE 0
            END DESC, name ASC
        """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "name", column = "name"),
        @Result(property = "position", column = "position", 
                typeHandler = com.devhour.infrastructure.typehandler.PositionTypeHandler.class),
        @Result(property = "level1Code", column = "level_1_code"),
        @Result(property = "level1Name", column = "level_1_name"),
        @Result(property = "level2Code", column = "level_2_code"),
        @Result(property = "level2Name", column = "level_2_name"),
        @Result(property = "level3Code", column = "level_3_code"),
        @Result(property = "level3Name", column = "level_3_name"),
        @Result(property = "level4Code", column = "level_4_code"),
        @Result(property = "level4Name", column = "level_4_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<ApprovalAuthority> findAllWithApprovalAuthority();
}