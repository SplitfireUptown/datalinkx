package com.datalinkx.dataio.client.flink;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月27日 10:06:16
 */
public interface FlinkClient {

    @GET("/jobs/{jobId}/exceptions")
    JsonNode jobExceptions(@Path("jobId") String jobId);

    @GET("/v1/jobs/{jobId}")
    JsonNode jobStatus(@Path("jobId") String jobId);

    @GET("/v1/jobs/{jobId}/accumulators")
    JsonNode jobAccumulators(@Path("jobId") String jobId);

    @GET("/v1/jobs/{jobId}/yarn-cancel")
    JsonNode jobStop(@Path("jobId") String jobId);
}
