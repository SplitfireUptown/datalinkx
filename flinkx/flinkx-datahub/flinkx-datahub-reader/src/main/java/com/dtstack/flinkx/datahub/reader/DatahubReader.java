package com.dtstack.flinkx.datahub.reader;


import com.dtstack.flinkx.datahub.config.DatahubConfigKeys;
import com.dtstack.flinkx.config.DataTransferConfig;
import com.dtstack.flinkx.config.ReaderConfig;
import com.dtstack.flinkx.datahub.format.DatahubInputFormat;
import com.dtstack.flinkx.datahub.format.DatahubInputFormatBuilder;
import com.dtstack.flinkx.reader.BaseDataReader;
import com.dtstack.flinkx.reader.MetaColumn;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

import java.util.List;

public class DatahubReader extends BaseDataReader {
    public String endpoint;
    public String accessId;
    public String accessKey;
    public String project;
    public String topic;
    public String subscriptionId;
    public Integer fetchSize;
    public List<MetaColumn> metaColumns;

    public DatahubReader(DataTransferConfig config, StreamExecutionEnvironment env) {
        super(config, env);
        ReaderConfig readerConfig = config.getJob().getContent().get(0).getReader();
        this.endpoint = readerConfig.getParameter().getStringVal(DatahubConfigKeys.ENDPOINT);
        this.accessId = readerConfig.getParameter().getStringVal(DatahubConfigKeys.ACCESSID);
        this.accessKey = readerConfig.getParameter().getStringVal(DatahubConfigKeys.ACCESSKEY);
        this.project = readerConfig.getParameter().getStringVal(DatahubConfigKeys.PROJECT);
        this.topic = readerConfig.getParameter().getStringVal(DatahubConfigKeys.TOPIC);
        this.subscriptionId = readerConfig.getParameter().getStringVal(DatahubConfigKeys.SUBSCRIPTIONID);
        this.fetchSize = readerConfig.getParameter().getIntVal(DatahubConfigKeys.FETCHSIZE, 1000);
        this.metaColumns = MetaColumn.getMetaColumns(readerConfig.getParameter().getColumn());
    }
//
//    public static Consumer getConsumer() {
//        return DataHubConsumer.createConsumer(project, topic, subscriptionId, endpoint, accessId, accessKey);
//    }

//    public void excute() {
//        DataHubClient dataHunClientTask = new DataHubClient(Integer.parseInt(fetchSize), getConsumer(),metaColumns);
//        DataHubConsumer.executor.execute(dataHunClientTask);
//
//    }

    @Override
    public DataStream<Row> readData() {
        DatahubInputFormatBuilder builder = new DatahubInputFormatBuilder(new DatahubInputFormat());
        builder.setDataTransferConfig(dataTransferConfig);
        builder.setRestoreConfig(restoreConfig);
        builder.setTopic(topic);
        builder.setAccessId(accessId);
        builder.setAccessKey(accessKey);
        builder.setMetaColumns(metaColumns);
        builder.setProject(project);
        builder.setFetchSize(fetchSize);
        builder.setEndpoint(endpoint);
        builder.setSubscriptionId(subscriptionId);
        return createInput(builder.finish());
    }
}
