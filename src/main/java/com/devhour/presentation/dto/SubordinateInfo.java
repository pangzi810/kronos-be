package com.devhour.presentation.dto;

/**
 * 部下情報DTO
 * 
 * 部下一覧画面で表示する部下の基本情報とプロジェクト数を含む
 * ページネーション対応された部下一覧取得APIのレスポンス要素として使用
 * 
 * 責務:
 * - 部下の基本情報（ID、名前、メールアドレス）
 * - 現在のプロジェクト数
 * - キャパシティ警告フラグ
 */
public class SubordinateInfo {
    
    private String id;
    private String name;
    private String email;
    private Integer currentProjectCount;
    private Boolean isMaxCapacity;
    
    public SubordinateInfo() {
    }
    
    public SubordinateInfo(String id, String name, String email, 
                          Integer currentProjectCount, Boolean isMaxCapacity) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.currentProjectCount = currentProjectCount;
        this.isMaxCapacity = isMaxCapacity;
    }
    
    /**
     * ユーザーエンティティからSubordinateInfoを作成
     * 
     * @param userId ユーザーID
     * @param name ユーザー名
     * @param email メールアドレス
     * @param projectCount 現在のプロジェクト数
     * @return SubordinateInfo
     */
    public static SubordinateInfo from(String userId, String name, String email, Integer projectCount) {
        Integer count = projectCount != null ? projectCount : 0;
        Boolean isMaxCapacity = count >= 5; // TODO: 設定値として外部化を検討
        
        return new SubordinateInfo(userId, name, email, count, isMaxCapacity);
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getCurrentProjectCount() {
        return currentProjectCount;
    }
    
    public void setCurrentProjectCount(Integer currentProjectCount) {
        this.currentProjectCount = currentProjectCount;
    }
    
    public Boolean getIsMaxCapacity() {
        return isMaxCapacity;
    }
    
    public void setIsMaxCapacity(Boolean isMaxCapacity) {
        this.isMaxCapacity = isMaxCapacity;
    }
}