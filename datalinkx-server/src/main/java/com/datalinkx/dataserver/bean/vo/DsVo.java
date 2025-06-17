package com.datalinkx.dataserver.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

public class DsVo {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class MCPDsList {
        private String name;
        private String type;
        private String host;
        private Integer port;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class MCPDsInfo {
        private String name;
        private String type;
        private String host;
        private Integer port;
        private String username;
        private String config;
        private String schema;
    }
}
