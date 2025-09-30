package com.hmall.item.es;


import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElasticIndexTest {

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

    // 创建索引
    @Test
    void testCreateIndex() throws IOException {
        // 准备request对象
        CreateIndexRequest request = new CreateIndexRequest("item");
        // 准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }
    // 查询索引
    @Test
    void testGetIndex() throws IOException {
        boolean result = client.indices().exists(new GetIndexRequest("item"), RequestOptions.DEFAULT);
        System.out.println("布尔值为：" + result);
    }
    // 删除索引
    @Test
    void testDeleteIndex() throws IOException {
        client.indices().delete(new DeleteIndexRequest("item"), RequestOptions.DEFAULT);
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
