package com.devhour.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.devhour.config.ProjectStatusMappingConfiguration;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * データマッピングドメインサービス
 * 
 * JIRA統合において共通フォーマットJSONとProjectエンティティ間の
 * データマッピングを担当するドメインサービス
 * 
 * 責務:
 * - 共通フォーマットJSONからJIRAイシューキーの抽出
 * - 共通フォーマットJSONから新規Projectエンティティの作成
 * - 共通フォーマットJSONによる既存Projectエンティティの更新
 * - フィールドマッピングと検証
 * - データ品質の担保（文字数制限、日付検証など）
 */
@Component
@Slf4j
public class DataMappingDomainService {
    
    private final ObjectMapper objectMapper;
    private final ProjectStatusMappingConfiguration statusMappingConfig;
    private final Map<String, ProjectStatus> statusMappingMap;
    
    public DataMappingDomainService(ObjectMapper objectMapper, 
                                  ProjectStatusMappingConfiguration statusMappingConfig) {
        this.objectMapper = objectMapper;
        this.statusMappingConfig = statusMappingConfig;
        this.statusMappingMap = statusMappingConfig.buildStatusMappingMap();
    }
    
    /**
     * 共通フォーマットJSONからJIRAイシューキーを抽出
     * 
     * @param commonFormatJson 共通フォーマットJSON文字列
     * @return JIRAイシューキー
     * @throws DataMappingException データマッピングエラーの場合
     */
    public String extractIssueKey(String commonFormatJson) {
        try {
            JsonNode root = objectMapper.readTree(commonFormatJson);
            JsonNode issueKeyNode = root.get("issueKey");
            
            if (issueKeyNode == null || issueKeyNode.isNull()) {
                throw new DataMappingException("Issue key not found in common format JSON");
            }
            
            String issueKey = issueKeyNode.asText().trim();
            if (issueKey.isEmpty()) {
                throw new DataMappingException("Issue key is empty in common format JSON");
            }
            
            return issueKey;
            
        } catch (JsonProcessingException e) {
            throw new DataMappingException("Failed to parse common format JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * 共通フォーマットJSONから新規Projectエンティティを作成
     * 
     * @param commonFormatJson 共通フォーマットJSON文字列
     * @param createdBy 作成者ID
     * @return 新規Projectエンティティ
     * @throws DataMappingException データマッピングエラーの場合
     */
    public Project createProjectFromCommonFormat(String commonFormatJson, String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new DataMappingException("createdBy is required for project creation");
        }
        
        CommonFormatProject commonFormat = parseCommonFormat(commonFormatJson);
        validateCommonFormat(commonFormat);
        
        return mapCommonFormatToProject(commonFormat, createdBy.trim(), null);
    }
    
    /**
     * 共通フォーマットJSONで既存Projectエンティティを更新
     * 
     * @param existingProject 既存のProjectエンティティ
     * @param commonFormatJson 共通フォーマットJSON文字列
     * @return 更新されたProjectエンティティ
     * @throws DataMappingException データマッピングエラーの場合
     */
    public Project updateProjectFromCommonFormat(Project existingProject, String commonFormatJson) {
        if (existingProject == null) {
            throw new DataMappingException("existingProject is required for project update");
        }
        
        CommonFormatProject commonFormat = parseCommonFormat(commonFormatJson);
        validateCommonFormat(commonFormat);
        
        return mapCommonFormatToProject(commonFormat, null, existingProject);
    }
    
    /**
     * 共通フォーマットJSONの構造
     * Velocityテンプレートによって生成される標準化されたJSON形式
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommonFormatProject {

        public static final String COMMON_FORMAT_VM_TEMPLATE = """
            {
              "issueId": "$!{id}",
              "issueKey": "$!{key}",
              "description": "$!{fields.description}",
              "projectKey": "$!{fields.project.key}",
              "projectName": "$!{fields.summary}",
              "reporter": "$!{fields.reporter.displayName}",
              "created": "$!{fields.created}",
              "updated": "$!{fields.updated}",
              "status": "$!{fields.status.name}"
            }
            """;

        private String issueKey;           // JIRA issue key (required)
        private String projectName;       // Project name
        private String description;       // Project description
        private String status;           // Project status (PLANNING/ACTIVE/COMPLETED/CANCELLED)
        private String startDate;        // Start date (ISO format: YYYY-MM-DD)
        private String endDate;          // End date (ISO format: YYYY-MM-DD)
        
        // Additional fields from different templates
        private Map<String, Object> customFields; // Custom fields from JIRA
        private List<String> labels;              // JIRA labels
        private List<String> components;          // JIRA components
        
        // Default constructor
        public CommonFormatProject() {}
        
        // Getters and setters
        public String getIssueKey() { return issueKey; }
        public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public Map<String, Object> getCustomFields() { return customFields; }
        public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }
        
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        
        public List<String> getComponents() { return components; }
        public void setComponents(List<String> components) { this.components = components; }
    }
    
    /**
     * データマッピング例外
     */
    public static class DataMappingException extends RuntimeException {
        public DataMappingException(String message) {
            super(message);
        }
        
        public DataMappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * JSONからCommonFormatProjectオブジェクトをパース
     */
    private CommonFormatProject parseCommonFormat(String commonFormatJson) {
        try {
            return objectMapper.readValue(commonFormatJson, CommonFormatProject.class);
        } catch (JsonProcessingException e) {
            throw new DataMappingException("Failed to parse common format JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * 共通フォーマットデータの基本検証
     */
    private void validateCommonFormat(CommonFormatProject commonFormat) {
        if (commonFormat == null) {
            throw new DataMappingException("Common format data is null");
        }
        
        if (commonFormat.getIssueKey() == null || commonFormat.getIssueKey().trim().isEmpty()) {
            throw new DataMappingException("Issue key is required in common format data");
        }
        
        if (commonFormat.getProjectName() == null || commonFormat.getProjectName().trim().isEmpty()) {
            throw new DataMappingException("Project name is required in common format data");
        }
    }
    
    /**
     * 共通フォーマットからProjectエンティティへのマッピング
     */
    private Project mapCommonFormatToProject(CommonFormatProject commonFormat, String createdBy, Project existingProject) {
        
        // Map project name (required)
        String name = validateAndTrimString(commonFormat.getProjectName(), "projectName");
        if (name.length() > 255) {
            name = name.substring(0, 255);
            log.warn("Project name truncated to 255 characters for issue key: {}", commonFormat.getIssueKey());
        }
        
        // Map description (optional)
        String description = trimStringOrNull(commonFormat.getDescription());
        if (description != null && description.length() > 1000) {
            description = description.substring(0, 1000);
            log.warn("Project description truncated to 1000 characters for issue key: {}", commonFormat.getIssueKey());
        }
        
        // Map dates with validation
        LocalDate startDate = parseDate(commonFormat.getStartDate(), "startDate");
        LocalDate endDate = parseDate(commonFormat.getEndDate(), "endDate");
        
        // Validate date logic - if parsed end date is before start date, adjust it
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            log.warn("Start date is after end date for issue key: {}, adjusting end date to be 6 months after start date", commonFormat.getIssueKey());
            endDate = startDate.plusMonths(6);
        }
        
        // Map project status with fallback
        ProjectStatus status = mapProjectStatus(commonFormat.getStatus());
        
        String customFields = null;
        try {
            customFields = commonFormat.getCustomFields() != null ? objectMapper.writeValueAsString(commonFormat.getCustomFields()) : null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        // Create or update project
        if (existingProject == null) {
            // Create new project
            String issueKey = commonFormat.getIssueKey().trim();
            
            // Handle missing dates by using defaults
            LocalDate actualStartDate = startDate;
            LocalDate actualEndDate = endDate;
            
            if (actualStartDate == null) {
                actualStartDate = LocalDate.now();
                log.info("Using current date as start date for project {} as original start date was null/invalid", issueKey);
            }
            
            if (actualEndDate == null) {
                actualEndDate = actualStartDate.plusMonths(6); // Default 6 months duration
                log.info("Using default end date (+6 months from start) for project {} as original end date was null/invalid", issueKey);
            }
            
            // Final validation after fallback dates are set
            if (actualStartDate.isAfter(actualEndDate)) {
                log.warn("Final validation: Start date {} is after end date {} for issue key: {}, adjusting end date to be 6 months after start date", 
                    actualStartDate, actualEndDate, issueKey);
                actualEndDate = actualStartDate.plusMonths(6);
            }
            
            Project newProject = Project.create(name, description, actualStartDate, actualEndDate, createdBy, issueKey, customFields);
            
            // Set the status after creation if it's different from default
            if (!ProjectStatus.PLANNING.equals(status)) {
                try {
                    newProject.updateFromJira(name, description, actualStartDate, actualEndDate, status, null);
                } catch (IllegalStateException e) {
                    // If status transition is not allowed, keep the original status
                    log.warn("Could not set status {} for new project {}, keeping PLANNING", status, issueKey);
                }
            }
            return newProject;
        } else {
            // Update existing project - use existing dates if new ones are null
            LocalDate updateStartDate = startDate != null ? startDate : existingProject.getStartDate();
            LocalDate updateEndDate = endDate != null ? endDate : existingProject.getPlannedEndDate();
            
            existingProject.updateFromJira(name, description, updateStartDate, updateEndDate, status, customFields);
            return existingProject;
        }
    }
    
    /**
     * プロジェクトステータスのマッピング
     * application.propertiesの設定を使用してJIRAステータス文字列を内部ProjectStatusにマッピング
     */
    private ProjectStatus mapProjectStatus(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return statusMappingConfig.getDefaultStatus();
        }
        
        String normalizedStatus = statusString.trim().toUpperCase();
        
        ProjectStatus mappedStatus = statusMappingMap.get(normalizedStatus);
        if (mappedStatus != null) {
            return mappedStatus;    
        }
        
        log.warn("Unknown project status '{}', defaulting to {}", statusString, statusMappingConfig.getDefaultStatus());
        return statusMappingConfig.getDefaultStatus();
    }
    
    /**
     * 日付パース（複数フォーマット対応）
     */
    private LocalDate parseDate(String dateString, String fieldName) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateString.trim();
        
        // Common date formats from JIRA
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS+0900"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MMM/yy"),
            DateTimeFormatter.ofPattern("dd/MMM/yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (trimmed.contains("T")) {
                    return LocalDateTime.parse(trimmed, formatter).toLocalDate();
                } else {
                    return LocalDate.parse(trimmed, formatter);
                }
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        log.warn("Unable to parse date '{}' for field '{}', returning null", dateString, fieldName);
        return null;
    }
    
    /**
     * 必須文字列フィールドの検証とトリム
     */
    private String validateAndTrimString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new DataMappingException("Required field '" + fieldName + "' is null or empty");
        }
        return value.trim();
    }
    
    /**
     * 可選文字列フィールドのトリム（nullまたは空の場合はnullを返す）
     */
    private String trimStringOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}