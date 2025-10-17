package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

/**
 * DetailStatus値オブジェクトのテストクラス
 */
@DisplayName("DetailStatus値オブジェクト")
class DetailStatusTest {
    
    @Nested
    @DisplayName("定数値のテスト")
    class ConstantValuesTest {
        
        @Test
        @DisplayName("SUCCESS定数が正しく定義されている")
        void testSuccessConstant() {
            assertThat(DetailStatus.SUCCESS.getValue()).isEqualTo("SUCCESS");
            assertThat(DetailStatus.SUCCESS.getDisplayName()).isEqualTo("成功");
        }
        
        @Test
        @DisplayName("ERROR定数が正しく定義されている")
        void testErrorConstant() {
            assertThat(DetailStatus.ERROR.getValue()).isEqualTo("ERROR");
            assertThat(DetailStatus.ERROR.getDisplayName()).isEqualTo("エラー");
        }
    }
    
    @Nested
    @DisplayName("fromValue()メソッドのテスト")
    class FromValueTest {
        
        @Test
        @DisplayName("SUCCESS値で正しくDetailStatusを取得できる")
        void testFromValueSuccess() {
            DetailStatus result = DetailStatus.fromValue("SUCCESS");
            
            assertThat(result).isEqualTo(DetailStatus.SUCCESS);
            assertThat(result.getValue()).isEqualTo("SUCCESS");
            assertThat(result.getDisplayName()).isEqualTo("成功");
        }
        
        @Test
        @DisplayName("ERROR値で正しくDetailStatusを取得できる")
        void testFromValueError() {
            DetailStatus result = DetailStatus.fromValue("ERROR");
            
            assertThat(result).isEqualTo(DetailStatus.ERROR);
            assertThat(result.getValue()).isEqualTo("ERROR");
            assertThat(result.getDisplayName()).isEqualTo("エラー");
        }
        
        @Test
        @DisplayName("null値でIllegalArgumentExceptionがスローされる")
        void testFromValueNull() {
            assertThatThrownBy(() -> DetailStatus.fromValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("詳細ステータスはnullにできません");
        }
        
        @Test
        @DisplayName("不正な値でIllegalArgumentExceptionがスローされる")
        void testFromValueInvalid() {
            assertThatThrownBy(() -> DetailStatus.fromValue("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な詳細ステータスです: INVALID (許可された値: SUCCESS, ERROR)");
        }
        
        @Test
        @DisplayName("空文字でIllegalArgumentExceptionがスローされる")
        void testFromValueEmpty() {
            assertThatThrownBy(() -> DetailStatus.fromValue(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な詳細ステータスです:  (許可された値: SUCCESS, ERROR)");
        }
        
        @Test
        @DisplayName("小文字の値でIllegalArgumentExceptionがスローされる")
        void testFromValueLowercase() {
            assertThatThrownBy(() -> DetailStatus.fromValue("success"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な詳細ステータスです: success (許可された値: SUCCESS, ERROR)");
        }
    }
    
    @Nested
    @DisplayName("判定メソッドのテスト")
    class PredicateMethodsTest {
        
        @Test
        @DisplayName("SUCCESS定数でisSuccess()がtrueを返す")
        void testIsSuccessTrue() {
            assertThat(DetailStatus.SUCCESS.isSuccess()).isTrue();
            assertThat(DetailStatus.SUCCESS.isError()).isFalse();
        }
        
        @Test
        @DisplayName("ERROR定数でisError()がtrueを返す")
        void testIsErrorTrue() {
            assertThat(DetailStatus.ERROR.isError()).isTrue();
            assertThat(DetailStatus.ERROR.isSuccess()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("toString()メソッドのテスト")
    class ToStringTest {
        
        @Test
        @DisplayName("SUCCESS定数のtoString()が値を返す")
        void testToStringSuccess() {
            assertThat(DetailStatus.SUCCESS.toString()).isEqualTo("SUCCESS");
        }
        
        @Test
        @DisplayName("ERROR定数のtoString()が値を返す")
        void testToStringError() {
            assertThat(DetailStatus.ERROR.toString()).isEqualTo("ERROR");
        }
    }
    
    @Nested
    @DisplayName("列挙型の基本機能テスト")
    class EnumBasicTest {
        
        @Test
        @DisplayName("values()で全ての定数を取得できる")
        void testValues() {
            DetailStatus[] values = DetailStatus.values();
            
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(DetailStatus.SUCCESS, DetailStatus.ERROR);
        }
        
        @Test
        @DisplayName("valueOf()で定数を取得できる")
        void testValueOf() {
            assertThat(DetailStatus.valueOf("SUCCESS")).isEqualTo(DetailStatus.SUCCESS);
            assertThat(DetailStatus.valueOf("ERROR")).isEqualTo(DetailStatus.ERROR);
        }
        
        @Test
        @DisplayName("ordinal()で順序を取得できる")
        void testOrdinal() {
            assertThat(DetailStatus.SUCCESS.ordinal()).isEqualTo(0);
            assertThat(DetailStatus.ERROR.ordinal()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("name()で定数名を取得できる")
        void testName() {
            assertThat(DetailStatus.SUCCESS.name()).isEqualTo("SUCCESS");
            assertThat(DetailStatus.ERROR.name()).isEqualTo("ERROR");
        }
    }
    
    @Nested
    @DisplayName("JsonValueアノテーションのテスト")
    class JsonValueTest {
        
        @Test
        @DisplayName("JsonValueアノテーションが正しく動作する")
        void testJsonValue() {
            // JsonValueアノテーションによりgetValue()が使用される
            assertThat(DetailStatus.SUCCESS.getValue()).isEqualTo("SUCCESS");
            assertThat(DetailStatus.ERROR.getValue()).isEqualTo("ERROR");
        }
    }
}