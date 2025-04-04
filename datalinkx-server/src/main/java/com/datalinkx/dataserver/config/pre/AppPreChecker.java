package com.datalinkx.dataserver.config.pre;

import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.xxljob.XxlJobClient;
import com.datalinkx.dataclient.client.xxljob.request.PageQueryParam;
import com.datalinkx.dataclient.client.xxljob.request.XxlJobGroupParam;
import com.datalinkx.dataclient.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.dataclient.client.xxljob.response.ReturnT;
import com.datalinkx.dataserver.client.JobClientApi;
import com.datalinkx.dataserver.config.properties.XxlClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.datalinkx.common.constants.MessageHubConstants.GLOBAL_COMMON_GROUP;

/**
 * @author: uptown
 * @date: 2025/4/3 19:48
 */
@Slf4j
@Component
public class AppPreChecker implements ApplicationRunner {

    @Autowired
    JobClientApi jobClientApi;
    @Autowired
    XxlClientProperties xxlClientProperties;

    @Value("${client.datajob.url}")
    String dataJobUrl;

    // 自动注册xxl-job执行器
    @Override
    public void run(ApplicationArguments args) {
        try {

            JobGroupPageListResp jobGroupPageListResp = jobClientApi.jobGroupPage(jobClientApi.getDefaultJobGroupParam());

            if (!ObjectUtils.isEmpty(jobGroupPageListResp) && !ObjectUtils.isEmpty(jobGroupPageListResp.getData())) {
                return;
            }

            String executorUrl = String.format("%s:%s", dataJobUrl.substring(0, dataJobUrl.lastIndexOf(":")), xxlClientProperties.getExecutorPort());

            String jobGroupSaveResp = jobClientApi.jobGroupSave(
                    XxlJobGroupParam.builder()
                            .addressList(executorUrl)
                            .title(GLOBAL_COMMON_GROUP)
                            .appname(GLOBAL_COMMON_GROUP)
                            .build()
            );

            log.info("auto create xxl-job datalinkx executor success");
        } catch (Exception e) {

            log.error("###### damn it bro，system is a unhealth status !!!");
            log.error("###### check it ", e);
        }
    }
}
