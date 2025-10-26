package com.devhour.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.config.JiraProjectStatusMappingConfiguration;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.devhour.domain.service.DataMappingDomainService.DataMappingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DataMappingDomainServiceのテストクラス
 * 
 * JIRAとの連携において共通フォーマットJSONとProjectエンティティ間の
 * データマッピング機能をテストする
 */
@ExtendWith(MockitoExtension.class)
class DataMappingDomainServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private JiraProjectStatusMappingConfiguration statusMappingConfig;

    private DataMappingDomainService domainService;

    @BeforeEach
    void setUp() {
        // Setup default status mapping configuration
        Map<String, ProjectStatus> defaultMappingMap = new HashMap<>();
        defaultMappingMap.put("ACTIVE", ProjectStatus.IN_PROGRESS);
        defaultMappingMap.put("IN_PROGRESS", ProjectStatus.IN_PROGRESS);
        defaultMappingMap.put("STARTED", ProjectStatus.IN_PROGRESS);
        defaultMappingMap.put("ONGOING", ProjectStatus.IN_PROGRESS);
        defaultMappingMap.put("CLOSED", ProjectStatus.CLOSED);
        defaultMappingMap.put("DONE", ProjectStatus.CLOSED);
        defaultMappingMap.put("FINISHED", ProjectStatus.CLOSED);
        defaultMappingMap.put("RESOLVED", ProjectStatus.CLOSED);
        defaultMappingMap.put("CLOSED", ProjectStatus.CLOSED);
        defaultMappingMap.put("CANCELLED", ProjectStatus.CLOSED);
        defaultMappingMap.put("CANCELED", ProjectStatus.CLOSED);
        defaultMappingMap.put("ABANDONED", ProjectStatus.CLOSED);
        defaultMappingMap.put("REJECTED", ProjectStatus.CLOSED);
        defaultMappingMap.put("DRAFT", ProjectStatus.DRAFT);
        defaultMappingMap.put("NEW", ProjectStatus.DRAFT);
        defaultMappingMap.put("OPEN", ProjectStatus.DRAFT);
        defaultMappingMap.put("TO_DO", ProjectStatus.DRAFT);
        defaultMappingMap.put("BACKLOG", ProjectStatus.DRAFT);

        lenient().when(statusMappingConfig.buildStatusMappingMap()).thenReturn(defaultMappingMap);
        lenient().when(statusMappingConfig.getDefaultStatus()).thenReturn(ProjectStatus.DRAFT);
        
        domainService = new DataMappingDomainService(objectMapper, statusMappingConfig);
    }

    // ========== テストデータ ==========

    private static final String VALID_COMMON_FORMAT_JSON = """
    {
      "issueKey": "PROJ-123",
      "projectCode": "PROJ",
      "projectName": "Test Project",
      "description": "Test project description",
      "status": "ACTIVE",
      "startDate": "2024-01-15",
      "endDate": "2024-06-15",
      "projectManager": "John Doe",
      "projectManagerEmail": "john.doe@company.com",
      "budget": 100000.0,
      "priority": "HIGH",
      "lastUpdated": "2024-01-20T10:30:00Z",
      "createdDate": "2024-01-01T09:00:00Z"
    }
    """;

    private static final String MINIMAL_COMMON_FORMAT_JSON = """
    {
      "issueKey": "PROJ-456",
      "projectName": "Minimal Project"
    }
    """;

    private static final String INVALID_JSON_FORMAT = """
    {
      "issueKey": "PROJ-123",
      "projectName": "Test Project"
      "missing_comma": "invalid"
    }
    """;

    private static final String MISSING_ISSUE_KEY_JSON = """
    {
      "projectName": "Test Project",
      "description": "Test project description"
    }
    """;

    private static final String EMPTY_ISSUE_KEY_JSON = """
    {
      "issueKey": "",
      "projectName": "Test Project"
    }
    """;

    private static final String LONG_PROJECT_NAME_JSON = """
    {
      "issueKey": "PROJ-789",
      "projectName": "%s"
    }
    """.formatted("A".repeat(300)); // 300文字のプロジェクト名

    private static final String VARIOUS_DATE_FORMATS_JSON = """
    {
      "issueKey": "PROJ-101",
      "projectName": "Date Format Test",
      "startDate": "2024-01-15T09:00:00.000Z",
      "endDate": "2024-07-15"
    }
    """;

    private static final String INVALID_DATE_JSON = """
    {
      "issueKey": "PROJ-102",
      "projectName": "Invalid Date Test",
      "startDate": "invalid-date",
      "endDate": "2024-12-31"
    }
    """;

    private static final String VARIOUS_STATUS_JSON = """
    {
      "issueKey": "PROJ-103",
      "projectName": "Status Test",
      "status": "%s"
    }
    """;

    // ========== extractIssueKey のテスト ==========

    @Test
    void extractIssueKey_validJson_success() throws Exception {
        // Arrange
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode issueKeyNode = mock(JsonNode.class);
        
        when(objectMapper.readTree(VALID_COMMON_FORMAT_JSON)).thenReturn(rootNode);
        when(rootNode.get("issueKey")).thenReturn(issueKeyNode);
        when(issueKeyNode.isNull()).thenReturn(false);
        when(issueKeyNode.asText()).thenReturn("PROJ-123");

        // Act
        String result = domainService.extractIssueKey(VALID_COMMON_FORMAT_JSON);

        // Assert
        assertEquals("PROJ-123", result);
    }

    @Test
    void extractIssueKey_invalidJson_throwsDataMappingException() throws Exception {
        // Arrange
        when(objectMapper.readTree(any(String.class)))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.extractIssueKey(INVALID_JSON_FORMAT));
        
        assertTrue(exception.getMessage().contains("Failed to parse common format JSON"));
    }

    @Test
    void extractIssueKey_missingIssueKey_throwsDataMappingException() throws Exception {
        // Arrange
        JsonNode rootNode = mock(JsonNode.class);
        
        when(objectMapper.readTree(MISSING_ISSUE_KEY_JSON)).thenReturn(rootNode);
        when(rootNode.get("issueKey")).thenReturn(null);

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.extractIssueKey(MISSING_ISSUE_KEY_JSON));
        
        assertEquals("Issue key not found in common format JSON", exception.getMessage());
    }

    @Test
    void extractIssueKey_nullIssueKey_throwsDataMappingException() throws Exception {
        // Arrange
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode issueKeyNode = mock(JsonNode.class);
        
        when(objectMapper.readTree(EMPTY_ISSUE_KEY_JSON)).thenReturn(rootNode);
        when(rootNode.get("issueKey")).thenReturn(issueKeyNode);
        when(issueKeyNode.isNull()).thenReturn(true);

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.extractIssueKey(EMPTY_ISSUE_KEY_JSON));
        
        assertEquals("Issue key not found in common format JSON", exception.getMessage());
    }

    @Test
    void extractIssueKey_emptyIssueKey_throwsDataMappingException() throws Exception {
        // Arrange
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode issueKeyNode = mock(JsonNode.class);
        
        when(objectMapper.readTree(EMPTY_ISSUE_KEY_JSON)).thenReturn(rootNode);
        when(rootNode.get("issueKey")).thenReturn(issueKeyNode);
        when(issueKeyNode.isNull()).thenReturn(false);
        when(issueKeyNode.asText()).thenReturn("");

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.extractIssueKey(EMPTY_ISSUE_KEY_JSON));
        
        assertEquals("Issue key is empty in common format JSON", exception.getMessage());
    }

    // ========== createProjectFromCommonFormat のテスト ==========

    @Test
    void createProjectFromCommonFormat_validJson_success() throws Exception {
        // Arrange
        String createdBy = "user123";
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-123");
        commonFormat.setProjectName("Test Project");
        commonFormat.setDescription("Test description");
        commonFormat.setStatus("ACTIVE");
        commonFormat.setStartDate("2024-01-15");
        commonFormat.setEndDate("2024-06-15");

        when(objectMapper.readValue(eq(VALID_COMMON_FORMAT_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(VALID_COMMON_FORMAT_JSON, createdBy);

        // Assert
        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        assertEquals("Test description", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 15), result.getStartDate());
        assertEquals(LocalDate.of(2024, 6, 15), result.getPlannedEndDate());
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
        assertEquals("PROJ-123", result.getJiraIssueKey());
        assertEquals(createdBy, result.getCreatedBy());
        assertTrue(result.hasJiraIntegration());
    }

    @Test
    void createProjectFromCommonFormat_minimalData_success() throws Exception {
        // Arrange
        String createdBy = "user123";
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-456");
        commonFormat.setProjectName("Minimal Project");

        when(objectMapper.readValue(eq(MINIMAL_COMMON_FORMAT_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(MINIMAL_COMMON_FORMAT_JSON, createdBy);

        // Assert
        assertNotNull(result);
        assertEquals("Minimal Project", result.getName());
        assertNull(result.getDescription());
        assertNotNull(result.getStartDate()); // Default dates are used
        assertNotNull(result.getPlannedEndDate()); // Default dates are used
        assertEquals(ProjectStatus.DRAFT, result.getStatus());
        assertEquals("PROJ-456", result.getJiraIssueKey());
    }

    @Test
    void createProjectFromCommonFormat_longProjectName_truncated() throws Exception {
        // Arrange
        String createdBy = "user123";
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-789");
        commonFormat.setProjectName("A".repeat(300)); // 300文字

        when(objectMapper.readValue(eq(LONG_PROJECT_NAME_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(LONG_PROJECT_NAME_JSON, createdBy);

        // Assert
        assertNotNull(result);
        assertEquals(255, result.getName().length());
        assertEquals("A".repeat(255), result.getName());
    }

    @Test
    void createProjectFromCommonFormat_invalidDates_handledGracefully() throws Exception {
        // Arrange
        String createdBy = "user123";
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-102");
        commonFormat.setProjectName("Invalid Date Test");
        commonFormat.setStartDate("invalid-date");
        commonFormat.setEndDate("2024-12-31");

        when(objectMapper.readValue(eq(INVALID_DATE_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(INVALID_DATE_JSON, createdBy);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getStartDate()); // Default start date is used when invalid
        assertNotNull(result.getPlannedEndDate()); // End date is adjusted to be after start date
        assertTrue(result.getPlannedEndDate().isAfter(result.getStartDate())); // End date must be after start date
    }

    @Test
    void createProjectFromCommonFormat_nullCreatedBy_throwsDataMappingException() throws Exception {
        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.createProjectFromCommonFormat(VALID_COMMON_FORMAT_JSON, null));
        
        assertTrue(exception.getMessage().contains("createdBy"));
    }

    // ========== updateProjectFromCommonFormat のテスト ==========

    @Test
    void updateProjectFromCommonFormat_validUpdate_success() throws Exception {
        // Arrange
        Project existingProject = Project.create("Original Name", "Original Description",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), "creator", "PROJ-123", null);
        
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-123");
        commonFormat.setProjectName("Updated Project");
        commonFormat.setDescription("Updated description");
        commonFormat.setStatus("IN_PROGRESS");
        commonFormat.setStartDate("2024-01-15");
        commonFormat.setEndDate("2024-07-15");

        when(objectMapper.readValue(eq(VALID_COMMON_FORMAT_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.updateProjectFromCommonFormat(existingProject, VALID_COMMON_FORMAT_JSON);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Project", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 15), result.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 15), result.getPlannedEndDate());
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
        assertEquals("PROJ-123", result.getJiraIssueKey());
    }

    @Test
    void updateProjectFromCommonFormat_nullExistingProject_throwsDataMappingException() {
        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.updateProjectFromCommonFormat(null, VALID_COMMON_FORMAT_JSON));
        
        assertTrue(exception.getMessage().contains("existingProject"));
    }

    // ========== ステータスマッピングのテスト ==========

    @Test
    void mapProjectStatus_activeVariants_returnsActive() throws Exception {
        String[] activeStatuses = {"ACTIVE", "IN_PROGRESS", "STARTED", "ONGOING"};
        
        for (String status : activeStatuses) {
            // Arrange
            String jsonWithStatus = String.format(VARIOUS_STATUS_JSON, status);
            DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
            commonFormat.setIssueKey("PROJ-103");
            commonFormat.setProjectName("Status Test");
            commonFormat.setStatus(status);

            when(objectMapper.readValue(eq(jsonWithStatus), eq(DataMappingDomainService.CommonFormatProject.class)))
                .thenReturn(commonFormat);

            // Act
            Project result = domainService.createProjectFromCommonFormat(jsonWithStatus, "creator");

            // Assert
            assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus(), 
                "Status '" + status + "' should map to IN_PROGRESS");
        }
    }

    @Test
    void mapProjectStatus_completedVariants_returnsCompleted() throws Exception {
        String[] completedStatuses = {"CLOSED", "DONE", "FINISHED", "RESOLVED", "CLOSED"};
        
        for (String status : completedStatuses) {
            // Arrange
            String jsonWithStatus = String.format(VARIOUS_STATUS_JSON, status);
            DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
            commonFormat.setIssueKey("PROJ-103");
            commonFormat.setProjectName("Status Test");
            commonFormat.setStatus(status);

            when(objectMapper.readValue(eq(jsonWithStatus), eq(DataMappingDomainService.CommonFormatProject.class)))
                .thenReturn(commonFormat);

            // Act
            Project result = domainService.createProjectFromCommonFormat(jsonWithStatus, "creator");

            // Assert
            assertEquals(ProjectStatus.CLOSED, result.getStatus(),
                "Status '" + status + "' should map to COMPLETED");
        }
    }

    @Test
    void mapProjectStatus_cancelledVariants_returnsCancelled() throws Exception {
        String[] cancelledStatuses = {"CANCELLED", "CANCELED", "ABANDONED", "REJECTED"};
        
        for (String status : cancelledStatuses) {
            // Arrange
            String jsonWithStatus = String.format(VARIOUS_STATUS_JSON, status);
            DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
            commonFormat.setIssueKey("PROJ-103");
            commonFormat.setProjectName("Status Test");
            commonFormat.setStatus(status);

            when(objectMapper.readValue(eq(jsonWithStatus), eq(DataMappingDomainService.CommonFormatProject.class)))
                .thenReturn(commonFormat);

            // Act
            Project result = domainService.createProjectFromCommonFormat(jsonWithStatus, "creator");

            // Assert
            assertEquals(ProjectStatus.CLOSED, result.getStatus(),
                "Status '" + status + "' should map to CANCELLED");
        }
    }

    @Test
    void mapProjectStatus_planningVariants_returnsPlanning() throws Exception {
        String[] planningStatuses = {"DRAFT", "NEW", "OPEN", "TO_DO", "BACKLOG"};
        
        for (String status : planningStatuses) {
            // Arrange
            String jsonWithStatus = String.format(VARIOUS_STATUS_JSON, status);
            DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
            commonFormat.setIssueKey("PROJ-103");
            commonFormat.setProjectName("Status Test");
            commonFormat.setStatus(status);

            when(objectMapper.readValue(eq(jsonWithStatus), eq(DataMappingDomainService.CommonFormatProject.class)))
                .thenReturn(commonFormat);

            // Act
            Project result = domainService.createProjectFromCommonFormat(jsonWithStatus, "creator");

            // Assert
            assertEquals(ProjectStatus.DRAFT, result.getStatus(),
                "Status '" + status + "' should map to PLANNING");
        }
    }

    @Test
    void mapProjectStatus_unknownStatus_returnsPlanning() throws Exception {
        // Arrange
        String jsonWithStatus = String.format(VARIOUS_STATUS_JSON, "UNKNOWN_STATUS");
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-103");
        commonFormat.setProjectName("Status Test");
        commonFormat.setStatus("UNKNOWN_STATUS");

        when(objectMapper.readValue(eq(jsonWithStatus), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(jsonWithStatus, "creator");

        // Assert
        assertEquals(ProjectStatus.DRAFT, result.getStatus());
    }

    @Test
    void mapProjectStatus_nullStatus_returnsPlanning() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-103");
        commonFormat.setProjectName("Status Test");
        commonFormat.setStatus(null);

        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat("{}", "creator");

        // Assert
        assertEquals(ProjectStatus.DRAFT, result.getStatus());
    }

    // ========== 日付パースのテスト ==========

    @Test
    void parseDate_variousFormats_success() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-101");
        commonFormat.setProjectName("Date Format Test");
        commonFormat.setStartDate("2024-01-15T09:00:00.000Z");
        commonFormat.setEndDate("2024-07-15");

        when(objectMapper.readValue(eq(VARIOUS_DATE_FORMATS_JSON), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat(VARIOUS_DATE_FORMATS_JSON, "creator");

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 1, 15), result.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 15), result.getPlannedEndDate());
    }

    // ========== 日付ロジックの検証テスト ==========

    @Test
    void createProject_startDateAfterEndDate_adjustsEndDate() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-104");
        commonFormat.setProjectName("Date Logic Test");
        commonFormat.setStartDate("2024-06-15");
        commonFormat.setEndDate("2024-01-15"); // End date before start date

        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act
        Project result = domainService.createProjectFromCommonFormat("{}", "creator");

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 6, 15), result.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 15), result.getPlannedEndDate()); // End date adjusted to start date + 6 months
    }

    // ========== エラーハンドリングのテスト ==========

    @Test
    void createProjectFromCommonFormat_jsonProcessingException_throwsDataMappingException() throws Exception {
        // Arrange
        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenThrow(new JsonProcessingException("JSON parsing error") {});

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.createProjectFromCommonFormat(VALID_COMMON_FORMAT_JSON, "creator"));
        
        assertTrue(exception.getMessage().contains("Failed to parse common format JSON"));
        assertNotNull(exception.getCause());
    }

    @Test
    void createProjectFromCommonFormat_missingProjectName_throwsDataMappingException() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-105");
        commonFormat.setProjectName(null); // Missing project name

        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.createProjectFromCommonFormat("{}", "creator"));
        
        assertTrue(exception.getMessage().contains("Project name"));
    }

    @Test
    void createProjectFromCommonFormat_emptyProjectName_throwsDataMappingException() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey("PROJ-106");
        commonFormat.setProjectName(""); // Empty project name

        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.createProjectFromCommonFormat("{}", "creator"));
        
        assertTrue(exception.getMessage().contains("Project name"));
    }

    @Test
    void createProjectFromCommonFormat_missingIssueKey_throwsDataMappingException() throws Exception {
        // Arrange
        DataMappingDomainService.CommonFormatProject commonFormat = new DataMappingDomainService.CommonFormatProject();
        commonFormat.setIssueKey(null); // Missing issue key
        commonFormat.setProjectName("Test Project");

        when(objectMapper.readValue(any(String.class), eq(DataMappingDomainService.CommonFormatProject.class)))
            .thenReturn(commonFormat);

        // Act & Assert
        DataMappingException exception = assertThrows(DataMappingException.class,
            () -> domainService.createProjectFromCommonFormat("{}", "creator"));
        
        assertTrue(exception.getMessage().contains("Issue key"));
    }
}