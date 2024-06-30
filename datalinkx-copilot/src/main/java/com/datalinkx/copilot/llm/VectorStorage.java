package com.datalinkx.copilot.llm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.copilot.bean.ElasticVectorData;
import com.datalinkx.copilot.client.response.EmbeddingResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.lucene.search.function.ScriptScoreFunction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VectorStorage {

    @Autowired
    RestHighLevelClient esClient;

    public String getCollectionName(){
        //演示效果使用，固定前缀+日期
        return "datalinkx-local-knowledge-lib";
    }

    /**
     * 初始化向量数据库index
     * @param collectionName 名称
     * @param dim 维度
     */
    @SneakyThrows
    public void initCollection(String collectionName, int dim) {
        // 查看向量索引是否存在，此方法为固定默认索引字段
        IndicesClient indices = esClient.indices();
        GetIndexRequest getIndexRequest = new GetIndexRequest(collectionName);
        if (!indices.exists(getIndexRequest, RequestOptions.DEFAULT)) {
            CreateIndexRequest request = new CreateIndexRequest(collectionName);
            request.mapping(this.elasticMapping(dim));
            this.esClient.indices().create(request, RequestOptions.DEFAULT);
        }
    }

    @SneakyThrows
    public void store(String collectionName, EmbeddingResult embeddingResult) {
        ElasticVectorData ele = new ElasticVectorData();
        ele.setVector(embeddingResult.getEmbedding());
        ele.setContent(embeddingResult.getContent());

        IndexRequest indexRequest = new IndexRequest(collectionName)
                .source(JsonUtils.toJson(ele), XContentType.JSON);

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(indexRequest);

        BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            log.error("vector data save error: {}", bulkResponse.buildFailureMessage());
            throw new RuntimeException("vector data save error" + bulkResponse.buildFailureMessage());
        }
    }

    @SneakyThrows
    public String retrieval(String collectionName, double[] vector) {
        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", vector);

        int page = 0;
        int size = 5;
        // 最低得分
        float minScore = 1.0f;

        // 构建查询向量
        // 计算cos值+1，避免出现负数的情况，得到结果后，实际score值在减1再计算
        Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, "cosineSimilarity(params.query_vector, 'vector')+1", params);
        ScriptScoreQueryBuilder scriptScoreQueryBuilder = new ScriptScoreQueryBuilder(QueryBuilders.boolQuery(), script);

        SearchRequest searchRequest = new SearchRequest(collectionName);
        searchRequest.source(new SearchSourceBuilder().query(scriptScoreQueryBuilder).from(page).size(size).minScore(minScore));

        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = searchResponse.getHits();
        List<String> results = new LinkedList<>();
        for (SearchHit hit : searchHits) {
            ElasticVectorData ele = JsonUtils.toObject(hit.getSourceAsString(), ElasticVectorData.class);
            results.add(ele.getContent());
        }

        return CollectionUtil.join(results, "");
    }

    private Map<String, Object> elasticMapping(int dims) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("_class", MapUtil.builder("type", "keyword").put("doc_values", "false").put("index", "false").build());
        properties.put("chunkId", MapUtil.builder("type", "keyword").build());
        properties.put("content", MapUtil.builder("type", "keyword").build());
        properties.put("docId", MapUtil.builder("type", "keyword").build());
        // 向量
        properties.put("vector", MapUtil.builder("type", "dense_vector").put("dims", Objects.toString(dims)).build());
        Map<String, Object> root = new HashMap<>();
        root.put("properties", properties);
        return root;
    }

}
