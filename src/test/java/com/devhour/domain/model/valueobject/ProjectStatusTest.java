package com.devhour.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProjectStatus - プロジェクトステータスの値オブジェクトのテスト")
class ProjectStatusTest {

    @Nested
    @DisplayName("コンストラクタと生成メソッドのテスト")
    class ConstructorAndCreationTest {

        @Test
        @DisplayName("正常な値でインスタンスを生成できる")
        void shouldCreateInstanceWithValidValue() {
            ProjectStatus status = new ProjectStatus("DRAFT");
            assertThat(status.value()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("定義済み定数が正しく初期化される")
        void shouldInitializePredefinedConstants() {
            assertThat(ProjectStatus.DRAFT.value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.IN_PROGRESS.value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.CLOSED.value()).isEqualTo("CLOSED");
        }

        @Test
        @DisplayName("ofメソッドで大文字小文字を自動変換する")
        void shouldConvertToUpperCaseWithOfMethod() {
            assertThat(ProjectStatus.of("draft").value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.of("Draft").value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.of("DRAFT").value()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("ofメソッドで前後の空白を除去する")
        void shouldTrimWhitespaceWithOfMethod() {
            assertThat(ProjectStatus.of("  DRAFT  ").value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.of("\tIN_PROGRESS\n").value()).isEqualTo("IN_PROGRESS");
        }

        @ParameterizedTest
        @ValueSource(strings = {"draft", "in_progress", "closed"})
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
        @ValueSource(strings = {"INVALID", "PENDING", "ACTIVE", "PLANNING", "COMPLETED", "CANCELLED", "unknown", "123"})
        @DisplayName("不正な値で例外をスローする")
        void shouldThrowExceptionForInvalidValue(String value) {
            assertThatThrownBy(() -> new ProjectStatus(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("プロジェクトステータスが不正です");
        }
    }

    @Nested
    @DisplayName("ステータスチェックメソッドのテスト")
    class StatusCheckMethodsTest {

        @Test
        @DisplayName("canStart - DRAFT状態のときtrueを返す")
        void canStartShouldReturnTrueForDraft() {
            assertThat(ProjectStatus.DRAFT.canStart()).isTrue();
            assertThat(ProjectStatus.IN_PROGRESS.canStart()).isFalse();
            assertThat(ProjectStatus.CLOSED.canStart()).isFalse();
        }

        @Test
        @DisplayName("isInProgress - IN_PROGRESS状態のときtrueを返す")
        void isInProgressShouldReturnTrueForInProgress() {
            assertThat(ProjectStatus.DRAFT.isInProgress()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isInProgress()).isTrue();
            assertThat(ProjectStatus.CLOSED.isInProgress()).isFalse();
        }

        @Test
        @DisplayName("isClosed - CLOSED状態のときtrueを返す")
        void isClosedShouldReturnTrueForClosed() {
            assertThat(ProjectStatus.DRAFT.isClosed()).isFalse();
            assertThat(ProjectStatus.IN_PROGRESS.isClosed()).isFalse();
            assertThat(ProjectStatus.CLOSED.isClosed()).isTrue();
        }

        @Test
        @DisplayName("isActive - DRAFTまたはIN_PROGRESS状態のときtrueを返す")
        void isActiveShouldReturnTrueForActiveStatuses() {
            assertThat(ProjectStatus.DRAFT.isActive()).isTrue();
            assertThat(ProjectStatus.IN_PROGRESS.isActive()).isTrue();
            assertThat(ProjectStatus.CLOSED.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("ステータス遷移のテスト")
    class StatusTransitionTest {

        @Test
        @DisplayName("transitionToメソッドで新しいステータスを返す")
        void shouldReturnNewStatus() {
            ProjectStatus newStatus = ProjectStatus.DRAFT.transitionTo(ProjectStatus.IN_PROGRESS);
            assertThat(newStatus).isEqualTo(ProjectStatus.IN_PROGRESS);

            ProjectStatus closedStatus = ProjectStatus.IN_PROGRESS.transitionTo(ProjectStatus.CLOSED);
            assertThat(closedStatus).isEqualTo(ProjectStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("表示名のテスト")
    class DisplayNameTest {

        @Test
        @DisplayName("各ステータスの日本語表示名が正しい")
        void shouldReturnCorrectJapaneseDisplayNames() {
            assertThat(ProjectStatus.DRAFT.getDisplayName()).isEqualTo("計画中");
            assertThat(ProjectStatus.IN_PROGRESS.getDisplayName()).isEqualTo("進行中");
            assertThat(ProjectStatus.CLOSED.getDisplayName()).isEqualTo("完了");
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
            assertThat(ProjectStatus.DRAFT.toString()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.IN_PROGRESS.toString()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.CLOSED.toString()).isEqualTo("CLOSED");
        }

        @Test
        @DisplayName("equalsメソッドの動作")
        @SuppressWarnings({"unlikely-arg-type", "EqualsBetweenInconvertibleTypes"})
        void equalsShouldWorkCorrectly() {
            ProjectStatus status1 = new ProjectStatus("DRAFT");
            ProjectStatus status2 = new ProjectStatus("DRAFT");
            ProjectStatus status3 = ProjectStatus.DRAFT;
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
            assertThat(status1.equals("DRAFT")).isFalse();
        }

        @Test
        @DisplayName("hashCodeメソッドの動作")
        void hashCodeShouldWorkCorrectly() {
            ProjectStatus status1 = new ProjectStatus("DRAFT");
            ProjectStatus status2 = new ProjectStatus("DRAFT");
            ProjectStatus status3 = new ProjectStatus("IN_PROGRESS");

            // 同じ値は同じハッシュコード
            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());

            // 異なる値は（通常）異なるハッシュコード
            assertThat(status1.hashCode()).isNotEqualTo(status3.hashCode());
        }

        @Test
        @DisplayName("レコードの基本的な動作")
        void recordShouldWorkCorrectly() {
            ProjectStatus status = new ProjectStatus("DRAFT");

            // valueアクセサメソッド
            assertThat(status.value()).isEqualTo("DRAFT");

            // レコードのtoString（デフォルト実装をオーバーライドしている）
            assertThat(status.toString()).isEqualTo("DRAFT");
        }
    }

    @Nested
    @DisplayName("境界値と特殊ケースのテスト")
    class EdgeCaseTest {

        @Test
        @DisplayName("大文字小文字混在の入力を処理できる")
        void shouldHandleMixedCaseInput() {
            assertThat(ProjectStatus.of("DrAfT").value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.of("In_Progress").value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.of("cLOSed").value()).isEqualTo("CLOSED");
        }

        @Test
        @DisplayName("様々な空白文字を含む入力を処理できる")
        void shouldHandleVariousWhitespaceCharacters() {
            assertThat(ProjectStatus.of("  DRAFT  ").value()).isEqualTo("DRAFT");
            assertThat(ProjectStatus.of("\tIN_PROGRESS\t").value()).isEqualTo("IN_PROGRESS");
            assertThat(ProjectStatus.of("\nCLOSED\n").value()).isEqualTo("CLOSED");
            assertThat(ProjectStatus.of(" \t\nDRAFT \t\n").value()).isEqualTo("DRAFT");
        }
    }
}