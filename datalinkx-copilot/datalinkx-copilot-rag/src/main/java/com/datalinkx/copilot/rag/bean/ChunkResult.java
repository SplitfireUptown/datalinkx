package com.datalinkx.copilot.rag.bean;

import lombok.Data;

@Data
public class ChunkResult {
    private String docId;
    private int chunkId;
    private String content;
}
