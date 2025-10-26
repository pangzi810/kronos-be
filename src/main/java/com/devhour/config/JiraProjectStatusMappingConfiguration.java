package com.devhour.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.devhour.domain.model.valueobject.ProjectStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * プロジェクトステータスマッピング設定クラス
 * 
 * JIRA統合におけるプロジェクトステータスマッピング設定を管理する。
 * application.propertiesでJIRAステータス文字列と内部ProjectStatusの
 * マッピングを設定可能にする。
 * 
 * 設定例:
 * project.status.mapping.draft=PLANNING,NEW,OPEN,TO_DO,BACKLOG
 * project.status.mapping.in-progress=ACTIVE,IN_PROGRESS,STARTED,ONGOING
 * project.status.mapping.closed=COMPLETED,DONE,FINISHED,RESOLVED,CLOSED
 */
@Data
@Component
@ConfigurationProperties("project.status.mapping")
@Slf4j
public class JiraProjectStatusMappingConfiguration {
    
    /**
     * DRAFTにマッピングするJIRAステータス文字列のリスト
     */
    private String draft = "DRAFT,NEW,OPEN,TO_DO,BACKLOG";
    
    /**
     * IN_PROGRESSにマッピングするJIRAステータス文字列のリスト
     */
    private String inProgress = "ACTIVE,IN_PROGRESS,STARTED,ONGOING";
    
    /**
     * COMPLETEDにマッピングするJIRAステータス文字列のリスト
     */
    private String closed = "COMPLETED,DONE,FINISHED,RESOLVED,CLOSED";
    
    /**
     * デフォルトのプロジェクトステータス（文字列）
     */
    private String defaultStatus = "DRAFT";
    
    /**
     * デフォルトステータスをProjectStatus値オブジェクトとして取得
     */
    public ProjectStatus getDefaultProjectStatus() {
        if (defaultStatus == null || defaultStatus.trim().isEmpty()) {
            return ProjectStatus.DRAFT;
        }

        try {
            return ProjectStatus.of(defaultStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid default status '{}', using PLANNING", defaultStatus);
            return ProjectStatus.DRAFT;
        }
    }
    
    /**
     * 設定からステータスマッピングMapを構築
     * 
     * @return JIRAステータス文字列（大文字）からProjectStatusへのマッピング
     */
    public Map<String, ProjectStatus> buildStatusMappingMap() {
        Map<String, ProjectStatus> mappingMap = new HashMap<>();
        
        // DRAFTマッピング
        if (draft != null && !draft.trim().isEmpty()) {
            for (String status : draft.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.DRAFT);
                }
            }
        }
        
        // IN_PROGRESSマッピング
        if (inProgress != null && !inProgress.trim().isEmpty()) {
            for (String status : inProgress.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.IN_PROGRESS);
                }
            }
        }
        
        // COMPLETEDマッピング
        if (closed != null && !closed.trim().isEmpty()) {
            for (String status : closed.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.CLOSED);
                }
            }
        }
        
        log.info("プロジェクトステータスマッピング設定完了: マッピング数={}, デフォルト={}", 
                mappingMap.size(), defaultStatus);
        
        return mappingMap;
    }
    
    /**
     * 設定の検証
     * 
     * @return 設定が有効かどうか
     */
    public boolean isValid() {
        return (inProgress != null && !inProgress.trim().isEmpty()) ||
               (closed != null && !closed.trim().isEmpty()) ||
               (draft != null && !draft.trim().isEmpty());
    }
}