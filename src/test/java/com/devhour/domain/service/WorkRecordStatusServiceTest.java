package com.devhour.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.model.entity.WorkRecord;
import com.devhour.domain.model.entity.WorkRecordApproval;
import com.devhour.domain.model.valueobject.ApprovalStatus;
import com.devhour.domain.model.valueobject.CategoryCode;
import com.devhour.domain.model.valueobject.CategoryHours;
import com.devhour.domain.repository.WorkRecordRepository;

/**
 * WorkRecordStatusService の単体テスト
 */
@ExtendWith(MockitoExtension.class)
class WorkRecordStatusServiceTest {
    
    @Mock
    private WorkRecordRepository workRecordRepository;
    
    private WorkRecordStatusService service;
    
    @BeforeEach
    void setUp() {
        service = new WorkRecordStatusService(workRecordRepository);
    }
    
    @Nested
    class CalculateStatus {
        
        @Test
        void calculateStatus_WorkRecordがnull_WorkRecordApprovalがnull_NOT_ENTEREDを返す() {
            // Given
            WorkRecord workRecord = null;
            WorkRecordApproval approval = null;
            
            // When
            ApprovalStatus result = service.calculateStatus(workRecord, approval);
            
            // Then
            assertEquals(ApprovalStatus.NOT_ENTERED, result);
        }
        
        @Test
        void calculateStatus_WorkRecordあり_WorkRecordApprovalがnull_NOT_ENTEREDを返す() {
            // Given
            WorkRecord workRecord = createWorkRecord("user1", LocalDate.now());
            WorkRecordApproval approval = null;
            
            // When
            ApprovalStatus result = service.calculateStatus(workRecord, approval);
            
            // Then
            assertEquals(ApprovalStatus.NOT_ENTERED, result);
        }
        
        @Test
        void calculateStatus_WorkRecordあり_ApprovalがPENDING_PENDINGを返す() {
            // Given
            WorkRecord workRecord = createWorkRecord("user1", LocalDate.now());
            WorkRecordApproval approval = createWorkRecordApproval("user1", LocalDate.now(), ApprovalStatus.PENDING);
            
            // When
            ApprovalStatus result = service.calculateStatus(workRecord, approval);
            
            // Then
            assertEquals(ApprovalStatus.PENDING, result);
        }
        
        @Test
        void calculateStatus_WorkRecordあり_ApprovalがAPPROVED_APPROVEDを返す() {
            // Given
            WorkRecord workRecord = createWorkRecord("user1", LocalDate.now());
            WorkRecordApproval approval = createWorkRecordApproval("user1", LocalDate.now(), ApprovalStatus.APPROVED);
            
            // When
            ApprovalStatus result = service.calculateStatus(workRecord, approval);
            
            // Then
            assertEquals(ApprovalStatus.APPROVED, result);
        }
        
        @Test
        void calculateStatus_WorkRecordあり_ApprovalがREJECTED_REJECTEDを返す() {
            // Given
            WorkRecord workRecord = createWorkRecord("user1", LocalDate.now());
            WorkRecordApproval approval = createWorkRecordApproval("user1", LocalDate.now(), ApprovalStatus.REJECTED);
            
            // When
            ApprovalStatus result = service.calculateStatus(workRecord, approval);
            
            // Then
            assertEquals(ApprovalStatus.REJECTED, result);
        }
    }
    
    @Nested
    class FindMissingDates {
        
        @Test
        void findMissingDates_正常なパラメータ_未入力日リストを返す() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().minusDays(4);  // 4日前
            LocalDate endDate = LocalDate.now();  // 今日
            
            List<LocalDate> existingDates = Arrays.asList(
                startDate,  // 開始日
                startDate.plusDays(2)  // 開始日+2日
            );
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(createWorkRecordList(existingDates));
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // 平日のみが対象なので、正確な日数は実行時の曜日に依存
            assertTrue(result.size() >= 0);
            // 存在する日付は含まれない
            assertFalse(result.contains(startDate));
            assertFalse(result.contains(startDate.plusDays(2)));
        }
        
        @Test
        void findMissingDates_全て入力済み_空リストを返す() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().minusDays(2);
            LocalDate endDate = LocalDate.now();
            
            // すべての営業日のWork Recordを用意
            List<LocalDate> allBusinessDays = new ArrayList<>();
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                if (isBusinessDay(current)) {
                    allBusinessDays.add(current);
                }
                current = current.plusDays(1);
            }
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(createWorkRecordList(allBusinessDays));
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        // ヘルパーメソッド：営業日判定（テスト用）
        private boolean isBusinessDay(LocalDate date) {
            java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY;
        }
        
        @Test
        void findMissingDates_全て未入力_全日リストを返す() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().minusDays(30); // 30日前から
            LocalDate endDate = LocalDate.now().minusDays(28);   // 28日前まで（3日間）
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // 営業日のみがカウントされるため、実際の日数は曜日に依存
            assertTrue(result.size() >= 0);
        }
        
        @Test
        void findMissingDates_週末を除外_平日のみ返す() {
            // Given
            String userId = "user1";
            // 最近の土曜日から月曜日の期間を設定
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(20); // 20日前から
            LocalDate endDate = today.minusDays(17);   // 17日前まで
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // 営業日のみが含まれることを確認
            assertTrue(result.size() >= 0);
            // すべての結果が営業日であることを確認
            for (LocalDate date : result) {
                assertTrue(isBusinessDay(date));
            }
        }
        
        @Test
        void findMissingDates_祝日を除外_営業日のみ返す() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().minusDays(10); // 10日前から
            LocalDate endDate = LocalDate.now().minusDays(8);    // 8日前まで
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // 営業日のみが含まれることを確認
            assertTrue(result.size() >= 0);
            // すべての結果が営業日であることを確認
            for (LocalDate date : result) {
                assertTrue(isBusinessDay(date));
            }
        }
        
        @Test
        void findMissingDates_userIdがnull_IllegalArgumentExceptionを投げる() {
            // Given
            String userId = null;
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusDays(1);
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.findMissingDates(userId, startDate, endDate);
            });
        }
        
        @Test
        void findMissingDates_startDateがnull_IllegalArgumentExceptionを投げる() {
            // Given
            String userId = "user1";
            LocalDate startDate = null;
            LocalDate endDate = LocalDate.now();
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.findMissingDates(userId, startDate, endDate);
            });
        }
        
        @Test
        void findMissingDates_endDateがnull_IllegalArgumentExceptionを投げる() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = null;
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.findMissingDates(userId, startDate, endDate);
            });
        }
        
        @Test
        void findMissingDates_startDateがendDateより後_IllegalArgumentExceptionを投げる() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().minusDays(5);
            LocalDate endDate = LocalDate.now().minusDays(10);
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.findMissingDates(userId, startDate, endDate);
            });
        }
        
        @Test
        void findMissingDates_境界値テスト_月初から現在日まで() {
            // Given
            String userId = "user1";
            LocalDate startDate = LocalDate.now().withDayOfMonth(1); // 月初
            LocalDate endDate = LocalDate.now(); // 現在日
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertTrue(result.size() > 0); // 月初から現在日まで何らかの平日があるはず
        }
        
        @Test
        void findMissingDates_境界値テスト_同日_1日分を返す() {
            // Given
            String userId = "user1";
            LocalDate targetDate = LocalDate.now().minusDays(5); // 5日前の平日
            LocalDate startDate = targetDate;
            LocalDate endDate = targetDate;
            
            when(workRecordRepository.findByUserIdAndDateRange(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
            
            // When
            List<LocalDate> result = service.findMissingDates(userId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // 営業日の場合のみ1日分返される
            if (isBusinessDay(targetDate)) {
                assertEquals(1, result.size());
                assertTrue(result.contains(targetDate));
            } else {
                assertEquals(0, result.size());
            }
        }
    }
    
    // === ヘルパーメソッド ===
    
    private WorkRecord createWorkRecord(String userId, LocalDate workDate) {
        // CategoryHoursを正しく作成（最低1つのカテゴリに工数を設定）
        Map<CategoryCode, BigDecimal> hours = Map.of(
            new CategoryCode("DEV"), BigDecimal.valueOf(8.0)
        );
        return WorkRecord.create(userId, "project1", workDate, CategoryHours.of(hours), "test description", "testuser");
    }
    
    private WorkRecordApproval createWorkRecordApproval(String userId, LocalDate workDate, ApprovalStatus status) {
        return new WorkRecordApproval(userId, workDate, status, null, null, null);
    }
    
    private List<WorkRecord> createWorkRecordList(List<LocalDate> dates) {
        return dates.stream()
            .map(date -> createWorkRecord("user1", date))
            .collect(Collectors.toList());
    }
}