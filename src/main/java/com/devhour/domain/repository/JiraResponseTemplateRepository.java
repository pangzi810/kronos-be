package com.devhour.domain.repository;

import java.util.List;
import java.util.Optional;
import com.devhour.domain.model.entity.JiraResponseTemplate;

/**
 * レスポンステンプレートリポジトリインターフェース
 * 
 * JIRA同期機能で使用するレスポンステンプレートエンティティの永続化を担当するリポジトリ
 * ドメイン駆動設計におけるRepositoryパターンの実装
 * 
 * 責務:
 * - レスポンステンプレートエンティティのCRUD操作
 * - テンプレート名による検索と重複チェック機能（テンプレート選択に必要）
 * - 名前パターンによるテンプレート検索（管理画面での検索機能）
 * - 利用可能テンプレート一覧の取得（アクティブなテンプレートの管理）
 * - ページネーション対応の一覧取得
 * - テンプレート統計情報の取得
 * 
 * ビジネス要件:
 * - REQ-5.1: フィールドマッピング設定画面でのテンプレート管理
 * - REQ-5.7: テンプレート検証・テスト機能
 * - テンプレート名の一意性制約（重複禁止）
 * - JsonPath形式でのJIRAフィールドマッピング管理
 * - VelocityテンプレートエンジンによるJSON変換
 */
public interface JiraResponseTemplateRepository {
    
    /**
     * レスポンステンプレートIDでテンプレートを検索
     * 
     * @param id レスポンステンプレートID
     * @return レスポンステンプレートエンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException idがnullの場合
     */
    Optional<JiraResponseTemplate> findById(String id);
    
    /**
     * テンプレート名でレスポンステンプレートを検索
     * 
     * テンプレート名の重複チェックや特定テンプレートの検索に使用
     * テンプレート選択機能での名前ベース検索に必要
     * 
     * @param templateName テンプレート名
     * @return レスポンステンプレートエンティティ（存在しない場合は空のOptional）
     * @throws IllegalArgumentException templateNameがnullの場合
     */
    Optional<JiraResponseTemplate> findByTemplateName(String templateName);
    
    /**
     * 全レスポンステンプレート一覧を取得
     * 
     * 管理画面でのテンプレート一覧表示に使用
     * テンプレート名昇順でソート
     * 
     * @return 全レスポンステンプレートのリスト
     */
    List<JiraResponseTemplate> findAll();
    
    /**
     * 利用可能なレスポンステンプレート一覧を取得
     * 
     * JQLクエリ設定時のテンプレート選択や、
     * アクティブなテンプレートの管理に使用
     * テンプレート名昇順でソート
     * 
     * @return 利用可能なレスポンステンプレートのリスト
     */
    List<JiraResponseTemplate> findAvailableTemplates();
    
    /**
     * テンプレート名パターンでレスポンステンプレートを検索
     * 
     * 管理画面でのテンプレート検索機能に使用
     * 部分一致検索（大文字小文字区別あり）
     * 
     * @param pattern 検索パターン（部分一致）
     * @return 検索条件に一致するレスポンステンプレートリスト（テンプレート名昇順）
     * @throws IllegalArgumentException patternがnullの場合
     */
    List<JiraResponseTemplate> searchByNamePattern(String pattern);
    
    /**
     * テンプレート名の存在チェック
     * 
     * テンプレート作成時の重複チェックに使用
     * テンプレート名の一意性制約を保証
     * 
     * @param templateName チェックするテンプレート名
     * @return 存在する場合true
     * @throws IllegalArgumentException templateNameがnullの場合
     */
    boolean existsByTemplateName(String templateName);
    
    /**
     * テンプレート名の存在チェック（指定IDを除外）
     * 
     * テンプレート更新時の重複チェックに使用
     * 自分自身を除外してテンプレート名の重複をチェック
     * 
     * @param templateName チェックするテンプレート名
     * @param excludeId 除外するテンプレートID
     * @return 存在する場合true
     * @throws IllegalArgumentException templateName または excludeId がnullの場合
     */
    boolean existsByTemplateNameExcludingId(String templateName, String excludeId);
    
    /**
     * レスポンステンプレートの存在チェック
     * 
     * @param id レスポンステンプレートID
     * @return 存在する場合true
     * @throws IllegalArgumentException idがnullの場合
     */
    boolean existsById(String id);
    
    /**
     * レスポンステンプレートを保存
     * 
     * 新規作成・更新の両方で使用
     * テンプレート名の重複チェックはアプリケーションサービスで実行
     * 
     * @param responseTemplate 保存対象のレスポンステンプレートエンティティ
     * @return 保存されたレスポンステンプレートエンティティ
     * @throws IllegalArgumentException responseTemplateがnullの場合
     */
    JiraResponseTemplate save(JiraResponseTemplate responseTemplate);
    
    /**
     * 複数レスポンステンプレートを一括保存
     * 
     * 大量データの同期処理や初期データ投入に使用
     * 
     * @param responseTemplates 保存対象のレスポンステンプレートエンティティのリスト
     * @return 保存されたレスポンステンプレートエンティティのリスト
     * @throws IllegalArgumentException responseTemplatesがnullの場合
     */
    List<JiraResponseTemplate> saveAll(List<JiraResponseTemplate> responseTemplates);
    
    /**
     * レスポンステンプレートを削除
     * 
     * 物理削除を実行（レスポンステンプレートは論理削除ではなく物理削除）
     * 関連するJQLクエリとの関連は削除前に確認が必要
     * 
     * @param id 削除対象のレスポンステンプレートID
     * @throws IllegalArgumentException idがnullの場合
     */
    void deleteById(String id);
    
    /**
     * ページネーション対応でレスポンステンプレート一覧を取得
     * 
     * 管理画面でのページング表示に使用（REQ-5.1対応）
     * 作成日時降順でソート（新しいテンプレートが先頭）
     * 
     * @param limit 取得件数制限
     * @param offset 取得開始位置
     * @return レスポンステンプレートリスト
     * @throws IllegalArgumentException limit < 0 または offset < 0の場合
     */
    List<JiraResponseTemplate> findAllWithPagination(int limit, int offset);
    
    /**
     * 全レスポンステンプレート数をカウント
     * 
     * ページネーション計算や統計情報取得に使用
     * 
     * @return レスポンステンプレート総数
     */
    long countAll();
}