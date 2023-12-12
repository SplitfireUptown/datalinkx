package com.datalinkx.datajob.client.flink;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
