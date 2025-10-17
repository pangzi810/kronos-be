package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SyncStatus値オブジェクトのテストクラス
 */
@DisplayName("SyncStatus値オブジェクト")
class JiraSyncStatusTest {
    
    @Nested
    @DisplayName("定数値のテスト")
    class ConstantValuesTest {
        
        @Test
        @DisplayName("IN_PROGRESS定数が正しく定義されている")
        void testInProgressConstant() {
            assertThat(JiraSyncStatus.IN_PROGRESS.getValue()).isEqualTo("IN_PROGRESS");
            assertThat(JiraSyncStatus.IN_PROGRESS.getDisplayName()).isEqualTo("実行中");
        }
        
        @Test
        @DisplayName("COMPLETED定数が正しく定義されている")
        void testCompletedConstant() {
            assertThat(JiraSyncStatus.COMPLETED.getValue()).isEqualTo("COMPLETED");
            assertThat(JiraSyncStatus.COMPLETED.getDisplayName()).isEqualTo("完了");
        }
        
        @Test
        @DisplayName("FAILED定数が正しく定義されている")
        void testFailedConstant() {
            assertThat(JiraSyncStatus.FAILED.getValue()).isEqualTo("FAILED");
            assertThat(JiraSyncStatus.FAILED.getDisplayName()).isEqualTo("失敗");
        }
    }
    
    @Nested
    @DisplayName("fromValue()メソッドのテスト")
    class FromValueTest {
        
        @Test
        @DisplayName("IN_PROGRESS値で正しくSyncStatusを取得できる")
        void testFromValueInProgress() {
            JiraSyncStatus result = JiraSyncStatus.fromValue("IN_PROGRESS");
            
            assertThat(result).isEqualTo(JiraSyncStatus.IN_PROGRESS);
            assertThat(result.getValue()).isEqualTo("IN_PROGRESS");
            assertThat(result.getDisplayName()).isEqualTo("実行中");
        }
        
        @Test
        @DisplayName("COMPLETED値で正しくSyncStatusを取得できる")
        void testFromValueCompleted() {
            JiraSyncStatus result = JiraSyncStatus.fromValue("COMPLETED");
            
            assertThat(result).isEqualTo(JiraSyncStatus.COMPLETED);
            assertThat(result.getValue()).isEqualTo("COMPLETED");
            assertThat(result.getDisplayName()).isEqualTo("完了");
        }
        
        @Test
        @DisplayName("FAILED値で正しくSyncStatusを取得できる")
        void testFromValueFailed() {
            JiraSyncStatus result = JiraSyncStatus.fromValue("FAILED");
            
            assertThat(result).isEqualTo(JiraSyncStatus.FAILED);
            assertThat(result.getValue()).isEqualTo("FAILED");
            assertThat(result.getDisplayName()).isEqualTo("失敗");
        }
        
        @Test
        @DisplayName("null値でIllegalArgumentExceptionがスローされる")
        void testFromValueNull() {
            assertThatThrownBy(() -> JiraSyncStatus.fromValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("同期ステータスはnullにできません");
        }
        
        @Test
        @DisplayName("不正な値でIllegalArgumentExceptionがスローされる")
        void testFromValueInvalid() {
            assertThatThrownBy(() -> JiraSyncStatus.fromValue("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な同期ステータスです: INVALID (許可された値: IN_PROGRESS, COMPLETED, FAILED)");
        }
        
        @Test
        @DisplayName("空文字でIllegalArgumentExceptionがスローされる")
        void testFromValueEmpty() {
            assertThatThrownBy(() -> JiraSyncStatus.fromValue(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("不正な同期ステータスです:  (許可された値: IN_PROGRESS, COMPLETED, FAILED)");
        }
    }
    
    @Nested
    @DisplayName("判定メソッドのテスト")
    class PredicateMethodsTest {
        
        @Test
        @DisplayName("IN_PROGRESS定数でisInProgress()がtrueを返す")
        void testIsInProgressTrue() {
            assertThat(JiraSyncStatus.IN_PROGRESS.isInProgress()).isTrue();
            assertThat(JiraSyncStatus.IN_PROGRESS.isCompleted()).isFalse();
            assertThat(JiraSyncStatus.IN_PROGRESS.isFailed()).isFalse();
            assertThat(JiraSyncStatus.IN_PROGRESS.isFinished()).isFalse();
        }
        
        @Test
        @DisplayName("COMPLETED定数でisCompleted()がtrueを返す")
        void testIsCompletedTrue() {
            assertThat(JiraSyncStatus.COMPLETED.isCompleted()).isTrue();
            assertThat(JiraSyncStatus.COMPLETED.isInProgress()).isFalse();
            assertThat(JiraSyncStatus.COMPLETED.isFailed()).isFalse();
            assertThat(JiraSyncStatus.COMPLETED.isFinished()).isTrue();
        }
        
        @Test
        @DisplayName("FAILED定数でisFailed()がtrueを返す")
        void testIsFailedTrue() {
            assertThat(JiraSyncStatus.FAILED.isFailed()).isTrue();
            assertThat(JiraSyncStatus.FAILED.isInProgress()).isFalse();
            assertThat(JiraSyncStatus.FAILED.isCompleted()).isFalse();
            assertThat(JiraSyncStatus.FAILED.isFinished()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("ステータス遷移のテスト")
    class TransitionTest {
        
        @Test
        @DisplayName("IN_PROGRESSからCOMPLETEDへの遷移が可能")
        void testTransitionFromInProgressToCompleted() {
            assertThat(JiraSyncStatus.IN_PROGRESS.canTransitionTo(JiraSyncStatus.COMPLETED)).isTrue();
        }
        
        @Test
        @DisplayName("IN_PROGRESSからFAILEDへの遷移が可能")
        void testTransitionFromInProgressToFailed() {
            assertThat(JiraSyncStatus.IN_PROGRESS.canTransitionTo(JiraSyncStatus.FAILED)).isTrue();
        }
        
        @Test
        @DisplayName("COMPLETEDから他のステータスへの遷移は不可")
        void testTransitionFromCompleted() {
            assertThat(JiraSyncStatus.COMPLETED.canTransitionTo(JiraSyncStatus.IN_PROGRESS)).isFalse();
            assertThat(JiraSyncStatus.COMPLETED.canTransitionTo(JiraSyncStatus.FAILED)).isFalse();
            assertThat(JiraSyncStatus.COMPLETED.canTransitionTo(JiraSyncStatus.COMPLETED)).isFalse();
        }
        
        @Test
        @DisplayName("FAILEDから他のステータスへの遷移は不可")
        void testTransitionFromFailed() {
            assertThat(JiraSyncStatus.FAILED.canTransitionTo(JiraSyncStatus.IN_PROGRESS)).isFalse();
            assertThat(JiraSyncStatus.FAILED.canTransitionTo(JiraSyncStatus.COMPLETED)).isFalse();
            assertThat(JiraSyncStatus.FAILED.canTransitionTo(JiraSyncStatus.FAILED)).isFalse();
        }
        
        @Test
        @DisplayName("同じステータスへの遷移は不可")
        void testTransitionToSame() {
            assertThat(JiraSyncStatus.IN_PROGRESS.canTransitionTo(JiraSyncStatus.IN_PROGRESS)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("toString()メソッドのテスト")
    class ToStringTest {
        
        @Test
        @DisplayName("各定数のtoString()が値を返す")
        void testToString() {
            assertThat(JiraSyncStatus.IN_PROGRESS.toString()).isEqualTo("IN_PROGRESS");
            assertThat(JiraSyncStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
            assertThat(JiraSyncStatus.FAILED.toString()).isEqualTo("FAILED");
        }
    }
    
    @Nested
    @DisplayName("列挙型の基本機能テスト")
    class EnumBasicTest {
        
        @Test
        @DisplayName("values()で全ての定数を取得できる")
        void testValues() {
            JiraSyncStatus[] values = JiraSyncStatus.values();
            
            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(
                JiraSyncStatus.IN_PROGRESS, 
                JiraSyncStatus.COMPLETED, 
                JiraSyncStatus.FAILED
            );
        }
        
        @Test
        @DisplayName("valueOf()で定数を取得できる")
        void testValueOf() {
            assertThat(JiraSyncStatus.valueOf("IN_PROGRESS")).isEqualTo(JiraSyncStatus.IN_PROGRESS);
            assertThat(JiraSyncStatus.valueOf("COMPLETED")).isEqualTo(JiraSyncStatus.COMPLETED);
            assertThat(JiraSyncStatus.valueOf("FAILED")).isEqualTo(JiraSyncStatus.FAILED);
        }
    }
}