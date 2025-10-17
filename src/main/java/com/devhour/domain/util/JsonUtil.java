package com.devhour.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON変換ユーティリティ
 *
 * ドメインレイヤーで使用するJSON変換機能を提供
 * エンティティから直接使用可能な静的メソッド
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = createConfiguredMapper();

    private JsonUtil() {
        // ユーティリティクラスのためインスタンス化を防ぐ
    }

    /**
     * 設定済みObjectMapperを作成
     */
    private static ObjectMapper createConfiguredMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * オブジェクトをJSON文字列に変換
     *
     * @param object 変換対象オブジェクト
     * @return JSON文字列、変換エラーの場合はエラーメッセージ
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return String.format("JSON変換エラー: %s", e.getMessage());
        }
    }

    /**
     * JSON文字列からオブジェクトに変換
     *
     * @param json JSON文字列
     * @param clazz 変換先クラス
     * @param <T> 変換先型
     * @return 変換されたオブジェクト
     * @throws JsonProcessingException 変換エラーの場合
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return MAPPER.readValue(json, clazz);
    }
}