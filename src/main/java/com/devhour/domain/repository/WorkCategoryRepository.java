package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;

/**
 * 作業カテゴリリポジトリインターフェース
 * 
 * 作業カテゴリエンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepository パターンの実装
 * 
 * 責務:
 * - 作業カテゴリエンティティの CRUD 操作
 * - カテゴリコード・表示順に基づく検索機能
 */
public interface WorkCategoryRepository {
    
    /**
     * カテゴリIDで作業カテゴリを検索
     * 
     * @param categoryId カテゴリID
     * @return 作業カテゴリエンティティ（存在しない場合は空のOptional）
     */
    Optional<WorkCategory> findById(String categoryId);
    
    /**
     * カテゴリコードで作業カテゴリを検索
     * 
     * @param code カテゴリコード
     * @return 作業カテゴリエンティティ（存在しない場合は空のOptional）
     */
    Optional<WorkCategory> findByCode(CategoryCode code);
    
    /**
     * 全作業カテゴリを取得
     * 
     * @return 全作業カテゴリのリスト（表示順昇順）
     */
    List<WorkCategory> findAll();
    
    /**
     * アクティブな作業カテゴリを取得
     * 
     * @return アクティブな作業カテゴリのリスト（表示順昇順）
     */
    List<WorkCategory> findAllActive();
    
    /**
     * カテゴリコードの存在チェック
     * 
     * @param code カテゴリコード
     * @return 存在する場合true
     */
    boolean existsByCode(CategoryCode code);
    
    /**
     * カテゴリIDの存在チェック
     * 
     * @param categoryId カテゴリID
     * @return 存在する場合true
     */
    boolean existsById(String categoryId);
    
    /**
     * 作業カテゴリを保存
     * 新規作成・更新の両方で使用
     * 
     * @param workCategory 保存対象の作業カテゴリエンティティ
     * @return 保存された作業カテゴリエンティティ
     */
    WorkCategory save(WorkCategory workCategory);
    
    /**
     * 複数の作業カテゴリを一括保存
     * 
     * @param workCategories 保存対象の作業カテゴリエンティティのリスト
     * @return 保存された作業カテゴリエンティティのリスト
     */
    List<WorkCategory> saveAll(List<WorkCategory> workCategories);
    
    /**
     * 作業カテゴリを削除
     * 
     * @param categoryId 削除対象のカテゴリID
     */
    void deleteById(String categoryId);
    
}