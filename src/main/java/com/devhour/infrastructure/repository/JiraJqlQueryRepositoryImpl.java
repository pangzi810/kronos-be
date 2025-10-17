package com.devhour.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.model.entity.JiraJqlQuery;
import com.devhour.domain.repository.JiraJqlQueryRepository;
import com.devhour.infrastructure.mapper.JiraJqlQueryMapper;

/**
 * JQLクエリリポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してJqlQueryRepositoryインターフェースを実装
 * Spring管理のトランザクション下でJQLクエリエンティティの永続化操作を提供
 * 
 * 責務:
 * - JqlQueryMapperへの委譲によるデータアクセス
 * - ドメインレイヤとインフラストラクチャレイヤの境界管理
 * - パラメータ検証とエラーハンドリング
 * - トランザクション管理の適用
 * 
 * アーキテクチャ:
 * - Domain Repository Pattern の実装
 * - MyBatis annotation-based mapping
 * - Spring Transaction Management
 */
@Repository
@Transactional(readOnly = true)
public class JiraJqlQueryRepositoryImpl implements JiraJqlQueryRepository {
    
    private final JiraJqlQueryMapper jqlQueryMapper;
    
    public JiraJqlQueryRepositoryImpl(JiraJqlQueryMapper jqlQueryMapper) {
        this.jqlQueryMapper = jqlQueryMapper;
    }
    
    @Override
    public Optional<JiraJqlQuery> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("JQLクエリIDは必須です");
        }
        return jqlQueryMapper.selectById(id);
    }
    
    @Override
    public Optional<JiraJqlQuery> findByQueryName(String queryName) {
        if (queryName == null) {
            throw new IllegalArgumentException("クエリ名は必須です");
        }
        return jqlQueryMapper.selectByQueryName(queryName);
    }
    
    @Override
    public List<JiraJqlQuery> findAll() {
        // 全件取得なのでMAX_VALUEを指定
        return jqlQueryMapper.selectAllWithPagination(Integer.MAX_VALUE, 0);
    }
    
    @Override
    public List<JiraJqlQuery> findActiveQueriesOrderByPriority() {
        return jqlQueryMapper.selectActiveQueriesOrderByPriority();
    }
    
    @Override
    public List<JiraJqlQuery> findByTemplateId(String templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("テンプレートIDは必須です");
        }
        return jqlQueryMapper.selectByTemplateId(templateId);
    }
    
    @Override
    public boolean existsByQueryName(String queryName) {
        if (queryName == null) {
            throw new IllegalArgumentException("クエリ名は必須です");
        }
        return jqlQueryMapper.selectByQueryName(queryName).isPresent();
    }
    
    @Override
    public boolean existsById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("JQLクエリIDは必須です");
        }
        return jqlQueryMapper.selectById(id).isPresent();
    }
    
    @Override
    @Transactional
    public JiraJqlQuery save(JiraJqlQuery jqlQuery) {
        if (jqlQuery == null) {
            throw new IllegalArgumentException("JQLクエリエンティティは必須です");
        }
        
        if (!jqlQueryMapper.selectById(jqlQuery.getId()).isPresent()) {
            // 新規作成
            jqlQueryMapper.insert(
                jqlQuery.getId(),
                jqlQuery.getQueryName(),
                jqlQuery.getJqlExpression(),
                jqlQuery.getTemplateId(),
                jqlQuery.isActive(),
                jqlQuery.getPriority(),
                jqlQuery.getCreatedAt(),
                jqlQuery.getUpdatedAt(),
                jqlQuery.getCreatedBy(),
                jqlQuery.getUpdatedBy()
            );
        } else {
            // 更新
            jqlQueryMapper.update(
                jqlQuery.getId(),
                jqlQuery.getQueryName(),
                jqlQuery.getJqlExpression(),
                jqlQuery.getTemplateId(),
                jqlQuery.isActive(),
                jqlQuery.getPriority(),
                jqlQuery.getUpdatedAt(),
                jqlQuery.getUpdatedBy()
            );
        }
        return jqlQuery;
    }
    
    @Override
    @Transactional
    public List<JiraJqlQuery> saveAll(List<JiraJqlQuery> jqlQueries) {
        if (jqlQueries == null) {
            throw new IllegalArgumentException("JQLクエリリストは必須です");
        }
        
        jqlQueries.forEach(this::save);
        return jqlQueries;
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("JQLクエリIDは必須です");
        }
        jqlQueryMapper.deleteById(id);
    }
    
    @Override
    public List<JiraJqlQuery> findAllWithPagination(int limit, int offset) {
        if (limit < 0) {
            throw new IllegalArgumentException("取得件数は0以上である必要があります");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("取得開始位置は0以上である必要があります");
        }
        
        return jqlQueryMapper.selectAllWithPagination(limit, offset);
    }
    
    @Override
    public long countActiveQueries() {
        return jqlQueryMapper.countActiveQueries();
    }
}