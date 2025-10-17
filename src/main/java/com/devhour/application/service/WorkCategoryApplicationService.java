package com.devhour.application.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryName;
import com.devhour.domain.model.valueobject.DisplayOrder;
import com.devhour.domain.repository.WorkCategoryRepository;

/**
 * 作業カテゴリアプリケーションサービス
 * 
 * 作業カテゴリマスター管理に関するユースケースを実装
 * 
 * 責務:
 * - 作業カテゴリの作成・更新・削除
 * - カテゴリ表示順の管理
 * - システム必須カテゴリの保護
 * - マスターデータ管理機能の提供
 */
@Service
@Transactional
public class WorkCategoryApplicationService {
    
    private final WorkCategoryRepository workCategoryRepository;
    
    public WorkCategoryApplicationService(WorkCategoryRepository workCategoryRepository) {
        this.workCategoryRepository = workCategoryRepository;
    }
    
    /**
     * 新しい作業カテゴリを作成
     * 
     * @param code カテゴリコード
     * @param name カテゴリ名
     * @param description カテゴリ説明
     * @param colorCode 表示色コード（オプション）
     * @param userId 作成者のユーザーID
     * @return 作成された作業カテゴリ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     * @throws IllegalStateException カテゴリコード重複や権限不足の場合
     */
    public WorkCategory createWorkCategory(CategoryCode code, CategoryName name, String description,
                                         String colorCode, String userId) {
        // カテゴリコードの重複チェック
        if (workCategoryRepository.existsByCode(code)) {
            throw new IllegalStateException(String.format("カテゴリコード '%s' は既に使用されています", code.value()));
        }
        
        // 表示順を自動で設定（最後に追加）
        int maxDisplayOrder = 999; // Maximum allowed display order
        DisplayOrder displayOrder = new DisplayOrder(maxDisplayOrder);
        
        // 作業カテゴリ作成
        WorkCategory workCategory = WorkCategory.create(code, name, description, displayOrder, colorCode, userId);
        
        return workCategoryRepository.save(workCategory);
    }
    
    /**
     * 作業カテゴリ情報を更新
     * 
     * @param categoryId カテゴリID
     * @param name 新しいカテゴリ名
     * @param description 新しい説明
     * @param colorCode 新しい表示色コード
     * @param userId 更新者のユーザーID
     * @return 更新された作業カテゴリ
     * @throws IllegalArgumentException カテゴリが存在しない場合
     * @throws IllegalStateException 権限不足の場合
     */
    public WorkCategory updateWorkCategory(String categoryId, CategoryName name, String description,
                                         String colorCode, String userId) {
        WorkCategory workCategory = workCategoryRepository.findById(categoryId)
            .orElseThrow(() -> EntityNotFoundException.workCategoryNotFound(categoryId));
        
        workCategory.updateCategoryInfo(name, description, workCategory.getDisplayOrder(), colorCode);
        
        return workCategoryRepository.save(workCategory);
    }
    
    
    /**
     * 作業カテゴリを有効化
     * 
     * @param categoryId カテゴリID
     * @param userId 実行者のユーザーID
     * @return 更新された作業カテゴリ
     * @throws IllegalArgumentException カテゴリが存在しない場合
     * @throws IllegalStateException 権限不足の場合
     */
    public WorkCategory activateWorkCategory(String categoryId, String userId) {
        WorkCategory workCategory = workCategoryRepository.findById(categoryId)
            .orElseThrow(() -> EntityNotFoundException.workCategoryNotFound(categoryId));
        
        workCategory.activate();
        
        return workCategoryRepository.save(workCategory);
    }
    
    /**
     * 作業カテゴリを無効化
     * 
     * @param categoryId カテゴリID
     * @param userId 実行者のユーザーID
     * @return 更新された作業カテゴリ
     * @throws IllegalArgumentException カテゴリが存在しない場合
     * @throws IllegalStateException システム必須カテゴリや権限不足の場合
     */
    public WorkCategory deactivateWorkCategory(String categoryId, String userId) {
        WorkCategory workCategory = workCategoryRepository.findById(categoryId)
            .orElseThrow(() -> EntityNotFoundException.workCategoryNotFound(categoryId));
        
        workCategory.deactivate(); // システム必須カテゴリの場合は例外発生
        
        return workCategoryRepository.save(workCategory);
    }
    
    
    // === 検索・取得メソッド ===
    
    /**
     * カテゴリIDで作業カテゴリを取得
     * 
     * @param categoryId カテゴリID
     * @return 作業カテゴリ（存在しない場合は空のOptional）
     */
    @Transactional(readOnly = true)
    public Optional<WorkCategory> findById(String categoryId) {
        return workCategoryRepository.findById(categoryId);
    }
    
    
    /**
     * 全作業カテゴリを取得
     * 
     * @return 全作業カテゴリのリスト（表示順）
     */
    @Transactional(readOnly = true)
    public List<WorkCategory> findAllWorkCategories() {
        return workCategoryRepository.findAll();
    }
    
    /**
     * アクティブな作業カテゴリを取得
     * 
     * @return アクティブな作業カテゴリのリスト（表示順）
     */
    @Transactional(readOnly = true)
    public List<WorkCategory> findActiveWorkCategories() {
        return workCategoryRepository.findAllActive();
    }
    
    

    
    
    
    
}