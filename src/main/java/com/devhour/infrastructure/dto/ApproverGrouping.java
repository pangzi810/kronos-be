package com.devhour.infrastructure.dto;

/**
 * 承認者関係のグループ化結果を表すDTO
 * バッチ処理向けの効率的なデータ取得で使用
 * 
 * V44マイグレーション対応：メールアドレスベース
 */
public class ApproverGrouping {
    
    private String targetEmail;
    private String approverEmails;
    
    public ApproverGrouping() {
        // MyBatisのマッピング用デフォルトコンストラクタ
    }
    
    public ApproverGrouping(String targetEmail, String approverEmails) {
        this.targetEmail = targetEmail;
        this.approverEmails = approverEmails;
    }
    
    /**
     * 対象者メールアドレス
     */
    public String getTargetEmail() {
        return targetEmail;
    }
    
    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }
    
    /**
     * 承認者メールアドレス（カンマ区切り）
     */
    public String getApproverEmails() {
        return approverEmails;
    }
    
    public void setApproverEmails(String approverEmails) {
        this.approverEmails = approverEmails;
    }
    
    @Override
    public String toString() {
        return String.format("ApproverGrouping{targetEmail='%s', approverEmails='%s'}", 
            targetEmail, approverEmails);
    }
}