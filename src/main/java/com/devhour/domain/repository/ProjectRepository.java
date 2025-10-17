package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;

/**
 * プロジェクトリポジトリインターフェース
 * 
 * プロジェクトエンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepository パターンの実装
 * 
 * 責務:
 * - プロジェクトエンティティの CRUD 操作
 * - プロジェクト状態・期間に基づく検索機能
 * - ビジネス要件に基づく複合検索機能
 */
public interface ProjectRepository {
    
    /**
     * プロジェクトIDでプロジェクトを検索
     * 
     * @param projectId プロジェクトID
     * @return プロジェクトエンティティ（存在しない場合は空のOptional）
     */
    Optional<Project> findById(String projectId);
    
    /**
     * プロジェクト名でプロジェクトを検索
     * プロジェクト名の重複チェックに使用
     * 
     * @param name プロジェクト名
     * @return プロジェクトエンティティ（存在しない場合は空のOptional）
     */
    Optional<Project> findByName(String name);
    
    /**
     * JIRAイシューキーでプロジェクトを検索
     * JIRA同期機能でイシューキーからプロジェクトを特定する際に使用
     * 
     * @param jiraIssueKey JIRAイシューキー
     * @return プロジェクトエンティティ（存在しない場合は空のOptional）
     */
    Optional<Project> findByJiraIssueKey(String jiraIssueKey);
    
    /**
     * 全プロジェクト一覧を取得
     * 
     * @return 全プロジェクトのリスト（作成日降順）
     */
    List<Project> findAll();
    
    /**
     * 指定状態のプロジェクト一覧を取得
     * 
     * @param status プロジェクト状態
     * @return 指定状態のプロジェクトのリスト
     */
    List<Project> findByStatus(ProjectStatus status);
    
    /**
     * アクティブなプロジェクト一覧を取得
     * （計画中・進行中・一時停止中のプロジェクト）
     * 
     * @return アクティブなプロジェクトのリスト
     */
    List<Project> findActiveProjects();
    
    /**
     * 最近工数記録されたプロジェクト一覧を取得
     * 
     * @return 最近工数記録されたプロジェクトのリスト
     */
    List<Project> findRecentWorkRecordedProjects();
    
    /**
     * プロジェクト名での部分一致検索
     * 
     * @param namePattern 検索パターン（部分一致）
     * @return マッチしたプロジェクトのリスト
     */
    List<Project> searchByName(String namePattern);
    
    /**
     * プロジェクト名での部分一致検索
     * 
     * @param namePattern 検索パターン（部分一致）
     * @param jiraIssueKey JIRAイシューキー（部分一致）
     * @return マッチしたプロジェクトのリスト
     */
    List<Project> searchByNameOrJiraIssueKey(String query);

    /**
     * プロジェクト名の存在チェック
     * プロジェクト作成時の重複チェックに使用
     * 
     * @param name プロジェクト名
     * @return 存在する場合true
     */
    boolean existsByName(String name);
    
    /**
     * プロジェクトの存在チェック
     * 
     * @param projectId プロジェクトID
     * @return 存在する場合true
     */
    boolean existsById(String projectId);
    
    /**
     * プロジェクトを保存
     * 新規作成・更新の両方で使用
     * 
     * @param project 保存対象のプロジェクトエンティティ
     * @return 保存されたプロジェクトエンティティ
     */
    Project save(Project project);
    
    /**
     * 複数プロジェクトを一括保存
     * 
     * @param projects 保存対象のプロジェクトエンティティのリスト
     * @return 保存されたプロジェクトエンティティのリスト
     */
    List<Project> saveAll(List<Project> projects);
    
    /**
     * プロジェクトを削除
     * 物理削除ではなく、通常は論理削除（ステータス変更）を推奨
     * 
     * @param projectId 削除対象のプロジェクトID
     */
    void deleteById(String projectId);
}