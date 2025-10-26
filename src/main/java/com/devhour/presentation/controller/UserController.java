package com.devhour.presentation.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.devhour.application.service.UserApplicationService;
import com.devhour.domain.model.entity.User;
import com.devhour.presentation.dto.UserSearchCriteria;
import com.devhour.presentation.dto.response.UserListResponse;

/**
 * ユーザー管理REST APIコントローラー
 * 
 * ユーザーの作成・更新・削除・検索機能を提供
 * 
 * エンドポイント: - POST /api/users: ユーザー作成 - PUT /api/users/{id}: ユーザー情報更新 - GET /api/users/{id}:
 * ユーザー詳細取得 - GET /api/users/active/developers: アクティブな開発者一覧取得
 * - PATCH /api/users/{id}/activate: ユーザー有効化 - PATCH /api/users/{id}/deactivate: ユーザー無効化 -
 * GET /api/users/exists/username/{username}: ユーザー名存在チェック - GET /api/users/exists/email/{email}:
 * メールアドレス存在チェック
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * ユーザー詳細取得
     * 
     * @param userId ユーザーID
     * @return ユーザー詳細情報
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        Optional<User> user = userApplicationService.findById(userId);

        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * アクティブな開発者一覧取得
     * 
     * プロジェクトアサインやリソース管理で使用する開発者一覧を取得 認証されたユーザーのみアクセス可能
     * 
     * @return アクティブな開発者一覧レスポンス
     */
    @GetMapping("/active/developers")
    public ResponseEntity<List<User>> getActiveDevelopers() {
        List<User> developers = userApplicationService.findActiveDevelopers();

        return ResponseEntity.ok(developers);
    }

    /**
     * 全ユーザー一覧取得（ページネーション対応）
     * 
     * @param page ページ番号（0始まり、デフォルト0）
     * @param size ページサイズ（デフォルト20、最大100）
     * @param sortBy ソート項目（デフォルトcreatedAt）
     * @param sortOrder ソート順序（ASC/DESC、デフォルトDESC）
     * @param status ステータスフィルタ（ACTIVE/INACTIVE）
     * @param search 検索キーワード（ユーザー名、メール、フルネームの部分一致）
     * @return ユーザー一覧とページネーション情報
     */
    @GetMapping
    public ResponseEntity<?> getUsersWithPagination(@RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        // 検索条件を構築
        UserSearchCriteria criteria =
                UserSearchCriteria.builder().page(page).size(size).sortBy(sortBy)
                        .sortOrder(sortOrder).status(status).search(search).build();

        // ページネーション対応のユーザー一覧を取得
        UserListResponse response = userApplicationService.getUsersWithPagination(criteria);

        return ResponseEntity.ok(response);
    }
}
