package com.devhour.infrastructure.migration;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V19マイグレーション（部下アサイン用インデックス）のテスト
 */
@SpringJUnitConfig
@Import({com.devhour.config.TestSecurityConfiguration.class, DataSourceAutoConfiguration.class})
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Migration test requires full database setup")
class V19MigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testIndexesCreated() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // supervisor_relationships テーブルのインデックス確認
            assertIndexExists(metaData, "supervisor_relationships", "idx_supervisor_relationships_supervisor_effective");
            
            // users テーブルのインデックス確認
            assertIndexExists(metaData, "users", "idx_users_active_deleted");
            assertIndexExists(metaData, "users", "idx_users_role_active_deleted");
        }
    }
    
    /**
     * 指定されたテーブルにインデックスが存在することを確認
     */
    private void assertIndexExists(DatabaseMetaData metaData, String tableName, String indexName) throws Exception {
        try (ResultSet resultSet = metaData.getIndexInfo(null, null, tableName, false, false)) {
            boolean indexFound = false;
            while (resultSet.next()) {
                if (indexName.equals(resultSet.getString("INDEX_NAME"))) {
                    indexFound = true;
                    break;
                }
            }
            assertTrue(indexFound, "Index " + indexName + " should exist on table " + tableName);
        }
    }
    
    @Test
    void testIndexColumnsCorrect() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // supervisor_relationships_supervisor_effective インデックスのカラム確認
            assertIndexColumns(metaData, "supervisor_relationships", "idx_supervisor_relationships_supervisor_effective", 
                              new String[]{"supervisor_id", "effective_to"});
            
            // users_active_deleted インデックスのカラム確認
            assertIndexColumns(metaData, "users", "idx_users_active_deleted", 
                              new String[]{"is_active", "deleted_at"});
            
            // users_role_active_deleted インデックスのカラム確認
            assertIndexColumns(metaData, "users", "idx_users_role_active_deleted", 
                              new String[]{"role", "is_active", "deleted_at"});
        }
    }
    
    /**
     * インデックスが指定されたカラムで構成されていることを確認
     */
    private void assertIndexColumns(DatabaseMetaData metaData, String tableName, String indexName, 
                                   String[] expectedColumns) throws Exception {
        try (ResultSet resultSet = metaData.getIndexInfo(null, null, tableName, false, false)) {
            int columnIndex = 0;
            while (resultSet.next()) {
                if (indexName.equals(resultSet.getString("INDEX_NAME"))) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    assertTrue(columnIndex < expectedColumns.length, 
                              "Index " + indexName + " has more columns than expected");
                    assertEquals(expectedColumns[columnIndex], columnName.toLowerCase(), 
                               "Index " + indexName + " column at position " + columnIndex + " should be " + expectedColumns[columnIndex]);
                    columnIndex++;
                }
            }
            assertEquals(expectedColumns.length, columnIndex, 
                        "Index " + indexName + " should have " + expectedColumns.length + " columns");
        }
    }
}