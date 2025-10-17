package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 作業カテゴリ作成リクエスト
 */
public class WorkCategoryCreateRequest {
    
    @NotBlank(message = "カテゴリコードは必須です")
    @Size(max = 20, message = "カテゴリコードは20文字以内で入力してください")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "カテゴリコードは大文字英数字とアンダースコアのみ使用可能です")
    private String code;
    
    @NotBlank(message = "カテゴリ名は必須です")
    @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください")
    private String name;
    
    @Size(max = 200, message = "カテゴリ説明は200文字以内で入力してください")
    private String description;
    
    @Size(max = 7, message = "カラーコードは7文字以内で入力してください")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "カラーコードは#で始まる16進数6桁で入力してください")
    private String colorCode;
    
    @NotNull(message = "表示順序は必須です")
    private Integer displayOrder;
    
    public WorkCategoryCreateRequest() {
    }
    
    public WorkCategoryCreateRequest(String code, String name, String description, 
                                   String colorCode, Integer displayOrder, boolean systemRequired) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.displayOrder = displayOrder;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}