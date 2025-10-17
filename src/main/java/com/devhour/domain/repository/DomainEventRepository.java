package com.devhour.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.devhour.domain.event.WorkRecordApprovalEvent;

/**
 * ドメインイベントリポジトリインターフェース
 * 
 * ドメインイベントの永続化操作を定義
 * Outboxパターンでの信頼性のあるイベント配信を実現
 */
public interface DomainEventRepository {
    
    /**
     * ドメインイベントを保存
     * 
     * @param event 工数記録承認イベント
     * @param partitionKey パーティションキー（通常はuserId）
     */
    void save(WorkRecordApprovalEvent event, String partitionKey);
    
    /**
     * ドメインイベントを保存（userIdをパーティションキーとして使用）
     * 
     * @param event 工数記録承認イベント
     */
    void save(WorkRecordApprovalEvent event);
    
    /**
     * イベントIDでドメインイベントを取得
     * 
     * @param eventId イベントID
     * @return ドメインイベント（オプショナル）
     */
    Optional<WorkRecordApprovalEvent> findById(String eventId);
    
    /**
     * 未発行のイベントを取得
     * 
     * @param limit 取得件数上限
     * @return 未発行イベントのリスト
     */
    List<WorkRecordApprovalEvent> findPendingEvents(int limit);
    
    /**
     * イベントを発行済みとしてマーク
     * 
     * @param eventId イベントID
     */
    void markAsPublished(String eventId);
    
    /**
     * イベント発行失敗を記録
     * 
     * @param eventId イベントID
     * @param errorMessage エラーメッセージ
     */
    void markAsFailed(String eventId, String errorMessage);
    
    /**
     * リトライ回数をインクリメント
     * 
     * @param eventId イベントID
     */
    void incrementRetryCount(String eventId);
    
    /**
     * 集約IDでイベントを取得
     * 
     * @param aggregateId 集約ID（workRecordId）
     * @return イベントのリスト
     */
    List<WorkRecordApprovalEvent> findByAggregateId(String aggregateId);
    
    /**
     * 指定日時以前の発行済みイベントを削除
     * 
     * @param beforeDate 基準日時
     * @return 削除件数
     */
    int deleteOldPublishedEvents(LocalDateTime beforeDate);
    
    /**
     * リトライ可能なイベントを取得
     * 
     * @param maxRetryCount 最大リトライ回数
     * @param limit 取得件数上限
     * @return リトライ可能イベントのリスト
     */
    List<WorkRecordApprovalEvent> findRetryableEvents(int maxRetryCount, int limit);
    
    /**
     * イベントの存在チェック
     * 
     * @param eventId イベントID
     * @return 存在する場合true
     */
    boolean existsById(String eventId);
}