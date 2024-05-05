package com.datalinkx.dataserver.client.datalinkxjob;

import com.datalinkx.common.result.WebResult;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DatalinkXJobClient {
    @POST("/data/transfer/stream_exec")
    WebResult<String> dataTransExec(@Query("detail") String dataTransJobDetail);
}
