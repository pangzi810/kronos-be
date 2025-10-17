package com.devhour.domain.model.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.util.JsonUtil;

/**
 * 同期履歴詳細エンティティ
 * 
 * JIRA同期で各プロジェクトの同期詳細結果を管理するエンティティ
 * 
 * 責務:
 * - 個別プロジェクト同期結果の記録
 * - 処理時間とエラー情報の管理
 * - 同期履歴との関連付け管理
 * - JIRAイシューキーとプロジェクトの関連付け記録
 */
public class JiraSyncHistoryDetail {
    
    private String id;
    private String syncHistoryId;
    private Integer seq;
    private String operation;
    private DetailStatus status;
    private String result;
    private LocalDateTime processedAt;
    
    // 親の同期履歴との関連（遅延読み込み用）
    private JiraSyncHistory syncHistory;
    
    private JiraSyncHistoryDetail() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }

    /**
     * プライベートコンストラクタ（新規作成用）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private JiraSyncHistoryDetail(String id, String syncHistoryId, Integer seq, String operation,
                             DetailStatus status, String result, LocalDateTime processedAt) {
        this.id = id;
        this.syncHistoryId = syncHistoryId;
        this.seq = seq;
        this.operation = operation;
        this.status = status;
        this.result = result;
        this.processedAt = processedAt;
    }
    
    /**
     * 成功した詳細レコードを作成するファクトリーメソッド
     *
     * @param syncHistoryId 同期履歴ID
     * @param operation 実行された操作
     * @return 成功した詳細レコード
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraSyncHistoryDetail createSuccess(String syncHistoryId, Integer seq, String operation, String result) {
        validateCreateParameters(syncHistoryId);

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // 操作の正規化
        String normalizedOperation = operation != null && !operation.trim().isEmpty()
            ? operation.trim()
            : null;

        return new JiraSyncHistoryDetail(id, syncHistoryId.trim(), seq, normalizedOperation,
                                   DetailStatus.SUCCESS, result, now);
    }
    
    /**
     * 成功した詳細レコードを作成するファクトリーメソッド（オブジェクトをJSON文字列に変換）
     *
     * @param syncHistoryId 同期履歴ID
     * @param operation 実行された操作
     * @param result 結果オブジェクト（JSON文字列に変換される）
     * @return 成功した詳細レコード
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraSyncHistoryDetail createSuccess(String syncHistoryId, Integer seq, String operation, Object result) {
        String jsonResult = JsonUtil.toJson(result);
        return createSuccess(syncHistoryId, seq, operation, jsonResult);
    }

    /**
     * エラーが発生した詳細レコードを作成するファクトリーメソッド
     *
     * @param syncHistoryId 同期履歴ID
     * @param operation 実行しようとした操作
     * @param result 結果メッセージ（nullable）
     * @return エラーした詳細レコード
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraSyncHistoryDetail createError(String syncHistoryId, Integer seq, String operation, String result) {
        validateCreateParameters(syncHistoryId);

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // 結果メッセージの正規化
        String normalizedResult = result != null && !result.trim().isEmpty()
            ? result.trim()
            : null;

        // 操作の正規化
        String normalizedOperation = operation != null && !operation.trim().isEmpty()
            ? operation.trim()
            : null;

        return new JiraSyncHistoryDetail(id, syncHistoryId.trim(), seq, normalizedOperation,
                                   DetailStatus.ERROR, normalizedResult, now);
    }

    /**
     * エラーが発生した詳細レコードを作成するファクトリーメソッド（オブジェクトをJSON文字列に変換）
     *
     * @param syncHistoryId 同期履歴ID
     * @param operation 実行しようとした操作
     * @param result 結果オブジェクト（JSON文字列に変換される）
     * @return エラーした詳細レコード
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraSyncHistoryDetail createError(String syncHistoryId, Integer seq, String operation, Object result) {
        String jsonResult = JsonUtil.toJson(result);
        return createError(syncHistoryId, seq, operation, jsonResult);
    }
    
    /**
     * 既存同期履歴詳細を復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     *
     * @param id 詳細ID
     * @param syncHistoryId 同期履歴ID
     * @param operation 操作
     * @param status ステータス
     * @param result 結果メッセージ
     * @param processedAt 処理日時
     * @return 復元されたSyncHistoryDetailエンティティ
     */
    public static JiraSyncHistoryDetail restore(String id, String syncHistoryId, Integer seq, String operation,
                                          DetailStatus status,
                                          String result, LocalDateTime processedAt) {
        return new JiraSyncHistoryDetail(id, syncHistoryId, seq, operation,
                                   status, result, processedAt);
    }
    
    /**
     * 作成パラメータの検証
     */
    private static void validateCreateParameters(String syncHistoryId) {
        // 同期履歴IDの検証
        if (syncHistoryId == null || syncHistoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("同期履歴IDは必須です");
        }
    }
    
    /**
     * 処理が成功したかを判定
     * 
     * @return 成功の場合true
     */
    public boolean isSuccess() {
        return status.isSuccess();
    }
    
    /**
     * 処理でエラーが発生したかを判定
     * 
     * @return エラーの場合true
     */
    public boolean isError() {
        return status.isError();
    }
    
    
    
    
    /**
     * 処理時間を計算（同期履歴の開始時刻から詳細の処理時刻まで）
     * 
     * @param syncStartTime 同期開始時刻
     * @return 処理時間（ミリ秒）。同期開始時刻がnullの場合は0
     */
    public long getProcessingTime(LocalDateTime syncStartTime) {
        if (syncStartTime == null || processedAt == null) {
            return 0;
        }
        
        if (syncStartTime.isAfter(processedAt)) {
            return 0; // 開始時刻より前の処理時刻は不正とみなす
        }
        
        return Duration.between(syncStartTime, processedAt).toMillis();
    }
    
    /**
     * 同期履歴を設定（リポジトリからの読み込み用）
     * 
     * @param syncHistory 同期履歴
     */
    public void setSyncHistory(JiraSyncHistory syncHistory) {
        this.syncHistory = syncHistory;
    }
    
    /**
     * 同期履歴を取得
     * 
     * @return 同期履歴（遅延読み込み）
     */
    public JiraSyncHistory getSyncHistory() {
        return syncHistory;
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getSyncHistoryId() { return syncHistoryId; }
    public Integer getSeq() { return seq; }
    public String getOperation() { return operation; }
    public DetailStatus getStatus() { return status; }
    public String getResult() { return result; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    
    // MyBatis用のpackage-privateセッター
    void setId(String id) { this.id = id; }
    void setSyncHistoryId(String syncHistoryId) { this.syncHistoryId = syncHistoryId; }
    void setSeq(Integer seq) { this.seq = seq; }
    void setOperation(String operation) { this.operation = operation; }
    void setStatus(DetailStatus status) { this.status = status; }
    void setResult(String result) { this.result = result; }
    void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JiraSyncHistoryDetail that = (JiraSyncHistoryDetail) obj;
        return Objects.equals(id, that.id);
    }
    
    /**
     * ハッシュコード（IDベース）
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 文字列表現
     */
    @Override
    public String toString() {
        return String.format("SyncHistoryDetail{id='%s', syncHistoryId='%s', operation='%s', status=%s, processedAt=%s}",
                           id, syncHistoryId, operation, status, processedAt);
    }
}