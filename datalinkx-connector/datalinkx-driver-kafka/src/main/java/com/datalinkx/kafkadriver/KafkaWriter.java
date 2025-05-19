package com.datalinkx.kafkadriver;

import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class KafkaWriter extends AbstractWriter {
    public String topic;
    public String timezone;
    public CommonSetting producerSettings;
    public List<String> tableFields;
}
