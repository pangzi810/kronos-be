package com.devhour.infrastructure.mapper;

import com.devhour.infrastructure.event.DomainEventEntity;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DomainEventMapper {
    
    @Insert("""
        INSERT INTO domain_events (
            event_id, aggregate_id, aggregate_type, event_type, event_action,
            event_data, event_status, partition_key, occurred_at,
            retry_count, created_at
        ) VALUES (
            #{eventId}, #{aggregateId}, #{aggregateType}, #{eventType}, #{eventAction},
            #{eventData}, #{eventStatus}, #{partitionKey}, #{occurredAt},
            0, CURRENT_TIMESTAMP
        )
        """)
    void insert(DomainEventEntity event);
    
    @Update("""
        UPDATE domain_events
        SET event_status = #{status},
            published_at = #{publishedAt},
            retry_count = #{retryCount},
            error_message = #{errorMessage}
        WHERE event_id = #{eventId}
        """)
    void updateStatus(@Param("eventId") String eventId,
                     @Param("status") String status,
                     @Param("publishedAt") LocalDateTime publishedAt,
                     @Param("retryCount") int retryCount,
                     @Param("errorMessage") String errorMessage);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_status = 'PENDING'
          AND retry_count < #{maxRetryCount}
        ORDER BY created_at ASC
        LIMIT #{limit}
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findPendingEvents(@Param("maxRetryCount") int maxRetryCount,
                                              @Param("limit") int limit);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_id = #{eventId}
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    Optional<DomainEventEntity> findById(@Param("eventId") String eventId);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE aggregate_id = #{aggregateId}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findByAggregateId(@Param("aggregateId") String aggregateId);
    
    @Update("""
        UPDATE domain_events
        SET retry_count = retry_count + 1
        WHERE event_id = #{eventId}
        """)
    void incrementRetryCount(@Param("eventId") String eventId);
    
    @Delete("""
        DELETE FROM domain_events
        WHERE event_status = 'PUBLISHED'
          AND published_at < #{beforeDate}
        """)
    int deleteOldPublishedEvents(@Param("beforeDate") LocalDateTime beforeDate);
    
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_type = #{eventType}
          AND event_status = 'PENDING'
          AND retry_count < 3
        ORDER BY created_at ASC
        LIMIT #{limit}
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findPendingEventsByType(@Param("eventType") String eventType,
                                                     @Param("limit") int limit);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_type = #{eventType}
          AND JSON_EXTRACT(event_data, #{jsonPath}) = #{value}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findByEventDataJsonPath(@Param("eventType") String eventType,
                                                     @Param("jsonPath") String jsonPath,
                                                     @Param("value") String value);
    
    @Delete("""
        DELETE FROM domain_events
        WHERE event_type = #{eventType}
          AND event_status = 'PUBLISHED'
          AND published_at < #{beforeDate}
        """)
    int deleteOldPublishedEventsByType(@Param("eventType") String eventType,
                                       @Param("beforeDate") LocalDateTime beforeDate);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_type = #{eventType}
          AND event_status = 'PENDING'
          AND retry_count < #{maxRetryCount}
        ORDER BY created_at ASC
        LIMIT #{limit}
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findRetryableEventsByType(@Param("eventType") String eventType,
                                                       @Param("maxRetryCount") int maxRetryCount,
                                                       @Param("limit") int limit);
    
    @Select("""
        SELECT event_id, aggregate_id, aggregate_type, event_type, event_action,
               event_data, event_status, partition_key, occurred_at,
               published_at, retry_count, error_message, created_at
        FROM domain_events
        WHERE event_type = #{eventType}
          AND occurred_at BETWEEN #{startDate} AND #{endDate}
        ORDER BY occurred_at DESC
        """)
    @Results({
        @Result(property = "eventId", column = "event_id"),
        @Result(property = "aggregateId", column = "aggregate_id"),
        @Result(property = "aggregateType", column = "aggregate_type"),
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "eventAction", column = "event_action"),
        @Result(property = "eventData", column = "event_data"),
        @Result(property = "eventStatus", column = "event_status"),
        @Result(property = "partitionKey", column = "partition_key"),
        @Result(property = "occurredAt", column = "occurred_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "retryCount", column = "retry_count"),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<DomainEventEntity> findByEventTypeAndDateRange(@Param("eventType") String eventType,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}