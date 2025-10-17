package com.devhour.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka設定クラス
 * 
 * Kafkaが利用可能な場合のみ有効化される
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    // Kafka configuration will be auto-configured by Spring Boot
    // when spring.kafka.enabled=true
}