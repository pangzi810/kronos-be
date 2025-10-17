package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 複数部下の一括プロジェクトアサインリクエスト
 * 
 * 部下プロジェクトアサイン機能のバッチアサインAPIのリクエストボディとして使用
 * Bean Validationによるリクエストデータの検証を実装
 * 
 * 責務:
 * - プロジェクトIDとアサイン対象部下IDリストの受け取り
 * - リクエストデータの妥当性検証
 * - 一度にアサインできる部下数の制限
 */
public class BatchAssignmentRequest {
    
    @NotNull(message = "プロジェクトIDは必須です")
    private String projectId;
    
    @NotEmpty(message = "部下IDリストは空にできません")
    @Size(max = 100, message = "一度にアサインできる部下は100人までです")
    private List<@NotNull String> subordinateIds;
    
    public BatchAssignmentRequest() {
    }
    
    public BatchAssignmentRequest(String projectId, List<String> subordinateIds) {
        this.projectId = projectId;
        this.subordinateIds = subordinateIds;
    }
    
    /**
     * アサイン対象の部下数を取得
     * 
     * @return 部下数
     */
    public int getSubordinateCount() {
        return subordinateIds != null ? subordinateIds.size() : 0;
    }
    
    /**
     * 重複する部下IDを除去
     * 
     * @return 重複除去されたリクエスト
     */
    public BatchAssignmentRequest withDistinctSubordinateIds() {
        if (subordinateIds == null) {
            return this;
        }
        
        List<String> distinctIds = subordinateIds.stream()
            .distinct()
            .toList();
        
        return new BatchAssignmentRequest(projectId, distinctIds);
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public List<String> getSubordinateIds() {
        return subordinateIds;
    }
    
    public void setSubordinateIds(List<String> subordinateIds) {
        this.subordinateIds = subordinateIds;
    }
}