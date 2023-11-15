package com.datalinkx.dataio.client.datalinkxserver;

import com.datalinkx.dataio.bean.JobStateForm;
import com.datalinkx.dataio.bean.JobSyncModeForm;
import com.datalinkx.driver.model.DataTransJobDetail;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryBean;


/**
 * @Description TODO
 * @createTime 2021年05月27日 10:06:16
 */
public interface DatalinkXServerClient {

    @GET("/api/job_graph/execute_info")
    WebResult<DataTransJobDetail> getJobExecInfo(@Query("jobId") String jobId);

    @POST("/api/job_graph/update_job")
    @FormUrlEncoded
    WebResult<String> updateJobStatus(@QueryBean JobStateForm jobStateForm);

    @POST("/api/job_graph/update_sync_mode")
    @FormUrlEncoded
    WebResult<String> updateSyncMode(@QueryBean JobSyncModeForm jobSyncModeForm);

    @POST("/api/job_graph/update_job_task_rel")
    @FormUrlEncoded
    WebResult<String> updateJobTaskRel(@Field("jobId") String jobId, @Field("taskId") String taskId);

}
