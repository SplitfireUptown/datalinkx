package com.datalinkx.copilot.bean;

import lombok.Data;

@Data
public class ElasticVectorData {
    private String content;
    private double[] vector;
}
