package com.devhour.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.devhour.domain.model.entity.JiraSyncHistory;
import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;
import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.service.DataMappingDomainService.DataMappingException;

/**
 * JiraSyncDomainServiceのテストクラス
 *
 * JIRA同期処理において、プロジェクト変更適用をテストする
 */
@ExtendWith(MockitoExtension.class)
class JiraSyncDomainServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DataMappingDomainService dataMappingDomainService;

    private JiraSyncDomainService jiraSyncDomainService;

    @BeforeEach
    void setUp() {
        jiraSyncDomainService = new JiraSyncDomainService(projectRepository, dataMappingDomainService);
    }

    // ========== applyProjectChanges のテスト ==========

    @Test
    void applyProjectChanges_既存プロジェクトが存在_JIRAデータで更新される() throws Exception {
        // Given
        String issueKey = "PROJ-123";
        String commonFormatJson = createValidCommonFormatJson(issueKey);
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        // 既存のローカルプロジェクト
        Project existingProject = Project.create("Local Project", "Local desc",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), "creator", issueKey, null);

        // JIRAからの更新されたプロジェクト情報
        Project updatedProject = Project.create("JIRA Project", "JIRA desc",
            LocalDate.of(2024, 2, 1), LocalDate.of(2024, 7, 1), "creator", issueKey, null);

        when(dataMappingDomainService.extractIssueKey(commonFormatJson)).thenReturn(issueKey);
        when(projectRepository.findByJiraIssueKey(issueKey)).thenReturn(Optional.of(existingProject));
        when(dataMappingDomainService.updateProjectFromCommonFormat(existingProject, commonFormatJson))
            .thenReturn(updatedProject);

        // When
        jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);

        // Then
        verify(projectRepository).save(updatedProject);
        assertEquals(1, syncHistory.getDetails().size());
        assertEquals("Project Updated", syncHistory.getDetails().get(0).getOperation());
        assertEquals(DetailStatus.SUCCESS, syncHistory.getDetails().get(0).getStatus());
    }

    @Test
    void applyProjectChanges_新規プロジェクト_JIRAデータから作成される() throws Exception {
        // Given
        String issueKey = "PROJ-456";
        String commonFormatJson = createValidCommonFormatJson(issueKey);
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        Project newProject = Project.create("New JIRA Project", "New desc",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), "jira-sync", issueKey, null);

        when(dataMappingDomainService.extractIssueKey(commonFormatJson)).thenReturn(issueKey);
        when(projectRepository.findByJiraIssueKey(issueKey)).thenReturn(Optional.empty());
        when(dataMappingDomainService.createProjectFromCommonFormat(commonFormatJson, "jira-sync"))
            .thenReturn(newProject);

        // When
        jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);

        // Then
        verify(projectRepository).save(newProject);
        assertEquals(1, syncHistory.getDetails().size());
        assertEquals("Instantiate from JIRA", syncHistory.getDetails().get(0).getOperation());
        assertEquals(DetailStatus.SUCCESS, syncHistory.getDetails().get(0).getStatus());
        assertTrue(syncHistory.getDetails().get(0).getResult().contains("Project created"));
    }

    @Test
    void applyProjectChanges_データマッピングエラー_エラー詳細が記録される() throws Exception {
        // Given
        String commonFormatJson = "invalid json";
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        when(dataMappingDomainService.extractIssueKey(commonFormatJson))
            .thenThrow(new DataMappingException("Invalid JSON format"));

        // When
        jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);

        // Then
        assertEquals(1, syncHistory.getDetails().size());
        assertEquals("Resolving Conflicts (Error)", syncHistory.getDetails().get(0).getOperation());
        assertEquals(DetailStatus.ERROR, syncHistory.getDetails().get(0).getStatus());
        assertTrue(syncHistory.getDetails().get(0).getResult().contains("Invalid JSON format"));
    }

    @Test
    void applyProjectChanges_既存プロジェクト更新エラー_エラー詳細が記録される() throws Exception {
        // Given
        String issueKey = "PROJ-123";
        String commonFormatJson = createValidCommonFormatJson(issueKey);
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        Project existingProject = Project.create("Local Project", "Local desc",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), "creator", issueKey, null);

        when(dataMappingDomainService.extractIssueKey(commonFormatJson)).thenReturn(issueKey);
        when(projectRepository.findByJiraIssueKey(issueKey)).thenReturn(Optional.of(existingProject));
        when(dataMappingDomainService.updateProjectFromCommonFormat(existingProject, commonFormatJson))
            .thenThrow(new DataMappingException("Update failed"));

        // When
        jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);

        // Then
        assertEquals(1, syncHistory.getDetails().size());
        assertEquals("Project Update Failed", syncHistory.getDetails().get(0).getOperation());
        assertEquals(DetailStatus.ERROR, syncHistory.getDetails().get(0).getStatus());
        assertTrue(syncHistory.getDetails().get(0).getResult().contains("Update failed"));
    }

    @Test
    void applyProjectChanges_新規プロジェクト作成エラー_エラー詳細が記録される() throws Exception {
        // Given
        String issueKey = "PROJ-456";
        String commonFormatJson = createValidCommonFormatJson(issueKey);
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        when(dataMappingDomainService.extractIssueKey(commonFormatJson)).thenReturn(issueKey);
        when(projectRepository.findByJiraIssueKey(issueKey)).thenReturn(Optional.empty());
        when(dataMappingDomainService.createProjectFromCommonFormat(commonFormatJson, "jira-sync"))
            .thenThrow(new DataMappingException("Creation failed"));

        // When
        jiraSyncDomainService.applyProjectChanges(commonFormatJson, syncHistory);

        // Then
        assertEquals(1, syncHistory.getDetails().size());
        assertEquals("Instantiate from JIRA", syncHistory.getDetails().get(0).getOperation());
        assertEquals(DetailStatus.ERROR, syncHistory.getDetails().get(0).getStatus());
        assertTrue(syncHistory.getDetails().get(0).getResult().contains("Creation failed"));
    }

    @Test
    void applyProjectChanges_nullパラメータ_例外が発生する() {
        JiraSyncHistory syncHistory = JiraSyncHistory.startSync(JiraSyncType.SCHEDULED, "system");

        assertThrows(IllegalArgumentException.class, () ->
            jiraSyncDomainService.applyProjectChanges(null, syncHistory));

        assertThrows(IllegalArgumentException.class, () ->
            jiraSyncDomainService.applyProjectChanges("", syncHistory));

        assertThrows(IllegalArgumentException.class, () ->
            jiraSyncDomainService.applyProjectChanges("valid json", null));
    }

    // ========== ヘルパーメソッド ==========

    private String createValidCommonFormatJson(String issueKey) {
        return """
            {
                "issueKey": "%s",
                "summary": "Test Project",
                "description": "Test Description",
                "status": "IN_PROGRESS",
                "created": "2024-01-01T00:00:00Z",
                "updated": "2024-01-15T12:00:00Z"
            }
            """.formatted(issueKey);
    }
}