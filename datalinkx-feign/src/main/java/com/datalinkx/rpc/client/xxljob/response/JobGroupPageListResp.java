package com.datalinkx.rpc.client.xxljob.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author: uptown
 * @date: 2025/4/4 12:20
 */
@Data
public class JobGroupPageListResp {
    private Integer recordsFiltered;
    private Integer recordsTotal;
    @Getter
    @Setter
    @JsonProperty("data")
    private List<JobGroupDetail> data;

    @Data
    public static class JobGroupDetail {
        private String addressList;
        private Integer addressType;
        private String appname;
        private String updateTime;
        private Long id;
        private List<String> registryList;
        private String title;
    }
}
