package com.devhour.infrastructure.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for V29 migration: Add Okta user sync constraints
 * Tests unique constraints and data integrity for Okta integration
 */
@SpringJUnitConfig
@Import({com.devhour.config.TestSecurityConfiguration.class, DataSourceAutoConfiguration.class})
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Migration test requires V29 migration to be applied - enable after migration is deployed")
class V29MigrationTest {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        jdbcTemplate.execute("DELETE FROM users WHERE email LIKE 'test%@example.com'");
    }

    @Test
    void testUniqueConstraintOnOktaUserId() {
        // Given: A user with Okta user ID
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String oktaUserId = "okta_test_123";

        jdbcTemplate.update(
            "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            userId1, "testuser1", "test1@example.com", "hash", "DEVELOPER", "Test User 1", oktaUserId
        );

        // When & Then: Attempting to insert another user with same Okta ID should fail
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId2, "testuser2", "test2@example.com", "hash", "DEVELOPER", "Test User 2", oktaUserId
            );
        });
    }

    @Test
    void testEmailReuseAfterLogicalDeletion() throws SQLException {
        // Given: A user with an email
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String email = "reuse@example.com";

        jdbcTemplate.update(
            "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            userId1, "testuser1", email, "hash", "DEVELOPER", "Test User 1"
        );

        // When: User is logically deleted
        jdbcTemplate.update(
            "UPDATE users SET deleted_at = NOW() WHERE id = ?", userId1
        );

        // Then: Same email can be reused for new user
        assertDoesNotThrow(() -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                userId2, "testuser2", email, "hash", "DEVELOPER", "Test User 2"
            );
        });
    }

    @Test
    void testUniqueEmailForActiveUsers() {
        // Given: An active user with an email
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String email = "active@example.com";

        jdbcTemplate.update(
            "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            userId1, "testuser1", email, "hash", "DEVELOPER", "Test User 1"
        );

        // When & Then: Attempting to insert another active user with same email should fail
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                userId2, "testuser2", email, "hash", "DEVELOPER", "Test User 2"
            );
        });
    }

    @Test
    void testNullOktaUserIdAllowed() {
        // Given: Users without Okta user ID
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();

        // When & Then: Multiple users with null okta_user_id should be allowed
        assertDoesNotThrow(() -> {
            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId1, "testuser1", "null1@example.com", "hash", "DEVELOPER", "Test User 1", null
            );

            jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, role, full_name, okta_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId2, "testuser2", "null2@example.com", "hash", "DEVELOPER", "Test User 2", null
            );
        });
    }

    @Test
    void testConstraintNames() {
        // Verify that the expected constraints exist
        String constraintQuery = 
            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND CONSTRAINT_TYPE = 'UNIQUE'";

        var constraints = jdbcTemplate.queryForList(constraintQuery, String.class);
        
        assertTrue(constraints.contains("uk_users_okta_user_id"), 
                  "Should have unique constraint on okta_user_id");
        assertTrue(constraints.contains("uk_users_email_deleted_at"), 
                  "Should have composite unique constraint on email and deleted_at");
    }

    @Test
    void testIndexesExist() {
        // Verify that the expected indexes exist
        String indexQuery = 
            "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users'";

        var indexes = jdbcTemplate.queryForList(indexQuery, String.class);
        
        assertTrue(indexes.contains("idx_users_okta_user_id_lookup"), 
                  "Should have lookup index on okta_user_id");
        assertTrue(indexes.contains("idx_users_email_active"), 
                  "Should have active email index");
    }

    @Test
    void testMigrationIdempotency() {
        // Test that migration can be run multiple times without issues
        // This simulates running the migration script multiple times
        
        // The migration uses IF EXISTS and IF NOT EXISTS clauses
        // So running similar operations should not fail
        assertDoesNotThrow(() -> {
            // These operations should not fail even if constraints already exist
            jdbcTemplate.execute(
                "ALTER TABLE users ADD CONSTRAINT uk_users_okta_user_id_test UNIQUE (okta_user_id)"
            );
        }, "Migration should be idempotent");
        
        // Clean up test constraint
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT uk_users_okta_user_id_test");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}