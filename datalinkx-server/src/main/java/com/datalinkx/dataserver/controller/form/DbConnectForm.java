package com.datalinkx.dataserver.controller.form;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class DbConnectForm {
    @Data
    public static class DbTreeTableForm {
        @JsonProperty("connect_id")
        String connectId;
        String ref;
    }

    @Data
    public static class DbTreeForm {
        @JsonProperty("connect_id")
        String connectId;
    }

    @Data
    public static class QueryForm {
        @JsonProperty("connect_id")
        String connectId;

        @JsonProperty("sql")
        String sql;
    }

    @Data
    public static class WhereForm {
        @JsonProperty("connect_id")
        String connectId;

        @JsonProperty("ref")
        String ref;

        @JsonProperty("where")
        String where;
    }

    @Data
    public static class TablePreviewForm {
        @JsonProperty("connect_id")
        String connectId;

        @JsonProperty("table_id")
        String tableId;

        @JsonProperty("ref")
        String ref;
    }

    @Data
    public static class ConnectCreateForm {
        @JsonProperty("setup")
        String setup;

        @JsonProperty("type")
        String type;

        String owner;
    }

    @Data
    public static class ConnectUpdateForm {
        @JsonProperty("connect_id")
        String connectId;

        @JsonProperty("setup")
        Map<String, Object> setup;

        @JsonProperty("type")
        String type;

        String owner;
    }
}
