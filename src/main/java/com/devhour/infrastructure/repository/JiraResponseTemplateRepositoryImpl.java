package com.devhour.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraResponseTemplate;
import com.devhour.domain.repository.JiraResponseTemplateRepository;
import com.devhour.infrastructure.mapper.JiraResponseTemplateMapper;

/**
 * レスポンステンプレートリポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してResponseTemplateRepositoryインターフェースを実装
 * Spring管理のトランザクション下でレスポンステンプレートエンティティの永続化操作を提供
 * 
 * 責務:
 * - ResponseTemplateMapperへの委譲によるデータアクセス
 * - ドメインレイヤとインフラストラクチャレイヤの境界管理
 * - パラメータ検証とエラーハンドリング
 * - トランザクション管理の適用
 * - テンプレート名による検索機能の提供
 * 
 * アーキテクチャ:
 * - Domain Repository Pattern の実装
 * - MyBatis annotation-based mapping
 * - Spring Transaction Management
 * 
 * 設計方針:
 * - 読み取り専用操作には @Transactional(readOnly = true) を適用
 * - 書き込み操作には @Transactional を適用
 * - null チェックによる防御的プログラミング
 * - 既存のJqlQueryRepositoryImplと同じパターンを踏襲
 */
@Repository
@Transactional(readOnly = true)
public class JiraResponseTemplateRepositoryImpl implements JiraResponseTemplateRepository {
    
    private final JiraResponseTemplateMapper responseTemplateMapper;
    
    public JiraResponseTemplateRepositoryImpl(JiraResponseTemplateMapper responseTemplateMapper) {
        this.responseTemplateMapper = responseTemplateMapper;
    }
    
    @Override
    public Optional<JiraResponseTemplate> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("レスポンステンプレートIDは必須です");
        }
        return responseTemplateMapper.selectById(id);
    }
    
    @Override
    public Optional<JiraResponseTemplate> findByTemplateName(String templateName) {
        if (templateName == null) {
            throw new IllegalArgumentException("テンプレート名は必須です");
        }
        return responseTemplateMapper.selectByTemplateName(templateName);
    }
    
    @Override
    public List<JiraResponseTemplate> findAll() {
        return responseTemplateMapper.selectAll();
    }
    
    @Override
    public List<JiraResponseTemplate> findAvailableTemplates() {
        // 現在はすべてのテンプレートが利用可能
        // 将来的に無効化フラグが追加された場合はここでフィルタリング
        return responseTemplateMapper.selectAll();
    }
    
    @Override
    public List<JiraResponseTemplate> searchByNamePattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("検索パターンは必須です");
        }
        return responseTemplateMapper.searchByNamePattern(pattern);
    }
    
    @Override
    public boolean existsByTemplateName(String templateName) {
        if (templateName == null) {
            throw new IllegalArgumentException("テンプレート名は必須です");
        }
        return responseTemplateMapper.existsByTemplateName(templateName);
    }
    
    @Override
    public boolean existsByTemplateNameExcludingId(String templateName, String excludeId) {
        if (templateName == null) {
            throw new IllegalArgumentException("テンプレート名は必須です");
        }
        if (excludeId == null) {
            throw new IllegalArgumentException("除外IDは必須です");
        }
        return responseTemplateMapper.existsByTemplateNameExcludingId(templateName, excludeId);
    }
    
    @Override
    public boolean existsById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("レスポンステンプレートIDは必須です");
        }
        return responseTemplateMapper.selectById(id).isPresent();
    }
    
    @Override
    @Transactional
    public JiraResponseTemplate save(JiraResponseTemplate responseTemplate) {
        if (responseTemplate == null) {
            throw new IllegalArgumentException("レスポンステンプレートエンティティは必須です");
        }
        
        if (!responseTemplateMapper.selectById(responseTemplate.getId()).isPresent()) {
            // 新規作成
            responseTemplateMapper.insert(
                responseTemplate.getId(),
                responseTemplate.getTemplateName(),
                responseTemplate.getVelocityTemplate(),
                responseTemplate.getTemplateDescription(),
                responseTemplate.getCreatedAt(),
                responseTemplate.getUpdatedAt()
            );
        } else {
            // 更新
            responseTemplateMapper.update(
                responseTemplate.getId(),
                responseTemplate.getTemplateName(),
                responseTemplate.getVelocityTemplate(),
                responseTemplate.getTemplateDescription(),
                responseTemplate.getUpdatedAt()
            );
        }
        return responseTemplate;
    }
    
    @Override
    @Transactional
    public List<JiraResponseTemplate> saveAll(List<JiraResponseTemplate> responseTemplates) {
        if (responseTemplates == null) {
            throw new IllegalArgumentException("レスポンステンプレートリストは必須です");
        }
        
        responseTemplates.forEach(this::save);
        return responseTemplates;
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("レスポンステンプレートIDは必須です");
        }
        responseTemplateMapper.deleteById(id);
    }
    
    @Override
    public List<JiraResponseTemplate> findAllWithPagination(int limit, int offset) {
        if (limit < 0) {
            throw new IllegalArgumentException("取得件数は0以上である必要があります");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("取得開始位置は0以上である必要があります");
        }
        
        return responseTemplateMapper.selectAllWithPagination(limit, offset);
    }
    
    @Override
    public long countAll() {
        return responseTemplateMapper.countAll();
    }
}