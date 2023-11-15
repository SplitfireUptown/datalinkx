package com.dtstack.flinkx.datahub.client;

import com.dtstack.flinkx.datahub.DatahubInputSplit;
import com.dtstack.flinkx.datahub.format.DatahubInputFormat;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class DataHubConsumer {
    public ExecutorService executor = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory
            .Builder()
            .namingPattern("dataHubConsumerThread-" + Thread.currentThread().getName())
            .daemon(true)
            .build());

    public String endpoint;
    public String accessId;
    public String accessKey;
    public String project;
    public String topic;
    public String subscriptionId;
    public Integer fetchSize;

    protected DataHubClient client;

    public DataHubConsumer(String endpoint, String accessId, String accessKey, String project, String topic, String subscriptionId, Integer fetchSize) {
        this.endpoint = endpoint;
        this.accessId = accessId;
        this.accessKey = accessKey;
        this.project = project;
        this.topic = topic;
        this.subscriptionId = subscriptionId;
        this.fetchSize = fetchSize;
    }

    public DataHubConsumer createClient(DatahubInputFormat format, DatahubInputSplit datahubInputSplit) {

        client = new DataHubClient(project, topic, subscriptionId, endpoint,
                accessId, accessKey, format, datahubInputSplit, fetchSize);
        return this;
    }

    public void execute() {
        executor.execute(client);
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
