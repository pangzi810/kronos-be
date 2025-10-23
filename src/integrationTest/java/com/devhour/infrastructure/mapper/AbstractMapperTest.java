package com.devhour.infrastructure.mapper;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Mapper統合テスト用の基底クラス
 *
 * TestcontainersでMySQLコンテナを起動し、実際のMySQLに対してテストを実行する。
 * コンテナは別のランダムポートで起動されるため、既存のMySQLコンテナと競合しない。
 * これにより、H2との互換性問題やFlywayチェックサム問題を回避できる。
 */
@SpringBootTest
@Testcontainers
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractMapperTest {

    /**
     * テスト用MySQLコンテナ
     *
     * - ランダムなホストポートで起動（既存のMySQL:3306と競合しない）
     * - withReuse(false): テストクラスごとに新しいコンテナを起動してクリーンな環境を保証
     * - コンテナ名を明示的に指定しない（自動生成されたランダム名を使用）
     */
    @Container
    @SuppressWarnings("resource")
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false)  // 各テストクラスで新しいコンテナを起動
            .withEnv("MYSQL_ROOT_PASSWORD", "root")
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--default-authentication-plugin=mysql_native_password"
            );

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        // Testcontainersが自動的に割り当てたポートを使用
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Flyway設定 - チェックサム検証を有効化（新しいコンテナなので問題なし）
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
    }
}
