package com.devhour.domain.exception;

/**
 * 不正パラメーター例外
 * 
 * ビジネスルールに違反するパラメーター値が入力された際に発生する例外
 * この例外はクライアントエラー（400 Bad Request）として扱われる
 * 
 * 使用例:
 * - 日付範囲の制限違反
 * - 必須パラメーターの欠如
 * - フォーマット違反
 * - ビジネスルールに基づく値の制約違反
 */
public class InvalidParameterException extends DomainException {
    
    /**
     * メッセージ付きコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public InvalidParameterException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因付きコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}