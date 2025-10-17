package com.devhour.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.devhour.domain.model.entity.User;
import com.devhour.domain.repository.UserRepository;
import com.devhour.infrastructure.mapper.UserMapper;
import com.devhour.presentation.dto.UserSearchCriteria;

/**
 * ユーザーリポジトリ実装クラス
 * 
 * MyBatisマッパーを使用してUserRepositoryインターフェースを実装
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    
    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    public Optional<User> findById(String userId) {
        return userMapper.findById(userId);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByEmailAndDeletedAtIsNull(String email) {
        return userMapper.findByEmailAndDeletedAtIsNull(email);
    }
    
    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }
    
    @Override
    public User save(User user) {
        if (userMapper.findById(user.getId()).isEmpty()) {
            // 新規作成
            if (user.getOktaUserId() != null) {
                // Oktaユーザーの場合
                userMapper.insertWithOkta(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getUserStatus(),
                    user.getLastLoginAt(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getOktaUserId()
                );
            } else {
                // 通常ユーザーの場合
                userMapper.insert(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getUserStatus(),
                    user.getLastLoginAt(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                );
            }
        } else {
            // 更新
            if (user.getOktaUserId() != null) {
                // Oktaユーザーの場合（Okta情報も更新）
                userMapper.updateWithOkta(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getUpdatedAt(),
                    user.getOktaUserId()
                );
            } else {
                // 通常ユーザーの場合
                userMapper.update(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getUpdatedAt()
                );
            }
        }
        return user;
    }
    
    
    @Override
    public void updateActiveStatus(String userId, boolean isActive) {
        User.UserStatus status = isActive ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE;
        userMapper.updateActiveStatus(userId, status, LocalDateTime.now());
    }
    
    /**
     * ユーザーステータス更新
     */
    @Override
    public void updateUserStatus(String userId, User.UserStatus userStatus) {
        userMapper.updateUserStatus(userId, userStatus, LocalDateTime.now());
    }
    
    /**
     * 最終ログイン時刻更新
     */
    @Override
    public void updateLastLoginAt(String userId, LocalDateTime lastLoginAt) {
        userMapper.updateLastLoginAt(userId, lastLoginAt, LocalDateTime.now());
    }
    
    @Override
    public void deleteById(String userId) {
        throw new UnsupportedOperationException("Delete operation is not supported. Use soft delete instead.");
        // userMapper.softDelete(userId, LocalDateTime.now(), LocalDateTime.now());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public Optional<User> findByOktaUserId(String oktaUserId) {
        return userMapper.findByOktaUserId(oktaUserId);
    }

    @Override
    public boolean existsByOktaUserId(String oktaUserId) {
        return userMapper.existsByOktaUserId(oktaUserId);
    }
    
    @Override
    public boolean existsById(String userId) {
        return userMapper.findById(userId).isPresent();
    }
    
    @Override
    public List<User> findAllActive() {
        return userMapper.findAllActive();
    }
    
    
    
    
    
    @Override
    public List<User> searchByFullName(String fullNamePattern) {
        return userMapper.searchByFullName(fullNamePattern);
    }
    
    @Override
    public List<User> searchUsers(String username, String email, Boolean isActive) {
        String userStatus = null;
        if (isActive != null) {
            userStatus = isActive ? "ACTIVE" : "INACTIVE";
        }
        return userMapper.searchUsers(username, email, userStatus);
    }
    
    @Override
    public List<User> findUsersWithPagination(UserSearchCriteria criteria) {
        try {
            String userStatusParam = null;
            
            if (criteria.getStatus() != null) {
                userStatusParam = criteria.getStatus();
            }
            
            return userMapper.selectUsersWithPagination(
                criteria.getSearch(),
                userStatusParam,
                criteria.getSortBy(),
                criteria.getSortOrder(),
                criteria.getSize(),
                criteria.getOffset()
            );
        } catch (Exception e) {
            throw new RuntimeException("ユーザーのページネーション検索に失敗しました: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long countUsers(UserSearchCriteria criteria) {
        try {
            String userStatusParam = null;
            
            if (criteria.getStatus() != null) {
                userStatusParam = criteria.getStatus();
            }
            
            return userMapper.countUsers(
                criteria.getSearch(),
                userStatusParam
            );
        } catch (Exception e) {
            throw new RuntimeException("ユーザー数のカウントに失敗しました: " + e.getMessage(), e);
        }
    }
}