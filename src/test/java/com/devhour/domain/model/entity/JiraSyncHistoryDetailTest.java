package com.devhour.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.devhour.domain.model.valueobject.DetailStatus;

/**
 * JiraSyncHistoryDetailエンティティのテストクラス
 */
@DisplayName("JiraSyncHistoryDetailエンティティ")
class JiraSyncHistoryDetailTest {

    @Nested
    @DisplayName("createSuccess()ファクトリーメソッドのテスト")
    class CreateSuccessTest {

        @Test
        @DisplayName("成功した詳細レコードを正しく作成できる")
        void testCreateSuccess() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createSuccess(
                "sync-history-123", 1, "CREATED", null);

            assertThat(detail.getId()).isNotNull();
            assertThat(detail.getSyncHistoryId()).isEqualTo("sync-history-123");
            assertThat(detail.getOperation()).isEqualTo("CREATED");
            assertThat(detail.getStatus()).isEqualTo(DetailStatus.SUCCESS);
            assertThat(detail.getResult()).isNull();
            assertThat(detail.getProcessedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("operationがnullで成功レコードを作成できる")
        void testCreateSuccessWithNullOperation() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createSuccess(
                "sync-history-123", 1, null, null);

            assertThat(detail.getOperation()).isNull();
            assertThat(detail.getStatus()).isEqualTo(DetailStatus.SUCCESS);
        }

        @Test
        @DisplayName("operationが空文字で成功レコードを作成できる")
        void testCreateSuccessWithEmptyOperation() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createSuccess(
                "sync-history-123", 1, "  ", null);

            assertThat(detail.getOperation()).isNull();
            assertThat(detail.getStatus()).isEqualTo(DetailStatus.SUCCESS);
        }

        @Test
        @DisplayName("同期履歴IDがnullで例外がスローされる")
        void testCreateSuccessWithNullSyncHistoryId() {
            assertThatThrownBy(() -> JiraSyncHistoryDetail.createSuccess(
                null, 1, "CREATED", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("同期履歴IDは必須です");
        }

        @Test
        @DisplayName("同期履歴IDが空文字で例外がスローされる")
        void testCreateSuccessWithEmptySyncHistoryId() {
            assertThatThrownBy(() -> JiraSyncHistoryDetail.createSuccess(
                "  ", 1, "CREATED", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("同期履歴IDは必須です");
        }
    }

    @Nested
    @DisplayName("createError()ファクトリーメソッドのテスト")
    class CreateErrorTest {

        @Test
        @DisplayName("エラー詳細レコードを正しく作成できる")
        void testCreateError() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
                "sync-history-123", 1, "SYNC_ERROR", "Connection failed");

            assertThat(detail.getId()).isNotNull();
            assertThat(detail.getSyncHistoryId()).isEqualTo("sync-history-123");
            assertThat(detail.getOperation()).isEqualTo("SYNC_ERROR");
            assertThat(detail.getStatus()).isEqualTo(DetailStatus.ERROR);
            assertThat(detail.getResult()).isEqualTo("Connection failed");
            assertThat(detail.getProcessedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("operationがnullでエラーレコードを作成できる")
        void testCreateErrorWithNullOperation() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
                "sync-history-123", 1, null, "Error message");

            assertThat(detail.getOperation()).isNull();
            assertThat(detail.getResult()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("エラーメッセージがnullでエラーレコードを作成できる")
        void testCreateErrorWithNullMessage() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
                "sync-history-123", 1, "ERROR", null);

            assertThat(detail.getResult()).isNull();
        }

        @Test
        @DisplayName("エラーメッセージが空文字でnullとして作成される")
        void testCreateErrorWithEmptyMessage() {
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.createError(
                "sync-history-123", 1, "ERROR", "  ");

            assertThat(detail.getResult()).isNull();
        }
    }

    @Nested
    @DisplayName("restore()ファクトリーメソッドのテスト")
    class RestoreTest {

        @Test
        @DisplayName("成功レコードを復元できる")
        void testRestoreSuccessRecord() {
            LocalDateTime processedTime = LocalDateTime.now().minusMinutes(5);
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.restore(
                "detail-id", "sync-history-123", 1, "UPDATED", DetailStatus.SUCCESS,
                null, processedTime
            );

            assertThat(detail.getId()).isEqualTo("detail-id");
            assertThat(detail.getSyncHistoryId()).isEqualTo("sync-history-123");
            assertThat(detail.getOperation()).isEqualTo("UPDATED");
            assertThat(detail.getStatus()).isEqualTo(DetailStatus.SUCCESS);
            assertThat(detail.getResult()).isNull();
            assertThat(detail.getProcessedAt()).isEqualTo(processedTime);
        }

        @Test
        @DisplayName("エラーレコードを復元できる")
        void testRestoreErrorRecord() {
            LocalDateTime processedTime = LocalDateTime.now().minusMinutes(5);
            JiraSyncHistoryDetail detail = JiraSyncHistoryDetail.restore(
                "detail-id", "sync-history-123", 1, "SYNC_ERROR", DetailStatus.ERROR,
                "Connection timeout", processedTime
            );

            assertThat(detail.getStatus()).isEqualTo(DetailStatus.ERROR);
            assertThat(detail.getResult()).isEqualTo("Connection timeout");
        }
    }
}