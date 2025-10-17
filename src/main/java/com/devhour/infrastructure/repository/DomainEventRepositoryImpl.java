package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.repository.DomainEventRepository;
import com.devhour.infrastructure.event.DomainEventEntity;
import com.devhour.infrastructure.mapper.DomainEventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ドメインイベントリポジトリ実装
 * 
 * MyBatisを使用したドメインイベントの永続化実装
 * Outboxパターンで信頼性のあるイベント配信を保証
 */
@Repository
public class DomainEventRepositoryImpl implements DomainEventRepository {
    
    private final DomainEventMapper mapper;
    private final ObjectMapper objectMapper;
    
    public DomainEventRepositoryImpl(DomainEventMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void save(WorkRecordApprovalEvent event, String partitionKey) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            
            DomainEventEntity entity = new DomainEventEntity(
                event.eventId(),
                event.userId(), // userIdを集約IDとして使用
                "WorkRecordApproval",
                "WorkRecordApprovalEvent",
                event.action(),
                eventData,
                partitionKey,
                event.occurredAt()
            );
            
            mapper.insert(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("イベントのシリアライズに失敗しました", e);
        }
    }
    
    @Override
    public void save(WorkRecordApprovalEvent event) {
        save(event, event.userId()); // userIdをデフォルトのパーティションキーとして使用
    }
    
    @Override
    public Optional<WorkRecordApprovalEvent> findById(String eventId) {
        Optional<DomainEventEntity> entity = mapper.findById(eventId);
        return entity.map(this::deserializeApprovalEvent);
    }
    
    @Override
    public List<WorkRecordApprovalEvent> findPendingEvents(int limit) {
        List<DomainEventEntity> entities = mapper.findPendingEvents(3, limit);
        return entities.stream()
            .map(this::deserializeApprovalEvent)
            .toList();
    }
    
    @Override
    public void markAsPublished(String eventId) {
        mapper.updateStatus(eventId, "PUBLISHED", LocalDateTime.now(), 0, null);
    }
    
    @Override
    public void markAsFailed(String eventId, String errorMessage) {
        Optional<DomainEventEntity> entity = mapper.findById(eventId);
        if (entity.isPresent()) {
            int retryCount = entity.get().getRetryCount() + 1;
            mapper.updateStatus(eventId, "FAILED", null, retryCount, errorMessage);
        }
    }
    
    @Override
    public void incrementRetryCount(String eventId) {
        mapper.incrementRetryCount(eventId);
    }
    
    @Override
    public List<WorkRecordApprovalEvent> findByAggregateId(String aggregateId) {
        List<DomainEventEntity> entities = mapper.findByAggregateId(aggregateId);
        return entities.stream()
            .map(this::deserializeApprovalEvent)
            .toList();
    }
    
    @Override
    public int deleteOldPublishedEvents(LocalDateTime beforeDate) {
        return mapper.deleteOldPublishedEvents(beforeDate);
    }
    
    @Override
    public List<WorkRecordApprovalEvent> findRetryableEvents(int maxRetryCount, int limit) {
        List<DomainEventEntity> entities = mapper.findPendingEvents(maxRetryCount, limit);
        return entities.stream()
            .map(this::deserializeApprovalEvent)
            .toList();
    }
    
    @Override
    public boolean existsById(String eventId) {
        return mapper.findById(eventId).isPresent();
    }
    
    /**
     * DomainEventEntityからWorkRecordApprovalEventへデシリアライズ
     */
    private WorkRecordApprovalEvent deserializeApprovalEvent(DomainEventEntity entity) {
        try {
            return objectMapper.readValue(entity.getEventData(), WorkRecordApprovalEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("イベントのデシリアライズに失敗しました: " + entity.getEventId(), e);
        }
    }
}