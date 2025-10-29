package com.devhour.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Okta JWT トークンから groups クレームを抽出するユーティリティクラス
 *
 * Oktaのグループ情報を取得し、アプリケーション内での権限管理に使用する
 */
@Component
public class OktaGroupsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(OktaGroupsExtractor.class);

    /**
     * JWTトークンからグループ情報を抽出
     *
     * @param jwt JWTトークン
     * @return グループ名のリスト（グループが存在しない場合は空リスト）
     */
    public List<String> extractGroups(Jwt jwt) {
        if (jwt == null) {
            logger.warn("JWT is null, returning empty groups list");
            return Collections.emptyList();
        }

        try {
            // groupsクレームを取得
            Object groupsClaim = jwt.getClaim("groups");

            if (groupsClaim == null) {
                logger.debug("No groups claim found in JWT for user: {}", jwt.getSubject());
                return Collections.emptyList();
            }

            List<String> groups = new ArrayList<>();

            if (groupsClaim instanceof List<?> list) {
                // List形式のgroupsクレーム
                for (Object item : list) {
                    if (item instanceof String groupName) {
                        groups.add(groupName);
                    }
                }
            } else if (groupsClaim instanceof String groupString) {
                // 文字列形式（カンマ区切り）の場合
                String[] groupArray = groupString.split(",");
                for (String group : groupArray) {
                    String trimmed = group.trim();
                    if (!trimmed.isEmpty()) {
                        groups.add(trimmed);
                    }
                }
            }

            logger.debug("Extracted {} groups for user {}: {}",
                        groups.size(), jwt.getSubject(), groups);

            return groups;

        } catch (Exception e) {
            logger.error("Error extracting groups from JWT for user {}: {}",
                        jwt.getSubject(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * ユーザーが特定のグループに所属しているか確認
     *
     * @param jwt JWTトークン
     * @param groupName 確認するグループ名
     * @return グループに所属している場合true
     */
    public boolean hasGroup(Jwt jwt, String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return false;
        }

        List<String> groups = extractGroups(jwt);
        return groups.contains(groupName);
    }

    /**
     * ユーザーが指定されたグループのいずれかに所属しているか確認
     *
     * @param jwt JWTトークン
     * @param groupNames 確認するグループ名のリスト
     * @return いずれかのグループに所属している場合true
     */
    public boolean hasAnyGroup(Jwt jwt, List<String> groupNames) {
        if (groupNames == null || groupNames.isEmpty()) {
            return false;
        }

        List<String> userGroups = extractGroups(jwt);
        return groupNames.stream().anyMatch(userGroups::contains);
    }

    /**
     * ユーザーが指定された全てのグループに所属しているか確認
     *
     * @param jwt JWTトークン
     * @param groupNames 確認するグループ名のリスト
     * @return 全てのグループに所属している場合true
     */
    public boolean hasAllGroups(Jwt jwt, List<String> groupNames) {
        if (groupNames == null || groupNames.isEmpty()) {
            return false;
        }

        List<String> userGroups = extractGroups(jwt);
        return userGroups.containsAll(groupNames);
    }
}
