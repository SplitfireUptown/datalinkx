package com.datalinkx.sse.config;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.messagehub.config.annotation.MessageHub;
import org.springframework.stereotype.Service;

/**
 * @author: uptown
 * @date: 2023/11/13 21:48
 */
@Service
public class SsePushProcessor {

    @MessageHub(
            topic = MessageHubConstants.JOB_PROGRESS_TOPIC,
            group = MessageHubConstants.GLOBAL_COMMON_GROUP,
            type = MessageHubConstants.REDIS_STREAM_TYPE)
    public void pushJobStatus(String jobStatusMsg) {
        SseEmitterServer.sendMessage(MetaConstants.JobStatus.SSE_JOB_STATUS, jobStatusMsg);
    }
}
