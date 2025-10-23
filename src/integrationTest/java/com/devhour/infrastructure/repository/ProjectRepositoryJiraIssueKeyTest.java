package com.devhour.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.domain.model.entity.Project;
import com.devhour.domain.model.valueobject.ProjectStatus;
import com.devhour.infrastructure.mapper.ProjectMapper;

/**
 * ProjectRepositoryImplのJIRAイシューキー検索機能のテストクラス
 * 
 * JIRA同期機能で使用されるfindByJiraIssueKey メソッドの動作を検証
 * プロジェクトとJIRAイシューキーの関連付け機能をテスト
 * 
 * テスト観点:
 * - JIRAイシューキーによるプロジェクト検索
 * - 存在しないJIRAイシューキーでの検索
 * - nullパラメータでのエラーハンドリング
 * - 空文字・空白文字での検索
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectRepository JIRAイシューキー検索 テスト")
class ProjectRepositoryJiraIssueKeyTest {
    
    @Mock
    private ProjectMapper projectMapper;
    
    @InjectMocks
    private ProjectRepositoryImpl repository;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testProject = Project.create(
            "テストプロジェクト",
            "テスト用のプロジェクトです",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            "admin"
        );
    }
    
    // ========================================
    // findByJiraIssueKey テスト
    // ========================================
    
    @Test
    @DisplayName("findByJiraIssueKey - 正常系: JIRAイシューキーでプロジェクトが見つかる場合")
    void findByJiraIssueKey_Success_WhenProjectExists() {
        // Given
        String jiraIssueKey = "PROJ-123";
        when(projectMapper.selectByJiraIssueKey(jiraIssueKey)).thenReturn(Optional.of(testProject));
        
        // When
        Optional<Project> result = repository.findByJiraIssueKey(jiraIssueKey);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectMapper).selectByJiraIssueKey(jiraIssueKey);
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 正常系: JIRAイシューキーでプロジェクトが見つからない場合")
    void findByJiraIssueKey_Success_WhenProjectNotExists() {
        // Given
        String jiraIssueKey = "NONEXISTENT-999";
        when(projectMapper.selectByJiraIssueKey(jiraIssueKey)).thenReturn(Optional.empty());
        
        // When
        Optional<Project> result = repository.findByJiraIssueKey(jiraIssueKey);
        
        // Then
        assertFalse(result.isPresent());
        verify(projectMapper).selectByJiraIssueKey(jiraIssueKey);
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 異常系: JIRAイシューキーがnullの場合")
    void findByJiraIssueKey_ThrowException_WhenJiraIssueKeyIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByJiraIssueKey(null)
        );
        assertEquals("JIRAイシューキーは必須です", exception.getMessage());
        verify(projectMapper, never()).selectByJiraIssueKey(any());
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 異常系: JIRAイシューキーが空文字の場合")
    void findByJiraIssueKey_ThrowException_WhenJiraIssueKeyIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByJiraIssueKey("")
        );
        assertEquals("JIRAイシューキーは必須です", exception.getMessage());
        verify(projectMapper, never()).selectByJiraIssueKey(any());
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 異常系: JIRAイシューキーが空白文字のみの場合")
    void findByJiraIssueKey_ThrowException_WhenJiraIssueKeyIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repository.findByJiraIssueKey("   ")
        );
        assertEquals("JIRAイシューキーは必須です", exception.getMessage());
        verify(projectMapper, never()).selectByJiraIssueKey(any());
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 正常系: 特殊文字を含むJIRAイシューキーの場合")
    void findByJiraIssueKey_Success_WhenJiraIssueKeyContainsSpecialCharacters() {
        // Given
        String jiraIssueKey = "PROJECT_123-456";
        when(projectMapper.selectByJiraIssueKey(jiraIssueKey)).thenReturn(Optional.of(testProject));
        
        // When
        Optional<Project> result = repository.findByJiraIssueKey(jiraIssueKey);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectMapper).selectByJiraIssueKey(jiraIssueKey);
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 正常系: 長いJIRAイシューキーの場合")
    void findByJiraIssueKey_Success_WhenJiraIssueKeyIsLong() {
        // Given
        String jiraIssueKey = "VERY_LONG_PROJECT_NAME_WITH_MANY_CHARACTERS-999999";
        when(projectMapper.selectByJiraIssueKey(jiraIssueKey)).thenReturn(Optional.of(testProject));
        
        // When
        Optional<Project> result = repository.findByJiraIssueKey(jiraIssueKey);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectMapper).selectByJiraIssueKey(jiraIssueKey);
    }
    
    @Test
    @DisplayName("findByJiraIssueKey - 正常系: 数字のみのJIRAイシューキーの場合")
    void findByJiraIssueKey_Success_WhenJiraIssueKeyIsNumericOnly() {
        // Given
        String jiraIssueKey = "123456";
        when(projectMapper.selectByJiraIssueKey(jiraIssueKey)).thenReturn(Optional.of(testProject));
        
        // When
        Optional<Project> result = repository.findByJiraIssueKey(jiraIssueKey);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectMapper).selectByJiraIssueKey(jiraIssueKey);
    }
}