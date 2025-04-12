package com.datalinkx.rpc.client.xxljob;


import feign.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * xxl-job service api interface
 */
@FeignClient(name = "xxlLoginClient", url = "${xxl-job.client.url}")
@ConditionalOnProperty(prefix = "xxl-job.client", name = "url")
public interface XxlLoginClient {
    @PostMapping("/xxl-job-admin/login")
    Response login(@RequestParam("userName") String userName,
                   @RequestParam("password") String password);
}
