package com.devhour.domain.model.entity;

import com.devhour.domain.model.valueobject.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkRecordApproval エンティティテスト")
class WorkRecordApprovalTest {

    private WorkRecordApproval approval;
    private final String userId = "user-001";
    private final LocalDate workDate = LocalDate.of(2025, 1, 14);
    private final String approverId = "supervisor-001";

    @BeforeEach
    void setUp() {
        approval = new WorkRecordApproval(userId, workDate);
    }

    @Nested
    @DisplayName("初期化")
    class Initialization {
        
        @Test
        @DisplayName("新規作成時はPENDINGステータスで初期化される")
        void shouldInitializeWithPendingStatus() {
            assertThat(approval.getUserId()).isEqualTo(userId);
            assertThat(approval.getWorkDate()).isEqualTo(workDate);
            assertThat(approval.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
            assertThat(approval.getApproverId()).isNull();
            assertThat(approval.getApprovedAt()).isNull();
            assertThat(approval.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("既存データから復元できる")
        void shouldCreateFromExistingData() {
            LocalDateTime approvedAt = LocalDateTime.now();
            WorkRecordApproval existing = new WorkRecordApproval(
                userId,
                workDate,
                ApprovalStatus.APPROVED,
                approverId,
                approvedAt,
                null
            );

            assertThat(existing.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(existing.getApproverId()).isEqualTo(approverId);
            assertThat(existing.getApprovedAt()).isEqualTo(approvedAt);
        }
    }

    @Nested
    @DisplayName("承認処理")
    class Approve {
        
        @Test
        @DisplayName("PENDINGステータスから承認できる")
        void shouldApproveFromPendingStatus() {
            approval.approve(approverId);

            assertThat(approval.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(approval.getApproverId()).isEqualTo(approverId);
            assertThat(approval.getApprovedAt()).isNotNull();
            assertThat(approval.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("REJECTEDステータスから承認できる")
        void shouldApproveFromRejectedStatus() {
            approval.reject(approverId, "修正が必要");
            approval.approve(approverId);

            assertThat(approval.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(approval.getRejectionReason()).isNull(); // 却下理由はクリアされる
        }

        @Test
        @DisplayName("既に承認済みの場合は例外が発生する")
        void shouldThrowExceptionWhenAlreadyApproved() {
            approval.approve(approverId);

            assertThatThrownBy(() -> approval.approve(approverId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("既に承認済みです");
        }

        @Test
        @DisplayName("承認者IDがnullの場合は例外が発生する")
        void shouldThrowExceptionWhenApproverIdIsNull() {
            assertThatThrownBy(() -> approval.approve(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("承認者IDは必須です");
        }
    }

    @Nested
    @DisplayName("差し戻し処理")
    class Reject {
        
        @Test
        @DisplayName("PENDINGステータスから差し戻しできる")
        void shouldRejectFromPendingStatus() {
            String reason = "カテゴリ分類が不適切";
            approval.reject(approverId, reason);

            assertThat(approval.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
            assertThat(approval.getApproverId()).isEqualTo(approverId);
            assertThat(approval.getApprovedAt()).isNotNull();
            assertThat(approval.getRejectionReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("承認済みステータスから差し戻しできない")
        void shouldNotRejectFromApprovedStatus() {
            approval.approve(approverId);

            assertThatThrownBy(() -> approval.reject(approverId, "理由"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("承認済みのため差し戻しできません");
        }

        @Test
        @DisplayName("却下理由がnullの場合は例外が発生する")
        void shouldThrowExceptionWhenReasonIsNull() {
            assertThatThrownBy(() -> approval.reject(approverId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("却下理由は必須です");
        }

        @Test
        @DisplayName("却下理由が空文字の場合は例外が発生する")
        void shouldThrowExceptionWhenReasonIsEmpty() {
            assertThatThrownBy(() -> approval.reject(approverId, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("却下理由は必須です");
        }
    }

    @Nested
    @DisplayName("編集可能性チェック")
    class EditableCheck {
        
        @Test
        @DisplayName("PENDINGステータスの場合は編集可能")
        void shouldBeEditableWhenPending() {
            assertThat(approval.isEditable()).isTrue();
        }

        @Test
        @DisplayName("REJECTEDステータスの場合は編集可能")
        void shouldBeEditableWhenRejected() {
            approval.reject(approverId, "修正してください");
            assertThat(approval.isEditable()).isTrue();
        }

        @Test
        @DisplayName("APPROVEDステータスの場合は編集不可")
        void shouldNotBeEditableWhenApproved() {
            approval.approve(approverId);
            assertThat(approval.isEditable()).isFalse();
        }
    }

    @Nested
    @DisplayName("ステータス確認")
    class StatusCheck {
        
        @Test
        @DisplayName("承認済みかどうかを確認できる")
        void shouldCheckIfApproved() {
            assertThat(approval.isApproved()).isFalse();
            
            approval.approve(approverId);
            assertThat(approval.isApproved()).isTrue();
        }

        @Test
        @DisplayName("差し戻し状態かどうかを確認できる")
        void shouldCheckIfRejected() {
            assertThat(approval.isRejected()).isFalse();
            
            approval.reject(approverId, "修正が必要");
            assertThat(approval.isRejected()).isTrue();
        }

        @Test
        @DisplayName("承認待ち状態かどうかを確認できる")
        void shouldCheckIfPending() {
            assertThat(approval.isPending()).isTrue();
            
            approval.approve(approverId);
            assertThat(approval.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("同一性と等価性")
    class EqualsAndHashCode {
        
        @Test
        @DisplayName("同じuser_idとwork_dateの場合は等価")
        void shouldBeEqualWithSameUserIdAndWorkDate() {
            WorkRecordApproval other = new WorkRecordApproval(userId, workDate);
            
            assertThat(approval).isEqualTo(other);
            assertThat(approval.hashCode()).isEqualTo(other.hashCode());
        }

        @Test
        @DisplayName("異なるuser_idの場合は等価でない")
        void shouldNotBeEqualWithDifferentUserId() {
            WorkRecordApproval other = new WorkRecordApproval("user-002", workDate);
            
            assertThat(approval).isNotEqualTo(other);
        }

        @Test
        @DisplayName("異なるwork_dateの場合は等価でない")
        void shouldNotBeEqualWithDifferentWorkDate() {
            WorkRecordApproval other = new WorkRecordApproval(userId, workDate.plusDays(1));
            
            assertThat(approval).isNotEqualTo(other);
        }
    }
}