package com.devhour.infrastructure.event;

import java.time.LocalDateTime;

/**
 * ドメインイベントエンティティ
 * 
 * ドメインイベントをデータベースに永続化するためのエンティティ
 * OutboxパターンでKafkaへの配信を保証
 */
public class DomainEventEntity {
    
    private String eventId;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private String eventAction;
    private String eventData;
    private String eventStatus;
    private String partitionKey;
    private LocalDateTime occurredAt;
    private LocalDateTime publishedAt;
    private int retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    
    public DomainEventEntity() {
        // MyBatisのマッピング用
    }
    
    /**
     * 新しいドメインイベントエンティティを作成
     * 
     * @param eventId イベントID
     * @param aggregateId 集約ID
     * @param aggregateType 集約タイプ
     * @param eventType イベントタイプ
     * @param eventAction イベントアクション
     * @param eventData イベントデータ（JSON）
     * @param partitionKey パーティションキー
     * @param occurredAt 発生日時
     */
    public DomainEventEntity(String eventId, String aggregateId, String aggregateType,
                            String eventType, String eventAction, String eventData,
                            String partitionKey, LocalDateTime occurredAt) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.eventAction = eventAction;
        this.eventData = eventData;
        this.eventStatus = "PENDING";
        this.partitionKey = partitionKey;
        this.occurredAt = occurredAt;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * イベントを発行済みとしてマーク
     */
    public void markAsPublished() {
        this.eventStatus = "PUBLISHED";
        this.publishedAt = LocalDateTime.now();
    }
    
    /**
     * イベント発行失敗を記録
     * 
     * @param errorMessage エラーメッセージ
     */
    public void markAsFailed(String errorMessage) {
        this.eventStatus = "FAILED";
        this.errorMessage = errorMessage;
        this.retryCount++;
    }
    
    /**
     * リトライ可能かチェック
     * 
     * @param maxRetryCount 最大リトライ回数
     * @return リトライ可能な場合true
     */
    public boolean canRetry(int maxRetryCount) {
        return "PENDING".equals(eventStatus) && retryCount < maxRetryCount;
    }
    
    // ゲッター・セッター
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = eventAction; }
    
    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
    
    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }
    
    public String getPartitionKey() { return partitionKey; }
    public void setPartitionKey(String partitionKey) { this.partitionKey = partitionKey; }
    
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}