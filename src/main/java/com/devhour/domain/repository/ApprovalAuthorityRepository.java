package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;

/**
 * 承認権限リポジトリインターフェース
 * 
 * 承認権限エンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepository パターンの実装
 * 
 * 責務:
 * - 承認権限エンティティの CRUD 操作
 * - 承認者選択UIのための検索機能
 * - 組織階層・役職レベルに基づく検索機能
 * - 工数承認ワークフローの承認権限管理
 */
public interface ApprovalAuthorityRepository {
    
    /**
     * 全承認権限一覧を取得
     * 管理画面での承認権限者一覧表示に使用
     * 
     * @return 全承認権限のリスト（作成日昇順）
     */
    List<ApprovalAuthority> findAll();
    
    /**
     * メールアドレスで承認権限を検索
     * 承認権限者の特定・重複チェックに使用
     * 
     * @param email メールアドレス
     * @return 承認権限エンティティ（存在しない場合は空のOptional）
     */
    Optional<ApprovalAuthority> findByEmail(String email);
    
    /**
     * 名前またはメールアドレスでの部分一致検索
     * 承認者選択UI（ApprovalSelectionModal）での検索機能に使用
     * 承認者を検索・選択する際のユーザビリティ向上のため
     * 
     * @param query 検索クエリ（名前またはメールアドレスの部分一致）
     * @return マッチした承認権限のリスト（階層レベル降順、名前昇順）
     */
    List<ApprovalAuthority> searchByNameOrEmail(String query);
    
    /**
     * 承認権限を保存
     * 新規作成・更新の両方で使用
     * 
     * @param approvalAuthority 保存対象の承認権限エンティティ
     * @return 保存された承認権限エンティティ
     */
    ApprovalAuthority save(ApprovalAuthority approvalAuthority);
    
    /**
     * メールアドレスで承認権限を削除
     * 承認権限者の削除・無効化に使用
     * 
     * @param email 削除対象のメールアドレス
     */
    void deleteByEmail(String email);
    
    /**
     * 役職レベルで承認権限一覧を取得
     * 特定の役職レベルの承認者の一覧表示に使用
     * 
     * @param position 役職レベル
     * @return 指定役職の承認権限のリスト（名前昇順）
     */
    List<ApprovalAuthority> findByPosition(Position position);
    
    /**
     * 組織レベルコードで承認権限一覧を取得
     * 組織階層に基づく承認権限の管理・検索に使用
     * Level1-4の任意の組織レベルで検索可能
     * 
     * @param levelCode 組織レベルコード（Level1-4）
     * @param level 組織レベル（1-4）
     * @return 指定組織レベルの承認権限のリスト（階層レベル降順、名前昇順）
     * @throws IllegalArgumentException levelが1-4の範囲外の場合
     */
    List<ApprovalAuthority> findByLevelCode(String levelCode, int level);
    
    /**
     * 承認権限の存在チェック
     * 承認権限者の重複チェック・存在確認に使用
     * 
     * @param email メールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);
    
    /**
     * 組織内の承認権限者数をカウント
     * 組織管理・統計情報の表示に使用
     * 
     * @param levelCode 組織レベルコード
     * @param level 組織レベル（1-4）
     * @return 指定組織内の承認権限者数
     * @throws IllegalArgumentException levelが1-4の範囲外の場合
     */
    long countByLevelCode(String levelCode, int level);
    
    /**
     * 承認権限を持つユーザーの一覧を取得
     * 一般社員以外の全ての承認権限者を取得
     * 承認フローの構築・承認者候補の表示に使用
     * 
     * @return 承認権限を持つユーザーのリスト（階層レベル降順、名前昇順）
     */
    List<ApprovalAuthority> findAllWithApprovalAuthority();
}