package com.devhour.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.devhour.domain.model.valueobject.ProjectStatus;

/**
 * ProjectStatusMappingConfigurationの統合テストクラス
 * 
 * Spring Bootコンテキストでの設定ロードとプロパティバインディングをテストする
 */
@SpringJUnitConfig
@Import({ProjectStatusMappingConfiguration.class, com.devhour.config.TestSecurityConfiguration.class})
@ActiveProfiles("test")
class ProjectStatusMappingConfigurationIntegrationTest {

    @Autowired
    private ProjectStatusMappingConfiguration config;

    @Test
    void configurationLoadedCorrectly() {
        // Assert that the configuration is loaded by Spring Boot
        assertNotNull(config);
    }

    @Test
    void buildStatusMappingMap_integrationWithApplicationProperties_success() {
        // Act
        Map<String, ProjectStatus> mappingMap = config.buildStatusMappingMap();

        // Assert
        assertNotNull(mappingMap);
        assertFalse(mappingMap.isEmpty());
        
        // Verify that the configuration from application.properties is loaded correctly
        assertTrue(mappingMap.containsKey("ACTIVE"));
        assertTrue(mappingMap.containsKey("COMPLETED"));
        assertTrue(mappingMap.containsKey("CANCELLED"));
        assertTrue(mappingMap.containsKey("PLANNING"));
    }

    @Test
    void getDefaultStatus_integrationWithApplicationProperties_success() {
        // Act
        ProjectStatus defaultStatus = config.getDefaultStatus();

        // Assert
        assertNotNull(defaultStatus);
        assertEquals(ProjectStatus.PLANNING, defaultStatus);
    }

    @Test
    void isValid_integrationWithApplicationProperties_returnsTrue() {
        // Act
        boolean isValid = config.isValid();

        // Assert
        assertTrue(isValid);
    }
}