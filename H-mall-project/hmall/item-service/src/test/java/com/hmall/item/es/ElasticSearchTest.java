package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElasticSearchTest {

    private RestHighLevelClient client;

    @Test
    void testConnect() {
        System.out.println(client);
    }

    @BeforeEach
    void init() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.3.54", 9200, "http")
                )
        );
    }

    // DSL查询
    @Test
    void testSearch() throws IOException {
        // 1. 创建请求
        SearchRequest searchRequest = new SearchRequest("items");
        // 2. 创建DSL
        searchRequest.source().query(QueryBuilders.matchAllQuery());
        // 3. 发送请求
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        // 4. 处理响应
        SearchHits hits = search.getHits();
        hits.forEach(hit -> System.out.println(hit.getSourceAsString()));
    }

    // 实现以品牌名作为属性的聚合查询
    @Test
    void testAggregation() throws IOException {
        // 1. 创建请求
        SearchRequest searchRequest = new SearchRequest("items");
        // 2. 创建DSL
        searchRequest.source().size(0);
        // 3. 添加聚合
        searchRequest.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand.keyword").size(10));
        // 4. 响应结果
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        // 5. 处理结果
        Aggregations aggregations = search.getAggregations();
        // 6. 获取聚合结果
        Terms terms = aggregations.get("brandAgg");
        // 7. 获取桶
        terms.getBuckets().forEach(bucket -> System.out.println(bucket.getKeyAsString()));
    }

    @AfterEach
    void destroy() {
        try {
           if (client != null){
               client.close();
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 请求参数
    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
