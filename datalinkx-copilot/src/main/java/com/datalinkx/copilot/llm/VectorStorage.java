package com.datalinkx.copilot.llm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.datalinkx.copilot.bean.ElasticVectorData;
import com.datalinkx.copilot.client.response.EmbeddingResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VectorStorage {

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    public String getCollectionName(){
        //演示效果使用，固定前缀+日期
        return "datalinkx-local-knowledge-lib";
    }

    /**
     * 初始化向量数据库index
     * @param collectionName 名称
     * @param dim 维度
     */
    public boolean initCollection(String collectionName,int dim){
        // 查看向量索引是否存在，此方法为固定默认索引字段
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(collectionName));
        if (!indexOperations.exists()) {
            // 索引不存在，直接创建
            log.info("index not exists,create");
            //创建es的结构，简化处理
            Document document = Document.from(this.elasticMapping(dim));
            // 创建
            indexOperations.create(new HashMap<>(), document);
            return true;
        }
        return true;
    }

    public void store(String collectionName, EmbeddingResult embeddingResult){

        List<IndexQuery> results = new ArrayList<>();
        ElasticVectorData ele = new ElasticVectorData();
        ele.setVector(embeddingResult.getEmbedding());
        ele.setContent(embeddingResult.getContent());
        results.add(new IndexQueryBuilder().withObject(ele).build());
        // 构建数据包
        List<IndexedObjectInformation> bulkedResult = elasticsearchRestTemplate.bulkIndex(results, IndexCoordinates.of(collectionName));
        int size = CollectionUtil.size(bulkedResult);
        log.info("保存向量成功-size:{}", size);
    }

    public String retrieval(String collectionName,double[] vector){
        // Build the script,查询向量
        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", vector);
        // 计算cos值+1，避免出现负数的情况，得到结果后，实际score值在减1再计算
        Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, "cosineSimilarity(params.query_vector, 'vector')+1", params);
        ScriptScoreQueryBuilder scriptScoreQueryBuilder = new ScriptScoreQueryBuilder(QueryBuilders.boolQuery(), script);
        // 构建请求
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(scriptScoreQueryBuilder)
                .withPageable(Pageable.ofSize(3)).build();
        SearchHits<ElasticVectorData> dataSearchHits = this.elasticsearchRestTemplate.search(nativeSearchQuery, ElasticVectorData.class, IndexCoordinates.of(collectionName));
        //log.info("检索成功，size:{}", dataSearchHits.getTotalHits());
        List<SearchHit<ElasticVectorData>> data = dataSearchHits.getSearchHits();
        List<String> results = new LinkedList<>();
        for (SearchHit<ElasticVectorData> ele : data) {
            results.add(ele.getContent().getContent());
        }
        return CollectionUtil.join(results,"");
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
