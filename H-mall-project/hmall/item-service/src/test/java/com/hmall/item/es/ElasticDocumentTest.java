package com.hmall.item.es;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;


@SpringBootTest(properties = {"spring.profiles.active=local"})
public class ElasticDocumentTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;


    // 创建文档
    @Test
    void testCreateDoc() throws IOException {
        // 0. 准备数据
        ItemDoc itemDoc = BeanUtil.copyProperties(itemService.getById(317578), ItemDoc.class);
        // 1. 创建请求
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 2. 创建数据
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        // 3. 执行请求
        client.index(request, RequestOptions.DEFAULT);
    }

    // 查询文档
    @Test
    void testGetDoc() throws IOException {
        // 1. 创建请求
        GetRequest request = new GetRequest("items", "317578");
        // 2. 发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3. 处理响应
        System.out.println(JSONUtil.toBean(response.getSourceAsString(), ItemDoc.class));
    }

    // 删除文档
    @Test
    void testDeleteDoc() throws IOException {
        // 1. 创建请求
        DeleteRequest request = new DeleteRequest("items", "317578");
        // 2. 发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    // 更新文档（全量修改）
    @Test
    void testUpdateALLDoc() throws IOException {
        // 0. 准备数据
        ItemDoc itemDoc = BeanUtil.copyProperties(itemService.getById(317578), ItemDoc.class);
        itemDoc.setPrice(1000);
        // 1. 创建请求
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 2. 创建数据
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        // 3. 执行请求
        client.index(request, RequestOptions.DEFAULT);
    }

    // 更新文档（局部修改）
    @Test
    void testUpdatePartDoc() throws IOException {
        // 1. 创建请求
        UpdateRequest request = new UpdateRequest("items", "317578");
        // 2. 准备更改数据
        request.doc(
                "price", 8000
        );
        // 3. 执行请求
        client.update(request, RequestOptions.DEFAULT);
    }

    // 批量处理
    @Test
    void testBatch() throws IOException {
        int pageNo = 1, size = 10;
        // 0. 准备数据
        Page<Item> page = itemService.lambdaQuery()
                .eq(Item::getStatus, 1)
                .page(Page.of(pageNo, size));
        List<Item> items = page.getRecords();
        if (items == null || items.isEmpty()) {
            return;
        }
        // 1. 创建请求
        BulkRequest request = new BulkRequest();
        // 2. 创建数据
        items.forEach(item -> {
            ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
            request.add(new IndexRequest("items").id(itemDoc.getId())
                    .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
        });
        // 3. 执行请求
        client.bulk(request, RequestOptions.DEFAULT);
    }

    @BeforeEach
    void init() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.3.54", 9200, "http")
                )
        );
    }

    @AfterEach
    void destroy() {
        try {
            if (client != null) {
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
