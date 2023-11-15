/*
 * Licensed to the Apache Software Foundation (ASF) under one
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

package com.dtstack.flinkx.kafkacustom.format;

import com.dtstack.flinkx.exception.DataSourceException;
import com.dtstack.flinkx.exception.WriteRecordException;
import com.dtstack.flinkx.kafkabase.util.Formatter;
import com.dtstack.flinkx.kafkabase.format.KafkaBaseOutputFormat;
import com.dtstack.flinkx.util.ExceptionUtil;
import com.dtstack.flinkx.util.GsonUtil;
import com.dtstack.flinkx.util.MapUtil;
import com.dtstack.flinkx.util.ProtoBufUtil;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.codehaus.jettison.json.JSONArray;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Date: 2019/11/21
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class KafkacustomOutputFormat extends KafkaBaseOutputFormat {
    private transient KafkaProducer<String, String> producer;

    @Override
    public void configure(Configuration parameters) {
        super.configure(parameters);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
        props.put(ProducerConfig.RETRIES_CONFIG, 1000000);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        if (producerSettings != null) {
            props.putAll(producerSettings);
        }
        producer = new KafkaProducer<>(props);
    }

    @Override
    protected void emit(Map event) throws IOException {
        heartBeatController.acquire();
        String tp = Formatter.format(event, topic, timezone);
        String outPutMsgString = GsonUtil.GSON.toJson(event);
        String transformMsg = "";
        if (compress != null) {
            switch (compress.toUpperCase()) {
                case "PROTOBUF":
                    transformMsg = new String(ProtoBufUtil.serializer(outPutMsgString));
                    break;
                default:
                    transformMsg = outPutMsgString;
            }
        } else {
            transformMsg = outPutMsgString;
        }
        producer.send(new ProducerRecord<>(tp, "", transformMsg), (metadata, exception) -> {
        if(Objects.nonNull(exception)){
            String errorMessage = String.format("send data failed,data 【%s】 ,error info  %s",event,ExceptionUtil.getErrorMessage(exception));
            LOG.warn(errorMessage);
            heartBeatController.onFailed(exception);
        } else {
            heartBeatController.onSuccess();
        }
        });
    }

    @Override
    public void closeInternal() {
        LOG.warn("kafka output closeInternal.");
        //未设置具体超时时间 关闭时间默认是long.value  导致整个方法长时间等待关闭不了，因此明确指定20s时间
        producer.close(KafkaBaseOutputFormat.CLOSE_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void writeSingleRecordInternal(Row row) throws WriteRecordException {
        try {
            Map<String, Object> map;
            int arity = row.getArity();
            if (tableFields != null && tableFields.size() >= arity) {
                map = new LinkedHashMap<>((arity << 2) / 3);
                for (int i = 0; i < arity; i++) {
                    Object value = null;
                    if (row.getField(i) instanceof Timestamp) {
                        // 不加不行啊，不加多个毫秒
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        value = simpleDateFormat.format(row.getField(i));
                    } else {
                        value = row.getField(i);
                        // value = org.apache.flink.util.StringUtils.arrayAwareToString(row.getField(i));
                    }
                    map.put(tableFields.get(i), value);
                }
            } else {
                if(arity == 1){
                    Object obj = row.getField(0);
                    if (obj instanceof Map) {
                        map = (Map<String, Object>) obj;
                    } else if (obj instanceof String) {
                        map = jsonDecoder.decode(obj.toString());
                    } else {
                        map = Collections.singletonMap("message", row.toString());
                    }
                }else{
                    map = Collections.singletonMap("message", row.toString());
                }
            }
            emit(map);
        } catch (Throwable e) {
            String errorMessage = ExceptionUtil.getErrorMessage(e);
            LOG.error("kafka writeSingleRecordInternal error:{}", errorMessage);
            //如果是数据源错误 直接抛出异常，而不是封装为WriteRecordException
            // 否则WriteRecordException会被上层捕获，导致任务无法结束
            if(e instanceof DataSourceException){
                throw (DataSourceException)e;
            }
            throw new WriteRecordException(errorMessage, e);
        }
    }
}
