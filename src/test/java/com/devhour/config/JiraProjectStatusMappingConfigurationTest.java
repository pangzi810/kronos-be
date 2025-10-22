package com.devhour.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.ProjectStatus;

/**
 * JiraProjectStatusMappingConfigurationのテストクラス
 *
 * JIRA統合におけるプロジェクトステータスマッピング設定の機能をテストする
 */
class JiraProjectStatusMappingConfigurationTest {

    private JiraProjectStatusMappingConfiguration config;

    @BeforeEach
    void setUp() {
        config = new JiraProjectStatusMappingConfiguration();
    }

    @Test
    void buildStatusMappingMap_defaultConfiguration_success() {
        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Check IN_PROGRESS mappings
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("ACTIVE"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("IN_PROGRESS"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("STARTED"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("ONGOING"));

        // Check COMPLETED mappings
        assertEquals(ProjectStatus.COMPLETED, result.get("COMPLETED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("DONE"));
        assertEquals(ProjectStatus.COMPLETED, result.get("FINISHED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("RESOLVED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("CLOSED"));

        // Check CANCELLED mappings
        assertEquals(ProjectStatus.CANCELLED, result.get("CANCELLED"));
        assertEquals(ProjectStatus.CANCELLED, result.get("CANCELED"));
        assertEquals(ProjectStatus.CANCELLED, result.get("ABANDONED"));
        assertEquals(ProjectStatus.CANCELLED, result.get("REJECTED"));

        // Check PLANNING mappings
        assertEquals(ProjectStatus.PLANNING, result.get("PLANNING"));
        assertEquals(ProjectStatus.PLANNING, result.get("NEW"));
        assertEquals(ProjectStatus.PLANNING, result.get("OPEN"));
        assertEquals(ProjectStatus.PLANNING, result.get("TO_DO"));
        assertEquals(ProjectStatus.PLANNING, result.get("BACKLOG"));
    }

    @Test
    void buildStatusMappingMap_customConfiguration_success() {
        // Arrange
        config.setInProgress("CUSTOM_ACTIVE,CUSTOM_PROGRESS");
        config.setCompleted("CUSTOM_DONE");
        config.setCancelled("CUSTOM_CANCELLED");
        config.setPlanning("CUSTOM_PLANNING");

        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("CUSTOM_ACTIVE"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("CUSTOM_PROGRESS"));
        assertEquals(ProjectStatus.COMPLETED, result.get("CUSTOM_DONE"));
        assertEquals(ProjectStatus.CANCELLED, result.get("CUSTOM_CANCELLED"));
        assertEquals(ProjectStatus.PLANNING, result.get("CUSTOM_PLANNING"));
    }

    @Test
    void buildStatusMappingMap_emptyConfiguration_returnsEmptyMap() {
        // Arrange
        config.setInProgress("");
        config.setCompleted("");
        config.setCancelled("");
        config.setPlanning("");

        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buildStatusMappingMap_nullConfiguration_returnsEmptyMap() {
        // Arrange
        config.setInProgress(null);
        config.setCompleted(null);
        config.setCancelled(null);
        config.setPlanning(null);

        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void buildStatusMappingMap_whitespaceHandling_success() {
        // Arrange
        config.setInProgress("  ACTIVE  , IN_PROGRESS  ,  , STARTED  ");
        config.setCompleted("COMPLETED,  ,DONE");

        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("ACTIVE"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("IN_PROGRESS"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("STARTED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("COMPLETED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("DONE"));

        // Empty entries should be ignored
        assertNull(result.get(""));
        assertNull(result.get(" "));
    }

    @Test
    void buildStatusMappingMap_caseInsensitive_success() {
        // Arrange
        config.setInProgress("active,In_Progress,STARTED");
        config.setCompleted("completed,Done");

        // Act
        Map<String, ProjectStatus> result = config.buildStatusMappingMap();

        // Assert
        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("ACTIVE"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("IN_PROGRESS"));
        assertEquals(ProjectStatus.IN_PROGRESS, result.get("STARTED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("COMPLETED"));
        assertEquals(ProjectStatus.COMPLETED, result.get("DONE"));
    }

    @Test
    void getDefaultStatus_validDefaultStatus_success() {
        // Arrange
        config.setDefaultStatus("IN_PROGRESS");

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.IN_PROGRESS, result);
    }

    @Test
    void getDefaultStatus_invalidDefaultStatus_returnsPlanningWithWarning() {
        // Arrange
        config.setDefaultStatus("INVALID_STATUS");

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.PLANNING, result);
    }

    @Test
    void getDefaultStatus_nullDefaultStatus_returnsPlanning() {
        // Arrange
        config.setDefaultStatus(null);

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.PLANNING, result);
    }

    @Test
    void getDefaultStatus_emptyDefaultStatus_returnsPlanning() {
        // Arrange
        config.setDefaultStatus("");

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.PLANNING, result);
    }

    @Test
    void getDefaultStatus_whitespaceDefaultStatus_returnsPlanning() {
        // Arrange
        config.setDefaultStatus("   ");

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.PLANNING, result);
    }

    @Test
    void getDefaultStatus_caseInsensitive_success() {
        // Arrange
        config.setDefaultStatus("completed");

        // Act
        ProjectStatus result = config.getDefaultStatus();

        // Assert
        assertEquals(ProjectStatus.COMPLETED, result);
    }

    @Test
    void isValid_allFieldsSet_returnsTrue() {
        // Act
        boolean result = config.isValid();

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_partialFieldsSet_returnsTrue() {
        // Arrange
        config.setInProgress("");
        config.setCompleted("DONE");
        config.setCancelled("");
        config.setPlanning("");

        // Act
        boolean result = config.isValid();

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_noFieldsSet_returnsFalse() {
        // Arrange
        config.setInProgress("");
        config.setCompleted("");
        config.setCancelled("");
        config.setPlanning("");

        // Act
        boolean result = config.isValid();

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_nullFields_returnsFalse() {
        // Arrange
        config.setInProgress(null);
        config.setCompleted(null);
        config.setCancelled(null);
        config.setPlanning(null);

        // Act
        boolean result = config.isValid();

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_whitespaceFields_returnsFalse() {
        // Arrange
        config.setInProgress("   ");
        config.setCompleted("   ");
        config.setCancelled("   ");
        config.setPlanning("   ");

        // Act
        boolean result = config.isValid();

        // Assert
        assertFalse(result);
    }
}