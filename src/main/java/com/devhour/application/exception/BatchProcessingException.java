package com.devhour.application.exception;

/**
 * バッチ処理専用例外クラス
 * 
 * CSVファイルの読み込み、フォーマット検証、
 * ファイルアクセス関連のエラーに使用される例外
 * 
 * この例外は回復不可能なバッチ処理エラーを表し、
 * 処理を中止すべき状況で発生する
 */
public class BatchProcessingException extends Exception {
    
    /**
     * エラーメッセージを指定してBatchProcessingExceptionを作成
     * 
     * @param message エラーメッセージ
     */
    public BatchProcessingException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因を指定してBatchProcessingExceptionを作成
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public BatchProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}