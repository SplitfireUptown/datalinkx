package com.datalinkx.copilot.rag.bean;

import lombok.Data;

@Data
public class ElasticVectorData {
    private String content;
    private double[] vector;
}
