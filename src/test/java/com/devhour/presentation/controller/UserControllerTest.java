package com.devhour.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.devhour.application.service.UserApplicationService;
import com.devhour.domain.model.entity.User;
import com.devhour.presentation.dto.response.UserListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * ユーザーコントローラー統合テスト
 */
@DisplayName("ユーザーコントローラー")
class UserControllerTest {
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @Mock
    private UserApplicationService userApplicationService;
    
    @InjectMocks
    private UserController userController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.devhour.presentation.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    
    
    @Test
    @DisplayName("ユーザー詳細取得 - 正常ケース")
    void getUser_Success() throws Exception {
        // Arrange
        String userId = "user-1";
        User mockUser = mock(User.class);
        when(userApplicationService.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk());
        
        verify(userApplicationService).findById(userId);
    }
    
    @Test
    @DisplayName("ユーザー詳細取得 - ユーザーが見つからない")
    void getUser_NotFound() throws Exception {
        // Arrange
        String userId = "nonexistent";
        when(userApplicationService.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound());
        
        verify(userApplicationService).findById(userId);
    }
    
    // Commented out: changePassword method was removed from UserApplicationService
    // @Test
    // @DisplayName("パスワード変更 - 正常ケース")
    // void changePassword_Success() throws Exception {
    //     // Arrange
    //     String userId = "user-1";
    //     PasswordChangeRequest request = new PasswordChangeRequest("newpassword123");
    //     User mockUser = mock(User.class);
    //     when(userApplicationService.changePassword(userId, request.getNewPassword()))
    //         .thenReturn(mockUser);
    //     
    //     // Act & Assert
    //     mockMvc.perform(patch("/api/users/" + userId + "/password")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isOk());
    //     
    //     verify(userApplicationService).changePassword(userId, "newpassword123");
    // }
    
    
    @Test
    @DisplayName("アクティブな開発者一覧取得 - 正常ケース")
    void getActiveDevelopers_Success() throws Exception {
        // Arrange
        List<User> developers = Arrays.asList(
            mock(User.class),
            mock(User.class)
        );
        when(userApplicationService.findActiveDevelopers()).thenReturn(developers);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/active/developers"))
                .andExpect(status().isOk());
        
        verify(userApplicationService).findActiveDevelopers();
    }
    
    @Test
    @DisplayName("アクティブな開発者一覧取得 - 空のリスト")
    void getActiveDevelopers_EmptyList() throws Exception {
        // Arrange
        List<User> emptyDevelopers = Arrays.asList();
        when(userApplicationService.findActiveDevelopers()).thenReturn(emptyDevelopers);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/active/developers"))
                .andExpect(status().isOk());
        
        verify(userApplicationService).findActiveDevelopers();
    }
    
    @Test
    @DisplayName("全ユーザー一覧取得 - デフォルトパラメータ")
    void getUsersWithPagination_DefaultParams() throws Exception {
        // Arrange
        UserListResponse mockResponse = mock(UserListResponse.class);
        when(userApplicationService.getUsersWithPagination(any())).thenReturn(mockResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
        
        verify(userApplicationService).getUsersWithPagination(any());
    }
    
    @Test
    @DisplayName("全ユーザー一覧取得 - カスタムパラメータ")
    void getUsersWithPagination_CustomParams() throws Exception {
        // Arrange
        UserListResponse mockResponse = mock(UserListResponse.class);
        when(userApplicationService.getUsersWithPagination(any())).thenReturn(mockResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                .param("page", "1")
                .param("size", "10")
                .param("sortBy", "username")
                .param("sortOrder", "ASC")
                .param("status", "ACTIVE")
                .param("search", "test"))
                .andExpect(status().isOk());
        
        verify(userApplicationService).getUsersWithPagination(any());
    }

}