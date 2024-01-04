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
package com.dtstack.flinkx.kafkacustom.client;

import com.dtstack.flinkx.decoder.IDecode;
import com.dtstack.flinkx.kafkabase.KafkaInputSplit;
import com.dtstack.flinkx.kafkabase.client.IClient;
import com.dtstack.flinkx.kafkabase.entity.kafkaState;
import com.dtstack.flinkx.kafkabase.enums.StartupMode;
import com.dtstack.flinkx.kafkabase.format.KafkaBaseInputFormat;
import com.dtstack.flinkx.reader.MetaColumn;
import com.dtstack.flinkx.restore.FormatState;
import com.dtstack.flinkx.util.ExceptionUtil;
import com.dtstack.flinkx.util.GsonUtil;
import com.dtstack.flinkx.util.ProtoBufUtil;
import com.dtstack.flinkx.util.SysUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.opencsv.CSVReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.api.common.InvalidProgramException;
import org.apache.flink.api.java.io.CsvReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.csvreader.CsvReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Date: 2019/12/25
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class KafkacustomClient implements IClient {
    protected static Logger LOG = LoggerFactory.getLogger(KafkacustomClient.class);
    //ck state指针
    private final AtomicReference<Collection<kafkaState>> stateReference;
    private volatile boolean running = true;
    private long pollTimeout;
    private boolean blankIgnore;
    private IDecode decode;
    private KafkaBaseInputFormat format;
    private KafkaConsumer<String, String> consumer;
    //是否触发checkpoint，需要提交offset指针
    private AtomicBoolean commit;

    @SuppressWarnings("unchecked")
    public KafkacustomClient(Properties clientProps, long pollTimeout, KafkaBaseInputFormat format, KafkaInputSplit kafkaInputSplit) {
        this.pollTimeout = pollTimeout;
        this.blankIgnore = format.getBlankIgnore();
        this.format = format;
        this.decode = format.getDecode();
        this.commit = new AtomicBoolean(false);
        this.stateReference = new AtomicReference<>();
        consumer = new KafkaConsumer<>(clientProps);
        StartupMode mode = format.getMode();
        List<kafkaState> stateList = kafkaInputSplit.getList();
        Map<TopicPartition, Long> partitionMap = new HashMap<>(Math.max((int) (stateList.size()/.75f) + 1, 16));
        Object stateMap = format.getState();
        boolean needToSeek = true;
        if(stateMap instanceof Map && MapUtils.isNotEmpty((Map<String, kafkaState>)stateMap)){
            Map<String, kafkaState> map = (Map<String, kafkaState>) stateMap;
            for (kafkaState state : map.values()) {
                TopicPartition tp = new TopicPartition(state.getTopic(), state.getPartition());
                //ck中保存的是当前已经读取的offset，恢复时从下一条开始读
                partitionMap.put(tp, state.getOffset() + 1);
            }
            LOG.info("init kafka client from [checkpoint], stateMap = {}", map);
        }else if(CollectionUtils.isEmpty(stateList)){
            running = false;
            LOG.warn("\n" +
                    "****************************************************\n" +
                    "*******************    WARN    *********************\n" +
                    "| this stateList in KafkaInputSplit is empty,      |\n" +
                    "| this channel will not assign any kafka topic,    |\n" +
                    "| therefore, no data will be read in this channel! |\n" +
                    "****************************************************");
            return;
        }else if(StartupMode.TIMESTAMP.equals(mode)){
            Map<TopicPartition, Long> timestampMap = new HashMap<>(Math.max((int) (stateList.size()/.75f) + 1, 16));
            for (kafkaState state : stateList) {
                TopicPartition tp = new TopicPartition(state.getTopic(), state.getPartition());
                timestampMap.put(tp,  state.getTimestamp());
                partitionMap.put(tp, null);
            }
            Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampMap);
            for (TopicPartition tp : partitionMap.keySet()) {
                OffsetAndTimestamp offsetAndTimestamp = offsets.get(tp);
                if (offsetAndTimestamp != null) {
                    partitionMap.put(tp, offsetAndTimestamp.offset());
                }
            }
            LOG.info("init kafka client from [timestamp], offsets = {}", offsets);
        }else if(StartupMode.SPECIFIC_OFFSETS.equals(mode)){
            for (kafkaState state : stateList) {
                TopicPartition tp = new TopicPartition(state.getTopic(), state.getPartition());
                partitionMap.put(tp, state.getOffset());
            }
            LOG.info("init kafka client from [specific-offsets], stateList = {}", stateList);
        }else{
            for (kafkaState state : stateList) {
                partitionMap.put(new TopicPartition(state.getTopic(), state.getPartition()), null);
            }
            needToSeek = false;
            LOG.info("init kafka client from [split], stateList = {}", stateList);
        }
        LOG.info("partitionList = {}", partitionMap.keySet());
        consumer.assign(partitionMap.keySet());
        if(needToSeek){
            for (Map.Entry<TopicPartition, Long> entry : partitionMap.entrySet()) {
                consumer.seek(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            LOG.error("KafkaClient run failed, Throwable = {}", ExceptionUtil.getErrorMessage(e));
        });
        try {
            while (running) {
                if(this.commit.getAndSet(false)){
                    final Collection<kafkaState> kafkaStates = stateReference.getAndSet(null);
                    if(kafkaStates != null){
                        LOG.info("submit kafka offset, kafkaStates = {}", kafkaStates);
                        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>(Math.max((int) (kafkaStates.size()/.75f) + 1, 16));
                        for (kafkaState state : kafkaStates) {
                            offsets.put(new TopicPartition(state.getTopic(), state.getPartition()), new OffsetAndMetadata(state.getOffset(), "no metadata"));
                        }
                        try {
                            consumer.commitAsync(offsets, (o, ex) -> {
                                if (ex != null) {
                                    LOG.warn("Committing offsets to Kafka failed, This does not compromise Flink's checkpoints. offsets = {}, e = {}", o, ExceptionUtil.getErrorMessage(ex));
                                } else {
                                    LOG.info("Committing offsets to Kafka async successfully, offsets = {}", o);
                                }
                            });
                        }catch (Exception e){
                            LOG.warn("Committing offsets to Kafka failed, This does not compromise Flink's checkpoints. offsets = {}, e = {}", offsets, ExceptionUtil.getErrorMessage(e));
                            try {
                                consumer.commitSync(offsets);
                                LOG.info("Committing offsets to Kafka successfully, offsets = {}", offsets);
                            }catch (Exception e1){
                                LOG.warn("Committing offsets to Kafka failed, This does not compromise Flink's checkpoints. offsets = {}, e = {}", offsets, ExceptionUtil.getErrorMessage(e1));
                            }
                        }
                    }
                }

                ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    LOG.debug("Kafka Client Received Message");
                }
                for (ConsumerRecord<String, String> r : records) {
                    boolean isIgnoreCurrent = r.value() == null || blankIgnore && StringUtils.isBlank(r.value());
                    if (isIgnoreCurrent) {
                        continue;
                    }
                    
                    try {
                        processMessage(r.value(), r.topic(), r.partition(), r.offset(), r.timestamp());
                    } catch (Throwable e) {
                        LOG.warn("kafka consumer fetch is error, message:{}, e = {}", r.value(), ExceptionUtil.getErrorMessage(e));
                    }
                }
            }
        } catch (WakeupException e) {
            LOG.warn("WakeupException to close kafka consumer, e = {}", ExceptionUtil.getErrorMessage(e));
        } catch (Throwable e) {
            LOG.warn("kafka consumer fetch is error, e = {}", ExceptionUtil.getErrorMessage(e));
        } finally {
            consumer.close();
        }
    }

    @Override
    public void processMessage(String message, String topic, Integer partition, Long offset, Long timestamp) {

//        Map<String, Object> event = decode.decode(message);
        String transformMsg = "";
        LinkedHashMap<String, Object> event = new LinkedHashMap<>();
//        ProtoBufUtil.deserializer((new String(byteMsg)).getBytes(), String.class)
        // 当有分割符的时候，默认读到的message是1，2，3这种字符串形式，没有分割符的时候默认是json字符串
        // 是否需要解压数据
        if (format.getDeCompress() != null) {
            switch (format.getDeCompress().toUpperCase()) {
                case "PROTOBUF":
                    transformMsg = ProtoBufUtil.deserializer(message.getBytes(), String.class);
                    break;
                default:
                    transformMsg = message;
            }
        } else {
            transformMsg = message;
        }
        if (!StringUtils.isEmpty(format.getSeparator())) {
            if (format.getMetaColumns() == null) {
                throw new InvalidProgramException(
                        "reader config metaColumns can not be null");
            }
            char separator = format.getSeparator().toCharArray()[0];

            CSVReader csvReader =  new CSVReader(new StringReader(transformMsg), separator);
            String[] valueList;
            try {
                valueList  = csvReader.readNext();
            } catch (IOException e){
                throw new RuntimeException(e);
            }

            for (MetaColumn metaColumn: format.getMetaColumns()) {
                event.put(metaColumn.getName(), valueList[metaColumn.getIndex()]);
            }
        } else {
            JsonObject  transformMsg2Map = GsonUtil.GSON.fromJson(transformMsg, JsonObject.class);
            if (transformMsg2Map != null) {
                Iterator<Map.Entry<String, JsonElement>> iterator = transformMsg2Map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonElement> item = iterator.next();
                    if (item.getValue() == null || item.getValue() instanceof JsonNull) {
                        event.put(item.getKey(), null);
                    } else if (item.getValue() instanceof JsonPrimitive) {
                        event.put(item.getKey(), item.getValue().getAsString());
                    } else {
                        event.put(item.getKey(), item.getValue().toString());
                    }
                }
            }
        }
        if (event != null && event.size() > 0) {
            format.processEvent(Pair.of(event, new kafkaState(topic, partition, offset, timestamp)));
        }
    }

    /**
     * 提交kafka offset
     * @param kafkaStates
     */
    public void submitOffsets(Collection<kafkaState> kafkaStates){
        this.commit.set(true);
        this.stateReference.getAndSet(kafkaStates);
    }

    @Override
    public void close() {
        try {
            running = false;
            consumer.wakeup();
        } catch (Exception e) {
            LOG.error("close kafka consumer error, e = {}", ExceptionUtil.getErrorMessage(e));
        }
    }

}
