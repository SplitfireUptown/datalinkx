package com.datalinkx.common.constants;


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
    }

    public static class JobStatus {
        public static final int JOB_TABLE_CREATE = 0;
        public static final int JOB_TABLE_SYNCING = 1;
        public static final int JOB_TABLE_NORMAL = 2;
        public static final int JOB_TABLE_ERROR = 3;
        public static final int JOB_TABLE_QUEUE = 4;
        public static final int JOB_TABLE_STOP = 5;

        public static final int JOB_STATUS_CREATE = 0;
        public static final int JOB_STATUS_SYNC = 1;
        public static final int JOB_STATUS_SUCCESS = 2;
        public static final int JOB_STATUS_ERROR = 3;
        public static final int JOB_STATUS_QUEUE = 4;
        public static final int JOB_STATUS_STOP = 5;

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
