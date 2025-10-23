package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.infrastructure.mapper.WorkRecordMapper;

@ExtendWith(MockitoExtension.class)
class WorkRecordRepositoryImplTest {

    @Mock
    private WorkRecordMapper workRecordMapper;

    private WorkRecordRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new WorkRecordRepositoryImpl(workRecordMapper);
    }

    @Test
    void testFindById_Found() {
        String workRecordId = "record123";
        WorkRecord expectedRecord = createTestWorkRecord(workRecordId, "user1", "project1");
        
        when(workRecordMapper.findById(workRecordId)).thenReturn(Optional.of(expectedRecord));
        
        Optional<WorkRecord> result = repository.findById(workRecordId);
        
        assertTrue(result.isPresent());
        assertEquals(expectedRecord, result.get());
        verify(workRecordMapper).findById(workRecordId);
    }

    @Test
    void testFindById_NotFound() {
        String workRecordId = "nonexistent";
        
        when(workRecordMapper.findById(workRecordId)).thenReturn(Optional.empty());
        
        Optional<WorkRecord> result = repository.findById(workRecordId);
        
        assertFalse(result.isPresent());
        verify(workRecordMapper).findById(workRecordId);
    }

    @Test
    void testFindByUserAndDate() {
        String userId = "user123";
        LocalDate workDate = LocalDate.of(2024, 11, 15);
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", userId, "project1"),
            createTestWorkRecord("record2", userId, "project2")
        );
        
        when(workRecordMapper.findByUserIdAndDate(userId, workDate)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findByUserIdAndDate(userId, workDate);
        
        assertEquals(2, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findByUserIdAndDate(userId, workDate);
    }

    @Test
    void testFindByUserAndDate_Empty() {
        String userId = "user123";
        LocalDate workDate = LocalDate.of(2024, 11, 15);
        
        when(workRecordMapper.findByUserIdAndDate(userId, workDate)).thenReturn(Arrays.asList());
        
        List<WorkRecord> result = repository.findByUserIdAndDate(userId, workDate);
        
        assertTrue(result.isEmpty());
        verify(workRecordMapper).findByUserIdAndDate(userId, workDate);
    }

    @Test
    void testFindByUser() {
        String userId = "user123";
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", userId, "project1"),
            createTestWorkRecord("record2", userId, "project2"),
            createTestWorkRecord("record3", userId, "project3")
        );
        
        when(workRecordMapper.findByUser(userId)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findByUser(userId);
        
        assertEquals(3, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findByUser(userId);
    }

    @Test
    void testFindByUser_Empty() {
        String userId = "userWithNoRecords";
        
        when(workRecordMapper.findByUser(userId)).thenReturn(Arrays.asList());
        
        List<WorkRecord> result = repository.findByUser(userId);
        
        assertTrue(result.isEmpty());
        verify(workRecordMapper).findByUser(userId);
    }

    @Test
    void testFindByProject() {
        String projectId = "project123";
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", "user1", projectId),
            createTestWorkRecord("record2", "user2", projectId),
            createTestWorkRecord("record3", "user3", projectId)
        );
        
        when(workRecordMapper.findByProject(projectId)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findByProject(projectId);
        
        assertEquals(3, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findByProject(projectId);
    }

    @Test
    void testFindByProject_Empty() {
        String projectId = "projectWithNoRecords";
        
        when(workRecordMapper.findByProject(projectId)).thenReturn(Arrays.asList());
        
        List<WorkRecord> result = repository.findByProject(projectId);
        
        assertTrue(result.isEmpty());
        verify(workRecordMapper).findByProject(projectId);
    }

    @Test
    void testFindByDateRange() {
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", "user1", "project1"),
            createTestWorkRecord("record2", "user2", "project2")
        );
        
        when(workRecordMapper.findByDateRange(startDate, endDate)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findByDateRange(startDate, endDate);
        
        assertEquals(2, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findByDateRange(startDate, endDate);
    }

    @Test
    void testFindByUserAndDateRange() {
        String userId = "user123";
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", userId, "project1"),
            createTestWorkRecord("record2", userId, "project2")
        );
        
        when(workRecordMapper.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        
        assertEquals(2, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    void testFindByUserAndDateRange_Empty() {
        String userId = "user123";
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        
        when(workRecordMapper.findByUserIdAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList());
        
        List<WorkRecord> result = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        
        assertTrue(result.isEmpty());
        verify(workRecordMapper).findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Test
    void testFindLatestByUser() {
        String userId = "user123";
        int limit = 5;
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", userId, "project1"),
            createTestWorkRecord("record2", userId, "project2"),
            createTestWorkRecord("record3", userId, "project3")
        );
        
        when(workRecordMapper.findLatestByUser(userId, limit)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findLatestByUser(userId, limit);
        
        assertEquals(3, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findLatestByUser(userId, limit);
    }

    @Test
    void testFindLatestByUser_LimitOne() {
        String userId = "user123";
        int limit = 1;
        List<WorkRecord> expectedRecords = Arrays.asList(
            createTestWorkRecord("record1", userId, "project1")
        );
        
        when(workRecordMapper.findLatestByUser(userId, limit)).thenReturn(expectedRecords);
        
        List<WorkRecord> result = repository.findLatestByUser(userId, limit);
        
        assertEquals(1, result.size());
        assertEquals(expectedRecords, result);
        verify(workRecordMapper).findLatestByUser(userId, limit);
    }

    @Test
    void testSave_NewWorkRecord() {
        WorkRecord workRecord = createTestWorkRecord("newRecord", "user1", "project1");
        
        when(workRecordMapper.findById("newRecord")).thenReturn(Optional.empty());
        
        WorkRecord result = repository.save(workRecord);
        
        assertEquals(workRecord, result);
        verify(workRecordMapper).findById("newRecord");
        verify(workRecordMapper).insert(
            eq("newRecord"),
            eq("user1"),
            eq("project1"),
            any(LocalDate.class),
            any(CategoryHours.class),
            eq("Test work description"),
            eq("createdBy"),
            any(LocalDateTime.class),
            eq("updatedBy"),
            any(LocalDateTime.class)
        );
    }

    @Test
    @org.junit.jupiter.api.Disabled("Needs proper mapper stubbing")
    void testSave_ExistingWorkRecord() {
        WorkRecord workRecord = createTestWorkRecord("existingRecord", "user1", "project1");
        
        when(workRecordMapper.findById("existingRecord")).thenReturn(Optional.of(workRecord));
        
        WorkRecord result = repository.save(workRecord);
        
        assertEquals(workRecord, result);
        verify(workRecordMapper).findById("existingRecord");
        verify(workRecordMapper).update(
            eq("existingRecord"),
            any(CategoryHours.class),
            eq("Test work description"),
            eq("updatedBy"),
            any(LocalDateTime.class)
        );
    }

    @Test
    void testSaveAll() {
        List<WorkRecord> workRecords = Arrays.asList(
            createTestWorkRecord("record1", "user1", "project1"),
            createTestWorkRecord("record2", "user1", "project2")
        );
        
        when(workRecordMapper.findById("record1")).thenReturn(Optional.empty());
        when(workRecordMapper.findById("record2")).thenReturn(Optional.of(workRecords.get(1)));
        
        List<WorkRecord> result = repository.saveAll(workRecords);
        
        assertEquals(workRecords, result);
        verify(workRecordMapper).findById("record1");
        verify(workRecordMapper).findById("record2");
    }

    @Test
    void testSaveAll_Empty() {
        List<WorkRecord> workRecords = Arrays.asList();
        
        List<WorkRecord> result = repository.saveAll(workRecords);
        
        assertTrue(result.isEmpty());
        verifyNoInteractions(workRecordMapper);
    }

    @Test
    void testDeleteById() {
        String workRecordId = "record123";
        
        repository.deleteById(workRecordId);
        
        verify(workRecordMapper).softDelete(
            eq(workRecordId),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        );
    }

    private WorkRecord createTestWorkRecord(String id, String userId, String projectId) {
        Map<CategoryCode, BigDecimal> hours = new HashMap<>();
        hours.put(CategoryCode.of("DEV"), new BigDecimal("4.0"));
        hours.put(CategoryCode.of("MEETING"), new BigDecimal("2.0"));
        hours.put(CategoryCode.of("BRD"), new BigDecimal("2.0"));
        
        return WorkRecord.restore(
            id,
            userId,
            projectId,
            LocalDate.now(),
            CategoryHours.of(hours),
            "Test work description",
            "createdBy",
            LocalDateTime.now(),
            "updatedBy",
            LocalDateTime.now()
        );
    }
}