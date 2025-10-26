package com.devhour.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import lombok.extern.slf4j.Slf4j;

/**
 * JIRA統合設定のバリデーター
 *
 * アプリケーション起動時にJIRA統合機能の設定を検証し、
 * 必須設定が不足している場合はエラーメッセージを出力してアプリケーションを終了する。
 *
 * 検証ルール:
 * - jira.integration.enabled=true の場合、以下が必須:
 *   - jira.base-url が正しく設定されていること
 *   - jira.auth.token (APIトークン) が設定されていること
 * - 設定が不足している場合、起動エラーとする
 *
 * @see JiraConfiguration
 */
@Configuration
@EnableConfigurationProperties(JiraConfiguration.class)
@Slf4j
public class JiraConfigurationValidator implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    private final JiraConfiguration jiraConfiguration;

    public JiraConfigurationValidator(Environment environment, JiraConfiguration jiraConfiguration) {
        this.environment = environment;
        this.jiraConfiguration = jiraConfiguration;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        boolean integrationEnabled = Boolean.parseBoolean(
            environment.getProperty("jira.integration.enabled", "false")
        );

        if (!integrationEnabled) {
            log.info("JIRA統合機能は無効です (jira.integration.enabled=false)");
            return;
        }

        log.info("JIRA統合機能は有効です (jira.integration.enabled=true) - 設定を検証します");

        // jira.base-url の検証
        String baseUrl = jiraConfiguration.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty() || baseUrl.contains("your-jira-instance")) {
            String errorMessage = String.format(
                "JIRA統合機能が有効ですが、jira.base-url が正しく設定されていません。%n" +
                "環境変数 JIRA_BASE_URL を設定するか、application.properties で jira.base-url を設定してください。%n" +
                "現在の値: %s%n" +
                "アプリケーションを終了します。",
                baseUrl
            );
            log.error(errorMessage);
            System.exit(1);
        }

        // 認証情報の検証（APIトークンのみ）
        if (!jiraConfiguration.isAuthenticationConfigured()) {
            String errorMessage = String.format(
                "JIRA統合機能が有効ですが、APIトークンが正しく設定されていません。%n" +
                "環境変数 JIRA_API_TOKEN を設定するか、%n" +
                "application.properties で jira.auth.token を設定してください。%n" +
                "アプリケーションを終了します。"
            );
            log.error(errorMessage);
            System.exit(1);
        }

        log.info("JIRA統合設定の検証が完了しました: baseUrl={}", baseUrl);
    }
}
