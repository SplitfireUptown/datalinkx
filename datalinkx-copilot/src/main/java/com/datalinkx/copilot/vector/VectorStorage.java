package com.datalinkx.copilot.vector;

import com.datalinkx.copilot.client.response.EmbeddingResult;

public interface VectorStorage {
    default String getCollectionName(){
        return "datalinkx-knowledge-lib";
    }
    // 初始化向量库
    void initCollection(String collectionName, int dim);
    // 存储向量库
    void store(String collectionName, EmbeddingResult embeddingResult);
    // 清空
    void truncate(String collectionName);
    // 检索向量库
    String retrieval(String collectionName, double[] vector);
}
