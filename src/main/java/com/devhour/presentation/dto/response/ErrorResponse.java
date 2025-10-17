package com.devhour.presentation.dto.response;

import java.time.LocalDateTime;

/**
 * API エラーレスポンス DTO
 * 
 * RESTful API でのエラー情報を統一的に表現するデータ転送オブジェクト
 * 
 * 責務:
 * - エラーコード、メッセージ、詳細情報の構造化
 * - エラーレスポンスの標準形式提供
 * - タイムスタンプによるエラー発生時刻の記録
 */
public record ErrorResponse(
    String code,
    String message,
    String details,
    LocalDateTime timestamp
) {
    /**
     * 簡略版エラーレスポンス作成
     * 
     * @param code エラーコード
     * @param message エラーメッセージ
     * @return ErrorResponseインスタンス
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, LocalDateTime.now());
    }
    
    /**
     * 詳細情報付きエラーレスポンス作成
     * 
     * @param code エラーコード
     * @param message エラーメッセージ
     * @param details 詳細情報
     * @return ErrorResponseインスタンス
     */
    public static ErrorResponse of(String code, String message, String details) {
        return new ErrorResponse(code, message, details, LocalDateTime.now());
    }
}