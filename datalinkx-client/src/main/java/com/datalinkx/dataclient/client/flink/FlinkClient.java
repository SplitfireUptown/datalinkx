package com.datalinkx.dataclient.client.flink;

import com.datalinkx.dataclient.client.flink.request.FlinkJobStopReq;
import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FlinkClient {

    @GET("/jobs/overview")
    JsonNode jobOverview();

    @GET("/jobs/{jobId}/exceptions")
    JsonNode jobExceptions(@Path("jobId") String jobId);

    @GET("/v1/jobs/{jobId}")
    JsonNode jobStatus(@Path("jobId") String jobId);

    @GET("/v1/jobs/{jobId}/accumulators")
    JsonNode jobAccumulators(@Path("jobId") String jobId);

    @GET("/jobs/{jobId}/checkpoints")
    JsonNode jobCheckpoint(@Path("jobId") String jobId);

    @POST("/jobs/{jobId}/stop")
    JsonNode jobStop(@Path("jobId") String jobId, @Body FlinkJobStopReq flinkJobStopReq);
}
