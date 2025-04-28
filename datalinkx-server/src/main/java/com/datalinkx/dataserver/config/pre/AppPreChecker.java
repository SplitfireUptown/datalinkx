package com.datalinkx.dataserver.config.pre;

import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.client.JobClientApi;
import com.datalinkx.dataserver.config.properties.XxlClientProperties;
import com.datalinkx.rpc.client.xxljob.request.XxlJobGroupParam;
import com.datalinkx.rpc.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.stream.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

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
    @Value("${client.datalinkxjob.url}")
    String dataJobUrl;
    @Resource
    DistributedLock distributedLock;

    // 自动注册xxl-job执行器
    @Override
    public void run(ApplicationArguments args) {
        // 防止其他节点已经创建
        String lockId = UUID.randomUUID().toString();
        try {
            while (true) {
                JobGroupPageListResp jobGroupPageListResp = jobClientApi.jobGroupPage(jobClientApi.getDefaultJobGroupParam());

                if (!ObjectUtils.isEmpty(jobGroupPageListResp) && !ObjectUtils.isEmpty(jobGroupPageListResp.getData())) {
                    break;
                }
                boolean isLock = distributedLock.lock(GLOBAL_COMMON_GROUP, lockId, DistributedLock.LOCK_TIME);
                if (isLock) {
                    String executorUrl = String.format("%s:%s", dataJobUrl.substring(0, dataJobUrl.lastIndexOf(":")), xxlClientProperties.getExecutorPort());

                    this.jobClientApi.jobGroupSave(
                            XxlJobGroupParam.builder()
                                    .addressList(executorUrl)
                                    .title(GLOBAL_COMMON_GROUP)
                                    .appname(GLOBAL_COMMON_GROUP)
                                    .build()
                    );

                    log.info("auto create xxl-job datalinkx executor success");
                    break;
                } else {
                    Thread.sleep(5000);
                }
            }
        } catch (Exception e) {
            log.error("###### damn it bro，system is a unhealth status !!!");
            log.error("###### check it ", e);
        } finally {
            try {

                distributedLock.unlock(GLOBAL_COMMON_GROUP, lockId);
            } catch (Exception e) {
                log.error("#### check redis health", e);
            }
        }
    }
}
