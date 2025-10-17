package com.devhour.presentation.dto.response;

import com.devhour.domain.model.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ユーザー一覧レスポンスDTO
 * 
 * ユーザー一覧API(/api/users)のレスポンスボディ
 * ユーザーのリストとページネーション情報を含む
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    
    /**
     * ユーザーリスト
     */
    private List<UserDto> users;
    
    /**
     * ページネーションメタデータ
     */
    private PageMetadata metadata;
    
    /**
     * ユーザー情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        
        /**
         * ユーザーID
         */
        private String userId;
        
        /**
         * ユーザー名
         */
        private String username;
        
        /**
         * メールアドレス
         */
        private String email;
        
        /**
         * フルネーム
         */
        private String fullName;
        
        
        /**
         * アクティブフラグ
         */
        private boolean active;
        
        /**
         * 作成日時
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        /**
         * 更新日時
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        
        /**
         * UserエンティティからDTOへの変換
         */
        public static UserDto fromEntity(User user) {
            return UserDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }
    
    /**
     * ページネーションメタデータ
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        
        /**
         * 総要素数
         */
        private long totalElements;
        
        /**
         * 総ページ数
         */
        private int totalPages;
        
        /**
         * 現在のページ番号（0始まり）
         */
        private int currentPage;
        
        /**
         * ページサイズ
         */
        private int pageSize;
        
        /**
         * 次のページが存在するか
         */
        private boolean hasNext;
        
        /**
         * 前のページが存在するか
         */
        private boolean hasPrevious;
    }
    
    /**
     * ユーザーリストとページネーション情報からレスポンスを作成
     */
    public static UserListResponse of(List<User> users, long totalElements, int page, int size) {
        List<UserDto> userDtos = users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        PageMetadata metadata = PageMetadata.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
        
        return UserListResponse.builder()
                .users(userDtos)
                .metadata(metadata)
                .build();
    }
}