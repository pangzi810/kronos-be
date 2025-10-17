package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.repository.WorkCategoryRepository;
import com.devhour.infrastructure.mapper.WorkCategoryMapper;

/**
 * 作業カテゴリリポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してWorkCategoryRepositoryインターフェースを実装
 */
@Repository
public class WorkCategoryRepositoryImpl implements WorkCategoryRepository {
    
    private final WorkCategoryMapper categoryMapper;
    
    public WorkCategoryRepositoryImpl(WorkCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    
    @Override
    public Optional<WorkCategory> findById(String categoryId) {
        return categoryMapper.findById(categoryId);
    }
    
    @Override
    public Optional<WorkCategory> findByCode(CategoryCode code) {
        return categoryMapper.findByCode(code.value());
    }
    
    @Override
    public List<WorkCategory> findAll() {
        return categoryMapper.findAll();
    }
    
    @Override
    public List<WorkCategory> findAllActive() {
        return categoryMapper.findActiveCategories();
    }
    
    

    
    @Override
    public boolean existsByCode(CategoryCode code) {
        return categoryMapper.existsByCode(code.value());
    }
    
    @Override
    public boolean existsById(String categoryId) {
        return findById(categoryId).isPresent();
    }
    
    @Override
    public WorkCategory save(WorkCategory workCategory) {
        if (!existsById(workCategory.getId())) {
            // 新規作成
            categoryMapper.insert(
                workCategory.getId(),
                workCategory.getCode().value(),
                workCategory.getName().getValue(),
                workCategory.getDescription(),
                workCategory.isActive(),
                workCategory.getDisplayOrder().value(),
                "system", // TODO: 現在のユーザーIDを取得する仕組みが必要
                workCategory.getCreatedAt(),
                "system", // updatedBy
                workCategory.getUpdatedAt()
            );
        } else {
            // 更新
            categoryMapper.update(
                workCategory.getId(),
                workCategory.getName().getValue(),
                workCategory.getDescription(),
                workCategory.getDisplayOrder().value(),
                "system", // updatedBy
                workCategory.getUpdatedAt()
            );
        }
        return workCategory;
    }
    
    @Override
    public List<WorkCategory> saveAll(List<WorkCategory> workCategories) {
        workCategories.forEach(this::save);
        return workCategories;
    }
    
    @Override
    public void deleteById(String categoryId) {
        categoryMapper.softDelete(categoryId, LocalDateTime.now(), "system", LocalDateTime.now());
    }
    
}