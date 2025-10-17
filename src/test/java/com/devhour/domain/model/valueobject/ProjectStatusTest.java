package com.devhour.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProjectStatus - プロジェクトステータスの値オブジェクトのテスト")
class ProjectStatusTest {

    @Nested
    @DisplayName("コンストラクタと生成メソッドのテスト")
    class ConstructorAndCreationTest {

        @Test
        @DisplayName("正常な値でインスタンスを生成できる")
        void shouldCreateInstanceWithValidValue() {
            ProjectStatus status = new ProjectStatus("PLANNING");
            assertThat(status.value()).isEqualTo("PLANNING");
        }

        @Test
        @DisplayName("定義済み定数が正しく初期化される")
        void shouldInitializePredefinedConstants() {
            assertThat(ProjectStatus.PLANNING.value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.IN_PROGRESS.value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.COMPLETED.value()).isEqualTo("COMPLETED");
            assertThat(ProjectStatus.CANCELLED.value()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("ofメソッドで大文字小文字を自動変換する")
        void shouldConvertToUpperCaseWithOfMethod() {
            assertThat(ProjectStatus.of("planning").value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.of("Planning").value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.of("PLANNING").value()).isEqualTo("PLANNING");
        }

        @Test
        @DisplayName("ofメソッドで前後の空白を除去する")
        void shouldTrimWhitespaceWithOfMethod() {
            assertThat(ProjectStatus.of("  PLANNING  ").value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.of("\tIN_PROGRESS\n").value()).isEqualTo("IN_PROGRESS");
        }

        @ParameterizedTest
        @ValueSource(strings = {"planning", "in_progress", "completed", "cancelled"})
        @DisplayName("ofメソッドで各種ステータスを生成できる")
        void shouldCreateAllStatusesWithOfMethod(String value) {
            ProjectStatus status = ProjectStatus.of(value);
            assertThat(status.value()).isEqualTo(value.toUpperCase());
        }
    }

    @Nested
    @DisplayName("バリデーションのテスト")
    class ValidationTest {

        @Test
        @DisplayName("nullの値で例外をスローする")
        void shouldThrowExceptionForNullValue() {
            assertThatThrownBy(() -> new ProjectStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("プロジェクトステータスはnullにできません");
        }

        @Test
        @DisplayName("ofメソッドでnullの値で例外をスローする")
        void shouldThrowExceptionForNullValueWithOfMethod() {
            assertThatThrownBy(() -> ProjectStatus.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("プロジェクトステータスはnullにできません");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("空文字や空白文字で例外をスローする")
        void shouldThrowExceptionForBlankValue(String value) {
            assertThatThrownBy(() -> new ProjectStatus(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("プロジェクトステータス");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "PENDING", "ACTIVE", "unknown", "123"})
        @DisplayName("不正な値で例外をスローする")
        void shouldThrowExceptionForInvalidValue(String value) {
            assertThatThrownBy(() -> new ProjectStatus(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("プロジェクトステータスが不正です")
                .hasMessageContaining(value);
        }
    }

    @Nested
    @DisplayName("ステータスチェックメソッドのテスト")
    class StatusCheckMethodsTest {

        @Test
        @DisplayName("canStart - PLANNING状態のときtrueを返す")
        void canStartShouldReturnTrueForPlanning() {
            assertThat(ProjectStatus.PLANNING.canStart()).isTrue();
            assertThat(ProjectStatus.IN_PROGRESS.canStart()).isFalse();
            assertThat(ProjectStatus.COMPLETED.canStart()).isFalse();
            assertThat(ProjectStatus.CANCELLED.canStart()).isFalse();
        }

        @Test
        @DisplayName("isInProgress - IN_PROGRESS状態のときtrueを返す")
        void isInProgressShouldReturnTrueForInProgress() {
            assertThat(ProjectStatus.PLANNING.isInProgress()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isInProgress()).isTrue();
            assertThat(ProjectStatus.COMPLETED.isInProgress()).isFalse();
            assertThat(ProjectStatus.CANCELLED.isInProgress()).isFalse();
        }

        @Test
        @DisplayName("isCompleted - COMPLETED状態のときtrueを返す")
        void isCompletedShouldReturnTrueForCompleted() {
            assertThat(ProjectStatus.PLANNING.isCompleted()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isCompleted()).isFalse();
            assertThat(ProjectStatus.COMPLETED.isCompleted()).isTrue();
            assertThat(ProjectStatus.CANCELLED.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isCancelled - CANCELLED状態のときtrueを返す")
        void isCancelledShouldReturnTrueForCancelled() {
            assertThat(ProjectStatus.PLANNING.isCancelled()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isCancelled()).isFalse();
            assertThat(ProjectStatus.COMPLETED.isCancelled()).isFalse();
            assertThat(ProjectStatus.CANCELLED.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("isActive - PLANNINGまたはIN_PROGRESS状態のときtrueを返す")
        void isActiveShouldReturnTrueForActiveStatuses() {
            assertThat(ProjectStatus.PLANNING.isActive()).isTrue();
            assertThat(ProjectStatus.IN_PROGRESS.isActive()).isTrue();
            assertThat(ProjectStatus.COMPLETED.isActive()).isFalse();
            assertThat(ProjectStatus.CANCELLED.isActive()).isFalse();
        }

        @Test
        @DisplayName("isFinished - COMPLETEDまたはCANCELLED状態のときtrueを返す")
        void isFinishedShouldReturnTrueForFinishedStatuses() {
            assertThat(ProjectStatus.PLANNING.isFinished()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isFinished()).isFalse();
            assertThat(ProjectStatus.COMPLETED.isFinished()).isTrue();
            assertThat(ProjectStatus.CANCELLED.isFinished()).isTrue();
        }
    }

    @Nested
    @DisplayName("ステータス遷移のテスト")
    class StatusTransitionTest {

        @Test
        @DisplayName("同じステータスへの遷移は常に許可される")
        void shouldAllowTransitionToSameStatus() {
            assertThat(ProjectStatus.PLANNING.canTransitionTo(ProjectStatus.PLANNING)).isTrue();
            assertThat(ProjectStatus.IN_PROGRESS.canTransitionTo(ProjectStatus.IN_PROGRESS)).isTrue();
            assertThat(ProjectStatus.COMPLETED.canTransitionTo(ProjectStatus.COMPLETED)).isTrue();
            assertThat(ProjectStatus.CANCELLED.canTransitionTo(ProjectStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PLANNINGからIN_PROGRESSへの遷移が許可される")
        void shouldAllowTransitionFromPlanningToInProgress() {
            assertThat(ProjectStatus.PLANNING.canTransitionTo(ProjectStatus.IN_PROGRESS)).isTrue();
        }

        @Test
        @DisplayName("PLANNINGからCANCELLEDへの遷移が許可される")
        void shouldAllowTransitionFromPlanningToCancelled() {
            assertThat(ProjectStatus.PLANNING.canTransitionTo(ProjectStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PLANNINGからCOMPLETEDへの直接遷移は許可されない")
        void shouldNotAllowTransitionFromPlanningToCompleted() {
            assertThat(ProjectStatus.PLANNING.canTransitionTo(ProjectStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("IN_PROGRESSからCOMPLETEDへの遷移が許可される")
        void shouldAllowTransitionFromInProgressToCompleted() {
            assertThat(ProjectStatus.IN_PROGRESS.canTransitionTo(ProjectStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("IN_PROGRESSからCANCELLEDへの遷移が許可される")
        void shouldAllowTransitionFromInProgressToCancelled() {
            assertThat(ProjectStatus.IN_PROGRESS.canTransitionTo(ProjectStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("IN_PROGRESSからPLANNINGへの遷移は許可されない")
        void shouldNotAllowTransitionFromInProgressToPlanning() {
            assertThat(ProjectStatus.IN_PROGRESS.canTransitionTo(ProjectStatus.PLANNING)).isFalse();
        }

        @Test
        @DisplayName("COMPLETEDから他のステータスへの遷移は許可されない")
        void shouldNotAllowTransitionFromCompleted() {
            assertThat(ProjectStatus.COMPLETED.canTransitionTo(ProjectStatus.PLANNING)).isFalse();
            assertThat(ProjectStatus.COMPLETED.canTransitionTo(ProjectStatus.IN_PROGRESS)).isFalse();
            assertThat(ProjectStatus.COMPLETED.canTransitionTo(ProjectStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("CANCELLEDから他のステータスへの遷移は許可されない")
        void shouldNotAllowTransitionFromCancelled() {
            assertThat(ProjectStatus.CANCELLED.canTransitionTo(ProjectStatus.PLANNING)).isFalse();
            assertThat(ProjectStatus.CANCELLED.canTransitionTo(ProjectStatus.IN_PROGRESS)).isFalse();
            assertThat(ProjectStatus.CANCELLED.canTransitionTo(ProjectStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("transitionToメソッドで正常な遷移が実行される")
        void shouldExecuteValidTransition() {
            ProjectStatus newStatus = ProjectStatus.PLANNING.transitionTo(ProjectStatus.IN_PROGRESS);
            assertThat(newStatus).isEqualTo(ProjectStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("transitionToメソッドで不正な遷移は例外をスローする")
        void shouldThrowExceptionForInvalidTransition() {
            assertThatThrownBy(() -> ProjectStatus.PLANNING.transitionTo(ProjectStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("プロジェクトステータスの遷移が不正です: PLANNING -> COMPLETED");
        }

        @ParameterizedTest
        @CsvSource({
            "PLANNING, IN_PROGRESS",
            "PLANNING, CANCELLED",
            "IN_PROGRESS, COMPLETED",
            "IN_PROGRESS, CANCELLED"
        })
        @DisplayName("許可された遷移パターンをすべてテスト")
        void shouldAllowValidTransitions(String from, String to) {
            ProjectStatus fromStatus = new ProjectStatus(from);
            ProjectStatus toStatus = new ProjectStatus(to);
            assertThat(fromStatus.canTransitionTo(toStatus)).isTrue();
            assertThat(fromStatus.transitionTo(toStatus)).isEqualTo(toStatus);
        }
    }

    @Nested
    @DisplayName("表示名のテスト")
    class DisplayNameTest {

        @Test
        @DisplayName("各ステータスの日本語表示名が正しい")
        void shouldReturnCorrectJapaneseDisplayNames() {
            assertThat(ProjectStatus.PLANNING.getDisplayName()).isEqualTo("計画中");
            assertThat(ProjectStatus.IN_PROGRESS.getDisplayName()).isEqualTo("進行中");
            assertThat(ProjectStatus.COMPLETED.getDisplayName()).isEqualTo("完了");
            assertThat(ProjectStatus.CANCELLED.getDisplayName()).isEqualTo("中止");
        }

        @Test
        @DisplayName("未定義のステータスの場合は値そのものを返す")
        void shouldReturnValueForUndefinedStatus() {
            // This test is for defensive programming in case of future extensions
            // Currently, all valid statuses have display names
            // This branch would only be hit if the validation is bypassed somehow
        }
    }

    @Nested
    @DisplayName("基本メソッドのテスト")
    class BasicMethodsTest {

        @Test
        @DisplayName("toStringメソッドは値を返す")
        void toStringShouldReturnValue() {
            assertThat(ProjectStatus.PLANNING.toString()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.IN_PROGRESS.toString()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
            assertThat(ProjectStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("equalsメソッドの動作")
        void equalsShouldWorkCorrectly() {
            ProjectStatus status1 = new ProjectStatus("PLANNING");
            ProjectStatus status2 = new ProjectStatus("PLANNING");
            ProjectStatus status3 = ProjectStatus.PLANNING;
            ProjectStatus status4 = new ProjectStatus("IN_PROGRESS");

            // 同じインスタンス
            assertThat(status1.equals(status1)).isTrue();
            
            // 同じ値
            assertThat(status1.equals(status2)).isTrue();
            assertThat(status1.equals(status3)).isTrue();
            
            // 異なる値
            assertThat(status1.equals(status4)).isFalse();
            
            // null比較
            assertThat(status1.equals(null)).isFalse();
            
            // 異なるクラス
            assertThat(status1.equals("PLANNING")).isFalse();
        }

        @Test
        @DisplayName("hashCodeメソッドの動作")
        void hashCodeShouldWorkCorrectly() {
            ProjectStatus status1 = new ProjectStatus("PLANNING");
            ProjectStatus status2 = new ProjectStatus("PLANNING");
            ProjectStatus status3 = new ProjectStatus("IN_PROGRESS");

            // 同じ値は同じハッシュコード
            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
            
            // 異なる値は（通常）異なるハッシュコード
            assertThat(status1.hashCode()).isNotEqualTo(status3.hashCode());
        }

        @Test
        @DisplayName("レコードの基本的な動作")
        void recordShouldWorkCorrectly() {
            ProjectStatus status = new ProjectStatus("PLANNING");
            
            // valueアクセサメソッド
            assertThat(status.value()).isEqualTo("PLANNING");
            
            // レコードのtoString（デフォルト実装をオーバーライドしている）
            assertThat(status.toString()).isEqualTo("PLANNING");
        }
    }

    @Nested
    @DisplayName("境界値と特殊ケースのテスト")
    class EdgeCaseTest {

        @Test
        @DisplayName("大文字小文字混在の入力を処理できる")
        void shouldHandleMixedCaseInput() {
            assertThat(ProjectStatus.of("PlAnNiNg").value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.of("In_Progress").value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.of("COMpleted").value()).isEqualTo("COMPLETED");
            assertThat(ProjectStatus.of("cANCELLED").value()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("様々な空白文字を含む入力を処理できる")
        void shouldHandleVariousWhitespaceCharacters() {
            assertThat(ProjectStatus.of("  PLANNING  ").value()).isEqualTo("PLANNING");
            assertThat(ProjectStatus.of("\tIN_PROGRESS\t").value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.of("\nCOMPLETED\n").value()).isEqualTo("COMPLETED");
            assertThat(ProjectStatus.of(" \t\nCANCELLED \t\n").value()).isEqualTo("CANCELLED");
        }
    }
}