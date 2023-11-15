/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.dtstack.flinkx.kafkacustom.format;

import com.dtstack.flinkx.constants.ConstantValue;
import com.dtstack.flinkx.kafkacustom.client.KafkacustomConsumer;
import com.dtstack.flinkx.kafkabase.KafkaInputSplit;
import com.dtstack.flinkx.kafkabase.entity.kafkaState;
import com.dtstack.flinkx.kafkabase.enums.KafkaVersion;
import com.dtstack.flinkx.kafkabase.enums.StartupMode;
import com.dtstack.flinkx.kafkabase.format.KafkaBaseInputFormat;
import com.dtstack.flinkx.kafkabase.util.KafkaUtil;
import com.dtstack.flinkx.reader.MetaColumn;
import com.dtstack.flinkx.restore.FormatState;
//import com.dtstack.flinkx.util.ProtoBufUtil;
import com.dtstack.flinkx.util.RangeSplitUtil;
import com.dtstack.flinkx.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.types.Row;
import org.apache.kafka.common.PartitionInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Date: 2019/11/21
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class KafkacustomInputFormat extends KafkaBaseInputFormat {

    @Override
    protected InputSplit[] createInputSplitsInternal(int minNumSplits) {
        List<kafkaState> stateList = new ArrayList<>();
        org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(KafkaUtil.geneConsumerProp(consumerSettings, mode));
        if(StartupMode.TIMESTAMP.equals(mode)){
            List<PartitionInfo> partitionInfoList = consumer.partitionsFor(topic);
            for (PartitionInfo p : partitionInfoList) {
                stateList.add(new kafkaState(p.topic(), p.partition(), null, timestamp));
            }
        }else if(StartupMode.SPECIFIC_OFFSETS.equals(mode)){
            stateList = KafkaUtil.parseSpecificOffsetsString(topic, offset);
        }else{
            String[] topics = topic.split(ConstantValue.COMMA_SYMBOL);
            if(topics.length == 1){
                List<PartitionInfo> partitionInfoList = consumer.partitionsFor(topic);
                for (PartitionInfo p : partitionInfoList) {
                    stateList.add(new kafkaState(p.topic(), p.partition(), null, null));
                }
            }else{
                for (String tp : topics) {
                    List<PartitionInfo> partitionInfoList = consumer.partitionsFor(tp);
                    for (PartitionInfo p : partitionInfoList) {
                        stateList.add(new kafkaState(p.topic(), p.partition(), null, null));
                    }
                }
            }
        }

        List<List<kafkaState>> list = RangeSplitUtil.subListBySegment(stateList, minNumSplits);
        InputSplit[] splits = new InputSplit[minNumSplits];
        for (int i = 0; i < minNumSplits; i++) {
            splits[i] = new KafkaInputSplit(i, list.get(i));
        }

        return splits;
    }

    @Override
    public void openInputFormat() throws IOException {
        super.openInputFormat();
        Properties props = KafkaUtil.geneConsumerProp(consumerSettings, mode);
        consumer = new KafkacustomConsumer(props);
    }

    @Override
    public FormatState getFormatState() {
        super.getFormatState();
        if (formatState != null && MapUtils.isNotEmpty(stateMap)) {
            KafkacustomConsumer kafkacustomConsumer = (KafkacustomConsumer) this.consumer;
            List<kafkaState> list = new ArrayList<>(stateMap.size());
            for (kafkaState kafkaState : stateMap.values()) {
                list.add(kafkaState.clone());
            }
            kafkacustomConsumer.submitOffsets(list);
        }
        return formatState;
    }

    @Override
    public KafkaVersion getKafkaVersion() {
        return KafkaVersion.kafka;
    }

    @Override
    public void processEvent(Pair<Map<String, Object>, kafkaState> pair) {
        try {
            Row row;
            if(CollectionUtils.isEmpty(metaColumns)){
                row = Row.of(pair.getLeft());
//                User newUserObj = ProtoBufUtil.deserializer(row, String.class));
            }else{
                row = new Row(metaColumns.size());
                for (int i = 0; i < metaColumns.size(); i++) {
                    MetaColumn metaColumn = metaColumns.get(i);
                    Object value = pair.getLeft().get(metaColumn.getName());
                    try {
                        Object obj = StringUtil.string2col(String.valueOf(value), metaColumn.getType(), metaColumn.getTimeFormat());
                        row.setField(i , obj);
                    } catch (Exception e) {
                        row.setField(i , null);
                    }
                }
            }
            queue.put(row);

            kafkaState state = pair.getRight();
            stateMap.put(String.format("%s-%s", state.getTopic(), state.getPartition()), state);
        } catch (InterruptedException e) {
            LOG.error("takeEvent interrupted pair:{} error:{}", pair, e);
        }
    }
}
