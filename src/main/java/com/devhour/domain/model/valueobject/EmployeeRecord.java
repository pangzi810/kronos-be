package com.devhour.domain.model.valueobject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.devhour.domain.model.entity.ApprovalAuthority;

/**
 * 従業員レコード値オブジェクト
 * 
 * CSVファイルから読み込んだ従業員データを表現する不変オブジェクト
 * バッチ処理やCSVインポート機能で使用
 * 
 * ビジネスルール:
 * - email、name、positionは必須
 * - emailは有効な形式である必要がある  
 * - positionは既存のPosition列挙型に対応する日本語名である必要がある
 * - level1〜level4の組織情報は任意（nullを許可）
 * - 承認権限の判定が可能
 * - ApprovalAuthorityエンティティへの変換が可能
 * 
 * CSVフォーマット対応:
 * メールアドレス,氏名,最上位の組織コード,最上位の組織名,２階層目の組織コード,２階層目の組織名,３階層目の組織コード,３階層目の組織名,４階層目の組織コード,４階層目の組織名,役職
 */
public record EmployeeRecord(
    String email,
    String name, 
    String position,
    String level1Code,
    String level1Name,
    String level2Code,
    String level2Name,
    String level3Code,
    String level3Name,
    String level4Code,
    String level4Name
) {
    
    /**
     * メールアドレスの形式検証用正規表現
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * コンストラクタ - レコードの compact constructor
     * 値の検証を実行し、必要に応じて空白をトリムする
     * 
     * @param email メールアドレス（必須）
     * @param name 名前（必須）
     * @param position 役職（必須、日本語名）
     * @param level1Code Level1組織コード（任意）
     * @param level1Name Level1組織名（任意）
     * @param level2Code Level2組織コード（任意）
     * @param level2Name Level2組織名（任意）
     * @param level3Code Level3組織コード（任意）
     * @param level3Name Level3組織名（任意）
     * @param level4Code Level4組織コード（任意）
     * @param level4Name Level4組織名（任意）
     * @throws IllegalArgumentException バリデーションエラーの場合
     */
    public EmployeeRecord {
        // 必須項目の検証とトリム
        email = validateAndTrimRequiredField(email, "メールアドレス");
        name = validateAndTrimRequiredField(name, "名前");
        position = validateAndTrimRequiredField(position, "役職");
        
        // メールアドレス形式の検証
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません");
        }
        
        // 役職の検証（Position列挙型に存在するかチェック）
        try {
            Position.fromJapaneseName(position);
        } catch (IllegalArgumentException e) {
            throw e; // Position側のエラーメッセージをそのまま使用
        }
        
        // 任意項目のトリム（nullでない場合のみ）
        level1Code = trimIfNotNull(level1Code);
        level1Name = trimIfNotNull(level1Name);
        level2Code = trimIfNotNull(level2Code);
        level2Name = trimIfNotNull(level2Name);
        level3Code = trimIfNotNull(level3Code);
        level3Name = trimIfNotNull(level3Name);
        level4Code = trimIfNotNull(level4Code);
        level4Name = trimIfNotNull(level4Name);
    }
    
    /**
     * 必須フィールドの検証とトリム
     */
    private static String validateAndTrimRequiredField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "は必須です");
        }
        return value.trim();
    }
    
    /**
     * null でない場合にトリムする
     */
    private static String trimIfNotNull(String value) {
        return value != null ? value.trim() : null;
    }
    
    /**
     * 承認権限を持っているかチェック
     * Positionの日本語名から判定
     * 
     * @return 承認権限がある場合true
     */
    public boolean hasApprovalAuthority() {
        Position pos = Position.fromJapaneseName(position);
        return pos.hasApprovalAuthority();
    }
    
    /**
     * 最上位の組織コードを取得
     * Level4 > Level3 > Level2 > Level1 の順で設定されているものを返す
     * 
     * @return 最上位の組織コード、すべてnullの場合はnull
     */
    public String getHighestLevelCode() {
        if (level4Code != null) return level4Code;
        if (level3Code != null) return level3Code;
        if (level2Code != null) return level2Code;
        if (level1Code != null) return level1Code;
        return null;
    }
    
    /**
     * 最上位の組織名を取得
     * Level4 > Level3 > Level2 > Level1 の順で設定されているものを返す
     * 
     * @return 最上位の組織名、すべてnullの場合はnull
     */
    public String getHighestLevelName() {
        if (level4Name != null) return level4Name;
        if (level3Name != null) return level3Name;
        if (level2Name != null) return level2Name;
        if (level1Name != null) return level1Name;
        return null;
    }
    
    /**
     * 組織パスを取得（組織名）
     * Level1からLevel4までの組織名を「>」で繋げたパス
     * nullの場合はスキップする
     * 
     * @return 組織名のパス文字列、すべてnullの場合は空文字
     */
    public String getOrganizationPath() {
        List<String> pathParts = new ArrayList<>();
        
        if (level1Name != null && !level1Name.trim().isEmpty()) {
            pathParts.add(level1Name.trim());
        }
        if (level2Name != null && !level2Name.trim().isEmpty()) {
            pathParts.add(level2Name.trim());
        }
        if (level3Name != null && !level3Name.trim().isEmpty()) {
            pathParts.add(level3Name.trim());
        }
        if (level4Name != null && !level4Name.trim().isEmpty()) {
            pathParts.add(level4Name.trim());
        }
        
        return String.join(" > ", pathParts);
    }
    
    /**
     * ApprovalAuthorityエンティティに変換（ファクトリーメソッド）
     * 
     * @return 新しいApprovalAuthorityエンティティ
     * @throws IllegalArgumentException エンティティ作成時のバリデーションエラー
     */
    public ApprovalAuthority toApprovalAuthority() {
        Position pos = Position.fromJapaneseName(position);
        
        return ApprovalAuthority.create(
            email,
            name,
            pos,
            level1Code,
            level1Name,
            level2Code,
            level2Name,
            level3Code,
            level3Name,
            level4Code,
            level4Name
        );
    }
    
    /**
     * 文字列表現
     * デバッグやログ出力用
     */
    @Override
    public String toString() {
        return String.format("EmployeeRecord{email='%s', name='%s', position='%s', organizationPath='%s'}", 
                           email, name, position, getOrganizationPath());
    }
}