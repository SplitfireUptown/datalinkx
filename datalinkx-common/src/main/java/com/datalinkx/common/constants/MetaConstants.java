package com.datalinkx.common.constants;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetaConstants {
    private MetaConstants() {
    }
    /**
     * 数据源类型管理
     */
    public static class DsType {
        // 数据源类型
        public static final Integer MYSQL = 1;
        public static final Integer ELASTICSEARCH = 2;
        public static final Integer ORACLE = 3;
        public static final Integer REDIS = 4;
        public static final Integer KAFKA = 100;

        public static final String REDIS_SPIT_STR = "!-!-!";

        public static final Map<Integer, String> TYPE_TO_DB_NAME_MAP = new HashMap<Integer, String>() {{
            put(MetaConstants.DsType.MYSQL, "mysql");
            put(MetaConstants.DsType.ELASTICSEARCH, "elasticsearch");
            put(MetaConstants.DsType.ORACLE, "oracle");
            put(MetaConstants.DsType.REDIS, "redis");
            put(MetaConstants.DsType.KAFKA, "kafka");
        }};
    }

    public static class JobType {
        public static final Integer JOB_TYPE_STREAM = 1;
        public static final Integer JOB_TYPE_BATCH = 0;
    }

    public static class JobStatus {
        public static final int JOB_STATUS_CREATE = 0;
        public static final int JOB_STATUS_SYNCING = 1;
        public static final int JOB_STATUS_SUCCESS = 2;
        public static final int JOB_STATUS_ERROR = 3;
        public static final int JOB_STATUS_STOP = 4;

        public static final String SSE_JOB_STATUS = "jobList";
    }

    public static class JobSyncMode {
        public static final String INCREMENT_MODE = "increment";
        public static final String OVERWRITE_MODE = "overwrite";
    }

    public static class CommonConstant {
        public static final String TRACE_ID = "trace_id";
    }
}
