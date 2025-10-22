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
 * project.status.mapping.in-progress=ACTIVE,IN_PROGRESS,STARTED,ONGOING
 * project.status.mapping.completed=COMPLETED,DONE,FINISHED,RESOLVED,CLOSED
 * project.status.mapping.cancelled=CANCELLED,CANCELED,ABANDONED,REJECTED
 * project.status.mapping.planning=PLANNING,NEW,OPEN,TO_DO,BACKLOG
 */
@Data
@Component
@ConfigurationProperties("project.status.mapping")
@Slf4j
public class JiraProjectStatusMappingConfiguration {
    
    /**
     * IN_PROGRESSにマッピングするJIRAステータス文字列のリスト
     */
    private String inProgress = "ACTIVE,IN_PROGRESS,STARTED,ONGOING";
    
    /**
     * COMPLETEDにマッピングするJIRAステータス文字列のリスト
     */
    private String completed = "COMPLETED,DONE,FINISHED,RESOLVED,CLOSED";
    
    /**
     * CANCELLEDにマッピングするJIRAステータス文字列のリスト
     */
    private String cancelled = "CANCELLED,CANCELED,ABANDONED,REJECTED";
    
    /**
     * PLANNINGにマッピングするJIRAステータス文字列のリスト
     */
    private String planning = "PLANNING,NEW,OPEN,TO_DO,BACKLOG";
    
    /**
     * デフォルトのプロジェクトステータス（文字列）
     */
    private String defaultStatus = "PLANNING";
    
    /**
     * デフォルトステータスをProjectStatus値オブジェクトとして取得
     */
    public ProjectStatus getDefaultStatus() {
        if (defaultStatus == null || defaultStatus.trim().isEmpty()) {
            return ProjectStatus.PLANNING;
        }
        
        try {
            return ProjectStatus.of(defaultStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid default status '{}', using PLANNING", defaultStatus);
            return ProjectStatus.PLANNING;
        }
    }
    
    /**
     * 設定からステータスマッピングMapを構築
     * 
     * @return JIRAステータス文字列（大文字）からProjectStatusへのマッピング
     */
    public Map<String, ProjectStatus> buildStatusMappingMap() {
        Map<String, ProjectStatus> mappingMap = new HashMap<>();
        
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
        if (completed != null && !completed.trim().isEmpty()) {
            for (String status : completed.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.COMPLETED);
                }
            }
        }
        
        // CANCELLEDマッピング
        if (cancelled != null && !cancelled.trim().isEmpty()) {
            for (String status : cancelled.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.CANCELLED);
                }
            }
        }
        
        // PLANNINGマッピング
        if (planning != null && !planning.trim().isEmpty()) {
            for (String status : planning.split(",")) {
                String trimmed = status.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    mappingMap.put(trimmed, ProjectStatus.PLANNING);
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
               (completed != null && !completed.trim().isEmpty()) ||
               (cancelled != null && !cancelled.trim().isEmpty()) ||
               (planning != null && !planning.trim().isEmpty());
    }
}