package com.devhour.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DisplayOrderの包括的なテストケース
 * 
 * 全てのビジネスルールと境界値を検証し、
 * 表示順の値オブジェクトとしての正確性を保証する
 */
@DisplayName("表示順値オブジェクトのテスト")
class DisplayOrderTest {

    @Test
    @DisplayName("正常値でのDisplayOrder生成が成功する")
    void shouldCreateDisplayOrderWithValidValue() {
        // Given & When
        DisplayOrder displayOrder = new DisplayOrder(1);
        
        // Then
        assertEquals(1, displayOrder.value());
        assertEquals(1, displayOrder.intValue());
    }
    
    @Test
    @DisplayName("of(Integer)メソッドで正常値のDisplayOrder生成が成功する")
    void shouldCreateDisplayOrderUsingOfIntegerMethod() {
        // Given & When
        DisplayOrder displayOrder = DisplayOrder.of(500);
        
        // Then
        assertEquals(500, displayOrder.value());
        assertEquals(500, displayOrder.intValue());
    }
    
    @Test
    @DisplayName("of(int)メソッドで正常値のDisplayOrder生成が成功する")
    void shouldCreateDisplayOrderUsingOfIntMethod() {
        // Given & When
        DisplayOrder displayOrder = DisplayOrder.of(999);
        
        // Then
        assertEquals(999, displayOrder.value());
        assertEquals(999, displayOrder.intValue());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 500, 998, 999})
    @DisplayName("有効な範囲内の値でDisplayOrder生成が成功する")
    void shouldCreateDisplayOrderWithValidRangeValues(int value) {
        // Given & When
        DisplayOrder displayOrder = DisplayOrder.of(value);
        
        // Then
        assertEquals(value, displayOrder.value());
        assertEquals(value, displayOrder.intValue());
    }
    
    @Test
    @DisplayName("null値でDisplayOrder生成時に例外がスローされる")
    void shouldThrowExceptionWhenValueIsNull() {
        // Given & When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new DisplayOrder(null)
        );
        assertEquals("表示順はnullにできません", exception.getMessage());
    }
    
    @Test
    @DisplayName("of(null)でDisplayOrder生成時に例外がスローされる")
    void shouldThrowExceptionWhenOfMethodCalledWithNull() {
        // Given & When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DisplayOrder.of((Integer) null)
        );
        assertEquals("表示順はnullにできません", exception.getMessage());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("最小値未満でDisplayOrder生成時に例外がスローされる")
    void shouldThrowExceptionWhenValueBelowMinimum(int invalidValue) {
        // Given & When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DisplayOrder.of(invalidValue)
        );
        assertTrue(exception.getMessage().contains("表示順は1-999の範囲で入力してください"));
        assertTrue(exception.getMessage().contains("入力値: " + invalidValue));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1000, 1001, 9999})
    @DisplayName("最大値超過でDisplayOrder生成時に例外がスローされる")
    void shouldThrowExceptionWhenValueAboveMaximum(int invalidValue) {
        // Given & When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DisplayOrder.of(invalidValue)
        );
        assertTrue(exception.getMessage().contains("表示順は1-999の範囲で入力してください"));
        assertTrue(exception.getMessage().contains("入力値: " + invalidValue));
    }
    
    @Test
    @DisplayName("next()メソッドで次の表示順が正しく生成される")
    void shouldGenerateNextDisplayOrder() {
        // Given
        DisplayOrder displayOrder = DisplayOrder.of(5);
        
        // When
        DisplayOrder next = displayOrder.next();
        
        // Then
        assertEquals(6, next.value());
        assertEquals(5, displayOrder.value()); // 元のオブジェクトは不変
    }
    
    @Test
    @DisplayName("最大値でnext()呼び出し時に例外がスローされる")
    void shouldThrowExceptionWhenNextCalledOnMaxValue() {
        // Given
        DisplayOrder maxDisplayOrder = DisplayOrder.of(999);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            maxDisplayOrder::next
        );
        assertTrue(exception.getMessage().contains("表示順が最大値(999)に達している"));
        assertTrue(exception.getMessage().contains("次の値を生成できません"));
    }
    
    @Test
    @DisplayName("previous()メソッドで前の表示順が正しく生成される")
    void shouldGeneratePreviousDisplayOrder() {
        // Given
        DisplayOrder displayOrder = DisplayOrder.of(5);
        
        // When
        DisplayOrder previous = displayOrder.previous();
        
        // Then
        assertEquals(4, previous.value());
        assertEquals(5, displayOrder.value()); // 元のオブジェクトは不変
    }
    
    @Test
    @DisplayName("最小値でprevious()呼び出し時に例外がスローされる")
    void shouldThrowExceptionWhenPreviousCalledOnMinValue() {
        // Given
        DisplayOrder minDisplayOrder = DisplayOrder.of(1);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            minDisplayOrder::previous
        );
        assertTrue(exception.getMessage().contains("表示順が最小値(1)に達している"));
        assertTrue(exception.getMessage().contains("前の値を生成できません"));
    }
    
    @Test
    @DisplayName("compareTo()メソッドで正しく比較される")
    void shouldCompareCorrectlyUsingCompareTo() {
        // Given
        DisplayOrder smaller = DisplayOrder.of(1);
        DisplayOrder larger = DisplayOrder.of(5);
        DisplayOrder equal = DisplayOrder.of(1);
        
        // When & Then
        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(equal));
    }
    
    @Test
    @DisplayName("isBefore()メソッドで正しく判定される")
    void shouldDetermineBeforeCorrectly() {
        // Given
        DisplayOrder earlier = DisplayOrder.of(1);
        DisplayOrder later = DisplayOrder.of(5);
        DisplayOrder same = DisplayOrder.of(1);
        
        // When & Then
        assertTrue(earlier.isBefore(later));
        assertFalse(later.isBefore(earlier));
        assertFalse(earlier.isBefore(same));
    }
    
    @Test
    @DisplayName("isAfter()メソッドで正しく判定される")
    void shouldDetermineAfterCorrectly() {
        // Given
        DisplayOrder earlier = DisplayOrder.of(1);
        DisplayOrder later = DisplayOrder.of(5);
        DisplayOrder same = DisplayOrder.of(5);
        
        // When & Then
        assertTrue(later.isAfter(earlier));
        assertFalse(earlier.isAfter(later));
        assertFalse(later.isAfter(same));
    }
    
    @Test
    @DisplayName("toString()で正しい文字列表現が返される")
    void shouldReturnCorrectStringRepresentation() {
        // Given
        DisplayOrder displayOrder = DisplayOrder.of(123);
        
        // When
        String result = displayOrder.toString();
        
        // Then
        assertEquals("123", result);
    }
    
    @Test
    @DisplayName("equals()で正しく等価性が判定される")
    void shouldDetermineEqualityCorrectly() {
        // Given
        DisplayOrder order1 = DisplayOrder.of(5);
        DisplayOrder order2 = DisplayOrder.of(5);
        DisplayOrder order3 = DisplayOrder.of(10);
        
        // When & Then
        assertEquals(order1, order2);
        assertNotEquals(order1, order3);
        assertEquals(order1, order1); // 自己参照
        assertNotEquals(order1, null);
        assertNotEquals(order1, "not a DisplayOrder");
    }
    
    @Test
    @DisplayName("hashCode()で一致する値は同じハッシュコードを返す")
    void shouldReturnSameHashCodeForEqualValues() {
        // Given
        DisplayOrder order1 = DisplayOrder.of(7);
        DisplayOrder order2 = DisplayOrder.of(7);
        DisplayOrder order3 = DisplayOrder.of(8);
        
        // When & Then
        assertEquals(order1.hashCode(), order2.hashCode());
        assertNotEquals(order1.hashCode(), order3.hashCode());
    }
    
    @Test
    @DisplayName("境界値での動作確認 - 最小値")
    void shouldHandleBoundaryValueMinimum() {
        // Given & When
        DisplayOrder minOrder = DisplayOrder.of(1);
        
        // Then
        assertEquals(1, minOrder.value());
        assertEquals(2, minOrder.next().value());
        
        // previous()は例外をスローすべき
        assertThrows(IllegalArgumentException.class, minOrder::previous);
    }
    
    @Test
    @DisplayName("境界値での動作確認 - 最大値")
    void shouldHandleBoundaryValueMaximum() {
        // Given & When
        DisplayOrder maxOrder = DisplayOrder.of(999);
        
        // Then
        assertEquals(999, maxOrder.value());
        assertEquals(998, maxOrder.previous().value());
        
        // next()は例外をスローすべき
        assertThrows(IllegalArgumentException.class, maxOrder::next);
    }
    
    @Test
    @DisplayName("最大値-1での動作確認")
    void shouldHandleNearMaximumValue() {
        // Given & When
        DisplayOrder nearMax = DisplayOrder.of(998);
        
        // Then
        assertEquals(998, nearMax.value());
        assertEquals(999, nearMax.next().value());
        assertEquals(997, nearMax.previous().value());
    }
    
    @Test
    @DisplayName("最小値+1での動作確認")
    void shouldHandleNearMinimumValue() {
        // Given & When
        DisplayOrder nearMin = DisplayOrder.of(2);
        
        // Then
        assertEquals(2, nearMin.value());
        assertEquals(3, nearMin.next().value());
        assertEquals(1, nearMin.previous().value());
    }
    
    @Test
    @DisplayName("不変性の確認 - nextやpreviousを呼んでも元オブジェクトは変更されない")
    void shouldMaintainImmutability() {
        // Given
        DisplayOrder original = DisplayOrder.of(5);
        int originalValue = original.value();
        
        // When
        DisplayOrder next = original.next();
        DisplayOrder previous = original.previous();
        
        // Then
        assertEquals(originalValue, original.value()); // 元の値は変更されない
        assertEquals(originalValue + 1, next.value());
        assertEquals(originalValue - 1, previous.value());
        
        // 複数回の操作でも不変性を保つ
        original.next().next().previous();
        assertEquals(originalValue, original.value());
    }
}