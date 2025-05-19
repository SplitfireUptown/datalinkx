package com.datalinkx.kafkadriver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonSetting {
    @JsonProperty("bootstrap.servers")
    public String bootstrapServers;
}
