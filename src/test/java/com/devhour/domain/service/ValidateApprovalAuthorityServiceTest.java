package com.devhour.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.exception.NoApprovalAuthorityException;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * ValidateApprovalAuthorityServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("承認権限検証ドメインサービス")
class ValidateApprovalAuthorityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApproverRepository approverRepository;

    @InjectMocks
    private ApprovalAuthorityValidationService validateApprovalAuthorityService;

    private String approverId;
    private String targetId;
    private User approverUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        approverId = "approver123";
        targetId = "target456";

        approverUser = User.restore(
            approverId,
            "承認者",
            "approver@example.com",
            "Manager",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        targetUser = User.restore(
            targetId,
            "対象者",
            "target@example.com",
            "Developer",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("有効な承認権限が存在する場合、例外を投げない")
    void validateAuthority_ValidAuthority_DoesNotThrowException() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class)))
            .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> validateApprovalAuthorityService.validateAuthority(approverId, targetId));

        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
        verify(approverRepository).isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class));
    }

    @Test
    @DisplayName("承認者が存在しない場合、falseを返す")
    void validateAuthority_ApproverNotFound_ReturnsFalse() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.empty());

        // When
        boolean result = validateApprovalAuthorityService.validateAuthority(approverId, targetId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(approverId);
    }

    @Test
    @DisplayName("対象者が存在しない場合、falseを返す")
    void validateAuthority_TargetNotFound_ReturnsFalse() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        // When
        boolean result = validateApprovalAuthorityService.validateAuthority(approverId, targetId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
    }

    @Test
    @DisplayName("承認権限が存在しない場合、falseを返す")
    void validateAuthority_NoAuthority_ReturnsFalse() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class)))
            .thenReturn(false);

        // When
        boolean result = validateApprovalAuthorityService.validateAuthority(approverId, targetId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
        verify(approverRepository).isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class));
    }

    @Test
    @DisplayName("NoApprovalAuthorityExceptionのプロパティが正しく設定される")
    void noApprovalAuthorityException_PropertiesAreSet() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class)))
            .thenReturn(false);

        // When & Then
        try {
            validateApprovalAuthorityService.validateAuthority(approverId, targetId);
        } catch (NoApprovalAuthorityException exception) {
            assertThat(exception.getApproverId()).isEqualTo(approverId);
            assertThat(exception.getTargetId()).isEqualTo(targetId);
        }
    }

    @Test
    @DisplayName("指定日付で有効な承認権限が存在する場合、例外を投げない")
    void validateAuthorityForDate_ValidAuthority_DoesNotThrowException() {
        // Given
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), eq(specificDate)))
            .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> validateApprovalAuthorityService.validateAuthorityForDate(approverId, targetId, specificDate));

        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
        verify(approverRepository).isValidApprover(eq("target@example.com"), eq("approver@example.com"), eq(specificDate));
    }

    @Test
    @DisplayName("指定日付で承認権限が存在しない場合、falseを返す")
    void validateAuthorityForDate_NoAuthority_ReturnsFalse() {
        // Given
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), eq(specificDate)))
            .thenReturn(false);

        // When
        boolean result = validateApprovalAuthorityService.validateAuthorityForDate(approverId, targetId, specificDate);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
        verify(approverRepository).isValidApprover(eq("target@example.com"), eq("approver@example.com"), eq(specificDate));
    }

    @Test
    @DisplayName("validateAuthorityメソッドが今日の日付でvalidateAuthorityForDateを呼び出す")
    void validateAuthority_CallsValidateAuthorityForDateWithToday() {
        // Given
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(approverRepository.isValidApprover(eq("target@example.com"), eq("approver@example.com"), any(LocalDate.class)))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> validateApprovalAuthorityService.validateAuthority(approverId, targetId));

        // Then
        verify(userRepository).findById(approverId);
        verify(userRepository).findById(targetId);
        verify(approverRepository).isValidApprover(eq("target@example.com"), eq("approver@example.com"), eq(LocalDate.now()));
    }
}