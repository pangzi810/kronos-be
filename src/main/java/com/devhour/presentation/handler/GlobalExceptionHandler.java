package com.devhour.presentation.handler;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.devhour.domain.exception.ApprovalException;
import com.devhour.domain.exception.EntityNotFoundException;
import com.devhour.domain.exception.InvalidParameterException;
import com.devhour.domain.exception.UnauthorizedException;
import com.devhour.domain.exception.UserProvisioningException;
import com.devhour.presentation.dto.response.ErrorResponse;

/**
 * グローバル例外ハンドラー
 * 
 * アプリケーション全体で発生する例外を統一的にハンドリングし、
 * 適切なHTTPステータスコードとエラーレスポンスを返却する
 * 
 * 責務:
 * - ドメイン例外のHTTPレスポンス変換
 * - バリデーション例外の統一的な処理
 * - 予期しない例外の安全な処理
 * - ログ出力とエラートラッキング
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 承認処理例外のハンドリング
     * 
     * @param ex ApprovalException
     * @param request WebRequest
     * @return 403 Forbidden with error details
     */
    @ExceptionHandler(ApprovalException.class)
    public ResponseEntity<ErrorResponse> handleApprovalException(ApprovalException ex, WebRequest request) {
        String errorCode = "APPROVAL_ERROR";
        
        // エラータイプに応じてエラーコードを設定
        switch (ex.getType()) {
            case AUTHORITY:
                errorCode = "APPROVAL_AUTHORITY_ERROR";
                break;
            case OWNERSHIP:
                errorCode = "APPROVAL_OWNERSHIP_ERROR";
                break;
            case STATUS:
                errorCode = "APPROVAL_STATUS_ERROR";
                break;
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorCode,
            ex.getMessage()
        );
        
        // ステータス遷移エラーの場合は403 Forbidden、その他も403で統一
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(errorResponse);
    }
    
    /**
     * エンティティが見つからない例外のハンドリング
     * 
     * @param ex EntityNotFoundException
     * @param request WebRequest
     * @return 404 Not Found with error details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex,
                                                             WebRequest request) {
        String errorCode = "ENTITY_NOT_FOUND";
        if (ex.getEntityType() != null) {
            errorCode = ex.getEntityType().toUpperCase() + "_NOT_FOUND";
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorCode,
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }
    
    /**
     * 認証エラー例外のハンドリング
     * 
     * @param ex UnauthorizedException
     * @param request WebRequest
     * @return 401 Unauthorized with error details
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        log.warn("Authentication required: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of("AUTHENTICATION_REQUIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * バリデーション例外のハンドリング
     * 
     * @param ex MethodArgumentNotValidException
     * @param request WebRequest
     * @return 400 Bad Request with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                        WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR",
            "入力データの検証に失敗しました",
            details
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 必須パラメータ不足例外のハンドリング
     * 
     * @param ex MissingServletRequestParameterException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
                                                              WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "MISSING_PARAMETER",
            "必須パラメータが不足しています: " + ex.getParameterName()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * パラメータ型不一致例外のハンドリング
     * 
     * @param ex MethodArgumentTypeMismatchException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                           WebRequest request) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_PARAMETER_TYPE",
            "パラメータ '" + ex.getName() + "' の値 '" + ex.getValue() + "' は " + expectedType + " 型ではありません"
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * パラメーター不正例外のハンドリング
     * 
     * @param ex InvalidParameterException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException ex,
                                                              WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_ARGUMENT",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 引数不正例外のハンドリング
     * 
     * @param ex IllegalArgumentException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                             WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_ARGUMENT",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 状態不正例外のハンドリング
     * 
     * @param ex IllegalStateException
     * @param request WebRequest
     * @return 409 Conflict with error message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                          WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_STATE",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse);
    }
    
    /**
     * 認証失敗例外のハンドリング
     * 
     * @param ex BadCredentialsException
     * @param request WebRequest
     * @return 401 Unauthorized with error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                            WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "AUTHENTICATION_FAILED",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse);
    }
    
    /**
     * 認証例外のハンドリング
     * 
     * @param ex AuthenticationException
     * @param request WebRequest
     * @return 401 Unauthorized with error message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                            WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "AUTHENTICATION_ERROR",
            "認証に失敗しました"
        );
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse);
    }
    
    /**
     * リソースが見つからない場合の例外処理
     * 
     * @param ex 発生したNoResourceFoundException
     * @param request リクエスト情報
     * @return 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "RESOURCE_NOT_FOUND",
            "要求されたリソースが見つかりません"
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }

    /**
     * 予期しない例外のハンドリング
     * 
     * @param ex Exception
     * @param request WebRequest
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        // ログ出力（本来はロガーを使用）
        System.err.println("Unexpected error occurred: " + ex.getMessage());
        ex.printStackTrace();
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_ERROR",
            "内部エラーが発生しました。システム管理者にお問い合わせください。"
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }


    /**
     * アクセス拒否例外のハンドリング
     * 削除済みユーザーAPI用（PMOロール不足）
     * 
     * @param ex AccessDeniedException
     * @param request WebRequest
     * @return 403 Forbidden with error message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "FORBIDDEN",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(errorResponse);
    }

    /**
     * データソース例外のハンドリング
     * データベース接続エラー用
     * 
     * @param ex DataSourceLookupFailureException
     * @param request WebRequest
     * @return 503 Service Unavailable with error message
     */
    @ExceptionHandler(DataSourceLookupFailureException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "SERVICE_UNAVAILABLE",
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(errorResponse);
    }

    /**
     * データアクセス例外のハンドリング
     * データベースエラー用
     * 
     * @param ex DataAccessException
     * @param request WebRequest
     * @return 500 Internal Server Error with generic error message
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleInternalError(DataAccessException ex, WebRequest request) {
        // ログ出力
        ex.printStackTrace();
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_ERROR",
            "内部エラーが発生しました。システム管理者にお問い合わせください。"
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

    /**
     * 日付解析例外のハンドリング
     * 
     * @param ex DateTimeParseException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(DateTimeParseException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_DATE_FORMAT",
            "日付形式が正しくありません: " + ex.getParsedString()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 数値形式例外のハンドリング
     * 
     * @param ex NumberFormatException
     * @param request WebRequest
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormat(NumberFormatException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVALID_NUMBER_FORMAT",
            "数値形式が正しくありません: " + ex.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 一般的なJWT例外のハンドリング
     * Spring SecurityのJwtExceptionをキャッチ
     * 
     * @param ex JwtException
     * @param request WebRequest
     * @return 401 Unauthorized with error message
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleGeneralJwtException(JwtException ex, WebRequest request) {
        log.warn("General JWT error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "JWT_VALIDATION_ERROR",
            "JWT token validation failed - please check your token"
        );
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse);
    }

    /**
     * ユーザープロビジョニング例外のハンドリング
     * 
     * @param ex UserProvisioningException
     * @param request WebRequest
     * @return HTTP status based on exception type
     */
    @ExceptionHandler(UserProvisioningException.class)
    public ResponseEntity<ErrorResponse> handleUserProvisioningException(
            UserProvisioningException ex, WebRequest request) {
        log.error("User provisioning error: {}", ex.getMessage(), ex);
        
        // エラーコードの決定
        String errorCode = determineProvisioningErrorCode(ex);
        HttpStatus httpStatus = determineProvisioningHttpStatus(ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorCode,
            ex.getMessage()
        );
        
        return ResponseEntity
            .status(httpStatus)
            .body(errorResponse);
    }
    
    /**
     * プロビジョニングエラーのエラーコードを決定
     */
    private String determineProvisioningErrorCode(UserProvisioningException ex) {
        if (ex.getErrorContext() != null) {
            switch (ex.getErrorContext()) {
                case "DUPLICATE_USER":
                    return "DUPLICATE_USER_ERROR";
                case "USER_SYNC":
                    return "USER_SYNC_ERROR";
                case "USER_CREATION":
                    return "USER_CREATION_ERROR";
                case "USER_UPDATE":
                    return "USER_UPDATE_ERROR";
                case "INSUFFICIENT_RIGHTS":
                    return "INSUFFICIENT_PROVISIONING_RIGHTS";
                default:
                    return "USER_PROVISIONING_ERROR";
            }
        }
        return "USER_PROVISIONING_ERROR";
    }
    
    /**
     * プロビジョニングエラーのHTTPステータスを決定
     */
    private HttpStatus determineProvisioningHttpStatus(UserProvisioningException ex) {
        if (ex.getErrorContext() != null) {
            switch (ex.getErrorContext()) {
                case "DUPLICATE_USER":
                    return HttpStatus.CONFLICT;
                case "INSUFFICIENT_RIGHTS":
                    return HttpStatus.FORBIDDEN;
                default:
                    return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}