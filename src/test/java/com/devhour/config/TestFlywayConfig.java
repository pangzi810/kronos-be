package com.devhour.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * テスト用Flyway設定
 *
 * V1マイグレーションファイルの変更によるチェックサム不一致を解決するため、
 * テスト環境ではFlywayのvalidationをスキップしてcleanとmigrateのみを実行する。
 */
@TestConfiguration
@Profile("test")
public class TestFlywayConfig {

    @Bean
    @Primary
    public FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, (f) -> {
            // Clean the database first (removes flyway_schema_history with old checksums)
            f.clean();
            // Then migrate (creates new flyway_schema_history with current checksums)
            f.migrate();
        });
    }
}
