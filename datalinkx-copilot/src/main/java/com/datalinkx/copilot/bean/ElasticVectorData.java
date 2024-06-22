package com.datalinkx.copilot.bean;

import lombok.Data;

@Data
public class ElasticVectorData {

    private String chunkId;
    private String content;
    private String docId;
    private double[] vector;
}
