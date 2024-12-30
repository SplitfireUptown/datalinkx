package com.datalinkx.compute.connector.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class JdbcSink extends TransformNode {
    private String url;
    private String driver;
    private String user;
    private String password;
    private String query;
}