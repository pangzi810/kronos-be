package com.devhour.infrastructure.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.repository.DomainEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ドメインイベント発行サービス
 * 
 * Outboxパターンでドメインイベントを確実にKafkaへ発行
 */
@Service
public class DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisher.class);
    private static final String TOPIC = "work-record-approval-changed";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DomainEventRepository domainEventRepository;
    
    public DomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               DomainEventRepository domainEventRepository,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.domainEventRepository = domainEventRepository;
    }
    
    /**
     * 定期的に未発行イベントを発行（5秒ごと）
     */
    // @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<WorkRecordApprovalEvent> pendingEvents = 
            domainEventRepository.findPendingEvents(BATCH_SIZE);
        
        for (WorkRecordApprovalEvent event : pendingEvents) {
            publishEvent(event);
        }
    }
    
    /**
     * リトライ可能なイベントを再発行（30秒ごと）
     */
    // @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryFailedEvents() {
        List<WorkRecordApprovalEvent> retryableEvents = 
            domainEventRepository.findRetryableEvents(MAX_RETRY_COUNT, BATCH_SIZE);
        
        for (WorkRecordApprovalEvent event : retryableEvents) {
            domainEventRepository.incrementRetryCount(event.eventId());
            publishEvent(event);
        }
    }
    
    /**
     * 古い発行済みイベントを削除（毎日深夜2時）
     */
    // @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deletedCount = domainEventRepository.deleteOldPublishedEvents(thirtyDaysAgo);
        logger.info("削除された古いイベント数: {}", deletedCount);
    }
    
    /**
     * 単一イベントを発行
     */
    private void publishEvent(WorkRecordApprovalEvent event) {
        try {
            // パーティションキーはuserIdを使用
            String partitionKey = event.userId();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(TOPIC, partitionKey, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 発行成功
                    domainEventRepository.markAsPublished(event.eventId());
                    logger.info("イベント発行成功: eventId={}, topic={}, partition={}, offset={}",
                        event.eventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    // 発行失敗
                    String errorMessage = ex.getMessage();
                    domainEventRepository.markAsFailed(event.eventId(), errorMessage);
                    logger.error("イベント発行失敗: eventId={}, error={}", 
                        event.eventId(), errorMessage, ex);
                }
            });
            
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            domainEventRepository.markAsFailed(event.eventId(), errorMessage);
            logger.error("イベント発行エラー: eventId={}, error={}", 
                event.eventId(), errorMessage, e);
        }
    }
    
    /**
     * 即座にイベントを発行（テスト用）
     */
    public void publishEventImmediately(String eventId) {
        domainEventRepository.findById(eventId)
            .ifPresent(this::publishEvent);
    }
}