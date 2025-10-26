package com.devhour.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.devhour.domain.model.entity.User;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;

/**
 * WorkRecordMapperの統合テスト
 *
 * Testcontainers MySQLコンテナを使用した統合テスト
 * AbstractMapperTestを継承してクリーンなMySQL環境でテストを実行
 */
@DisplayName("WorkRecordMapper統合テスト")
class WorkRecordMapperTest extends AbstractMapperTest {

    @Autowired
    private WorkRecordMapper workRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        // Create test users and projects that tests depend on
        createTestUser("demo-dev-uuid-0000-000000000003", "testdev", "testdev@example.com");
        createTestUser("demo-pmo-uuid-0000-000000000002", "testpmo", "testpmo@example.com");
        createTestUser("test-user", "testuser", "testuser@example.com");
        createTestProject("project-demo-uuid-000000000001", "Test Project", "demo-dev-uuid-0000-000000000003");
        createTestProject("project-demo-uuid-000000000002", "Test Project 2", "demo-dev-uuid-0000-000000000003");
    }

    @Test
    @DisplayName("工数記録挿入 - 正常ケース")
    void insert_Success() {
        // Arrange
        String id = "test-record-id";
        String userId = "demo-dev-uuid-0000-000000000003";
        String projectId = "project-demo-uuid-000000000001";
        LocalDate workDate = LocalDate.of(2024, 1, 15);
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        String description = "Test work record";
        LocalDateTime now = LocalDateTime.now();
        String createdBy = "test-user";
        String updatedBy = "test-user";

        // Act
        workRecordMapper.insert(id, userId, projectId, workDate, 
                               categoryHours, description, createdBy, now, updatedBy, now);

        // Assert
        Optional<WorkRecord> result = workRecordMapper.findById(id);
        assertThat(result).isPresent();
        WorkRecord record = result.get();
        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getUserId()).isEqualTo(userId);
        assertThat(record.getProjectId()).isEqualTo(projectId);
        assertThat(record.getWorkDate()).isEqualTo(workDate);
        assertThat(record.getDescription()).isEqualTo(description);
        assertThat(record.getCategoryHours()).isNotNull();
    }

    @Test
    @DisplayName("ID検索 - 存在する工数記録")
    void findById_ExistingRecord_ReturnsRecord() {
        // Arrange
        String id = "test-findbyid-record";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("4.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert(id, "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000001", LocalDate.now(), 
                               categoryHours, "Test", "system", now, "system", now);

        // Act
        Optional<WorkRecord> result = workRecordMapper.findById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("ID検索 - 存在しない工数記録")
    void findById_NonExistingRecord_ReturnsEmpty() {
        // Act
        Optional<WorkRecord> result = workRecordMapper.findById("non-existing-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ユーザーID・日付検索")
    void findByUserIdAndDate_ReturnsUserRecordsForDate() {
        // Arrange
        String userId = "demo-dev-uuid-0000-000000000003";
        LocalDate workDate = LocalDate.of(2024, 2, 1);
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("6.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("record1", userId, "project-demo-uuid-000000000001", workDate, 
                               categoryHours, "Work 1", "system", now, "system", now);
        workRecordMapper.insert("record2", userId, "project-demo-uuid-000000000002", workDate, 
                               categoryHours, "Work 2", "system", now, "system", now);
        workRecordMapper.insert("record3", "demo-pmo-uuid-0000-000000000002", "project-demo-uuid-000000000001", workDate, 
                               categoryHours, "Other work", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findByUserIdAndDate(userId, workDate);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(record -> record.getUserId().equals(userId));
        assertThat(result).allMatch(record -> record.getWorkDate().equals(workDate));
    }

    @Test
    @DisplayName("ユーザー・期間検索")
    void findByUserIdAndDateRange_ReturnsUserRecordsInRange() {
        // Arrange
        String userId = "demo-dev-uuid-0000-000000000003";
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("range1", userId, "project-demo-uuid-000000000001", LocalDate.of(2024, 3, 5), 
                               categoryHours, "Work in range", "system", now, "system", now);
        workRecordMapper.insert("range2", userId, "project-demo-uuid-000000000002", LocalDate.of(2024, 3, 15), 
                               categoryHours, "Work in range", "system", now, "system", now);
        workRecordMapper.insert("range3", userId, "project-demo-uuid-000000000001", LocalDate.of(2024, 2, 28), 
                               categoryHours, "Work before range", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findByUserIdAndDateRange(userId, startDate, endDate);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(record -> 
            !record.getWorkDate().isBefore(startDate) && 
            !record.getWorkDate().isAfter(endDate));
    }

    @Test
    @DisplayName("ユーザー全記録取得")
    void findByUser_ReturnsAllUserRecords() {
        // Arrange
        String userId = "demo-dev-uuid-0000-000000000003";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("all1", userId, "project-demo-uuid-000000000001", LocalDate.of(2024, 4, 1), 
                               categoryHours, "Work 1", "system", now, "system", now);
        workRecordMapper.insert("all2", userId, "project-demo-uuid-000000000002", LocalDate.of(2024, 4, 2), 
                               categoryHours, "Work 2", "system", now, "system", now);
        workRecordMapper.insert("all3", "demo-pmo-uuid-0000-000000000002", "project-demo-uuid-000000000001", LocalDate.of(2024, 4, 3), 
                               categoryHours, "Other work", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findByUser(userId);

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(record -> record.getUserId().equals(userId));
    }

    @Test
    @DisplayName("プロジェクト全記録取得")
    void findByProject_ReturnsAllProjectRecords() {
        // Arrange
        String projectId = "project-demo-uuid-000000000001";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("proj1", "demo-dev-uuid-0000-000000000003", projectId, LocalDate.of(2024, 5, 1), 
                               categoryHours, "Project work 1", "system", now, "system", now);
        workRecordMapper.insert("proj2", "demo-pmo-uuid-0000-000000000002", projectId, LocalDate.of(2024, 5, 2), 
                               categoryHours, "Project work 2", "system", now, "system", now);
        workRecordMapper.insert("proj3", "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000002", LocalDate.of(2024, 5, 1), 
                               categoryHours, "Other project work", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findByProject(projectId);

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(record -> record.getProjectId().equals(projectId));
    }

    @Test
    @DisplayName("期間全記録取得")
    void findByDateRange_ReturnsAllRecordsInRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 30);
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("date1", "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000001", LocalDate.of(2024, 6, 5), 
                               categoryHours, "Work in range", "system", now, "system", now);
        workRecordMapper.insert("date2", "demo-pmo-uuid-0000-000000000002", "project-demo-uuid-000000000002", LocalDate.of(2024, 6, 15), 
                               categoryHours, "Work in range", "system", now, "system", now);
        workRecordMapper.insert("date3", "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000002", LocalDate.of(2024, 7, 1), 
                               categoryHours, "Work after range", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findByDateRange(startDate, endDate);

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(record -> 
            !record.getWorkDate().isBefore(startDate) && 
            !record.getWorkDate().isAfter(endDate));
    }

    @Test
    @DisplayName("最新記録取得")
    void findLatestByUser_ReturnsLatestRecords() {
        // Arrange
        String userId = "demo-dev-uuid-0000-000000000003";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert("latest1", userId, "project-demo-uuid-000000000001", LocalDate.of(2024, 7, 1), 
                               categoryHours, "Latest 1", "system", now.minusDays(2), "system", now);
        workRecordMapper.insert("latest2", userId, "project-demo-uuid-000000000002", LocalDate.of(2024, 7, 2), 
                               categoryHours, "Latest 2", "system", now.minusDays(1), "system", now);
        workRecordMapper.insert("latest3", "demo-pmo-uuid-0000-000000000002", "project-demo-uuid-000000000001", LocalDate.of(2024, 7, 3), 
                               categoryHours, "Latest 3", "system", now, "system", now);

        // Act
        List<WorkRecord> result = workRecordMapper.findLatestByUser(userId, 2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(record -> record.getUserId().equals(userId));
        // Should be ordered by work_date DESC, created_at DESC
    }

    @Test
    @DisplayName("工数記録更新 - 正常ケース")
    void update_Success() {
        // Arrange
        String id = "test-update-record";
        CategoryHours originalCategoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        CategoryHours updatedCategoryHours = CategoryHours.of(Map.of(new CategoryCode("DEV"), new BigDecimal("6.0"), new CategoryCode("MEETING"), new BigDecimal("2.0")));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert(id, "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000001", LocalDate.now(), 
                               originalCategoryHours, "Original description", "system", now, "system", now);
        // Act
        int result = workRecordMapper.update(id, updatedCategoryHours, "Updated description", 
                                           "system", now.plusMinutes(1));

        // Assert
        assertThat(result).isEqualTo(1);
        
        Optional<WorkRecord> updatedRecord = workRecordMapper.findById(id);
        assertThat(updatedRecord).isPresent();
        assertThat(updatedRecord.get().getDescription()).isEqualTo("Updated description");
        assertThat(updatedRecord.get().getCategoryHours()).isNotNull();
    }

    @Test
    @DisplayName("説明更新 - 正常ケース")
    void updateDescription_Success() {
        // Arrange
        String id = "test-update-desc-record";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert(id, "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000001", LocalDate.now().plusDays(1), 
                               categoryHours, "Original description", "system", now, "system", now);

        // Act
        int result = workRecordMapper.updateDescription(id, "Updated description only", "system", now.plusMinutes(1));

        // Assert
        assertThat(result).isEqualTo(1);
        
        Optional<WorkRecord> updatedRecord = workRecordMapper.findById(id);
        assertThat(updatedRecord).isPresent();
        assertThat(updatedRecord.get().getDescription()).isEqualTo("Updated description only");
    }

    @Test
    @DisplayName("論理削除 - 正常ケース")
    void softDelete_Success() {
        // Arrange
        String id = "test-delete-record";
        CategoryHours categoryHours = CategoryHours.of(new CategoryCode("DEV"), new BigDecimal("8.0"));
        LocalDateTime now = LocalDateTime.now();
        
        workRecordMapper.insert(id, "demo-dev-uuid-0000-000000000003", "project-demo-uuid-000000000001", LocalDate.now().plusDays(2), 
                               categoryHours, "To be deleted", "system", now, "system", now);

        // Act
        int result = workRecordMapper.softDelete(id, now.plusMinutes(1), now.plusMinutes(1));

        // Assert
        assertThat(result).isEqualTo(1);
        
        // Should not be found after soft delete
        Optional<WorkRecord> deletedRecord = workRecordMapper.findById(id);
        assertThat(deletedRecord).isEmpty();
    }

    private void createTestUser(String userId, String username, String email) {
        if (userMapper.findById(userId).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            userMapper.insert(userId, username, email, "Test User " + username,
                             User.UserStatus.ACTIVE, null, now, now);
        }
    }

    private void createTestProject(String projectId, String projectName, String createdBy) {
        if (projectMapper.findById(projectId).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            projectMapper.insert(projectId, projectName, "Test Description", "DRAFT",
                               LocalDate.now(), LocalDate.now().plusDays(30), createdBy, now, now, null, null);
        }
    }
}