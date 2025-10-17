package com.devhour.presentation.dto.response;

import java.util.List;
import com.devhour.presentation.dto.SubordinateInfo;

/**
 * ページ分割部下レスポンスDTO
 * 
 * 部下一覧取得APIのレスポンス
 * ページネーション情報と部下情報のリストを含む
 * 
 * 責務:
 * - 部下情報のリスト
 * - ページネーション情報（総件数、総ページ数、現在ページ、ページサイズ）
 * - 20件/ページのページネーション対応
 */
public class PagedSubordinateResponse {
    
    private List<SubordinateInfo> subordinates;
    private Integer totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    
    public PagedSubordinateResponse() {
    }
    
    public PagedSubordinateResponse(List<SubordinateInfo> subordinates, 
                                   Integer totalElements, Integer totalPages, 
                                   Integer currentPage, Integer pageSize) {
        this.subordinates = subordinates;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
    
    /**
     * ページ分割レスポンスを作成
     * 
     * @param subordinates 部下情報のリスト
     * @param totalElements 総件数
     * @param currentPage 現在のページ（0ベース）
     * @param pageSize ページサイズ
     * @return PagedSubordinateResponse
     */
    public static PagedSubordinateResponse of(List<SubordinateInfo> subordinates, 
                                            Integer totalElements, 
                                            Integer currentPage, 
                                            Integer pageSize) {
        Integer totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        
        return new PagedSubordinateResponse(subordinates, totalElements, totalPages, currentPage, pageSize);
    }
    
    /**
     * 空のレスポンスを作成
     * 部下が存在しない場合に使用
     * 
     * @param currentPage 現在のページ
     * @param pageSize ページサイズ
     * @return 空のPagedSubordinateResponse
     */
    public static PagedSubordinateResponse empty(Integer currentPage, Integer pageSize) {
        return new PagedSubordinateResponse(List.of(), 0, 0, currentPage, pageSize);
    }
    
    /**
     * 部下が存在するかチェック
     * 
     * @return 部下が存在する場合true
     */
    public boolean hasSubordinates() {
        return subordinates != null && !subordinates.isEmpty();
    }
    
    public List<SubordinateInfo> getSubordinates() {
        return subordinates;
    }
    
    public void setSubordinates(List<SubordinateInfo> subordinates) {
        this.subordinates = subordinates;
    }
    
    public Integer getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}