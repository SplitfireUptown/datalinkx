package com.datalinkx.driver.dsdriver.kafkadriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaSetupInfo extends SetupInfo {
    public String server;
    public Integer port;
    public String timezone;
}
