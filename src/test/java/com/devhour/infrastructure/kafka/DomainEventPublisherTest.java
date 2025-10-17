package com.devhour.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.repository.DomainEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DomainEventPublisherのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DomainEventPublisher")
class DomainEventPublisherTest {
    
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Mock
    private DomainEventRepository domainEventRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private WorkRecordApproval approval;
    
    @Mock
    private WorkRecord workRecord;
    
    @InjectMocks
    private DomainEventPublisher domainEventPublisher;
    
    @Test
    @DisplayName("publishPendingEvents - 未発行イベントの発行")
    void publishPendingEvents_Success() {
        // Arrange
        when(approval.getUserId()).thenReturn("user1");
        when(approval.getWorkDate()).thenReturn(LocalDate.now());
        when(approval.getApproverId()).thenReturn("approver1");
        
        List<WorkRecord> workRecords = List.of(workRecord);
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        List<WorkRecordApprovalEvent> pendingEvents = List.of(event);
        
        when(domainEventRepository.findPendingEvents(100)).thenReturn(pendingEvents);
        
        // Act
        domainEventPublisher.publishPendingEvents();
        
        // Assert
        verify(domainEventRepository).findPendingEvents(100);
        verify(kafkaTemplate).send(eq("work-record-approval-changed"), eq(event.userId()), eq(event));
    }
    
    @Test
    @DisplayName("publishPendingEvents - 空のリスト")
    void publishPendingEvents_EmptyList() {
        // Arrange
        when(domainEventRepository.findPendingEvents(100)).thenReturn(List.of());
        
        // Act
        domainEventPublisher.publishPendingEvents();
        
        // Assert
        verify(domainEventRepository).findPendingEvents(100);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
    
    @Test
    @DisplayName("retryFailedEvents - 失敗イベントの再試行")
    void retryFailedEvents_Success() {
        // Arrange
        when(approval.getUserId()).thenReturn("user1");
        when(approval.getWorkDate()).thenReturn(LocalDate.now());
        when(approval.getApproverId()).thenReturn("approver1");
        
        List<WorkRecord> workRecords = List.of(workRecord);
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        List<WorkRecordApprovalEvent> retryableEvents = List.of(event);
        
        when(domainEventRepository.findRetryableEvents(3, 100)).thenReturn(retryableEvents);
        
        // Act
        domainEventPublisher.retryFailedEvents();
        
        // Assert
        verify(domainEventRepository).findRetryableEvents(3, 100);
        verify(domainEventRepository).incrementRetryCount(event.eventId());
        verify(kafkaTemplate).send(eq("work-record-approval-changed"), eq(event.userId()), eq(event));
    }
    
    @Test
    @DisplayName("cleanupOldEvents - 古いイベントの削除")
    void cleanupOldEvents_Success() {
        // Arrange
        when(domainEventRepository.deleteOldPublishedEvents(any(LocalDateTime.class))).thenReturn(10);
        
        // Act
        domainEventPublisher.cleanupOldEvents();
        
        // Assert
        verify(domainEventRepository).deleteOldPublishedEvents(any(LocalDateTime.class));
    }
    
    @Test
    @DisplayName("publishEventImmediately - 特定イベントの即時発行")
    void publishEventImmediately_Success() {
        // Arrange
        when(approval.getUserId()).thenReturn("user1");
        when(approval.getWorkDate()).thenReturn(LocalDate.now());
        when(approval.getApproverId()).thenReturn("approver1");
        
        String eventId = "event123";
        List<WorkRecord> workRecords = List.of(workRecord);
        WorkRecordApprovalEvent event = WorkRecordApprovalEvent.createForApproval(approval, workRecords);
        
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        // Act
        domainEventPublisher.publishEventImmediately(eventId);
        
        // Assert
        verify(domainEventRepository).findById(eventId);
        verify(kafkaTemplate).send(eq("work-record-approval-changed"), eq(event.userId()), eq(event));
    }
    
    @Test
    @DisplayName("publishEventImmediately - イベントが見つからない場合")
    void publishEventImmediately_NotFound() {
        // Arrange
        String eventId = "nonexistent";
        when(domainEventRepository.findById(eventId)).thenReturn(Optional.empty());
        
        // Act
        domainEventPublisher.publishEventImmediately(eventId);
        
        // Assert
        verify(domainEventRepository).findById(eventId);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}