package com.devhour.infrastructure.migration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * インデックス確認テスト
 * 実際に作成されたインデックスを確認するためのデバッグ用テスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Migration test requires full database setup")
class IndexVerificationTest {

    private static final Logger logger = LoggerFactory.getLogger(IndexVerificationTest.class);
    
    @Autowired
    private DataSource dataSource;

    @Test
    void listAllIndexes() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            logger.info("=== supervisor_relationships テーブルのインデックス ===");
            listIndexesForTable(metaData, "supervisor_relationships");
            
            logger.info("=== users テーブルのインデックス ===");
            listIndexesForTable(metaData, "users");
        }
    }
    
    private void listIndexesForTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                boolean nonUnique = resultSet.getBoolean("NON_UNIQUE");
                short ordinalPosition = resultSet.getShort("ORDINAL_POSITION");
                
                logger.info("Table: {}, Index: {}, Column: {}, Position: {}, NonUnique: {}", 
                           tableName, indexName, columnName, ordinalPosition, nonUnique);
            }
        }
    }
}