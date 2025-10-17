package com.devhour.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Approverエンティティのテスト")
class ApproverTest {

    @Nested
    @DisplayName("create()メソッドのテスト")
    class CreateTest {
        
        @Test
        @DisplayName("正常系：有効な値で承認者関係を作成できる")
        void testCreateWithValidValues() {
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime effectiveTo = LocalDateTime.of(2024, 12, 31, 23, 59);
            
            Approver relationship = Approver.create(
                targetEmail, approverEmail, effectiveFrom, effectiveTo
            );
            
            assertNotNull(relationship);
            assertNotNull(relationship.getId());
            assertEquals(targetEmail, relationship.getTargetEmail());
            assertEquals(approverEmail, relationship.getApproverEmail());
            assertEquals(effectiveFrom, relationship.getEffectiveFrom());
            assertEquals(effectiveTo, relationship.getEffectiveTo());
            assertNotNull(relationship.getCreatedAt());
            assertNotNull(relationship.getUpdatedAt());
        }
        
        @Test
        @DisplayName("正常系：有効終了日時なしで承認者関係を作成できる")
        void testCreateWithoutEffectiveTo() {
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            Approver relationship = Approver.create(
                targetEmail, approverEmail, effectiveFrom, null
            );
            
            assertNotNull(relationship);
            assertNull(relationship.getEffectiveTo());
        }
        
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "invalid-email", "@example.com", "user@", "user"})
        @DisplayName("異常系：無効な対象者メールアドレスで例外が発生する")
        void testCreateWithInvalidTargetEmail(String targetEmail) {
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Approver.create(targetEmail, approverEmail, effectiveFrom, null)
            );
            
            assertTrue(exception.getMessage().contains("対象者メールアドレス"));
        }
        
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "invalid-email", "@example.com", "approver@", "approver"})
        @DisplayName("異常系：無効な承認者メールアドレスで例外が発生する")
        void testCreateWithInvalidApproverEmail(String approverEmail) {
            String targetEmail = "user@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Approver.create(targetEmail, approverEmail, effectiveFrom, null)
            );
            
            assertTrue(exception.getMessage().contains("承認者メールアドレス"));
        }
        
        @Test
        @DisplayName("異常系：対象者と承認者が同一の場合、例外が発生する")
        void testCreateWithSameTargetAndApprover() {
            String email = "user@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Approver.create(email, email, effectiveFrom, null)
            );
            
            assertEquals("自己承認はできません", exception.getMessage());
        }
        
        @Test
        @DisplayName("異常系：有効開始日時がnullの場合、例外が発生する")
        void testCreateWithNullEffectiveFrom() {
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Approver.create(targetEmail, approverEmail, null, null)
            );
            
            assertEquals("有効開始日時は必須です", exception.getMessage());
        }
        
        @Test
        @DisplayName("異常系：有効終了日時が開始日時より前の場合、例外が発生する")
        void testCreateWithInvalidDateTimeRange() {
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 12, 31, 23, 59);
            LocalDateTime effectiveTo = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Approver.create(targetEmail, approverEmail, effectiveFrom, effectiveTo)
            );
            
            assertEquals("有効終了日時は開始日時以降である必要があります", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("restore()メソッドのテスト")
    class RestoreTest {
        
        @Test
        @DisplayName("正常系：すべての値を指定して復元できる")
        void testRestoreWithAllValues() {
            String id = "test-id-001";
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime effectiveTo = LocalDateTime.of(2024, 12, 31, 23, 59);
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
            
            Approver relationship = Approver.restore(
                id, targetEmail, approverEmail, effectiveFrom, effectiveTo,
                createdAt, updatedAt
            );
            
            assertEquals(id, relationship.getId());
            assertEquals(targetEmail, relationship.getTargetEmail());
            assertEquals(approverEmail, relationship.getApproverEmail());
            assertEquals(effectiveFrom, relationship.getEffectiveFrom());
            assertEquals(effectiveTo, relationship.getEffectiveTo());
            assertEquals(createdAt, relationship.getCreatedAt());
            assertEquals(updatedAt, relationship.getUpdatedAt());
        }
        
        @Test
        @DisplayName("正常系：有効終了日時なしで復元できる")
        void testRestoreWithoutEffectiveTo() {
            String id = "test-id-001";
            String targetEmail = "user@example.com";
            String approverEmail = "approver@example.com";
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime updatedAt = LocalDateTime.now();
            
            Approver relationship = Approver.restore(
                id, targetEmail, approverEmail, effectiveFrom, null,
                createdAt, updatedAt
            );
            
            assertNull(relationship.getEffectiveTo());
        }
    }
    
    @Nested
    @DisplayName("isValidForDate()メソッドのテスト")
    class IsValidForDateTest {
        
        @Test
        @DisplayName("正常系：有効期間内の日付でtrueを返す")
        void testIsValidForDateWithinRange() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            assertTrue(relationship.isValidForDate(LocalDate.of(2024, 1, 1)));
            assertTrue(relationship.isValidForDate(LocalDate.of(2024, 6, 15)));
            assertTrue(relationship.isValidForDate(LocalDate.of(2024, 12, 31)));
        }
        
        @Test
        @DisplayName("正常系：終了日時なしの場合、開始日以降でtrueを返す")
        void testIsValidForDateWithoutEndDateTime() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                null
            );
            
            assertTrue(relationship.isValidForDate(LocalDate.of(2024, 1, 1)));
            assertTrue(relationship.isValidForDate(LocalDate.of(2025, 1, 1)));
            assertTrue(relationship.isValidForDate(LocalDate.of(2030, 12, 31)));
        }
        
        @Test
        @DisplayName("異常系：開始日より前の日付でfalseを返す")
        void testIsValidForDateBeforeStart() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 6, 1, 0, 0),
                null
            );
            
            assertFalse(relationship.isValidForDate(LocalDate.of(2024, 5, 31)));
            assertFalse(relationship.isValidForDate(LocalDate.of(2024, 1, 1)));
        }
        
        @Test
        @DisplayName("異常系：終了日より後の日付でfalseを返す")
        void testIsValidForDateAfterEnd() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 30, 23, 59)
            );
            
            assertFalse(relationship.isValidForDate(LocalDate.of(2024, 7, 1)));
            assertFalse(relationship.isValidForDate(LocalDate.of(2024, 12, 31)));
        }
        
        static Stream<Arguments> provideValidDatesForParameterizedTest() {
            return Stream.of(
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59), LocalDate.of(2023, 12, 31), false),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59), LocalDate.of(2024, 1, 1), true),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59), LocalDate.of(2024, 6, 15), true),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59), LocalDate.of(2024, 12, 31), true),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59), LocalDate.of(2025, 1, 1), false),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), null, LocalDate.of(2023, 12, 31), false),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), null, LocalDate.of(2024, 1, 1), true),
                Arguments.of(LocalDateTime.of(2024, 1, 1, 0, 0), null, LocalDate.of(2025, 12, 31), true)
            );
        }
        
        @ParameterizedTest
        @MethodSource("provideValidDatesForParameterizedTest")
        @DisplayName("パラメータ化テスト：様々な日付パターンで有効性を検証")
        void testIsValidForDateWithVariousDates(LocalDateTime effectiveFrom, LocalDateTime effectiveTo, LocalDate checkDate, boolean expected) {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", effectiveFrom, effectiveTo
            );
            
            assertEquals(expected, relationship.isValidForDate(checkDate));
        }
    }
    
    @Nested
    @DisplayName("isCurrentlyValid()メソッドのテスト")
    class IsCurrentlyValidTest {
        
        @Test
        @DisplayName("正常系：現在有効な関係でtrueを返す")
        void testIsCurrentlyValidWithValidRange() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", yesterday, tomorrow
            );
            
            assertTrue(relationship.isCurrentlyValid());
        }
        
        @Test
        @DisplayName("正常系：終了日時なしで開始日時が過去の場合、trueを返す")
        void testIsCurrentlyValidWithoutEndDateTime() {
            LocalDateTime pastDateTime = LocalDateTime.now().minusMonths(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", pastDateTime, null
            );
            
            assertTrue(relationship.isCurrentlyValid());
        }
        
        @Test
        @DisplayName("異常系：開始日時が未来の場合、falseを返す")
        void testIsCurrentlyValidWithFutureStart() {
            LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", futureDateTime, null
            );
            
            assertFalse(relationship.isCurrentlyValid());
        }
        
        @Test
        @DisplayName("異常系：終了日時が過去の場合、falseを返す")
        void testIsCurrentlyValidWithPastEnd() {
            LocalDateTime pastStart = LocalDateTime.now().minusMonths(2);
            LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", pastStart, pastEnd
            );
            
            assertFalse(relationship.isCurrentlyValid());
        }
    }
    
    @Nested
    @DisplayName("hasExpired()メソッドのテスト")
    class HasExpiredTest {
        
        @Test
        @DisplayName("正常系：終了日時が過去の場合、trueを返す")
        void testHasExpiredWithPastEnd() {
            LocalDateTime pastStart = LocalDateTime.now().minusMonths(2);
            LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", pastStart, pastEnd
            );
            
            assertTrue(relationship.hasExpired());
        }
        
        @Test
        @DisplayName("正常系：終了日時が未来の場合、falseを返す")
        void testHasExpiredWithFutureEnd() {
            LocalDateTime pastStart = LocalDateTime.now().minusDays(1);
            LocalDateTime futureEnd = LocalDateTime.now().plusDays(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", pastStart, futureEnd
            );
            
            assertFalse(relationship.hasExpired());
        }
        
        @Test
        @DisplayName("正常系：終了日時なしの場合、falseを返す")
        void testHasExpiredWithoutEndDateTime() {
            LocalDateTime pastStart = LocalDateTime.now().minusMonths(1);
            
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", pastStart, null
            );
            
            assertFalse(relationship.hasExpired());
        }
    }
    
    @Nested
    @DisplayName("overlapsWithPeriod()メソッドのテスト")
    class OverlapsWithPeriodTest {
        
        @Test
        @DisplayName("正常系：期間が重複する場合、trueを返す")
        void testOverlapsWithPeriodWhenOverlapping() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 30, 23, 59)
            );
            
            // 完全に重複
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2024, 2, 1, 0, 0),
                LocalDateTime.of(2024, 5, 31, 23, 59)
            ));
            
            // 開始部分が重複
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2023, 12, 1, 0, 0),
                LocalDateTime.of(2024, 3, 31, 23, 59)
            ));
            
            // 終了部分が重複
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2024, 4, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            ));
            
            // 完全に包含
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            ));
        }
        
        @Test
        @DisplayName("正常系：期間が重複しない場合、falseを返す")
        void testOverlapsWithPeriodWhenNotOverlapping() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 6, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            // 前の期間
            assertFalse(relationship.overlapsWithPeriod(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 5, 31, 23, 59)
            ));
            
            // 後の期間
            assertFalse(relationship.overlapsWithPeriod(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
            ));
        }
        
        @Test
        @DisplayName("正常系：終了日時なしで期間が重複する場合、trueを返す")
        void testOverlapsWithPeriodWithoutEndDateTime() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 6, 1, 0, 0),
                null
            );
            
            // 開始以降の任意の期間と重複
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2024, 12, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
            ));
            
            // 開始前から開始後にかかる期間
            assertTrue(relationship.overlapsWithPeriod(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            ));
        }
        
        @Test
        @DisplayName("異常系：開始日時がnullの場合、例外が発生する")
        void testOverlapsWithPeriodWithNullFrom() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> relationship.overlapsWithPeriod(null, LocalDateTime.now())
            );
            
            assertEquals("開始日時は必須です", exception.getMessage());
        }
        
        @Test
        @DisplayName("異常系：終了日時がnullの場合、例外が発生する")
        void testOverlapsWithPeriodWithNullTo() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> relationship.overlapsWithPeriod(LocalDateTime.now(), null)
            );
            
            assertEquals("終了日時は必須です", exception.getMessage());
        }
        
        @Test
        @DisplayName("異常系：期間の開始が終了より後の場合、例外が発生する")
        void testOverlapsWithPeriodWithInvalidPeriod() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> relationship.overlapsWithPeriod(
                    LocalDateTime.of(2024, 12, 31, 23, 59),
                    LocalDateTime.of(2024, 1, 1, 0, 0)
                )
            );
            
            assertEquals("開始日時は終了日時以前である必要があります", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("equals()とhashCode()のテスト")
    class EqualsAndHashCodeTest {
        
        @Test
        @DisplayName("equals()：同じIDの場合、trueを返す")
        void testEqualsWithSameId() {
            String id = "test-id-001";
            
            Approver relationship1 = Approver.restore(
                id, "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0), null,
                LocalDateTime.now(), LocalDateTime.now()
            );
            
            Approver relationship2 = Approver.restore(
                id, "user2@example.com", "approver2@example.com",
                LocalDateTime.of(2025, 1, 1, 0, 0), null,
                LocalDateTime.now(), LocalDateTime.now()
            );
            
            assertEquals(relationship1, relationship2);
        }
        
        @Test
        @DisplayName("equals()：異なるIDの場合、falseを返す")
        void testEqualsWithDifferentId() {
            Approver relationship1 = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            Approver relationship2 = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            assertNotEquals(relationship1, relationship2);
        }
        
        @Test
        @DisplayName("equals()：同じオブジェクトの場合、trueを返す")
        void testEqualsWithSameObject() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            assertEquals(relationship, relationship);
        }
        
        @Test
        @DisplayName("equals()：nullの場合、falseを返す")
        void testEqualsWithNull() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            assertNotEquals(relationship, null);
        }
        
        @Test
        @DisplayName("equals()：異なるクラスの場合、falseを返す")
        void testEqualsWithDifferentClass() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            assertNotEquals(relationship, "not a relationship");
        }
        
        @Test
        @DisplayName("hashCode()：同じIDの場合、同じハッシュコードを返す")
        void testHashCodeWithSameId() {
            String id = "test-id-001";
            
            Approver relationship1 = Approver.restore(
                id, "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0), null,
                LocalDateTime.now(), LocalDateTime.now()
            );
            
            Approver relationship2 = Approver.restore(
                id, "user2@example.com", "approver2@example.com",
                LocalDateTime.of(2025, 1, 1, 0, 0), null,
                LocalDateTime.now(), LocalDateTime.now()
            );
            
            assertEquals(relationship1.hashCode(), relationship2.hashCode());
        }
        
        @Test
        @DisplayName("hashCode()：異なるIDの場合、通常異なるハッシュコードを返す")
        void testHashCodeWithDifferentId() {
            Approver relationship1 = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            Approver relationship2 = Approver.create(
                "user@example.com", "approver@example.com", 
                LocalDateTime.of(2024, 1, 1, 0, 0), null
            );
            
            assertNotEquals(relationship1.hashCode(), relationship2.hashCode());
        }
    }
    
    @Nested
    @DisplayName("toString()メソッドのテスト")
    class ToStringTest {
        
        @Test
        @DisplayName("toString()：適切な文字列表現を返す")
        void testToString() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            
            String result = relationship.toString();
            
            assertTrue(result.contains("Approver"));
            assertTrue(result.contains("user@example.com"));
            assertTrue(result.contains("approver@example.com"));
            assertTrue(result.contains("2024-01-01T00:00"));
            assertTrue(result.contains("2024-12-31T23:59"));
        }
        
        @Test
        @DisplayName("toString()：終了日時なしの場合も適切に表示する")
        void testToStringWithoutEffectiveTo() {
            Approver relationship = Approver.create(
                "user@example.com", "approver@example.com",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                null
            );
            
            String result = relationship.toString();
            
            assertTrue(result.contains("Approver"));
            assertTrue(result.contains("effectiveTo=null"));
        }
    }

    @Nested
    @DisplayName("メールアドレス検証のテスト")
    class EmailValidationTest {
        
        @Test
        @DisplayName("正常系：有効なメールアドレス形式")
        void testValidEmailFormats() {
            String[] validEmails = {
                "user@example.com",
                "test.user@domain.co.jp",
                "user+tag@example.org",
                "user_123@test-domain.com",
                "a@b.co"
            };
            
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            for (String email : validEmails) {
                assertDoesNotThrow(() -> {
                    Approver.create(email, "approver@example.com", effectiveFrom, null);
                }, "Valid email should not throw exception: " + email);
            }
        }
        
        @Test
        @DisplayName("異常系：無効なメールアドレス形式")
        void testInvalidEmailFormats() {
            String[] invalidEmails = {
                "plaintext",
                "@missinglocal.com",
                "missing-at-sign.com",
                "missing.domain@",
                "spaces in@email.com",
                "double@@domain.com",
                "user@domain",
                "user@.domain.com",
                "user@domain..com"
            };
            
            LocalDateTime effectiveFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
            String validEmail = "approver@example.com";
            
            for (String invalidEmail : invalidEmails) {
                assertThrows(IllegalArgumentException.class, () -> {
                    Approver.create(invalidEmail, validEmail, effectiveFrom, null);
                }, "Invalid email should throw exception: " + invalidEmail);
            }
        }
    }
}