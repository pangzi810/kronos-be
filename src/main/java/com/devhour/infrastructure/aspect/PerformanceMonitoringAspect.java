package com.devhour.infrastructure.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * パフォーマンス監視アスペクト
 * 
 * 指定した閾値を超える処理時間のメソッドを警告ログとして出力
 * サービス層のパフォーマンス問題を早期に検出
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    
    @Value("${performance.slow-method-threshold:1000}")
    private long slowMethodThreshold; // デフォルト1000ms
    
    /**
     * サービス層のメソッドを対象とするポイントカット
     */
    @Pointcut("within(com.devhour.application.service..*) || " +
              "within(com.devhour.domain.service..*)")
    public void serviceMethods() {}
    
    /**
     * リポジトリ層のメソッドを対象とするポイントカット
     */
    @Pointcut("within(com.devhour.infrastructure.repository..*)")
    public void repositoryMethods() {}
    
    /**
     * サービスメソッドのパフォーマンス監視
     * 
     * @param joinPoint 実行ポイント
     * @return メソッドの戻り値
     * @throws Throwable メソッド実行時の例外
     */
    @Around("serviceMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorPerformance(joinPoint, "SERVICE");
    }
    
    /**
     * リポジトリメソッドのパフォーマンス監視
     * 
     * @param joinPoint 実行ポイント
     * @return メソッドの戻り値
     * @throws Throwable メソッド実行時の例外
     */
    @Around("repositoryMethods()")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorPerformance(joinPoint, "REPOSITORY");
    }
    
    /**
     * パフォーマンス監視の共通処理
     * 
     * @param joinPoint 実行ポイント
     * @param layer レイヤー名
     * @return メソッドの戻り値
     * @throws Throwable メソッド実行時の例外
     */
    private Object monitorPerformance(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            // メソッド実行
            Object result = joinPoint.proceed();
            
            // 実行時間計算
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 閾値を超えた場合は警告
            if (executionTime > slowMethodThreshold) {
                logger.warn("⚠️ SLOW {} METHOD: [{}#{}] took {}ms (threshold: {}ms)", 
                           layer, className, methodName, executionTime, slowMethodThreshold);
            } else if (logger.isTraceEnabled()) {
                // トレースレベルでは全ての実行時間を記録
                logger.trace("[{}] {}#{} - {}ms", layer, className, methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ {} METHOD FAILED: [{}#{}] after {}ms - {}", 
                        layer, className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}