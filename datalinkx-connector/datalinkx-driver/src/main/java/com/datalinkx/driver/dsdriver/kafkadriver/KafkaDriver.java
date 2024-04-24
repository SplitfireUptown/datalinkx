package com.datalinkx.driver.dsdriver.kafkadriver;

import java.util.stream.Collectors;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.IStreamDriver;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.reader.ReaderInfo;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;
import com.datalinkx.driver.model.DataTransJobDetail;

public class KafkaDriver implements AbstractDriver<KafkaSetupInfo, KafkaReader, KafkaWriter>, IStreamDriver {
    private final KafkaSetupInfo kafkaSetupInfo;

    public KafkaDriver(String connectId) {
        this.kafkaSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , KafkaSetupInfo.class);
    }


    @Override
    public Object getReaderInfo(FlinkActionMeta param) {
        ReaderInfo<KafkaReader> readerInfo = new ReaderInfo<>();
        readerInfo.setName("kafkacustomreader");

        readerInfo.setParameter(KafkaReader.builder()
                .topic(kafkaSetupInfo.getServer())
                .mode(kafkaSetupInfo.getMode())
                .codec("text")
                .blankIgnore(false)
                .consumerSettings(CommonSetting.builder().bootstrapServers(kafkaSetupInfo.getServer()).build())
                        .column(param.getReader().getColumns().stream()
                                        .map(col -> MetaColumn.builder()
                                                .name(col.getName())
                                                .build()).collect(Collectors.toList()))
                .build());

        return readerInfo;
    }

    @Override
    public Object getWriterInfo(FlinkActionMeta param) {
        WriterInfo<KafkaWriter> writerInfo = new WriterInfo<>();
        writerInfo.setName("kafkacustomwriter");

        writerInfo.setParameter(KafkaWriter.builder().topic(kafkaSetupInfo.getTopic())
                .timezone(kafkaSetupInfo.getTimezone())
                .producerSettings(CommonSetting.builder().bootstrapServers(kafkaSetupInfo.server).build())
                .tableFields(param.getWriter().getColumns().stream().map(DataTransJobDetail.Column::getName).collect(Collectors.toList()))
                .build());
        return writerInfo;
    }
}
