package com.datalinkx.compute.connector.jdbc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class JdbcSource extends TransformNode {
    private String url;
    private String driver;
    @Builder.Default
    @JsonProperty("connection_check_timeout_sec")
    private Integer connectionCheckTimeoutSec = 60;
    private String user;
    private String password;
    private String query;
    @Builder.Default
    @JsonProperty("split.size")
    private Integer splitSize = 1000;
}
