package com.devhour.application.exception;

/**
 * レコード検証専用例外クラス
 * 
 * CSVファイル内の個別レコードの検証エラーに使用される例外
 * この例外が発生した場合、該当レコードはスキップされるが
 * 他のレコードの処理は継続される
 * 
 * 主な使用場面:
 * - メールアドレス形式の不正
 * - 必須項目の欠落
 * - 役職名の不正
 * - データ長制限違反
 */
public class RecordValidationException extends Exception {
    
    /**
     * エラーメッセージを指定してRecordValidationExceptionを作成
     * 
     * @param message エラーメッセージ
     */
    public RecordValidationException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因を指定してRecordValidationExceptionを作成
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public RecordValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}