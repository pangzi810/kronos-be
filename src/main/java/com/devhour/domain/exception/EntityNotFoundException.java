package com.devhour.domain.exception;

/**
 * エンティティが見つからない場合にスローされる例外
 * 
 * 指定されたIDやキーに対応するエンティティが
 * データベースに存在しない場合に使用される
 */
public class EntityNotFoundException extends RuntimeException {
    
    private final String entityType;
    private final String identifier;
    
    /**
     * エンティティタイプとIDを指定して例外を作成
     * 
     * @param entityType エンティティの種類（User, Project等）
     * @param identifier 検索に使用したID
     */
    public EntityNotFoundException(String entityType, String identifier) {
        super(String.format("%s not found with identifier: %s", entityType, identifier));
        this.entityType = entityType;
        this.identifier = identifier;
    }
    
    /**
     * エンティティタイプ、ID、原因となる例外を指定して例外を作成
     * 
     * @param entityType エンティティの種類
     * @param identifier 検索に使用したID
     * @param cause 原因となる例外
     */
    public EntityNotFoundException(String entityType, String identifier, Throwable cause) {
        super(String.format("%s not found with identifier: %s", entityType, identifier), cause);
        this.entityType = entityType;
        this.identifier = identifier;
    }
    
    /**
     * カスタムメッセージで例外を作成
     * 
     * @param message エラーメッセージ
     */
    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = null;
        this.identifier = null;
    }
    
    /**
     * カスタムメッセージと原因となる例外を指定して例外を作成
     * 
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = null;
        this.identifier = null;
    }
    
    /**
     * エンティティタイプを取得
     * 
     * @return エンティティタイプ
     */
    public String getEntityType() {
        return entityType;
    }
    
    /**
     * 識別子を取得
     * 
     * @return 識別子
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * User エンティティ用のファクトリメソッド
     * 
     * @param userId ユーザーID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException userNotFound(String userId) {
        return new EntityNotFoundException("User", userId);
    }
    
    /**
     * Project エンティティ用のファクトリメソッド
     * 
     * @param projectId プロジェクトID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException projectNotFound(String projectId) {
        return new EntityNotFoundException("Project", projectId);
    }
    
    /**
     * WorkRecord エンティティ用のファクトリメソッド
     * 
     * @param workRecordId 工数記録ID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException workRecordNotFound(String workRecordId) {
        return new EntityNotFoundException("WorkRecord", workRecordId);
    }
    
    /**
     * WorkCategory エンティティ用のファクトリメソッド
     * 
     * @param categoryId カテゴリID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException workCategoryNotFound(String categoryId) {
        return new EntityNotFoundException("WorkCategory", categoryId);
    }
    
    /**
     * SupervisorRelationship エンティティ用のファクトリメソッド
     * @deprecated Use approverNotFound instead
     * 
     * @param relationshipId 上長関係ID
     * @return EntityNotFoundException
     */
    @Deprecated
    public static EntityNotFoundException supervisorRelationshipNotFound(String relationshipId) {
        return new EntityNotFoundException("SupervisorRelationship", relationshipId);
    }
    
    /**
     * Approver エンティティ用のファクトリメソッド
     * 
     * @param approverId 承認者関係ID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException approverNotFound(String approverId) {
        return new EntityNotFoundException("Approver", approverId);
    }
    
    /**
     * ApprovalHistory エンティティ用のファクトリメソッド
     * 
     * @param historyId 承認履歴ID
     * @return EntityNotFoundException
     */
    public static EntityNotFoundException approvalHistoryNotFound(String historyId) {
        return new EntityNotFoundException("ApprovalHistory", historyId);
    }
}