package com.devhour.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AspectJ設定クラス
 * 
 * AOPを有効化し、アスペクトによる横断的関心事の実装を可能にする
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {
    // AspectJの自動プロキシを有効化
    // これによりControllerLoggingAspectなどのアスペクトが動作する
}