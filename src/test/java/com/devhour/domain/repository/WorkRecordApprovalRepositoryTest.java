package com.devhour.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;

@SpringBootTest
@Transactional
@Disabled
// @Sql(scripts = "/sql/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("WorkRecordApprovalRepository テスト")
class WorkRecordApprovalRepositoryTest {

    @Autowired
    private WorkRecordApprovalRepository repository;
    
    private WorkRecordApproval testApproval;
    private final String userId = "test-user-001";
    private final LocalDate workDate = LocalDate.of(2025, 1, 14);
    
    @BeforeEach
    void setUp() {
        testApproval = new WorkRecordApproval(userId, workDate);
    }

    @Nested
    @DisplayName("検索機能")
    class FindOperations {
        
        @Test
        @DisplayName("申請者IDと作業日で検索できる")
        void shouldFindByUserIdAndDate() {
            // Given
            repository.save(testApproval);
            
            // When
            Optional<WorkRecordApproval> found = repository.findByUserIdAndDate(userId, workDate);
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo(userId);
            assertThat(found.get().getWorkDate()).isEqualTo(workDate);
        }
        
        @Test
        @DisplayName("存在しない場合は空を返す")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<WorkRecordApproval> found = repository.findByUserIdAndDate("non-existent", workDate);
            
            // Then
            assertThat(found).isEmpty();
        }
        
        @Test
        @DisplayName("複数の申請者IDとステータスで検索できる")
        void shouldFindByUsersAndStatuses() {
            // Given
            WorkRecordApproval approval1 = new WorkRecordApproval("user-001", workDate);
            WorkRecordApproval approval2 = new WorkRecordApproval("user-002", workDate);
            approval2.approve("supervisor-001");
            WorkRecordApproval approval3 = new WorkRecordApproval("user-003", workDate);
            
            repository.save(approval1);
            repository.save(approval2);
            repository.save(approval3);
            
            // When
            List<WorkRecordApproval> found = repository.findByUsersAndStatuses(
                Arrays.asList("user-001", "user-002", "user-003"),
                Arrays.asList(ApprovalStatus.PENDING, ApprovalStatus.APPROVED)
            );
            
            // Then
            assertThat(found).hasSize(3);
        }
        
        @Test
        @DisplayName("期間内の承認レコードを検索できる")
        void shouldFindByUserIdAndDateRange() {
            // Given
            LocalDate startDate = LocalDate.of(2025, 1, 10);
            LocalDate endDate = LocalDate.of(2025, 1, 20);
            
            WorkRecordApproval approval1 = new WorkRecordApproval(userId, LocalDate.of(2025, 1, 12));
            WorkRecordApproval approval2 = new WorkRecordApproval(userId, LocalDate.of(2025, 1, 15));
            WorkRecordApproval approval3 = new WorkRecordApproval(userId, LocalDate.of(2025, 1, 25));
            
            repository.save(approval1);
            repository.save(approval2);
            repository.save(approval3);
            
            // When
            List<WorkRecordApproval> found = repository.findByUserIdAndDateRange(userId, startDate, endDate);
            
            // Then
            assertThat(found).hasSize(2);
            assertThat(found).extracting(WorkRecordApproval::getWorkDate)
                .containsExactlyInAnyOrder(
                    LocalDate.of(2025, 1, 12),
                    LocalDate.of(2025, 1, 15)
                );
        }
        
        @Test
        @DisplayName("承認ステータスで検索できる")
        void shouldFindByStatus() {
            // Given
            WorkRecordApproval approval1 = new WorkRecordApproval("user-001", workDate);
            WorkRecordApproval approval2 = new WorkRecordApproval("user-002", workDate.plusDays(1));
            approval2.approve("supervisor-001");
            WorkRecordApproval approval3 = new WorkRecordApproval("user-003", workDate.plusDays(2));
            approval3.reject("supervisor-001", "修正が必要");
            
            repository.save(approval1);
            repository.save(approval2);
            repository.save(approval3);
            
            // When
            List<WorkRecordApproval> pending = repository.findByStatus(ApprovalStatus.PENDING);
            List<WorkRecordApproval> approved = repository.findByStatus(ApprovalStatus.APPROVED);
            List<WorkRecordApproval> rejected = repository.findByStatus(ApprovalStatus.REJECTED);
            
            // Then
            assertThat(pending).hasSize(1);
            assertThat(approved).hasSize(1);
            assertThat(rejected).hasSize(1);
        }
    }

    @Nested
    @DisplayName("保存機能")
    class SaveOperations {
        
        @Test
        @DisplayName("新規承認レコードを保存できる")
        void shouldSaveNewApproval() {
            // When
            WorkRecordApproval saved = repository.save(testApproval);
            
            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getWorkDate()).isEqualTo(workDate);
            assertThat(saved.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }
        
        @Test
        @DisplayName("既存承認レコードを更新できる")
        void shouldUpdateExistingApproval() {
            // Given
            repository.save(testApproval);
            testApproval.approve("supervisor-001");
            
            // When
            WorkRecordApproval updated = repository.save(testApproval);
            Optional<WorkRecordApproval> found = repository.findByUserIdAndDate(userId, workDate);
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(found.get().getApproverId()).isEqualTo("supervisor-001");
        }
    }

    @Nested
    @DisplayName("削除機能")
    class DeleteOperations {
        
        @Test
        @DisplayName("申請者IDと作業日で削除できる")
        void shouldDeleteByUserIdAndDate() {
            // Given
            repository.save(testApproval);
            
            // When
            repository.delete(userId, workDate);
            Optional<WorkRecordApproval> found = repository.findByUserIdAndDate(userId, workDate);
            
            // Then
            assertThat(found).isEmpty();
        }
        
        @Test
        @DisplayName("申請者IDで全て削除できる")
        void shouldDeleteByUserId() {
            // Given
            WorkRecordApproval approval1 = new WorkRecordApproval(userId, workDate);
            WorkRecordApproval approval2 = new WorkRecordApproval(userId, workDate.plusDays(1));
            WorkRecordApproval approval3 = new WorkRecordApproval(userId, workDate.plusDays(2));
            
            repository.save(approval1);
            repository.save(approval2);
            repository.save(approval3);
            
            // When
            repository.deleteByUserId(userId);
            List<WorkRecordApproval> found = repository.findByUserId(userId);
            
            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("存在チェック")
    class ExistsOperations {
        
        @Test
        @DisplayName("存在する場合はtrueを返す")
        void shouldReturnTrueWhenExists() {
            // Given
            repository.save(testApproval);
            
            // When
            boolean exists = repository.exists(userId, workDate);
            
            // Then
            assertThat(exists).isTrue();
        }
        
        @Test
        @DisplayName("存在しない場合はfalseを返す")
        void shouldReturnFalseWhenNotExists() {
            // When
            boolean exists = repository.exists("non-existent", workDate);
            
            // Then
            assertThat(exists).isFalse();
        }
    }
}