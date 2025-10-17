package com.devhour.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 承認者関係エンティティ
 * 
 * ユーザー間の承認者-申請者関係を管理するエンティティ
 * V44マイグレーション対応版：メールアドレスベース
 * 
 * 責務:
 * - 承認者と対象者の関係管理（メールアドレスベース）
 * - 有効期間の管理
 * - 承認権限の検証
 * - 期間重複チェック
 */
public class Approver {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$"
    );
    
    private String id;
    private String targetEmail;
    private String approverEmail;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Approver() {
        // MyBatisのマッピング用
    }
    
    /**
     * プライベートコンストラクタ
     */
    private Approver(String id, String targetEmail, String approverEmail,
                     LocalDateTime effectiveFrom, LocalDateTime effectiveTo,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.targetEmail = targetEmail;
        this.approverEmail = approverEmail;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 新しい承認者関係を作成
     * 
     * @param targetEmail 対象者メールアドレス（承認を受ける側）
     * @param approverEmail 承認者メールアドレス（承認する側）
     * @param effectiveFrom 有効開始日時
     * @param effectiveTo 有効終了日時（オプション）
     * @return 新しいApproverエンティティ
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    public static Approver create(String targetEmail, String approverEmail,
                                  LocalDateTime effectiveFrom, LocalDateTime effectiveTo) {
        validateCreateParameters(targetEmail, approverEmail, effectiveFrom, effectiveTo);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new Approver(id, targetEmail.trim().toLowerCase(), 
                           approverEmail.trim().toLowerCase(), 
                           effectiveFrom, effectiveTo, now, now);
    }
    
    /**
     * 既存の承認者関係を復元
     * 
     * @param id エンティティID
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     * @param effectiveFrom 有効開始日時
     * @param effectiveTo 有効終了日時
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 復元されたApproverエンティティ
     */
    public static Approver restore(String id, String targetEmail, String approverEmail,
                                   LocalDateTime effectiveFrom, LocalDateTime effectiveTo,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Approver(id, targetEmail, approverEmail, effectiveFrom, effectiveTo,
                           createdAt, updatedAt);
    }
    
    /**
     * 新しい承認者関係を作成（後方互換性版 - ユーザーIDベース）
     * V44移行前のコードとの互換性を保つためのメソッド
     * 実際はメールアドレスを使用して内部で変換される必要がある
     * 
     * @param userId ユーザーID（実際はメールアドレスに変換される）
     * @param approverId 承認者ID（実際はメールアドレスに変換される）
     * @param effectiveFrom 有効開始日
     * @param effectiveTo 有効終了日時
     * @param createdBy 作成者ID
     * @return 新しいApproverエンティティ
     * @deprecated メールアドレスベースのcreateメソッドを使用してください
     * @throws UnsupportedOperationException 現在の実装ではIDからメールアドレスへの変換がサポートされていない
     */
    @Deprecated
    public static Approver create(String userId, String approverId,
                                  LocalDate effectiveFrom, LocalDate effectiveTo, String createdBy) {
        // このメソッドはユーザーIDをメールアドレスに変換する必要があるが、
        // エンティティ内ではUserRepositoryにアクセスできない。
        // アプリケーションサービス層で変換してから呼び出す必要がある
        throw new UnsupportedOperationException(
            "V44移行後はメールアドレスベースのcreate(targetEmail, approverEmail, effectiveFrom, effectiveTo)メソッドを使用してください"
        );
    }
    
    /**
     * メールアドレスベースの新しい承認者関係を作成するヘルパーメソッド
     * アプリケーションサービス層でIDをメールアドレスに変換してから使用
     * 
     * @param targetEmail 対象者メールアドレス
     * @param approverEmail 承認者メールアドレス
     * @param effectiveFrom 有効開始日（LocalDate）
     * @param effectiveTo 有効終了日（LocalDate、オプション）
     * @return 新しいApproverエンティティ
     */
    public static Approver createWithDates(String targetEmail, String approverEmail,
                                           LocalDate effectiveFrom, LocalDate effectiveTo) {
        LocalDateTime effectiveFromDateTime = effectiveFrom.atStartOfDay();
        LocalDateTime effectiveToDateTime = effectiveTo != null ? effectiveTo.atTime(23, 59, 59) : null;
        
        return create(targetEmail, approverEmail, effectiveFromDateTime, effectiveToDateTime);
    }
    
    /**
     * パラメータ検証
     */
    private static void validateCreateParameters(String targetEmail, String approverEmail,
                                               LocalDateTime effectiveFrom, LocalDateTime effectiveTo) {
        // 対象者メールアドレスの検証
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("対象者メールアドレスは必須です");
        }
        
        if (!EMAIL_PATTERN.matcher(targetEmail.trim()).matches()) {
            throw new IllegalArgumentException("対象者メールアドレスの形式が正しくありません");
        }
        
        // 承認者メールアドレスの検証
        if (approverEmail == null || approverEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("承認者メールアドレスは必須です");
        }
        
        if (!EMAIL_PATTERN.matcher(approverEmail.trim()).matches()) {
            throw new IllegalArgumentException("承認者メールアドレスの形式が正しくありません");
        }
        
        // 自己承認の防止
        if (targetEmail.trim().toLowerCase().equals(approverEmail.trim().toLowerCase())) {
            throw new IllegalArgumentException("自己承認はできません");
        }
        
        // 有効開始日時の検証
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("有効開始日時は必須です");
        }
        
        // 有効終了日時の検証
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("有効終了日時は開始日時以降である必要があります");
        }
    }
    
    /**
     * 指定日において有効かチェック
     * 
     * @param date チェック対象日
     * @return 有効な場合true
     */
    public boolean isValidForDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("チェック対象日は必須です");
        }
        
        LocalDate startDate = effectiveFrom.toLocalDate();
        
        if (date.isBefore(startDate)) {
            return false;
        }
        
        if (effectiveTo != null) {
            LocalDate endDate = effectiveTo.toLocalDate();
            if (date.isAfter(endDate)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 指定日時において有効かチェック
     * 
     * @param dateTime チェック対象日時
     * @return 有効な場合true
     */
    public boolean isValidForDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("チェック対象日時は必須です");
        }
        
        if (dateTime.isBefore(effectiveFrom)) {
            return false;
        }
        
        if (effectiveTo != null && dateTime.isAfter(effectiveTo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 現在有効かチェック
     * 
     * @return 有効な場合true
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(effectiveFrom)) {
            return false;
        }
        
        if (effectiveTo != null && now.isAfter(effectiveTo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 有効期限が切れているかチェック
     * 
     * @return 期限切れの場合true
     */
    public boolean hasExpired() {
        if (effectiveTo == null) {
            return false; // 終了日時なしは期限切れしない
        }
        
        return LocalDateTime.now().isAfter(effectiveTo);
    }
    
    /**
     * 指定された期間と重複するかチェック
     * 
     * @param from 期間開始日時
     * @param to 期間終了日時
     * @return 重複する場合true
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    public boolean overlapsWithPeriod(LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            throw new IllegalArgumentException("開始日時は必須です");
        }
        
        if (to == null) {
            throw new IllegalArgumentException("終了日時は必須です");
        }
        
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("開始日時は終了日時以前である必要があります");
        }
        
        // 期間重複判定
        // Case 1: このApproverの期間がチェック期間の後にある
        if (effectiveFrom.isAfter(to)) {
            return false;
        }
        
        // Case 2: このApproverの期間がチェック期間の前にある
        if (effectiveTo != null && effectiveTo.isBefore(from)) {
            return false;
        }
        
        // その他の場合は重複
        return true;
    }
    
    /**
     * 承認者関係を終了（後方互換性）
     * V44移行後は使用しない（メールアドレスベースでは終了機能を提供しない）
     * 
     * @param endDate 終了日
     * @param updatedBy 更新者ID
     * @deprecated V44移行後は使用しない
     */
    @Deprecated
    public void terminate(LocalDate endDate, String updatedBy) {
        throw new UnsupportedOperationException("V44移行後、承認者関係の手動終了は非対応です");
    }
    
    /**
     * 削除済み状態の確認（後方互換性）
     * V44移行後は論理削除機能を削除したため常にfalse
     * 
     * @return 常にfalse
     * @deprecated V44移行後は論理削除機能なし
     */
    @Deprecated
    public boolean isDeleted() {
        return false; // V44移行後は論理削除なし
    }
    
    /**
     * 現在有効かチェック（後方互換性メソッド名）
     * 
     * @return 有効な場合true
     * @deprecated isCurrentlyValid()を使用
     */
    @Deprecated
    public boolean isCurrentlyEffective() {
        return isCurrentlyValid();
    }
    
    /**
     * 指定日において有効かチェック（後方互換性メソッド名）
     * 
     * @param date チェック対象日
     * @return 有効な場合true
     * @deprecated isValidForDate()を使用
     */
    @Deprecated
    public boolean isEffectiveOn(LocalDate date) {
        return isValidForDate(date);
    }
    
    /**
     * ユーザーID取得（後方互換性）
     * V44移行後は対象者メールアドレスを返す
     * 
     * @return 対象者メールアドレス
     * @deprecated getTargetEmail()を使用
     */
    @Deprecated
    public String getUserId() {
        return targetEmail;
    }
    
    /**
     * 承認者ID取得（後方互換性）
     * V44移行後は承認者メールアドレスを返す
     * 
     * @return 承認者メールアドレス
     * @deprecated getApproverEmail()を使用
     */
    @Deprecated
    public String getApproverId() {
        return approverEmail;
    }
    
    /**
     * 作成者ID取得（後方互換性）
     * V44移行後はサポートしない
     * 
     * @return nullを返す
     * @deprecated V44移行後はサポートしない
     */
    @Deprecated
    public String getCreatedBy() {
        return null; // V44移行後は作成者情報を保持しない
    }
    
    /**
     * 更新者ID取得（後方互換性）
     * V44移行後はサポートしない
     * 
     * @return nullを返す
     * @deprecated V44移行後はサポートしない
     */
    @Deprecated
    public String getUpdatedBy() {
        return null; // V44移行後は更新者情報を保持しない
    }
    
    /**
     * 既存の承認者関係を復元するヘルパーメソッド（後方互換性）
     * V44移行前のデータを復元する際に使用
     * 
     * @param id エンティティID
     * @param userId ユーザーID（メールアドレスに変換される）
     * @param approverId 承認者ID（メールアドレスに変換される）
     * @param effectiveFrom 有効開始日時
     * @param effectiveTo 有効終了日時
     * @param isDeleted 削除フラグ（無視される）
     * @param createdBy 作成者ID
     * @param createdAt 作成日時
     * @param updatedBy 更新者ID
     * @param updatedAt 更新日時
     * @return 復元されたApproverエンティティ
     * @deprecated メールアドレスベースのrestoreメソッドを使用してください
     */
    @Deprecated
    public static Approver restore(String id, String userId, String approverId,
                                   LocalDate effectiveFrom, LocalDate effectiveTo,
                                   boolean isDeleted, String createdBy, LocalDateTime createdAt,
                                   String updatedBy, LocalDateTime updatedAt) {
        // このメソッドはユーザーIDをメールアドレスに変換してから使用する必要がある
        throw new UnsupportedOperationException(
            "V44移行後はメールアドレスベースのrestore(id, targetEmail, approverEmail, effectiveFrom, effectiveTo, createdAt, updatedAt)メソッドを使用してください"
        );
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getTargetEmail() { return targetEmail; }
    public String getApproverEmail() { return approverEmail; }
    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public LocalDateTime getEffectiveTo() { return effectiveTo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Approver that = (Approver) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Approver{id='%s', targetEmail='%s', approverEmail='%s', effectiveFrom=%s, effectiveTo=%s}",
            id, targetEmail, approverEmail, effectiveFrom, effectiveTo);
    }
}