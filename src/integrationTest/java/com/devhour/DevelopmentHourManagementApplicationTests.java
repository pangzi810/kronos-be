package com.devhour;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * DevelopmentHourManagementApplicationの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * Springコンテキストの起動確認
 */
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestRepositoryConfiguration.class, com.devhour.config.TestSecurityConfiguration.class})
class DevelopmentHourManagementApplicationTests {

	@Container
	@SuppressWarnings("resource")
	static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test")
			.withReuse(false)
			.withEnv("MYSQL_ROOT_PASSWORD", "root")
			.withCommand(
				"--character-set-server=utf8mb4",
				"--collation-server=utf8mb4_unicode_ci",
				"--default-authentication-plugin=mysql_native_password"
			);

	@DynamicPropertySource
	static void registerMySQLProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

		registry.add("spring.flyway.enabled", () -> "true");
		registry.add("spring.flyway.clean-disabled", () -> "false");
		registry.add("spring.flyway.locations", () -> "classpath:db/migration");
		registry.add("spring.flyway.baseline-on-migrate", () -> "true");
	}

	@Test
	void contextLoads() {
	}

}
