package com.datalinkx.dataclient.client.xxljob;

import java.util.Map;

import com.datalinkx.dataclient.client.xxljob.request.LogQueryParam;
import com.datalinkx.dataclient.client.xxljob.request.PageQueryParam;
import com.datalinkx.dataclient.client.xxljob.request.XxlJobGroupParam;
import com.datalinkx.dataclient.client.xxljob.request.XxlJobInfo;
import com.datalinkx.dataclient.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.dataclient.client.xxljob.response.ReturnT;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.QueryBean;

/**
 * xxl-job service api interface
 */
public interface XxlJobClient {

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobinfo/add")
    ReturnT<String> add(@QueryBean XxlJobInfo xxlJobInfo);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobgroup/save")
    ReturnT<String> jobGroupSave(@QueryBean XxlJobGroupParam xxlJobGroupParam);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobgroup/pageList")
    JobGroupPageListResp jobGroupPage(@QueryBean PageQueryParam queryParam);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobinfo/remove")
    ReturnT<String> remove(@Field("id") int id);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobinfo/start")
    ReturnT<String> start(@Field("id") int id);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobinfo/stop")
    ReturnT<String> stop(@Field("id") int id);

    @FormUrlEncoded
    @POST("/xxl-job-admin/jobinfo/trigger")
    ReturnT<String> trigger(@Field("id") int id, @Field("executorParam") String executorParam);
}
