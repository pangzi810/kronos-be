package com.devhour.infrastructure.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.devhour.domain.model.entity.Approver;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.infrastructure.dto.ApproverGrouping;
import com.devhour.infrastructure.mapper.ApproverMapper;

/**
 * 承認者関係リポジトリ実装
 * 
 * MyBatisを使用した承認者関係の永続化実装
 * V44マイグレーション対応版：メールアドレスベース
 * 
 * 責務:
 * - 承認者関係エンティティのCRUD操作（メールアドレスベース）
 * - メールアドレス形式の検証
 * - トランザクション管理
 * - 日付による有効性チェック機能
 * - 承認権限の検証機能
 * - バッチ処理に対応した検索機能
 */
@Repository
public class ApproverRepositoryImpl implements ApproverRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ApproverRepositoryImpl.class);
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$"
    );
    
    private final ApproverMapper mapper;
    
    public ApproverRepositoryImpl(ApproverMapper mapper) {
        this.mapper = mapper;
    }
    
    // ===== Core CRUD Operations =====
    
    @Override
    @Transactional
    public Approver save(Approver approver) {
        logger.debug("承認者関係を保存: {}", approver);
        
        if (approver == null) {
            throw new IllegalArgumentException("承認者関係は必須です");
        }
        
        validateApproverEntity(approver);
        
        if (mapper.findById(approver.getId()).isEmpty()) {
            // 新規作成
            mapper.insert(approver);
            logger.info("新規承認者関係を作成しました: {}", approver.getId());
        } else {
            // 更新
            mapper.update(approver);
            logger.info("承認者関係を更新しました: {}", approver.getId());
        }
        
        return approver;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Approver> findById(String id) {
        logger.debug("IDで承認者関係を検索: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("IDは必須です");
        }
        
        return mapper.findById(id);
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        logger.debug("IDで承認者関係を削除: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("IDは必須です");
        }
        
        // V44移行後は物理削除を実装
        Optional<Approver> approver = mapper.findById(id);
        if (approver.isPresent()) {
            // V44移行後はtargetEmailを使用
            mapper.deleteByUserId(approver.get().getTargetEmail());
            logger.info("承認者関係を削除しました: {} (target: {})", id, approver.get().getTargetEmail());
        }
    }
    
    // ===== Email-based Search Operations =====
    
    @Override
    @Transactional(readOnly = true)
    public List<Approver> findByTargetEmail(String targetEmail) {
        logger.debug("対象者メールアドレスで承認者関係を検索: {}", targetEmail);
        
        validateEmail(targetEmail, "対象者メールアドレス");
        
        List<Approver> result = mapper.findByTargetEmail(targetEmail.trim().toLowerCase());
        logger.debug("対象者メールアドレス '{}' の承認者関係を {} 件取得しました", targetEmail, result.size());
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Approver> findByApproverEmail(String approverEmail) {
        logger.debug("承認者メールアドレスで承認者関係を検索: {}", approverEmail);
        
        validateEmail(approverEmail, "承認者メールアドレス");
        
        List<Approver> result = mapper.findByApproverEmail(approverEmail.trim().toLowerCase());
        logger.debug("承認者メールアドレス '{}' の承認者関係を {} 件取得しました", approverEmail, result.size());
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isValidApprover(String targetEmail, String approverEmail, LocalDate date) {
        logger.debug("承認者関係の有効性チェック: target={}, approver={}, date={}", targetEmail, approverEmail, date);
        
        validateEmail(targetEmail, "対象者メールアドレス");
        validateEmail(approverEmail, "承認者メールアドレス");
        
        if (date == null) {
            throw new IllegalArgumentException("対象日は必須です");
        }
        
        boolean result = mapper.isValidApprover(targetEmail.trim().toLowerCase(), 
                                               approverEmail.trim().toLowerCase(), date);
        logger.debug("承認者関係の有効性チェック結果: {}", result);
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Approver> findValidApproversForDate(String targetEmail, LocalDate date) {
        logger.debug("指定日の有効な承認者を検索: target={}, date={}", targetEmail, date);
        
        validateEmail(targetEmail, "対象者メールアドレス");
        
        if (date == null) {
            throw new IllegalArgumentException("対象日は必須です");
        }
        
        List<Approver> result = mapper.findValidApproversForDate(targetEmail.trim().toLowerCase(), date);
        logger.debug("対象者 '{}' の指定日 '{}' の有効な承認者を {} 件取得しました", targetEmail, date, result.size());
        
        return result;
    }
    
    // ===== Bulk and Batch Operations =====
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Set<String>> findAllGroupedByTarget() {
        logger.debug("全承認者関係を対象者でグループ化して取得");
        
        List<ApproverGrouping> groupings = mapper.findAllGroupedByTarget();
        Map<String, Set<String>> result = new HashMap<>();
        
        for (ApproverGrouping grouping : groupings) {
            String targetEmail = grouping.getTargetEmail();
            String approverEmails = grouping.getApproverEmails();
            
            Set<String> approverSet = new HashSet<>();
            if (approverEmails != null && !approverEmails.trim().isEmpty()) {
                String[] emails = approverEmails.split(",");
                for (String email : emails) {
                    String trimmedEmail = email.trim();
                    if (!trimmedEmail.isEmpty()) {
                        approverSet.add(trimmedEmail);
                    }
                }
            }
            
            result.put(targetEmail, approverSet);
        }
        
        logger.debug("承認者関係グループ化結果: {} グループ取得", result.size());
        return result;
    }
    
    // ===== Maintenance Operations =====
    
    @Override
    @Transactional
    public void deleteByTargetAndApprover(String targetEmail, String approverEmail) {
        logger.debug("対象者と承認者の組み合わせで関係を削除: target={}, approver={}", targetEmail, approverEmail);
        
        validateEmail(targetEmail, "対象者メールアドレス");
        validateEmail(approverEmail, "承認者メールアドレス");
        
        mapper.deleteByTargetAndApprover(targetEmail.trim().toLowerCase(), 
                                        approverEmail.trim().toLowerCase());
        logger.info("承認者関係を削除しました: target={}, approver={}", targetEmail, approverEmail);
    }
    
    // ===== Validation Methods =====
    
    /**
     * メールアドレスの形式を検証
     * 
     * @param email 検証対象のメールアドレス
     * @param fieldName フィールド名（エラーメッセージ用）
     * @throws IllegalArgumentException メールアドレスが無効な場合
     */
    private void validateEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "は必須です");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(fieldName + "の形式が正しくありません: " + email);
        }
    }
    
    /**
     * 承認者エンティティの検証
     * 
     * @param approver 検証対象の承認者エンティティ
     * @throws IllegalArgumentException エンティティが無効な場合
     */
    private void validateApproverEntity(Approver approver) {
        if (approver.getId() == null || approver.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("承認者関係IDは必須です");
        }
        
        validateEmail(approver.getTargetEmail(), "対象者メールアドレス");
        validateEmail(approver.getApproverEmail(), "承認者メールアドレス");
        
        if (approver.getEffectiveFrom() == null) {
            throw new IllegalArgumentException("有効開始日時は必須です");
        }
        
        if (approver.getEffectiveTo() != null && 
            approver.getEffectiveTo().isBefore(approver.getEffectiveFrom())) {
            throw new IllegalArgumentException("有効終了日時は開始日時以降である必要があります");
        }
        
        // 自己承認の防止
        if (approver.getTargetEmail().toLowerCase().equals(approver.getApproverEmail().toLowerCase())) {
            throw new IllegalArgumentException("自己承認はできません");
        }
    }
}