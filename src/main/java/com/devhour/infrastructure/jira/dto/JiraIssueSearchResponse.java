package com.devhour.infrastructure.jira.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * JIRA Issue検索APIレスポンスDTO
 * 
 * JQLクエリ実行結果を表現するデータ転送オブジェクト
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueSearchResponse {

    /**
     * レスポンスの拡張情報フィールド
     */
    private String expand;

    /**
     * 検索結果の開始位置
     */
    private Integer startAt;

    /**
     * 1回のリクエストでの最大取得件数
     */
    private Integer maxResults;
    
    /**
     * 総件数（JQL検証時に使用）
     */
    private Integer total;

    /**
     * イシューの配列
     * JsonNodeとして保持し、柔軟にイシュー情報を扱えるようにする
     */
    private List<JsonNode> issues;
}