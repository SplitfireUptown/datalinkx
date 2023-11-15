package com.dtstack.flinkx.datahub.format;

import com.dtstack.flinkx.datahub.DatahubInputSplit;
import com.dtstack.flinkx.datahub.client.DataHubConsumer;
import com.dtstack.flinkx.decoder.DecodeEnum;
import com.dtstack.flinkx.decoder.IDecode;
import com.dtstack.flinkx.decoder.JsonDecoder;
import com.dtstack.flinkx.decoder.TextDecoder;
import com.dtstack.flinkx.inputformat.BaseRichInputFormat;
import com.dtstack.flinkx.reader.MetaColumn;
import com.dtstack.flinkx.restore.FormatState;
import com.dtstack.flinkx.util.ExceptionUtil;
import com.dtstack.flinkx.util.StringUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class DatahubInputFormat extends BaseRichInputFormat {
    public String endpoint;
    public String accessId;
    public String accessKey;
    public String project;
    public String topic;
    public String subscriptionId;
    public Integer fetchSize;
    public List<MetaColumn> metaColumns;

    protected transient BlockingQueue<Row> queue;
    protected Map<String, Object> stateMap;
    protected transient IDecode decode;
    protected String codec;
    protected transient DataHubConsumer consumer;
    protected volatile boolean running = false;

    @Override
    protected void openInternal(InputSplit inputSplit) throws IOException {
        super.openInputFormat();
        queue = new SynchronousQueue<>(false);
        stateMap = new HashMap(16);
        if (DecodeEnum.JSON.getName().equalsIgnoreCase(codec)) {
            decode = new JsonDecoder();
        } else {
            decode = new TextDecoder();
        }

        consumer.createClient(this, (DatahubInputSplit) inputSplit).execute();
    }

    @Override
    public void openInputFormat() throws IOException {
        super.openInputFormat();
        queue = new SynchronousQueue<>(false);
        stateMap = new HashMap<>(16);
        if (DecodeEnum.JSON.getName().equalsIgnoreCase(codec)) {
            decode = new JsonDecoder();
        } else {
            decode = new TextDecoder();
        }

        consumer = new DataHubConsumer(endpoint, accessId, accessKey, project, topic, subscriptionId, fetchSize);
    }

    @Override
    protected InputSplit[] createInputSplitsInternal(int minNumSplits) throws Exception {
        InputSplit[] splits = new InputSplit[minNumSplits];
        for (int i = 0; i < minNumSplits; i++) {
            splits[i] = new DatahubInputSplit(i);
        }
        return splits;
    }

    @Override
    protected Row nextRecordInternal(Row row) throws IOException {
        row = null;
        try {
            row = queue.take();
        } catch (InterruptedException e) {
            LOG.error("takeEvent interrupted error:{}", ExceptionUtil.getErrorMessage(e));
        }
        return row;
    }

    @Override
    protected void closeInternal() throws IOException {
        if (running) {
            consumer.close();
            running = false;
            LOG.warn("datahub close");
        }
    }

    @Override
    public boolean reachedEnd() throws IOException {
        return false;
    }

    public void processEvent(Map<String, Object> rowValueMap) {
        try {
            Row row = new Row(metaColumns.size());
            for (int i = 0; i < metaColumns.size(); i++) {
                MetaColumn metaColumn = metaColumns.get(i);
                Object value = rowValueMap.get(metaColumn.getName());
                Object obj = StringUtil.string2col(String.valueOf(value), metaColumn.getType(), metaColumn.getTimeFormat());
                row.setField(i, obj);
            }
            queue.put(row);
        } catch (InterruptedException e) {
            LOG.error("takeEvent interrupted error", e);
        }
    }


    @Override
    public FormatState getFormatState() {
        super.getFormatState();
        if (formatState != null && MapUtils.isNotEmpty(stateMap)) {
            formatState.setState(stateMap);
        }
        return formatState;
    }

    public IDecode getDecode() {
        return decode;
    }
}
