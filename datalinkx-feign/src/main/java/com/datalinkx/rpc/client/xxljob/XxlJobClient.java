package com.datalinkx.rpc.client.xxljob;


import com.datalinkx.rpc.client.xxljob.request.PageQueryParam;
import com.datalinkx.rpc.client.xxljob.request.XxlJobGroupParam;
import com.datalinkx.rpc.client.xxljob.request.XxlJobInfo;
import com.datalinkx.rpc.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.rpc.client.xxljob.response.ReturnT;
import com.datalinkx.rpc.config.FeignConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

/**
 * xxl-job service api interface
 */
@FeignClient(name = "xxlJobClient", url = "${xxl-job.client.url}", configuration = FeignConfig.class)
@ConditionalOnProperty(prefix = "xxl-job.client", name = "url")
public interface XxlJobClient {

    @RequestMapping("/xxl-job-admin/jobinfo/add")
    ReturnT<String> add(@SpringQueryMap XxlJobInfo xxlJobInfo);

    @PostMapping("/xxl-job-admin/jobgroup/save")
    ReturnT<String> jobGroupSave(@SpringQueryMap XxlJobGroupParam xxlJobGroupParam);

    @PostMapping("/xxl-job-admin/jobgroup/pageList")
    JobGroupPageListResp jobGroupPage(@SpringQueryMap PageQueryParam queryParam);

    @PostMapping("/xxl-job-admin/jobinfo/remove")
    ReturnT<String> remove(@RequestParam("id") int id);

    @PostMapping("/xxl-job-admin/jobinfo/start")
    ReturnT<String> start(@RequestParam("id") int id);

    @PostMapping("/xxl-job-admin/jobinfo/stop")
    ReturnT<String> stop(@RequestParam("id") int id);

    @PostMapping("/xxl-job-admin/jobinfo/trigger")
    ReturnT<String> trigger(@RequestParam("id") int id, @RequestParam("executorParam") String executorParam);
}
