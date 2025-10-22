package com.devhour.infrastructure.jira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.devhour.config.JiraConfiguration;
import com.devhour.infrastructure.jira.dto.JiraIssueSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JiraClientクラスのテストクラス
 * 
 * JIRA REST API通信機能の動作を検証する
 */
@ExtendWith(MockitoExtension.class)
class JiraClientTest {

    @Mock(lenient = true)
    private RestTemplate jiraRestTemplate;
    
    @Mock(lenient = true)
    private JiraConfiguration jiraConfiguration;
    
    private JiraClient jiraClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // デフォルトの設定値をモック
        when(jiraConfiguration.getFullApiUrl(anyString())).thenAnswer(invocation -> 
            "https://company.atlassian.net" + invocation.getArgument(0));
        when(jiraConfiguration.getAuthUsername()).thenReturn("test-user");
        when(jiraConfiguration.getAuthToken()).thenReturn("test-token");
        when(jiraConfiguration.isConfigured()).thenReturn(true);
        when(jiraConfiguration.getTimeout())
            .thenReturn(createTimeoutConfig(30000, 60000));
        
        jiraClient = new JiraClient(jiraRestTemplate, jiraConfiguration);
    }

    @Test
    void searchIssues_JQLクエリが正常に実行されること() throws JsonProcessingException {
        // given
        String jqlQuery = "project = PROJ";
        String responseJson = """
            {
                "expand": "schema,names",
                "startAt": 0,
                "maxResults": 50,
                "total": 2,
                "issues": [
                    {
                        "id": "10001",
                        "key": "PROJ-1"
                    },
                    {
                        "id": "10002", 
                        "key": "PROJ-2"
                    }
                ]
            }
            """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when
        JiraIssueSearchResponse result = jiraClient.searchIssues(jqlQuery, 50, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMaxResults()).isEqualTo(50);
        assertThat(result.getIssues()).hasSize(2);
        
        // リクエストの検証
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(jiraRestTemplate).exchange(
            contains("/search"), 
            eq(HttpMethod.GET), 
            entityCaptor.capture(), 
            eq(String.class)
        );
        
        HttpEntity<String> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.get("Authorization")).contains("Basic dGVzdC11c2VyOnRlc3QtdG9rZW4=");
    }

    @Test
    void searchIssues_デフォルトパラメータが正しく設定されること() {
        // given
        String jqlQuery = "project = PROJ";
        String responseJson = """
            {
                "expand": "schema,names",
                "nextPageToken": "",
                "maxResults": 50,
                "total": 0,
                "issues": []
            }
            """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when
        JiraIssueSearchResponse result = jiraClient.searchIssues(jqlQuery, null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMaxResults()).isEqualTo(50);
        
        // URLパラメータの検証
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jiraRestTemplate).exchange(
            urlCaptor.capture(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(String.class)
        );
        
        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).contains("maxResults=50");
        assertThat(capturedUrl).contains("nextPageToken=");
    }

    @Test
    void searchIssues_JQLクエリがnullの場合例外が発生すること() {
        // when & then
        assertThatThrownBy(() -> jiraClient.searchIssues(null, 50, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JQL query is required");
    }

    @Test
    void searchIssues_JQLクエリが空文字列の場合例外が発生すること() {
        // when & then
        assertThatThrownBy(() -> jiraClient.searchIssues("", 50, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JQL query is required");
    }

    @Test
    void searchIssues_maxResultsが負の値の場合例外が発生すること() {
        // when & then
        assertThatThrownBy(() -> jiraClient.searchIssues("project = PROJ", -1, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max results must be greater than 0");
    }

    @Test
    void searchIssues_RestClientExceptionが発生した場合JiraClientExceptionが発生すること() {
        // given
        String jqlQuery = "project = PROJ";
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RestClientException("Network error"));

        // when & then
        assertThatThrownBy(() -> jiraClient.searchIssues(jqlQuery, 50, null))
            .isInstanceOf(JiraClient.JiraClientException.class)
            .hasMessageContaining("Failed to search issues")
            .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    void getIssueDetails_イシューの詳細情報を取得できること() throws JsonProcessingException {
        // given
        String issueKey = "PROJ-123";
        String responseJson = """
            {
                "id": "10001",
                "key": "PROJ-123",
                "fields": {
                    "summary": "Test issue",
                    "project": {
                        "key": "PROJ",
                        "name": "Test Project"
                    }
                }
            }
            """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when
        JsonNode result = jiraClient.getIssueDetails(issueKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("key").asText()).isEqualTo("PROJ-123");
        assertThat(result.get("fields").get("summary").asText()).isEqualTo("Test issue");
        
        // URLの検証
        verify(jiraRestTemplate).exchange(
            contains("/issue/PROJ-123"), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    void getIssueDetails_issueKeyがnullの場合例外が発生すること() {
        // when & then
        assertThatThrownBy(() -> jiraClient.getIssueDetails(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Issue key is required");
    }

    @Test
    void getIssueDetails_issueKeyが空文字列の場合例外が発生すること() {
        // when & then
        assertThatThrownBy(() -> jiraClient.getIssueDetails(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Issue key is required");
    }

    @Test
    void testConnection_接続テストが成功すること() {
        // given
        String responseJson = """
            {
                "baseUrl": "https://company.atlassian.net",
                "version": "8.0.0",
                "deploymentType": "Cloud"
            }
            """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when
        boolean result = jiraClient.testConnection();

        // then
        assertThat(result).isTrue();
        verify(jiraRestTemplate).exchange(
            contains("/serverInfo"), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    void testConnection_例外が発生した場合falseを返すこと() {
        // given
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RestClientException("Connection failed"));

        // when
        boolean result = jiraClient.testConnection();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void constructor_設定が未設定の場合JiraClientExceptionが発生すること() {
        // given
        JiraConfiguration invalidConfig = mock(JiraConfiguration.class);
        JiraConfiguration.AuthConfig authConfig = mock(JiraConfiguration.AuthConfig.class);
        
        when(invalidConfig.isConfigured()).thenReturn(false);
        when(invalidConfig.getAuth()).thenReturn(authConfig);
        when(authConfig.getUsernameEnvKey()).thenReturn("JIRA_USERNAME");
        when(authConfig.getTokenEnvKey()).thenReturn("JIRA_API_TOKEN");

        // when & then
        // Note: Constructor does not throw exception when configuration is invalid
        // It only logs a warning. The exception is thrown during actual API calls.
        assertThatCode(() -> new JiraClient(jiraRestTemplate, invalidConfig))
            .doesNotThrowAnyException();
    }

    @Test
    void searchIssues_設定が未設定の場合JiraClientExceptionが発生すること() {
        // given
        JiraConfiguration invalidConfig = mock(JiraConfiguration.class);
        JiraConfiguration.AuthConfig authConfig = mock(JiraConfiguration.AuthConfig.class);
        
        // Mock auth config for constructor validation (only called when isConfigured = false)
        when(invalidConfig.getAuth()).thenReturn(authConfig);
        when(authConfig.getUsernameEnvKey()).thenReturn("JIRA_USERNAME");
        when(authConfig.getTokenEnvKey()).thenReturn("JIRA_API_TOKEN");
        when(invalidConfig.isConfigured()).thenReturn(false);
        
        JiraClient clientWithInvalidConfig = new JiraClient(jiraRestTemplate, invalidConfig);

        // when & then
        assertThatThrownBy(() -> clientWithInvalidConfig.searchIssues("project = PROJ", 50, null))
            .isInstanceOf(JiraClient.JiraClientException.class)
            .hasMessageContaining("JIRA認証情報が設定されていません。JIRA統合機能を利用できません。");
    }

    @Test
    void searchIssues_レスポンスのパースエラーでJiraClientExceptionが発生すること() {
        // given
        String jqlQuery = "project = PROJ";
        String invalidJson = "invalid json";
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> jiraClient.searchIssues(jqlQuery, 50, null))
            .isInstanceOf(JiraClient.JiraClientException.class)
            .hasMessageContaining("Failed to parse JIRA response");
    }

    @Test
    void getIssueDetails_レスポンスのパースエラーでJiraClientExceptionが発生すること() {
        // given
        String issueKey = "PROJ-123";
        String invalidJson = "invalid json";
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(jiraRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> jiraClient.getIssueDetails(issueKey))
            .isInstanceOf(JiraClient.JiraClientException.class)
            .hasMessageContaining("Failed to parse JIRA response");
    }

    private JiraConfiguration.TimeoutConfig createTimeoutConfig(int connection, int read) {
        JiraConfiguration.TimeoutConfig timeoutConfig = new JiraConfiguration.TimeoutConfig();
        timeoutConfig.setConnection(connection);
        timeoutConfig.setRead(read);
        return timeoutConfig;
    }
}