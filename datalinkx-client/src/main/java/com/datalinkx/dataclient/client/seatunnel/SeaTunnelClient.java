package com.datalinkx.dataclient.client.seatunnel;

import com.datalinkx.dataclient.client.seatunnel.request.ComputeJobGraph;
import com.datalinkx.dataclient.client.seatunnel.request.JobStopReq;
import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.http.*;

import java.util.Map;

public interface SeaTunnelClient {

    @POST("/hazelcast/rest/maps/submit-job")
    JsonNode jobSubmit(@Body ComputeJobGraph computeJobGraph);

    @GET("/hazelcast/rest/maps/running-job/{jobId}")
    JsonNode jobOverview(@Path("jobId") String jobId);

    @POST("/hazelcast/rest/maps/stop-job")
    JsonNode jobStop(JobStopReq jobStopReq);
}
