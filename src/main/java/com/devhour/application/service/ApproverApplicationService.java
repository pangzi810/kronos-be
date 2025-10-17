package com.devhour.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.service.ListApproverDomainService;

/**
 * 承認者管理アプリケーションサービス
 * 
 * コントローラー層からの承認者関係参照要求を処理するアプリケーションサービス。
 * ユーザーIDからUserエンティティを取得し、ListApproverServiceドメインサービスに
 * 委譲して承認者関係の検索を行う。
 * 
 * 主な責務:
 * - 承認対象者リストの取得（指定承認者の承認対象となるユーザー一覧）
 * - 承認者リストの取得（指定対象者を承認するユーザー一覧）
 * - ユーザーIDからUserエンティティへの変換とバリデーション
 * - トランザクション境界の管理（読み取り専用）
 * - エンティティ存在チェックと例外処理
 * 
 * アーキテクチャ:
 * - アプリケーションサービスとして、ユースケースの調整を担当
 * - ドメインサービス(ListApproverService)への委譲により、関心の分離を実現
 * - このサービスはユーザーIDの解決とバリデーションのみを担当
 * - 承認者関係の検索ロジックはドメインサービスに委譲
 */
@Service
@Transactional
public class ApproverApplicationService {
    
    private final UserRepository userRepository;
    private final ListApproverDomainService listApproverService;
    
    public ApproverApplicationService(UserRepository userRepository,
                                       ListApproverDomainService listApproverService) {
        this.userRepository = userRepository;
        this.listApproverService = listApproverService;
    }
    
    /**
     * 現在の承認対象者リストを取得
     * 
     * @param approverId 承認者ID
     * @return 承認対象者のユーザーリスト
     */
    @Transactional(readOnly = true)
    public List<User> getApprovalTargets(String approverId) {
        // 承認者IDからユーザーエンティティを取得
        User approverUser = userRepository.findById(approverId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(approverId));
        
        // ドメインサービスで承認対象者リストを取得
        return listApproverService.findApprovalTargetsByApprover(approverUser);
    }
    
    /**
     * ユーザーの承認者を取得
     * 
     * @param targetId 対象者ID
     * @return 承認者ユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> getApprovers(String targetId) {
        // 対象者IDからユーザーエンティティを取得
        User targetUser = userRepository.findById(targetId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(targetId));
        
        // ドメインサービスで承認者リストを取得
        return listApproverService.findApproversByTarget(targetUser);
    }
}