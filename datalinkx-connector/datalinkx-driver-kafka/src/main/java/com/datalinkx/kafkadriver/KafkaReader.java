package com.datalinkx.kafkadriver;


import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
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
public class KafkaReader extends AbstractReader {
    private String topic;
    private String mode;
    // 编码解码器类型，支持 json、text
    private String codec;
    // 是否忽略空值消息
    private Boolean blankIgnore;
    private CommonSetting consumerSettings;
    private List<String> column;
}