package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.infrastructure.mapper.ProjectMapper;

/**
 * プロジェクトリポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してProjectRepositoryインターフェースを実装
 */
@Repository
public class ProjectRepositoryImpl implements ProjectRepository {
    
    private final ProjectMapper projectMapper;
    
    public ProjectRepositoryImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }
    
    @Override
    public Optional<Project> findById(String projectId) {
        return projectMapper.findById(projectId);
    }
    
    @Override
    public Optional<Project> findByName(String name) {
        return projectMapper.findByName(name);
    }
    
    @Override
    public Optional<Project> findByJiraIssueKey(String jiraIssueKey) {
        if (jiraIssueKey == null || jiraIssueKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JIRAイシューキーは必須です");
        }
        return projectMapper.selectByJiraIssueKey(jiraIssueKey);
    }
    
    @Override
    public List<Project> findAll() {
        return projectMapper.findAll();
    }
    
    @Override
    public List<Project> findByStatus(ProjectStatus status) {
        return projectMapper.findByStatus(status.value());
    }
    
    @Override
    public List<Project> findActiveProjects() {
        return projectMapper.findActiveProjects();
    }
    
    @Override
    public List<Project> findRecentWorkRecordedProjects() {
        return projectMapper.findRecentWorkRecordedProjects();
    }
    
    @Override
    public List<Project> searchByName(String namePattern) {
        return projectMapper.searchByName(namePattern);
    }

    @Override
    public List<Project> searchByNameOrJiraIssueKey(String query) {
        return projectMapper.searchByNameOrJiraIssueKey(query);
    }
    
    @Override
    public boolean existsByName(String name) {
        return projectMapper.existsByName(name);
    }
    
    @Override
    public boolean existsById(String projectId) {
        return projectMapper.existsById(projectId);
    }
    
    @Override
    public Project save(Project project) {
        if (!projectMapper.existsById(project.getId())) {
            // 新規作成
            projectMapper.insert(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().value(),
                project.getStartDate(),
                project.getPlannedEndDate(),
                project.getCreatedBy(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getJiraIssueKey(),
                project.getCustomFields()
            );
        } else {
            // 更新
            projectMapper.update(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().value(),
                project.getStartDate(),
                project.getPlannedEndDate(),
                project.getJiraIssueKey(),
                project.getCustomFields(),
                project.getUpdatedAt()
            );
        }
        return project;
    }
    
    @Override
    public List<Project> saveAll(List<Project> projects) {
        projects.forEach(this::save);
        return projects;
    }
    
    @Override
    public void deleteById(String projectId) {
        projectMapper.softDelete(projectId, LocalDateTime.now(), LocalDateTime.now());
    }
    
    public long countByStatus(ProjectStatus status) {
        return projectMapper.countByStatus(status.value());
    }
    
}