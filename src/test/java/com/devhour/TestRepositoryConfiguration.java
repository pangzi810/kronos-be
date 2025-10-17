package com.devhour;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.devhour.domain.repository.ProjectRepository;
import com.devhour.domain.repository.UserRepository;
import com.devhour.domain.repository.WorkCategoryRepository;
import com.devhour.domain.repository.WorkRecordRepository;

/**
 * テスト用設定クラス
 * 
 * Repositoryのモックを提供して、テスト実行時にBean定義エラーを回避
 */
@TestConfiguration
public class TestRepositoryConfiguration {
    
    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }
    
    @Bean
    @Primary
    public ProjectRepository projectRepository() {
        return mock(ProjectRepository.class);
    }
    
    @Bean
    @Primary
    public WorkRecordRepository workRecordRepository() {
        return mock(WorkRecordRepository.class);
    }
    
    @Bean
    @Primary
    public WorkCategoryRepository workCategoryRepository() {
        return mock(WorkCategoryRepository.class);
    }
    
}