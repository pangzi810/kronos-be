package com.devhour.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.devhour.domain.exception.JiraSyncException;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.service.DataMappingDomainService.DataMappingException;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA同期ドメインサービス
 * 
 * JIRA統合において、issue_key-basedコンフリクト解決ロジック、
 * マスターデータ優先ポリシー（JIRA優先）、同期可能性チェックメソッド
 * を提供するドメインサービス
 * 
 * 責務:
 * - JIRAとローカルプロジェクト間のコンフリクト解決
 * - マスターデータ優先ポリシーの実装（JIRA > ローカル）
 * - プロジェクトの同期可能性チェック
 * - JQL条件に合致しないプロジェクトの除外マーク処理
 * - 重要フィールドの変更検出と管理者通知判定
 */
@Service
@Slf4j
public class JiraSyncDomainService {
    
    private final ProjectRepository projectRepository;
    private final DataMappingDomainService dataMappingDomainService;
    
    public JiraSyncDomainService(ProjectRepository projectRepository, 
                                DataMappingDomainService dataMappingDomainService) {
        this.projectRepository = projectRepository;
        this.dataMappingDomainService = dataMappingDomainService;
    }
    
    /**
     * プロジェクトコンフリクトを解決する（REQ-7.1, REQ-7.2）
     * 
     * JIRAから取得した共通フォーマットJSONに基づいて、
     * 既存のローカルプロジェクトとの競合を解決する。
     * JIRAデータを優先（マスター）として更新を行う。
     * 
     * @param commonFormatJson JIRA共通フォーマットJSON
     * @param syncHistory 同期履歴
     * @return コンフリクト解決結果
     * @throws IllegalArgumentException パラメータが不正な場合
     * @throws JiraSyncException 同期処理でエラーが発生した場合
     */
    public void applyProjectChanges(String commonFormatJson, JiraSyncHistory syncHistory) {
        validateConflictResolutionParameters(commonFormatJson, syncHistory);
        
        try {
            // 1. JIRAイシューキーを抽出
            String issueKey = dataMappingDomainService.extractIssueKey(commonFormatJson);
            log.debug("JIRAイシューキー抽出完了: {}", issueKey);
            
            // 2. 既存プロジェクトを検索
            Optional<Project> existingProjectOpt = projectRepository.findByJiraIssueKey(issueKey);
            
            if (existingProjectOpt.isPresent()) {
                // 3a. 既存プロジェクトが存在 → JIRAデータで更新（REQ-7.1）
                handleExistingProjectUpdate(existingProjectOpt.get(), commonFormatJson, syncHistory);
            } else {
                // 3b. 新規プロジェクト → JIRAデータから作成
                handleNewProjectCreation(commonFormatJson, syncHistory);
            }
        } catch (DataMappingException e) {
            // 4. データマッピングエラー → エラー詳細記録
            log.error("共通フォーマットJSONのマッピングエラー: {}", e.getMessage(), e);
            syncHistory.addDetail("Resolving Conflicts (Error)", DetailStatus.ERROR, "共通フォーマットJSONのマッピングエラー: {}" + e.getMessage());
        } catch (Exception e) {
            // 5. その他の予期しないエラー
            log.error("プロジェクトコンフリクト解決中に予期しないエラーが発生: {}", e.getMessage(), e);
            syncHistory.addDetail("Resolving Conflicts (Error)", DetailStatus.ERROR, "プロジェクトコンフリクト解決中に予期しないエラーが発生: {}" + e.getMessage());
        }
    }
    
    // ========== プライベートメソッド ==========
    
    /**
     * 既存プロジェクト更新処理
     */
    private void handleExistingProjectUpdate(Project existingProject, 
                                                               String commonFormatJson, 
                                                               JiraSyncHistory syncHistory) {
        try {
            // JIRAデータでプロジェクトを更新（マスター優先）
            Project updatedProject = dataMappingDomainService.updateProjectFromCommonFormat(
                existingProject, commonFormatJson);
            
            projectRepository.save(updatedProject);

            // 同期履歴詳細を作成（プロジェクト情報を文字列表現で記録）
            log.info("プロジェクト {} をJIRAデータで更新完了", updatedProject.getId());
            syncHistory.addDetail("Project Updated", DetailStatus.SUCCESS, updatedProject);
        } catch (DataMappingException e) {
            log.error("既存プロジェクト {} の更新中にマッピングエラー: {}", existingProject.getId(), e.getMessage());
            syncHistory.addDetail("Project Update Failed", DetailStatus.ERROR, "データマッピングエラー: " + e.getMessage());
        }
    }
    
    /**
     * 新規プロジェクト作成処理
     */
    private void handleNewProjectCreation(String commonFormatJson, 
                                                            JiraSyncHistory syncHistory) {
        try {
            // JIRAデータから新規プロジェクトを作成
            Project newProject = dataMappingDomainService.createProjectFromCommonFormat(
                commonFormatJson, "jira-sync");

            projectRepository.save(newProject);

            // 同期履歴詳細を作成（プロジェクト情報を文字列表現で記録）
            log.info("新規プロジェクト {} をJIRAデータから作成完了", newProject.getId());
            syncHistory.addDetail("Instantiate from JIRA", DetailStatus.SUCCESS,
                String.format("Project created: %s (%s)", newProject.getName(), newProject.getId()));
        } catch (DataMappingException e) {
            log.error("新規プロジェクト作成中にマッピングエラー: {}", e.getMessage());
            syncHistory.addDetail("Instantiate from JIRA", DetailStatus.ERROR, "データマッピングエラー: " + e.getMessage());
        }
    }
    
    // ========== バリデーションメソッド ==========
    
    private void validateConflictResolutionParameters(String commonFormatJson, JiraSyncHistory syncHistory) {
        if (commonFormatJson == null || commonFormatJson.trim().isEmpty()) {
            throw new IllegalArgumentException("共通フォーマットJSONは必須です");
        }
        if (syncHistory == null) {
            throw new IllegalArgumentException("同期履歴は必須です");
        }
    }
}