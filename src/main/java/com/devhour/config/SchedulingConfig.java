package com.devhour.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

/**
 * スケジューリング設定
 *
 * 定期実行タスクを有効化し、ShedLockによる分散環境での重複実行防止を設定
 * jira.integration.enabled=true の場合のみ有効化される
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m", defaultLockAtLeastFor = "1m")
@ConditionalOnProperty(
    name = "jira.integration.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class SchedulingConfig {
    
    /**
     * ShedLock用のLockProviderを設定
     * 
     * JDBCテンプレートを使用してデータベースベースのロック機能を提供する。
     * 分散環境でのスケジューラー重複実行を防ぐため。
     * 
     * @param dataSource データソース
     * @return JDBCテンプレートベースのLockProvider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new org.springframework.jdbc.core.JdbcTemplate(dataSource))
                .withTableName("shedlock")
                .build()
        );
    }
}