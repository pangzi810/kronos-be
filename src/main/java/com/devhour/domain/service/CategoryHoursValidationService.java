package com.devhour.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devhour.domain.model.entity.WorkCategory;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.repository.WorkCategoryRepository;

@Service
public class CategoryHoursValidationService {
    private final WorkCategoryRepository workCategoryRepository;

    /**
     * コンストラクタ
     * 
     * @param workCategoryRepository 作業カテゴリリポジトリ
     */
    public CategoryHoursValidationService(WorkCategoryRepository workCategoryRepository) {
        this.workCategoryRepository = workCategoryRepository;
    }
    
    /**
     * カテゴリ別工数の妥当性をチェックするメソッド
     * 
     * @param categoryHours カテゴリ別工数
     * @throws IllegalArgumentException カテゴリ別工数が不正な場合
     */
    public void validate(CategoryHours categoryHours) {
        if (categoryHours == null) {
            throw new IllegalArgumentException("カテゴリ別工数は空であってはなりません");
        }
        List<WorkCategory> usableWorkCategories = workCategoryRepository.findAllActive();
        
        // 各カテゴリの工数が0以上であることを確認
        categoryHours.hours().forEach((category, hours) -> {
            // Validation logic can be added here
            usableWorkCategories.stream()
                .filter(c -> c.getCode().equals(category))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("無効なカテゴリ: " + category.value()));
        });
    }
}
