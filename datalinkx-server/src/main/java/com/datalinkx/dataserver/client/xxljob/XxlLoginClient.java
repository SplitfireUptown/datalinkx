package com.datalinkx.dataserver.client.xxljob;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * xxl-job service api interface
 */
public interface XxlLoginClient {
    @FormUrlEncoded
    @POST("/xxl-job-admin/login")
    Call<ResponseBody> login(@Field("userName") String userName,
                             @Field("password") String password,
                             @Field("ifRemember") String ifRemember);
}
