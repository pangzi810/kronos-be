package com.devhour.infrastructure.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * コントローラーメソッドのログ出力アスペクト
 * 
 * 全てのコントローラーメソッドの開始・終了・例外をログ出力する横断的関心事の実装
 * MDC (Mapped Diagnostic Context) を使用してトレーサビリティを向上
 */
@Aspect
@Component
public class ControllerLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);
    
    /**
     * コントローラーパッケージの全メソッドを対象とするポイントカット
     */
    @Pointcut("within(com.devhour.presentation.controller..*)")
    public void controllerMethods() {}
    
    /**
     * REST APIアノテーション付きメソッドを対象とするポイントカット
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void restApiMethods() {}
    
    /**
     * メソッド実行前後のログ出力とパフォーマンス計測
     * 
     * @param joinPoint 実行ポイント
     * @return メソッドの戻り値
     * @throws Throwable メソッド実行時の例外
     */
    @Around("controllerMethods() && restApiMethods()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // リクエストパラメータをログ用に整形
        Map<String, Object> requestParams = extractRequestParameters(joinPoint);
        
        // メソッド開始ログ
        logger.info("==> [{}#{}] REQUEST START - Parameters: {}", 
                   className, methodName, requestParams);
        
        try {
            // メソッド実行
            Object result = joinPoint.proceed();
            
            // 実行時間計算
            long executionTime = System.currentTimeMillis() - startTime;
            
            // メソッド終了ログ（成功）
            logger.info("<== [{}#{}] REQUEST COMPLETED - Execution time: {}ms", 
                       className, methodName, executionTime);
            
            // デバッグレベルで結果の概要を出力
            if (logger.isDebugEnabled()) {
                logResultSummary(className, methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            // 実行時間計算
            long executionTime = System.currentTimeMillis() - startTime;
            
            // エラーログ
            logger.error("<== [{}#{}] REQUEST FAILED - Execution time: {}ms, Error: {}", 
                        className, methodName, executionTime, e.getMessage());
            
            // 例外を再スロー
            throw e;
        }
    }
    
    /**
     * リクエストパラメータを抽出
     * 
     * @param joinPoint 実行ポイント
     * @return パラメータマップ
     */
    private Map<String, Object> extractRequestParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        
        try {
            Method method = getMethod(joinPoint);
            if (method == null) return params;
            
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Object arg = args[i];
                
                // @PathVariable パラメータ
                if (param.isAnnotationPresent(PathVariable.class)) {
                    PathVariable pathVar = param.getAnnotation(PathVariable.class);
                    String paramName = pathVar.value().isEmpty() ? param.getName() : pathVar.value();
                    params.put(paramName, sanitizeValue(arg));
                }
                
                // @RequestParam パラメータ
                else if (param.isAnnotationPresent(RequestParam.class)) {
                    RequestParam requestParam = param.getAnnotation(RequestParam.class);
                    String paramName = requestParam.value().isEmpty() ? param.getName() : requestParam.value();
                    params.put(paramName, sanitizeValue(arg));
                }
                
                // @RequestBody は内容が大きい可能性があるため、型名のみ記録
                else if (param.isAnnotationPresent(org.springframework.web.bind.annotation.RequestBody.class)) {
                    params.put("requestBody", arg != null ? arg.getClass().getSimpleName() : "null");
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract request parameters: {}", e.getMessage());
        }
        
        return params;
    }
    
    /**
     * メソッドオブジェクトを取得
     * 
     * @param joinPoint 実行ポイント
     * @return メソッドオブジェクト
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            
            // パラメータ型の配列を取得
            Class<?>[] paramTypes = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.getClass() : null)
                .toArray(Class<?>[]::new);
            
            // メソッドを検索
            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName) && 
                    method.getParameterCount() == paramTypes.length) {
                    return method;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to get method: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 値をサニタイズ（パスワードなどの機密情報をマスク）
     * 
     * @param value 値
     * @return サニタイズされた値
     */
    private Object sanitizeValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        String valueStr = value.toString();
        
        // パスワード関連のパラメータはマスク
        if (valueStr.toLowerCase().contains("password")) {
            return "***MASKED***";
        }
        
        // 長すぎる値は切り詰め
        if (valueStr.length() > 100) {
            return valueStr.substring(0, 100) + "...(truncated)";
        }
        
        return valueStr;
    }
    
    /**
     * 結果の概要をログ出力
     * 
     * @param className クラス名
     * @param methodName メソッド名
     * @param result 結果オブジェクト
     */
    private void logResultSummary(String className, String methodName, Object result) {
        if (result == null) {
            logger.debug("[{}#{}] Result: null", className, methodName);
            return;
        }
        
        // ResponseEntityの場合はステータスコードを出力
        if (result instanceof org.springframework.http.ResponseEntity<?>) {
            org.springframework.http.ResponseEntity<?> response = 
                (org.springframework.http.ResponseEntity<?>) result;
            logger.debug("[{}#{}] Response Status: {}, Has Body: {}", 
                        className, methodName, 
                        response.getStatusCode(), 
                        response.hasBody());
        }
        // コレクションの場合はサイズを出力
        else if (result instanceof java.util.Collection<?>) {
            logger.debug("[{}#{}] Result: Collection size={}", 
                        className, methodName, 
                        ((java.util.Collection<?>) result).size());
        }
        // その他の場合は型名を出力
        else {
            logger.debug("[{}#{}] Result: {}", 
                        className, methodName, 
                        result.getClass().getSimpleName());
        }
    }
}