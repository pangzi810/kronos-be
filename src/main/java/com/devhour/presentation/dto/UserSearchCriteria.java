package com.devhour.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザー検索条件DTO
 * 
 * ユーザー一覧検索のパラメータを保持
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {
    
    /**
     * ページ番号（0始まり）
     */
    @Builder.Default
    private int page = 0;
    
    /**
     * ページサイズ
     */
    @Builder.Default
    private int size = 20;
    
    /**
     * ソート項目
     */
    @Builder.Default
    private String sortBy = "createdAt";
    
    /**
     * ソート順序（ASC/DESC）
     */
    @Builder.Default
    private String sortOrder = "DESC";
    
    /**
     * ステータスフィルタ（ACTIVE/INACTIVE/null）
     */
    private String status;
    
    /**
     * 検索キーワード（ユーザー名、メール、フルネームの部分一致）
     */
    private String search;
    
    /**
     * パラメータのバリデーション
     */
    public void validate() {
        // ページ番号の検証
        if (page < 0) {
            throw new IllegalArgumentException("ページ番号は0以上である必要があります");
        }
        
        // ページサイズの検証
        if (size <= 0) {
            throw new IllegalArgumentException("ページサイズは1以上である必要があります");
        }
        if (size > 100) {
            throw new IllegalArgumentException("ページサイズは100以下である必要があります");
        }
        
        // ステータスの検証
        if (status != null && !status.isEmpty()) {
            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
                throw new IllegalArgumentException("不正なステータス: " + status);
            }
        }
        
        // ソート順序の検証
        if (!sortOrder.equals("ASC") && !sortOrder.equals("DESC")) {
            throw new IllegalArgumentException("不正なソート順序: " + sortOrder);
        }
    }
    
    /**
     * オフセットの計算
     */
    public int getOffset() {
        return page * size;
    }
}