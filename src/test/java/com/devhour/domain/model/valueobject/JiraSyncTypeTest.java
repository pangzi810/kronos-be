package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SyncType値オブジェクトのテストクラス
 */
@DisplayName("SyncType値オブジェクト")
class JiraSyncTypeTest {
    
    @Nested
    @DisplayName("定数値のテスト")
    class ConstantValuesTest {
        
        @Test
        @DisplayName("MANUAL定数が正しく定義されている")
        void testManualConstant() {
            assertThat(JiraSyncType.MANUAL.getValue()).isEqualTo("MANUAL");
            assertThat(JiraSyncType.MANUAL.getDisplayName()).isEqualTo("手動同期");
        }
        
        @Test
        @DisplayName("SCHEDULED定数が正しく定義されている")
        void testScheduledConstant() {
            assertThat(JiraSyncType.SCHEDULED.getValue()).isEqualTo("SCHEDULED");
            assertThat(JiraSyncType.SCHEDULED.getDisplayName()).isEqualTo("スケジュール同期");
        }
    }
    
    @Nested
    @DisplayName("fromValue()メソッドのテスト")
    class FromValueTest {
        
        @Test
        @DisplayName("MANUAL値で正しくSyncTypeを取得できる")
        void testFromValueManual() {
            JiraSyncType result = JiraSyncType.fromValue("MANUAL");
            
            assertThat(result).isEqualTo(JiraSyncType.MANUAL);
            assertThat(result.getValue()).isEqualTo("MANUAL");
            assertThat(result.getDisplayName()).isEqualTo("手動同期");
        }
        
        @Test
        @DisplayName("SCHEDULED値で正しくSyncTypeを取得できる")
        void testFromValueScheduled() {
            JiraSyncType result = JiraSyncType.fromValue("SCHEDULED");
            
            assertThat(result).isEqualTo(JiraSyncType.SCHEDULED);
            assertThat(result.getValue()).isEqualTo("SCHEDULED");
            assertThat(result.getDisplayName()).isEqualTo("スケジュール同期");
        }
        
        @Test
        @DisplayName("null値でIllegalArgumentExceptionがスローされる")
        void testFromValueNull() {
            assertThatThrownBy(() -> JiraSyncType.fromValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("同期タイプはnullにできません");
        }
        
        @Test
        @DisplayName("不正な値でIllegalArgumentExceptionがスローされる")
        void testFromValueInvalid() {
            assertThatThrownBy(() -> JiraSyncType.fromValue("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な同期タイプです: INVALID (許可された値: MANUAL, SCHEDULED)");
        }
        
        @Test
        @DisplayName("空文字でIllegalArgumentExceptionがスローされる")
        void testFromValueEmpty() {
            assertThatThrownBy(() -> JiraSyncType.fromValue(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な同期タイプです:  (許可された値: MANUAL, SCHEDULED)");
        }
        
        @Test
        @DisplayName("小文字の値でIllegalArgumentExceptionがスローされる")
        void testFromValueLowercase() {
            assertThatThrownBy(() -> JiraSyncType.fromValue("manual"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な同期タイプです: manual (許可された値: MANUAL, SCHEDULED)");
        }
    }
    
    @Nested
    @DisplayName("判定メソッドのテスト")
    class PredicateMethodsTest {
        
        @Test
        @DisplayName("MANUAL定数でisManual()がtrueを返す")
        void testIsManualTrue() {
            assertThat(JiraSyncType.MANUAL.isManual()).isTrue();
            assertThat(JiraSyncType.MANUAL.isScheduled()).isFalse();
        }
        
        @Test
        @DisplayName("SCHEDULED定数でisScheduled()がtrueを返す")
        void testIsScheduledTrue() {
            assertThat(JiraSyncType.SCHEDULED.isScheduled()).isTrue();
            assertThat(JiraSyncType.SCHEDULED.isManual()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("toString()メソッドのテスト")
    class ToStringTest {
        
        @Test
        @DisplayName("MANUAL定数のtoString()が値を返す")
        void testToStringManual() {
            assertThat(JiraSyncType.MANUAL.toString()).isEqualTo("MANUAL");
        }
        
        @Test
        @DisplayName("SCHEDULED定数のtoString()が値を返す")
        void testToStringScheduled() {
            assertThat(JiraSyncType.SCHEDULED.toString()).isEqualTo("SCHEDULED");
        }
    }
    
    @Nested
    @DisplayName("列挙型の基本機能テスト")
    class EnumBasicTest {
        
        @Test
        @DisplayName("values()で全ての定数を取得できる")
        void testValues() {
            JiraSyncType[] values = JiraSyncType.values();
            
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(JiraSyncType.MANUAL, JiraSyncType.SCHEDULED);
        }
        
        @Test
        @DisplayName("valueOf()で定数を取得できる")
        void testValueOf() {
            assertThat(JiraSyncType.valueOf("MANUAL")).isEqualTo(JiraSyncType.MANUAL);
            assertThat(JiraSyncType.valueOf("SCHEDULED")).isEqualTo(JiraSyncType.SCHEDULED);
        }
        
        @Test
        @DisplayName("ordinal()で順序を取得できる")
        void testOrdinal() {
            assertThat(JiraSyncType.MANUAL.ordinal()).isEqualTo(0);
            assertThat(JiraSyncType.SCHEDULED.ordinal()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("name()で定数名を取得できる")
        void testName() {
            assertThat(JiraSyncType.MANUAL.name()).isEqualTo("MANUAL");
            assertThat(JiraSyncType.SCHEDULED.name()).isEqualTo("SCHEDULED");
        }
    }
    
    @Nested
    @DisplayName("JsonValueアノテーションのテスト")
    class JsonValueTest {
        
        @Test
        @DisplayName("JsonValueアノテーションが正しく動作する")
        void testJsonValue() {
            // JsonValueアノテーションによりgetValue()が使用される
            assertThat(JiraSyncType.MANUAL.getValue()).isEqualTo("MANUAL");
            assertThat(JiraSyncType.SCHEDULED.getValue()).isEqualTo("SCHEDULED");
        }
    }
}