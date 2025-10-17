package com.devhour.domain.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * 承認権限検証ドメインサービス
 * 
 * 承認権限の存在を検証し、適切な例外を投げる責務を持つ
 */
@Service
public class ApprovalAuthorityValidationService {

    private final UserRepository userRepository;
    private final ApproverRepository approverRepository;

    public ApprovalAuthorityValidationService(UserRepository userRepository,
                                           ApproverRepository approverRepository) {
        this.userRepository = userRepository;
        this.approverRepository = approverRepository;
    }

    /**
     * 承認権限を検証する（今日の日付）
     * 
     * @param approverId 承認者ID
     * @param targetId 対象者ID
     * @return 承認権限がある場合true、ユーザーが存在しない場合や権限がない場合false
     */
    public boolean validateAuthority(String approverId, String targetId) {
        return validateAuthorityForDate(approverId, targetId, LocalDate.now());
    }

    /**
     * 指定日付での承認権限を検証する
     * 
     * @param approverId 承認者ID
     * @param targetId 対象者ID
     * @param date 対象日付
     * @return 承認権限がある場合true、ユーザーが存在しない場合や権限がない場合false
     */
    public boolean validateAuthorityForDate(String approverId, String targetId, LocalDate date) {
        // 1. ユーザー存在チェック
        Optional<User> approverUser = userRepository.findById(approverId);
        Optional<User> targetUser = userRepository.findById(targetId);

        // ユーザーが存在しない場合はfalseを返す
        if (approverUser.isEmpty() || targetUser.isEmpty()) {
            return false;
        }

        // 2. 指定日付での承認権限チェック
        String approverEmail = approverUser.get().getEmail();
        String targetEmail = targetUser.get().getEmail();

        return approverRepository.isValidApprover(targetEmail, approverEmail, date);
    }

    /**
     * 承認権限をチェックして結果を返す（今日の日付）
     * ユーザーが存在しない場合はfalseを返す
     * 
     * @param approverId 承認者ID
     * @param targetId 対象者ID
     * @return 承認権限がある場合true、ユーザーが存在しない場合や権限がない場合false
     */
    public boolean hasAuthority(String approverId, String targetId) {
        return hasAuthorityForDate(approverId, targetId, LocalDate.now());
    }

    /**
     * 指定日付での承認権限をチェックして結果を返す
     * ユーザーが存在しない場合はfalseを返す
     * 
     * @param approverId 承認者ID
     * @param targetId 対象者ID
     * @param date 対象日付
     * @return 承認権限がある場合true、ユーザーが存在しない場合や権限がない場合false
     */
    public boolean hasAuthorityForDate(String approverId, String targetId, LocalDate date) {
        // 1. ユーザー存在チェック
        Optional<User> approverUser = userRepository.findById(approverId);
        Optional<User> targetUser = userRepository.findById(targetId);

        // ユーザーが存在しない場合はfalseを返す
        if (approverUser.isEmpty() || targetUser.isEmpty()) {
            return false;
        }

        // 2. 指定日付での承認権限チェック
        String approverEmail = approverUser.get().getEmail();
        String targetEmail = targetUser.get().getEmail();

        return approverRepository.isValidApprover(targetEmail, approverEmail, date);
    }
}