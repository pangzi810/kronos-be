package com.devhour.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.event.WorkRecordApprovalEvent;
import com.devhour.domain.repository.WorkRecordApprovalHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工数記録イベントコンシューマー
 * 
 * Kafkaから工数記録変更イベントを受信して処理
 */
@Service
public class WorkRecordApprovalEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkRecordApprovalEventConsumer.class);
    
    private final WorkRecordApprovalHistoryRepository approvalHistoryRepository;
    
    public WorkRecordApprovalEventConsumer(WorkRecordApprovalHistoryRepository approvalHistoryRepository,
                                  ObjectMapper objectMapper) {
        this.approvalHistoryRepository = approvalHistoryRepository;
    }
    
    /**
     * 工数記録変更イベントを処理
     */
    @KafkaListener(topics = "work-record-approval-changed", 
                  groupId = "${spring.kafka.consumer.group-id:work-hours-approval-group}",
                  containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleWorkRecordApprovalEvent(@Payload WorkRecordApprovalEvent event,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                            @Header(KafkaHeaders.OFFSET) long offset,
                                            Acknowledgment acknowledgment) {
        try {
            logger.info("イベント受信: eventId={}, action={}, topic={}, partition={}, offset={}",
                event.eventId(), event.action(), topic, partition, offset);
            
            // イベントIDをhistoryIdとして使用（workRecordId#version形式）
            String historyId = event.eventId();
            
            // 既に処理済みかチェック（重複排除）
            if (approvalHistoryRepository.existsById(historyId)) {
                logger.info("既に処理済みのイベント: historyId={}", historyId);
                acknowledgment.acknowledge();
                return;
            }
            
            // 承認履歴を作成
            // ApprovalHistory history = createApprovalHistory(event);
            // approvalHistoryRepository.save(history);
            
            // イベント処理完了をログ出力
            // logEventProcessed(event);
            
            // Kafkaオフセットをコミット
            acknowledgment.acknowledge();
            
            logger.info("イベント処理完了: eventId={}", event.eventId());
            
        } catch (Exception e) {
            logger.error("イベント処理エラー: eventId={}, error={}", 
                event.eventId(), e.getMessage(), e);
            // エラー時はオフセットをコミットしない（リトライ対象とする）
            throw e;
        }
    }
    
    /**
     * イベントから承認履歴を作成
     */
    // private ApprovalHistory createApprovalHistory(WorkRecordApprovalEvent event) {
    //     try {
    //         // イベント自体をJSON化してスナップショットとする
    //         String workRecordSnapshot = objectMapper.writeValueAsString(event);
            
    //         return ApprovalHistory.restore(
    //             event.eventId(),
    //             event.workRecordId(),
    //             event.userId(),
    //             event.projectId(),
    //             event.workDate(),
    //             event.categoryHours(),
    //             event.totalHours(),
    //             event.description(),
    //             event.action(),
    //             event.previousStatus(),
    //             event.currentStatus(),
    //             event.approverId(),
    //             event.rejectionReason(),
    //             workRecordSnapshot,
    //             event.occurredAt(),
    //             event.occurredAt()
    //         );
    //     } catch (Exception e) {
    //         throw new RuntimeException("承認履歴の作成に失敗しました", e);
    //     }
    // }
    
    // /**
    //  * イベント処理完了をログ出力
    //  */
    // private void logEventProcessed(WorkRecordApprovalEvent event) {
    //     String actionDescription = switch (event.action()) {
    //         case "CREATE" -> "工数記録作成";
    //         case "UPDATE" -> "工数記録更新";
    //         case "APPROVE" -> "承認";
    //         case "REJECT" -> "却下";
    //         case "RESUBMIT" -> "再申請";
    //         default -> event.action();
    //     };
        
    //     logger.info("アクション処理: {} - workRecordId={}, userId={}, projectId={}, workDate={}",
    //         actionDescription,
    //         event.workRecordId(),
    //         event.userId(),
    //         event.projectId(),
    //         event.workDate());
    // }
}