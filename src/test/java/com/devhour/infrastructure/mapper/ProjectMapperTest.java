package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;

/**
 * ProjectMapperの統合テスト
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ProjectMapper統合テスト")
class ProjectMapperTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        // Create common test user that most tests use
        createTestUser("user");
    }

    @Test
    @DisplayName("プロジェクト挿入 - 正常ケース")
    void insert_Success() {
        // Arrange
        String userId = "test-user";
        createTestUser(userId);

        String id = "test-project-id";
        String name = "Test Project";
        String description = "Test Description";
        String status = "PLANNING";
        LocalDate startDate = LocalDate.now();
        LocalDate plannedEndDate = LocalDate.now().plusDays(30);
        String createdBy = userId;
        LocalDateTime now = LocalDateTime.now();

        // Act
        projectMapper.insert(id, name, description, status, startDate, plannedEndDate, createdBy, now, now, null, null);

        // Assert
        Optional<Project> result = projectMapper.findById(id);
        assertThat(result).isPresent();
        Project project = result.get();
        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getName()).isEqualTo(name);
        assertThat(project.getDescription()).isEqualTo(description);
        assertThat(project.getStatus().value()).isEqualTo(status);
        assertThat(project.getStartDate()).isEqualTo(startDate);
        assertThat(project.getPlannedEndDate()).isEqualTo(plannedEndDate);
        assertThat(project.getCreatedBy()).isEqualTo(createdBy);
    }

    @Test
    @DisplayName("ID検索 - 存在するプロジェクト")
    void findById_ExistingProject_ReturnsProject() {
        // Arrange
        createTestUser("user");

        String id = "test-project-findbyid";
        String name = "FindById Project";
        LocalDateTime now = LocalDateTime.now();

        projectMapper.insert(id, name, "Description", "PLANNING", LocalDate.now(),
                           LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        Optional<Project> result = projectMapper.findById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("ID検索 - 存在しないプロジェクト")
    void findById_NonExistingProject_ReturnsEmpty() {
        // Act
        Optional<Project> result = projectMapper.findById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("プロジェクト名検索 - 存在するプロジェクト")
    void findByName_ExistingProject_ReturnsProject() {
        // Arrange
        createTestUser("user");

        String id = "test-project-name";
        String name = "Unique Project Name";
        LocalDateTime now = LocalDateTime.now();

        projectMapper.insert(id, name, "Description", "PLANNING", LocalDate.now(),
                           LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        Optional<Project> result = projectMapper.findByName(name);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("プロジェクト名検索 - 存在しないプロジェクト")
    void findByName_NonExistingProject_ReturnsEmpty() {
        // Act
        Optional<Project> result = projectMapper.findByName("Non-existing Project");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("全プロジェクト取得")
    void findAll_ReturnsAllProjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        projectMapper.insert("project1", "Project One", "Desc1", "PLANNING",
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("project2", "Project Two", "Desc2", "IN_PROGRESS",
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> result = projectMapper.findAll();

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).extracting(Project::getName).contains("Project One", "Project Two");
    }

    @Test
    @DisplayName("状態別プロジェクト取得")
    void findByStatus_ReturnsProjectsWithStatus() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        projectMapper.insert("planning-project", "Planning Project", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("progress-project", "In Progress Project", "Desc", "IN_PROGRESS", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> planningProjects = projectMapper.findByStatus("PLANNING");
        List<Project> progressProjects = projectMapper.findByStatus("IN_PROGRESS");

        // Assert
        assertThat(planningProjects).isNotEmpty();
        assertThat(planningProjects).allMatch(project -> "PLANNING".equals(project.getStatus().value()));
        assertThat(planningProjects).extracting(Project::getName).contains("Planning Project");

        assertThat(progressProjects).isNotEmpty();
        assertThat(progressProjects).allMatch(project -> "IN_PROGRESS".equals(project.getStatus().value()));
        assertThat(progressProjects).extracting(Project::getName).contains("In Progress Project");
    }

    @Test
    @DisplayName("アクティブプロジェクト取得")
    void findActiveProjects_ReturnsActiveProjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        projectMapper.insert("active-planning", "Active Planning", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("active-progress", "Active Progress", "Desc", "IN_PROGRESS", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("completed-project", "Completed Project", "Desc", "COMPLETED", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> result = projectMapper.findActiveProjects();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Project::getName)
                         .contains("Active Planning", "Active Progress");
        assertThat(result).extracting(Project::getName)
                         .doesNotContain("Completed Project");
        assertThat(result).allMatch(project -> 
            "PLANNING".equals(project.getStatus().value()) || 
            "IN_PROGRESS".equals(project.getStatus().value()));
    }

    @Test
    @DisplayName("工数記録可能プロジェクト取得")
    void findWorkRecordableProjects_ReturnsWorkRecordableProjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        projectMapper.insert("recordable-progress", "Recordable Progress", "Desc", "IN_PROGRESS", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("planning-project", "Planning Project", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("completed-project", "Completed Project", "Desc", "COMPLETED", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> result = projectMapper.findWorkRecordableProjects();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Project::getName)
                         .contains("Recordable Progress");
        assertThat(result).extracting(Project::getName)
                         .doesNotContain("Planning Project", "Completed Project");
        assertThat(result).allMatch(project -> 
            "IN_PROGRESS".equals(project.getStatus().value()) || 
            "ON_HOLD".equals(project.getStatus().value()));
    }

    @Test
    @DisplayName("開始日期間検索")
    void findByStartDateBetween_ReturnsProjectsInDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        LocalDate searchStart = LocalDate.of(2024, 1, 5);
        LocalDate searchEnd = LocalDate.of(2024, 1, 15);
        
        projectMapper.insert("early-project", "Early Project", "Desc", "PLANNING",
                           baseDate, baseDate.plusDays(30), "user", now, now, null, null);
        projectMapper.insert("in-range-project", "In Range Project", "Desc", "PLANNING",
                           LocalDate.of(2024, 1, 10), baseDate.plusDays(30), "user", now, now, null, null);
        projectMapper.insert("late-project", "Late Project", "Desc", "PLANNING",
                           LocalDate.of(2024, 1, 20), baseDate.plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> result = projectMapper.findByStartDateBetween(searchStart, searchEnd);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Project::getName).contains("In Range Project");
        assertThat(result).extracting(Project::getName).doesNotContain("Early Project", "Late Project");
        assertThat(result).allMatch(project -> 
            !project.getStartDate().isBefore(searchStart) && 
            !project.getStartDate().isAfter(searchEnd));
    }

    @Test
    @DisplayName("終了予定日期間検索")
    void findByPlannedEndDateBetween_ReturnsProjectsInDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        LocalDate searchStart = LocalDate.of(2024, 2, 5);
        LocalDate searchEnd = LocalDate.of(2024, 2, 15);
        
        projectMapper.insert("early-end-project", "Early End Project", "Desc", "PLANNING",
                           baseDate, LocalDate.of(2024, 2, 1), "user", now, now, null, null);
        projectMapper.insert("in-range-end-project", "In Range End Project", "Desc", "PLANNING",
                           baseDate, LocalDate.of(2024, 2, 10), "user", now, now, null, null);
        projectMapper.insert("late-end-project", "Late End Project", "Desc", "PLANNING",
                           baseDate, LocalDate.of(2024, 2, 20), "user", now, now, null, null);

        // Act
        List<Project> result = projectMapper.findByPlannedEndDateBetween(searchStart, searchEnd);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Project::getName).contains("In Range End Project");
        assertThat(result).extracting(Project::getName).doesNotContain("Early End Project", "Late End Project");
        assertThat(result).allMatch(project -> 
            !project.getPlannedEndDate().isBefore(searchStart) && 
            !project.getPlannedEndDate().isAfter(searchEnd));
    }

    @Test
    @DisplayName("プロジェクト名部分一致検索")
    void searchByName_ReturnsMatchingProjects() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        projectMapper.insert("mobile-app-project", "Mobile App Development", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("web-app-project", "Web Application Project", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("database-project", "Database Migration", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        List<Project> appResults = projectMapper.searchByName("App");
        List<Project> mobileResults = projectMapper.searchByName("Mobile");

        // Assert
        assertThat(appResults).hasSize(2);
        assertThat(appResults).extracting(Project::getName)
                             .containsExactly("Mobile App Development", "Web Application Project"); // Ordered by name ASC

        assertThat(mobileResults).hasSize(1);
        assertThat(mobileResults).extracting(Project::getName).contains("Mobile App Development");
    }

    @Test
    @DisplayName("プロジェクト更新 - 正常ケース")
    void update_Success() {
        // Arrange
        String id = "update-project";
        String originalName = "Original Project";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(id, originalName, "Original Description", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        String newName = "Updated Project";
        String newDescription = "Updated Description";
        LocalDate newStartDate = LocalDate.now().plusDays(1);
        LocalDate newEndDate = LocalDate.now().plusDays(40);
        LocalDateTime updateTime = now.plusMinutes(1);

        // Act
        int result = projectMapper.update(id, newName, newDescription, "IN_PROGRESS", newStartDate, newEndDate, null, null, updateTime);

        // Assert
        assertThat(result).isEqualTo(1);
        
        Optional<Project> updatedProject = projectMapper.findById(id);
        assertThat(updatedProject).isPresent();
        assertThat(updatedProject.get().getName()).isEqualTo(newName);
        assertThat(updatedProject.get().getDescription()).isEqualTo(newDescription);
        assertThat(updatedProject.get().getStartDate()).isEqualTo(newStartDate);
        assertThat(updatedProject.get().getPlannedEndDate()).isEqualTo(newEndDate);
    }

    @Test
    @DisplayName("プロジェクト更新 - 存在しないプロジェクト")
    void update_NonExistingProject_ReturnsZero() {
        // Act
        int result = projectMapper.update("non-existing", "Name", "Description", "IN_PROGRESS",
                                        LocalDate.now(), LocalDate.now().plusDays(30), null, null, LocalDateTime.now());

        // Assert
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("プロジェクト状態更新 - 正常ケース")
    void updateStatus_Success() {
        // Arrange
        String id = "status-update-project";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(id, "Status Project", "Description", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        String newStatus = "IN_PROGRESS";
        LocalDateTime updateTime = now.plusMinutes(1);

        // Act
        int result = projectMapper.updateStatus(id, newStatus, updateTime);

        // Assert
        assertThat(result).isEqualTo(1);
        
        Optional<Project> updatedProject = projectMapper.findById(id);
        assertThat(updatedProject).isPresent();
        assertThat(updatedProject.get().getStatus().value()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("プロジェクト論理削除 - 正常ケース")
    void softDelete_Success() {
        // Arrange
        String id = "delete-project";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(id, "Delete Project", "Description", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        LocalDateTime deleteTime = now.plusMinutes(1);

        // Act
        int result = projectMapper.softDelete(id, deleteTime, deleteTime);

        // Assert
        assertThat(result).isEqualTo(1);
        
        // Soft deleted project should not be found
        Optional<Project> deletedProject = projectMapper.findById(id);
        assertThat(deletedProject).isEmpty();
    }

    @Test
    @DisplayName("プロジェクト名存在チェック - 存在する場合")
    void existsByName_ExistingProject_ReturnsTrue() {
        // Arrange
        String name = "Existing Project Name";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert("exists-project", name, "Description", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        boolean result = projectMapper.existsByName(name);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("プロジェクト名存在チェック - 存在しない場合")
    void existsByName_NonExistingProject_ReturnsFalse() {
        // Act
        boolean result = projectMapper.existsByName("Non-existing Project Name");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("プロジェクトID存在チェック - 存在する場合")
    void existsById_ExistingProject_ReturnsTrue() {
        // Arrange
        String id = "exists-by-id-project";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(id, "Project Name", "Description", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        boolean result = projectMapper.existsById(id);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("プロジェクトID存在チェック - 存在しない場合")
    void existsById_NonExistingProject_ReturnsFalse() {
        // Act
        boolean result = projectMapper.existsById("non-existing-id");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("プロジェクト総数取得")
    void count_ReturnsCorrectCount() {
        // Arrange
        long initialCount = projectMapper.count();
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert("count-project1", "Count Project 1", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("count-project2", "Count Project 2", "Desc", "IN_PROGRESS", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        long result = projectMapper.count();

        // Assert
        assertThat(result).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("状態別プロジェクト数取得")
    void countByStatus_ReturnsCorrectCount() {
        // Arrange
        long initialPlanningCount = projectMapper.countByStatus("PLANNING");
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert("status-count1", "Status Count 1", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("status-count2", "Status Count 2", "Desc", "PLANNING", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        projectMapper.insert("status-count3", "Status Count 3", "Desc", "IN_PROGRESS", 
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);

        // Act
        long planningCount = projectMapper.countByStatus("PLANNING");
        long progressCount = projectMapper.countByStatus("IN_PROGRESS");

        // Assert
        assertThat(planningCount).isEqualTo(initialPlanningCount + 2);
        assertThat(progressCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("JIRAイシューキー検索 - 存在するプロジェクト")
    void selectByJiraIssueKey_ExistingProject_ReturnsProject() {
        // Arrange
        String projectId = "jira-issue-key-project";
        String jiraIssueKey = "TEST-123";
        LocalDateTime now = LocalDateTime.now();
        
        // プロジェクトを作成
        projectMapper.insert(projectId, "JIRA Test Project", "Description", "PLANNING",
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        
        // JIRAイシューキーを設定
        projectMapper.updateJiraIssueKey(projectId, jiraIssueKey, now);

        // Act
        Optional<Project> result = projectMapper.selectByJiraIssueKey(jiraIssueKey);

        // Assert
        assertThat(result).isPresent();
        Project project = result.get();
        assertThat(project.getId()).isEqualTo(projectId);
        assertThat(project.getJiraIssueKey()).isEqualTo(jiraIssueKey);
        assertThat(project.getName()).isEqualTo("JIRA Test Project");
    }

    @Test
    @DisplayName("JIRAイシューキー検索 - 存在しないイシューキー")
    void selectByJiraIssueKey_NonExistingIssueKey_ReturnsEmpty() {
        // Act
        Optional<Project> result = projectMapper.selectByJiraIssueKey("NON-EXISTING-123");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JIRAイシューキー検索 - nullイシューキー")
    void selectByJiraIssueKey_NullIssueKey_ReturnsEmpty() {
        // Act
        Optional<Project> result = projectMapper.selectByJiraIssueKey(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JIRAイシューキー検索 - 空文字イシューキー")
    void selectByJiraIssueKey_EmptyIssueKey_ReturnsEmpty() {
        // Act
        Optional<Project> result = projectMapper.selectByJiraIssueKey("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JIRAイシューキー更新 - 正常ケース")
    void updateJiraIssueKey_ExistingProject_Success() {
        // Arrange
        String projectId = "jira-update-project";
        String initialJiraKey = "INITIAL-123";
        String updatedJiraKey = "UPDATED-456";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(projectId, "JIRA Update Test", "Description", "PLANNING",
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        
        // 初期のJIRAイシューキーを設定
        projectMapper.updateJiraIssueKey(projectId, initialJiraKey, now);

        // Act
        int updateCount = projectMapper.updateJiraIssueKey(projectId, updatedJiraKey, now.plusMinutes(1));

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<Project> result = projectMapper.findById(projectId);
        assertThat(result).isPresent();
        assertThat(result.get().getJiraIssueKey()).isEqualTo(updatedJiraKey);
    }

    @Test
    @DisplayName("JIRAイシューキー更新 - 存在しないプロジェクト")
    void updateJiraIssueKey_NonExistingProject_ReturnsZero() {
        // Act
        int updateCount = projectMapper.updateJiraIssueKey(
            "non-existing-project-id", 
            "JIRA-999", 
            LocalDateTime.now()
        );

        // Assert
        assertThat(updateCount).isEqualTo(0);
    }

    @Test
    @DisplayName("JIRAイシューキー更新 - nullに設定")
    void updateJiraIssueKey_SetToNull_Success() {
        // Arrange
        String projectId = "jira-null-update-project";
        LocalDateTime now = LocalDateTime.now();
        
        projectMapper.insert(projectId, "JIRA Null Update Test", "Description", "PLANNING",
                           LocalDate.now(), LocalDate.now().plusDays(30), "user", now, now, null, null);
        
        // 初期のJIRAイシューキーを設定
        projectMapper.updateJiraIssueKey(projectId, "INITIAL-789", now);

        // Act - nullに更新
        int updateCount = projectMapper.updateJiraIssueKey(projectId, null, now.plusMinutes(1));

        // Assert
        assertThat(updateCount).isEqualTo(1);
        
        Optional<Project> result = projectMapper.findById(projectId);
        assertThat(result).isPresent();
        assertThat(result.get().getJiraIssueKey()).isNull();
    }

    private void createTestUser(String userId) {
        // Check if user already exists to avoid duplicate key errors
        if (userMapper.findById(userId).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            userMapper.insert(userId, userId, userId + "@test.com", "Test User " + userId,
                             User.UserStatus.ACTIVE, null, now, now);
        }
    }
}