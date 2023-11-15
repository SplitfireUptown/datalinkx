/*
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.flinkx.datahub.client;


import com.aliyun.datahub.client.exception.AuthorizationFailureException;
import com.aliyun.datahub.client.exception.DatahubClientException;
import com.aliyun.datahub.client.exception.InvalidParameterException;
import com.aliyun.datahub.client.exception.NoPermissionException;
import com.aliyun.datahub.client.exception.SubscriptionOfflineException;
import com.aliyun.datahub.client.exception.SubscriptionOffsetResetException;
import com.aliyun.datahub.client.exception.SubscriptionSessionInvalidException;
import com.aliyun.datahub.client.model.Field;
import com.aliyun.datahub.client.model.RecordEntry;
import com.aliyun.datahub.client.model.TupleRecordData;
import com.aliyun.datahub.client.util.JsonUtils;
import com.aliyun.datahub.clientlibrary.config.ConsumerConfig;
import com.aliyun.datahub.clientlibrary.consumer.Consumer;
import com.dtstack.flinkx.datahub.DatahubInputSplit;
import com.dtstack.flinkx.datahub.format.DatahubInputFormat;
import com.dtstack.flinkx.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataHubClient implements Runnable {
    protected static final Logger LOG = LoggerFactory.getLogger(DataHubClient.class);
    int maxRetry;
    Consumer consumer;
    DatahubInputFormat format;
    private volatile boolean running = true;

    public String endpoint;
    public String accessId;
    public String accessKey;
    public String project;
    public String topic;
    public String subscriptionId;
    public Integer fetchSize;

    public DataHubClient(String project, String topic, String subId, String endpoint,
                         String accessId, String accessKey, DatahubInputFormat format,
                         DatahubInputSplit datahubInputSplit, Integer fetchSize) {

        this.endpoint = endpoint;
        this.accessId = accessId;
        this.accessKey = accessKey;
        this.project = project;
        this.subscriptionId = subId;
        this.topic = topic;
        this.maxRetry = 3;
        this.format = format;
        this.fetchSize = fetchSize;
        createConsumer();
    }

    private void createConsumer() {
        LOG.info(String.format("project: %s, %s, %s, %s, %s, %s",
                project, endpoint, accessId, accessKey, topic, subscriptionId));
    }

    @Override
    public void run() {
        int printNum = 1;
        int i = 0;
        int j = 0;

        ConsumerConfig config = new ConsumerConfig(endpoint, accessId, accessKey);
        config.setFetchSize(fetchSize);
        this.consumer = new Consumer(project, topic, subscriptionId, config);

        try {
            while (running) {
                try {
                    RecordEntry record = consumer.read(maxRetry);
                    if (null != record) {
                        if (i < printNum) {
                            i++;
                            LOG.info("====datahub record info : {}, {}", this.topic, JsonUtils.toJson(record.getRecordData()));
                        }
                    } else {
                        if (j < printNum) {
                            j++;
                            LOG.info("====datahub record null: {}, {}", this.topic, j);
                        }
                    }

                    processEvent(record);
                } catch (SubscriptionOffsetResetException e) {
                    // 点位被重置,重新初始化consumer
                    try {
                        consumer.close();
                        createConsumer();
                    } catch (DatahubClientException e1) {
                        LOG.error("create consumer failed", e);
                        throw e;
                    }
                } catch (InvalidParameterException |
                        SubscriptionOfflineException |
                        SubscriptionSessionInvalidException |
                        AuthorizationFailureException |
                        NoPermissionException e) {
                    // 请求参数非法
                    // 订阅被下线
                    // 订阅下相同shard被其他客户端占用
                    // 签名不正确
                    // 没有权限
                    LOG.error("datahub read failed", e);
                    throw e;
                } catch (DatahubClientException e) {
                    // 基类异常，包含网络问题等，可以选择重试
                    LOG.error("datahub base error, like network, retry", e);
                    Thread.sleep(1000);
                }
            }
        } catch (Throwable e) {
            LOG.error("", e);
        } finally {
            consumer.close();
        }
    }

    public void processEvent(RecordEntry record) {
        if (record != null) {
            TupleRecordData recordData = (TupleRecordData) record.getRecordData();
            Map<String, Object> rowValueMap = new HashMap<>();
            for (Field field : recordData.getRecordSchema().getFields()) {
                rowValueMap.put(field.getName(), recordData.getField(field.getName()));
            }
            format.processEvent(rowValueMap);
        }
    }

    public void close() {
        try {
            running = false;
        } catch (Exception e) {
            LOG.error("close datahub consumer error, e = {}", ExceptionUtil.getErrorMessage(e));
        }
    }
}
