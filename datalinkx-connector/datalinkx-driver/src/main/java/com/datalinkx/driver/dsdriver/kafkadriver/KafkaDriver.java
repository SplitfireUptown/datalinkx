package com.datalinkx.driver.dsdriver.kafkadriver;

import java.util.stream.Collectors;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.TelnetUtil;
import com.datalinkx.driver.dsdriver.IStreamDriver;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.reader.ReaderInfo;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;
import com.datalinkx.driver.model.DataTransJobDetail;

public class KafkaDriver implements AbstractDriver<KafkaSetupInfo, KafkaReader, KafkaWriter>, IStreamDriver {
    private final KafkaSetupInfo kafkaSetupInfo;
    private final String connectId;
    public KafkaDriver(String connectId) {
        this.kafkaSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , KafkaSetupInfo.class);
        this.connectId = connectId;
    }


    @Override
    public Object connect(boolean check) throws Exception {
        TelnetUtil.telnet(this.kafkaSetupInfo.getServer(), this.kafkaSetupInfo.getPort());
        return null;
    }

    @Override
    public String getConnectId() {
        return this.connectId;
    }

    @Override
    public Object getReaderInfo(DataTransJobDetail.Reader reader) {
        ReaderInfo<KafkaReader> readerInfo = new ReaderInfo<>();
        readerInfo.setName("kafkacustomreader");

        readerInfo.setParameter(KafkaReader.builder()
                .topic(reader.getTableName())
                .codec("text")
                .blankIgnore(false)
                .consumerSettings(CommonSetting.builder().bootstrapServers(kafkaSetupInfo.getServer() + ":" + kafkaSetupInfo.getPort()).build())
                        .column(reader.getColumns().stream()
                                        .map(col -> MetaColumn.builder()
                                                .name(col.getName())
                                                .build()).collect(Collectors.toList()))
                .build());

        return readerInfo;
    }

    @Override
    public Object getWriterInfo(DataTransJobDetail.Writer writer) {
        WriterInfo<KafkaWriter> writerInfo = new WriterInfo<>();
        writerInfo.setName("kafkacustomwriter");

        writerInfo.setParameter(KafkaWriter.builder().topic(writer.getTableName())
                .timezone(kafkaSetupInfo.getTimezone())
                .producerSettings(CommonSetting.builder().bootstrapServers(kafkaSetupInfo.getServer() + ":" + kafkaSetupInfo.getPort()).build())
                .tableFields(writer.getColumns().stream().map(DataTransJobDetail.Column::getName).collect(Collectors.toList()))
                .build());
        return writerInfo;
    }
}
