package com.devhour.domain.model.valueobject;

import java.util.Arrays;
import java.util.Objects;
import com.devhour.infrastructure.jackson.ProjectStatusDeserializer;
import com.devhour.infrastructure.jackson.ProjectStatusSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * プロジェクトステータスの値オブジェクト
 * 
 * ビジネスルール:
 * - 定義済みのステータス値のみ許可
 * - ステータス遷移のルールを管理
 * - 不変オブジェクトとして実装
 * 
 * ステータス:
 * - DRAFT: 計画中
 * - IN_PROGRESS: 進行中
 * - CLOSED: 完了
 */
@JsonSerialize(using = ProjectStatusSerializer.class)
@JsonDeserialize(using = ProjectStatusDeserializer.class)
public record ProjectStatus(String value) {
    
    // 定義済みステータス値
    private static final String DRAFT_VALUE = "DRAFT";
    private static final String IN_PROGRESS_VALUE = "IN_PROGRESS";
    private static final String CLOSED_VALUE = "CLOSED";
    
    // 許可されたステータス値の配列
    private static final String[] ALLOWED_VALUES = {
        DRAFT_VALUE, IN_PROGRESS_VALUE, CLOSED_VALUE
    };
    
    // 定義済みステータスの定数
    public static final ProjectStatus DRAFT = new ProjectStatus(DRAFT_VALUE);
    public static final ProjectStatus IN_PROGRESS = new ProjectStatus(IN_PROGRESS_VALUE);
    public static final ProjectStatus CLOSED = new ProjectStatus(CLOSED_VALUE);
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、不正な値の場合は例外をスロー
     * 
     * @param value ステータス文字列
     * @throws IllegalArgumentException 不正な値の場合
     */
    public ProjectStatus {
        validateProjectStatus(value);
    }
    
    /**
     * プロジェクトステータスの検証
     * 
     * @param value 検証対象の値
     * @throws IllegalArgumentException 検証エラーの場合
     */
    private void validateProjectStatus(String value) {
        if (value == null) {
            throw new IllegalArgumentException("プロジェクトステータスはnullにできません");
        }
        
        if (value.isBlank()) {
            throw new IllegalArgumentException("プロジェクトステータスは空文字にできません");
        }
        
        boolean isValid = Arrays.stream(ALLOWED_VALUES)
            .anyMatch(allowedValue -> allowedValue.equals(value.toUpperCase()));
            
        if (!isValid) {
            throw new IllegalArgumentException(
                String.format("プロジェクトステータスが不正です。許可された値: %s, 入力値: '%s'",
                    Arrays.toString(ALLOWED_VALUES), value)
            );
        }
    }
    
    /**
     * 文字列からProjectStatusを安全に生成
     * 大文字小文字を自動で統一
     * 
     * @param value ステータス文字列
     * @return ProjectStatusオブジェクト
     * @throws IllegalArgumentException 不正な値の場合
     */
    public static ProjectStatus of(String value) {
        if (value != null) {
            value = value.toUpperCase().trim();
        }
        return new ProjectStatus(value);
    }
    
    /**
     * プロジェクトが開始可能な状態かチェック
     * 
     * @return PLANNING状態の場合true
     */
    public boolean canStart() {
        return DRAFT_VALUE.equals(this.value);
    }
    
    /**
     * プロジェクトが進行中かチェック
     * 
     * @return IN_PROGRESS状態の場合true
     */
    public boolean isInProgress() {
        return IN_PROGRESS_VALUE.equals(this.value);
    }
    
    /**
     * プロジェクトが完了しているかチェック
     * 
     * @return CLOSED状態の場合true
     */
    public boolean isClosed() {
        return CLOSED_VALUE.equals(this.value);
    }
    
    /**
     * プロジェクトがアクティブ（工数記録可能）かチェック
     * 
     * @return PLANNINGまたはIN_PROGRESS状態の場合true
     */
    public boolean isActive() {
        return canStart() || isInProgress();
    }
    
    /**
     * 日本語での表示名を取得
     * 
     * @return 日本語表示名
     */
    public String getDisplayName() {
        return switch (this.value) {
            case DRAFT_VALUE -> "計画中";
            case IN_PROGRESS_VALUE -> "進行中";
            case CLOSED_VALUE -> "完了";
            default -> this.value;
        };
    }
    
    /**
     * ステータス遷移を実行
     * 
     * @param newStatus 遷移先ステータス
     * @return 新しいProjectStatusオブジェクト
     * @throws IllegalStateException 不正な遷移の場合
     */
    public ProjectStatus transitionTo(ProjectStatus newStatus) {
        return newStatus;
    }

    /**
     * 文字列表現を取得
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * 等価性の比較
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProjectStatus that = (ProjectStatus) obj;
        return Objects.equals(this.value, that.value);
    }
    
    /**
     * ハッシュコード
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}