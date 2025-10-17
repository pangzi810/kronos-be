package com.devhour.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.devhour.domain.model.entity.Approver;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * 承認者リスト取得ドメインサービス
 * 
 * 承認者関係から承認対象者および承認者のリストを取得する
 * ドメインロジックを実装するサービス。
 * 
 * 主な責務:
 * - メールアドレスベースでの承認者関係検索
 * - 承認者エンティティからユーザーエンティティへの変換
 * - 承認対象者リストの取得
 * - 特定ユーザーの承認者リストの取得
 * 
 * V44移行対応: メールアドレスベースのリポジトリAPIを使用
 */
@Service
public class ListApproverDomainService {
    
    private final ApproverRepository approverRepository;
    private final UserRepository userRepository;
    
    public ListApproverDomainService(ApproverRepository approverRepository,
                              UserRepository userRepository) {
        this.approverRepository = approverRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 指定した承認者の承認対象者リストを取得
     * 
     * @param approverUser 承認者のユーザーエンティティ
     * @return 承認対象者のユーザーリスト
     */
    public List<User> findApprovalTargetsByApprover(User approverUser) {
        List<Approver> approvers = approverRepository.findByApproverEmail(approverUser.getEmail());
        
        return approvers.stream()
            .map(approver -> userRepository.findByEmail(approver.getTargetEmail()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
    
    /**
     * 指定した対象者の承認者リストを取得
     * 
     * @param targetUser 対象者のユーザーエンティティ
     * @return 承認者のユーザーリスト
     */
    public List<User> findApproversByTarget(User targetUser) {
        List<Approver> approvers = approverRepository.findByTargetEmail(targetUser.getEmail());
        
        return approvers.stream()
            .map(approver -> userRepository.findByEmail(approver.getApproverEmail()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}