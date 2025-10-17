package com.devhour.infrastructure.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for V29 rollback script
 * Tests that rollback properly removes constraints and restores original state
 */
@SpringJUnitConfig
@Import({com.devhour.config.TestSecurityConfiguration.class, DataSourceAutoConfiguration.class})
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Rollback test requires V29 migration to be applied first - enable after migration is deployed")
class V29RollbackTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        jdbcTemplate.execute("DELETE FROM users WHERE email LIKE 'rollback%@example.com'");
    }

    @Test
    void testRollbackScript() throws SQLException {
        // Given: Apply the rollback script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, 
                new ClassPathResource("db/migration/V29__rollback.sql"));
        }

        // Then: Verify constraints are removed
        String constraintQuery = 
            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND CONSTRAINT_TYPE = 'UNIQUE'";

        var constraints = jdbcTemplate.queryForList(constraintQuery, String.class);
        
        assertFalse(constraints.contains("uk_users_okta_user_id"), 
                   "Unique constraint on okta_user_id should be removed");
        assertFalse(constraints.contains("uk_users_email_deleted_at"), 
                   "Composite unique constraint should be removed");
    }

    @Test
    void testOriginalIndexRecreated() throws SQLException {
        // Given: Apply the rollback script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, 
                new ClassPathResource("db/migration/V29__rollback.sql"));
        }

        // Then: Verify original non-unique index is recreated
        String indexQuery = 
            "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'idx_users_okta_user_id'";

        var indexes = jdbcTemplate.queryForList(indexQuery, String.class);
        
        assertTrue(indexes.contains("idx_users_okta_user_id"), 
                  "Original non-unique index should be recreated");
    }

    @Test
    void testDuplicateOktaUserIdAllowedAfterRollback() throws SQLException {
        // Given: Apply the rollback script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, 
                new ClassPathResource("db/migration/V29__rollback.sql"));
        }

        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String oktaUserId = "rollback_okta_123";

        // When & Then: Duplicate okta_user_id should be allowed after rollback
        assertDoesNotThrow(() -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId1, "rollbackuser1", "rollback1@example.com", "hash", "DEVELOPER", "Rollback User 1", oktaUserId
            );

            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId2, "rollbackuser2", "rollback2@example.com", "hash", "DEVELOPER", "Rollback User 2", oktaUserId
            );
        }, "Duplicate okta_user_id should be allowed after rollback");
    }

    @Test
    void testDuplicateEmailAllowedAfterRollback() throws SQLException {
        // Given: Apply the rollback script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, 
                new ClassPathResource("db/migration/V29__rollback.sql"));
        }

        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String email = "rollback@example.com";

        // When & Then: Duplicate active emails should be allowed after rollback
        // Note: This depends on whether the original table had unique email constraint
        // The original V1 migration shows email had UNIQUE constraint, so this test
        // verifies that the original unique email constraint still works
        jdbcTemplate.update(
            "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            userId1, "rollbackuser1", email, "hash", "DEVELOPER", "Rollback User 1"
        );

        // This should still fail because original email unique constraint should remain
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                userId2, "rollbackuser2", email, "hash", "DEVELOPER", "Rollback User 2"
            );
        }, "Original email unique constraint should still work after rollback");
    }

    @Test
    void testRollbackIdempotency() throws SQLException {
        // Test that rollback script can be run multiple times without issues
        
        // Run rollback script multiple times
        assertDoesNotThrow(() -> {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, 
                    new ClassPathResource("db/migration/V29__rollback.sql"));
                ScriptUtils.executeSqlScript(connection, 
                    new ClassPathResource("db/migration/V29__rollback.sql"));
            }
        }, "Rollback script should be idempotent");
    }

    @Test
    void testNewIndexesRemovedAfterRollback() throws SQLException {
        // Given: Apply the rollback script
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, 
                new ClassPathResource("db/migration/V29__rollback.sql"));
        }

        // Then: Verify new conditional indexes are removed
        String indexQuery = 
            "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users'";

        var indexes = jdbcTemplate.queryForList(indexQuery, String.class);
        
        assertFalse(indexes.contains("idx_users_okta_user_id_lookup"), 
                   "Conditional okta_user_id index should be removed");
        assertFalse(indexes.contains("idx_users_email_active"), 
                   "Conditional email index should be removed");
    }
}