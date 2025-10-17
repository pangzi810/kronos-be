package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.valueobject.Position;
import com.devhour.domain.repository.ApprovalAuthorityRepository;
import com.devhour.infrastructure.mapper.ApprovalAuthorityMapper;

/**
 * 承認権限リポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してApprovalAuthorityRepositoryインターフェースを実装
 * ドメイン駆動設計のRepository パターンに従い、インフラストラクチャ層で
 * ドメイン層のリポジトリインターフェースを実装
 * 
 * 責務:
 * - MyBatisマッパーのラッピング
 * - トランザクション管理の適用
 * - 入力パラメータの検証
 * - ビジネスロジック（save メソッドでの create/update 判定）
 * - ログ出力
 */
@Repository
public class ApprovalAuthorityRepositoryImpl implements ApprovalAuthorityRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ApprovalAuthorityRepositoryImpl.class);
    
    private final ApprovalAuthorityMapper mapper;
    
    /**
     * コンストラクタインジェクション
     * 
     * @param mapper ApprovalAuthorityMapper
     */
    public ApprovalAuthorityRepositoryImpl(ApprovalAuthorityMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAuthority> findAll() {
        logger.debug("すべての承認権限を検索中");
        List<ApprovalAuthority> authorities = mapper.findAll();
        logger.debug("{}件の承認権限を取得しました", authorities.size());
        return authorities;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ApprovalAuthority> findByEmail(String email) {
        validateRequiredString(email, "メールアドレスは必須です");
        
        logger.debug("メールアドレスで承認権限を検索中: {}", email);
        Optional<ApprovalAuthority> authority = mapper.findByEmail(email);
        if (authority.isPresent()) {
            logger.debug("承認権限が見つかりました: ID={}", authority.get().getId());
        } else {
            logger.debug("承認権限が見つかりませんでした: email={}", email);
        }
        return authority;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAuthority> searchByNameOrEmail(String query) {
        validateRequiredString(query, "検索クエリは必須です");
        
        logger.debug("名前またはメールアドレスで承認権限を検索中: {}", query);
        List<ApprovalAuthority> authorities = mapper.searchByNameOrEmail(query);
        logger.debug("{}件の承認権限が見つかりました", authorities.size());
        return authorities;
    }
    
    @Override
    @Transactional
    public ApprovalAuthority save(ApprovalAuthority approvalAuthority) {
        if (approvalAuthority == null) {
            throw new IllegalArgumentException("承認権限エンティティは必須です");
        }
        
        logger.debug("承認権限を保存中: email={}", approvalAuthority.getEmail());
        
        // 既存エンティティの確認
        Optional<ApprovalAuthority> existing = mapper.findByEmail(approvalAuthority.getEmail());
        
        if (existing.isEmpty()) {
            // 新規作成
            logger.debug("新規承認権限を作成中: email={}", approvalAuthority.getEmail());
            mapper.insert(approvalAuthority);
            logger.info("新規承認権限を作成しました: id={}, email={}", 
                       approvalAuthority.getId(), approvalAuthority.getEmail());
        } else {
            // 既存エンティティの更新
            logger.debug("既存承認権限を更新中: email={}", approvalAuthority.getEmail());
            
            // 更新日時を設定
            ApprovalAuthority updatedAuthority = ApprovalAuthority.restore(
                approvalAuthority.getId(),
                approvalAuthority.getEmail(),
                approvalAuthority.getName(),
                approvalAuthority.getPosition(),
                approvalAuthority.getLevel1Code(),
                approvalAuthority.getLevel1Name(),
                approvalAuthority.getLevel2Code(),
                approvalAuthority.getLevel2Name(),
                approvalAuthority.getLevel3Code(),
                approvalAuthority.getLevel3Name(),
                approvalAuthority.getLevel4Code(),
                approvalAuthority.getLevel4Name(),
                existing.get().getCreatedAt(),  // 作成日時は元のまま
                LocalDateTime.now()             // 更新日時を現在時刻に設定
            );
            
            mapper.update(updatedAuthority);
            logger.info("既存承認権限を更新しました: id={}, email={}", 
                       updatedAuthority.getId(), updatedAuthority.getEmail());
            
            return updatedAuthority;
        }
        
        return approvalAuthority;
    }
    
    @Override
    @Transactional
    public void deleteByEmail(String email) {
        validateRequiredString(email, "メールアドレスは必須です");
        
        logger.debug("メールアドレスで承認権限を削除中: {}", email);
        mapper.deleteByEmail(email);
        logger.info("承認権限を削除しました: email={}", email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAuthority> findByPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("役職は必須です");
        }
        
        logger.debug("役職で承認権限を検索中: {}", position);
        List<ApprovalAuthority> authorities = mapper.findByPosition(position);
        logger.debug("{}件の承認権限が見つかりました", authorities.size());
        return authorities;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAuthority> findByLevelCode(String levelCode, int level) {
        validateRequiredString(levelCode, "組織レベルコードは必須です");
        validateLevel(level);
        
        logger.debug("組織レベルコードで承認権限を検索中: levelCode={}, level={}", levelCode, level);
        List<ApprovalAuthority> authorities = mapper.findByLevelCode(levelCode, level);
        logger.debug("{}件の承認権限が見つかりました", authorities.size());
        return authorities;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        validateRequiredString(email, "メールアドレスは必須です");
        
        logger.debug("メールアドレスの存在確認中: {}", email);
        boolean exists = mapper.existsByEmail(email);
        logger.debug("メールアドレスの存在確認結果: {} -> {}", email, exists);
        return exists;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByLevelCode(String levelCode, int level) {
        validateRequiredString(levelCode, "組織レベルコードは必須です");
        validateLevel(level);
        
        logger.debug("組織レベル別承認権限者数をカウント中: levelCode={}, level={}", levelCode, level);
        long count = mapper.countByLevelCode(levelCode, level);
        logger.debug("承認権限者数: {}件", count);
        return count;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAuthority> findAllWithApprovalAuthority() {
        logger.debug("承認権限を持つユーザー一覧を検索中");
        List<ApprovalAuthority> authorities = mapper.findAllWithApprovalAuthority();
        logger.debug("{}件の承認権限者を取得しました", authorities.size());
        return authorities;
    }
    
    /**
     * 必須文字列パラメータの検証
     * 
     * @param value 検証対象の文字列
     * @param errorMessage エラーメッセージ
     * @throws IllegalArgumentException 値がnullまたは空文字の場合
     */
    private void validateRequiredString(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * レベル値の検証
     * 
     * @param level 検証対象のレベル値
     * @throws IllegalArgumentException レベルが1-4の範囲外の場合
     */
    private void validateLevel(int level) {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("レベルは1から4の間で指定してください");
        }
    }
}