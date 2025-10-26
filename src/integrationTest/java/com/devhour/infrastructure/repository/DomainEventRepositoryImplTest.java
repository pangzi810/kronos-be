package com.devhour.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.infrastructure.event.DomainEventEntity;
import com.devhour.infrastructure.mapper.DomainEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DomainEventRepositoryImplTest {

    @Mock
    private DomainEventMapper mapper;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private DomainEventRepositoryImpl repository;
    
    @BeforeEach
    void setUp() {
        repository = new DomainEventRepositoryImpl(mapper, objectMapper);
    }
    
    @Test
    void save_WithPartitionKey_ShouldInsertEventWithSpecifiedPartitionKey() throws Exception {
        // Given
        WorkRecordApprovalEvent event = createTestEvent();
        String partitionKey = "custom-partition-key";
        String eventJson = "{\"eventId\":\"test-event\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        
        // When
        repository.save(event, partitionKey);
        
        // Then
        ArgumentCaptor<DomainEventEntity> captor = ArgumentCaptor.forClass(DomainEventEntity.class);
        verify(mapper).insert(captor.capture());
        
        DomainEventEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getEventId()).isEqualTo("test-event-id");
        assertThat(capturedEntity.getAggregateId()).isEqualTo("user-123");
        assertThat(capturedEntity.getAggregateType()).isEqualTo("WorkRecordApproval");
        assertThat(capturedEntity.getEventType()).isEqualTo("WorkRecordApprovalEvent");
        assertThat(capturedEntity.getEventAction()).isEqualTo("APPROVE");
        assertThat(capturedEntity.getEventData()).isEqualTo(eventJson);
        assertThat(capturedEntity.getPartitionKey()).isEqualTo(partitionKey);
    }
    
    @Test
    void save_WithoutPartitionKey_ShouldUseUserIdAsPartitionKey() throws Exception {
        // Given
        WorkRecordApprovalEvent event = createTestEvent();
        String eventJson = "{\"eventId\":\"test-event\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        
        // When
        repository.save(event);
        
        // Then
        ArgumentCaptor<DomainEventEntity> captor = ArgumentCaptor.forClass(DomainEventEntity.class);
        verify(mapper).insert(captor.capture());
        
        DomainEventEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getPartitionKey()).isEqualTo("user-123");
    }
    
    @Test
    void findById_ExistingEvent_ShouldReturnDeserializedEvent() throws Exception {
        // Given
        String eventId = "test-event-id";
        DomainEventEntity entity = createTestDomainEventEntity();
        WorkRecordApprovalEvent expectedEvent = createTestEvent();
        
        when(mapper.findById(eventId)).thenReturn(Optional.of(entity));
        when(objectMapper.readValue(entity.getEventData(), WorkRecordApprovalEvent.class))
            .thenReturn(expectedEvent);
        
        // When
        Optional<WorkRecordApprovalEvent> result = repository.findById(eventId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedEvent);
    }
    
    @Test
    void markAsPublished_ShouldUpdateEventStatus() {
        // Given
        String eventId = "test-event-id";
        
        // When
        repository.markAsPublished(eventId);
        
        // Then
        verify(mapper).updateStatus(eq(eventId), eq("PUBLISHED"), any(LocalDateTime.class), eq(0), isNull());
    }
    
    @Test
    void existsById_ShouldReturnTrueWhenEventExists() {
        // Given
        String eventId = "test-event-id";
        when(mapper.findById(eventId)).thenReturn(Optional.of(createTestDomainEventEntity()));
        
        // When
        boolean result = repository.existsById(eventId);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void existsById_ShouldReturnFalseWhenEventDoesNotExist() {
        // Given
        String eventId = "non-existent-event";
        when(mapper.findById(eventId)).thenReturn(Optional.empty());
        
        // When
        boolean result = repository.existsById(eventId);
        
        // Then
        assertThat(result).isFalse();
    }
    
    private WorkRecordApprovalEvent createTestEvent() {
        return new WorkRecordApprovalEvent(
            "test-event-id",
            "user-123",
            LocalDate.of(2024, 1, 15),
            "APPROVE",
            "approver-456",
            null,
            "APPROVED",
            List.of(),
            LocalDateTime.now()
        );
    }
    
    private DomainEventEntity createTestDomainEventEntity() {
        return new DomainEventEntity(
            "test-event-id",
            "user-123",
            "WorkRecordApproval",
            "WorkRecordApprovalEvent",
            "APPROVE",
            "{\"eventId\":\"test-event\"}",
            "user-123",
            LocalDateTime.now()
        );
    }
}