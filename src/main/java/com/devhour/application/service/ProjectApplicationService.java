package com.devhour.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;

/**
 * プロジェクトアプリケーションサービス
 * 
 * プロジェクト管理に関するユースケースを実装
 * 
 * 責務:
 * - プロジェクトの作成・更新・削除
 * - プロジェクト状態の変更管理
 * - プロジェクト検索・一覧取得
 * - PMO権限の検証とビジネスルール適用
 */
@Service
@Transactional
public class ProjectApplicationService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectApplicationService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * プロジェクト作成
     */
    public Project createProject(String userId, String name, LocalDate startDate,
            LocalDate plannedEndDate, String description) {
        // Userアクティブチェック
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));

        if (!user.isActive()) {
            throw new IllegalStateException("非アクティブユーザーはプロジェクトを作成できません");
        }

        // プロジェクト作成
        Project project = Project.create(name, description, startDate, plannedEndDate, userId);
        return projectRepository.save(project);
    }

    /**
     * プロジェクト更新
     */
    public Project updateProject(String projectId, String userId, String name,
            LocalDate startDate, LocalDate plannedEndDate, String description) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.projectNotFound(projectId));

        // 権限チェック
        validatePMOAccess(userId, project.getCreatedBy());

        // プロジェクト更新
        project.updateProjectInfo(name, description, startDate, plannedEndDate);
        return projectRepository.save(project);
    }

    /**
     * プロジェクト取得
     */
    public Optional<Project> findById(String projectId) {
        return projectRepository.findById(projectId);
    }

    /**
     * 全プロジェクト取得
     */
    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }
    
    /**
     * 工数記録可能プロジェクト一覧取得（ユーザーアサインメント考慮）
     * 
     * 全ての有効なプロジェクトを返す
     * 従来のアサインメント考慮は削除され、認証されたユーザーは全てのプロジェクトにアクセス可能
     * 
     * @param userId ユーザーID
     * @return 工数記録可能なプロジェクトのリスト（全て）
     */
    @Transactional(readOnly = true)
    public List<Project> findWorkRecordableProjectsForUser(String userId) {
        // ユーザーの存在確認
        userRepository.findById(userId)
            .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));
        
        // 全ての工数記録可能なプロジェクトを返す
        return projectRepository.findActiveProjects();
    }
    
    /**
     * 工数記録可能プロジェクト一覧取得（全体）
     * 
     * 開発者が工数記録を行うことができるプロジェクト（進行中ステータス）の一覧を取得
     * 
     * @return 工数記録可能なプロジェクトのリスト
     */
    @Transactional(readOnly = true)
    public List<Project> findWorkRecordableProjects() {
        return projectRepository.findActiveProjects();
    }

    /**
     * 最近工数記録されたプロジェクト一覧取得
     * 
     * 最近工数記録が行われたプロジェクトの一覧を取得
     * @return 最近工数記録されたプロジェクトのリスト
     */
    @Transactional(readOnly = true)
    public List<Project> findRecentWorkRecordedProjects() {
        return projectRepository.findRecentWorkRecordedProjects();
    }

    /**
     * プロジェクト削除
     */
    public void deleteProject(String projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.projectNotFound(projectId));

        // 権限チェック
        validatePMOAccess(userId, project.getCreatedBy());

        // プロジェクト削除
        projectRepository.deleteById(projectId);
    }

    /**
     * PMOアクセス権限の検証
     * 
     * @param userId           ユーザーID
     * @param projectCreatedBy プロジェクトの作成者ID
     * @throws IllegalStateException 権限不足の場合
     */
    private void validatePMOAccess(String userId, String projectCreatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));

        if (!user.isActive()) {
            throw new IllegalStateException("非アクティブユーザーはプロジェクトにアクセスできません");
        }
    }

    public List<Project> searchProjects(String query) {
        if (query == null || query.trim().isEmpty()) {
            return projectRepository.findAll();
        }
        return projectRepository.searchByNameOrJiraIssueKey(query);
    }
}