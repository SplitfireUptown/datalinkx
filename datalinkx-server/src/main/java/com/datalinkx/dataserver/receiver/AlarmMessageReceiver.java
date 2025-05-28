package com.datalinkx.dataserver.receiver;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;

import com.datalinkx.dataserver.bean.domain.AlarmComponentBean;
import com.datalinkx.dataserver.bean.domain.AlarmRuleBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.dto.AlarmMessageDto;
import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import com.datalinkx.dataserver.repository.AlarmRepository;
import com.datalinkx.dataserver.repository.AlarmRuleRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.messagepush.MessagePushService;
import com.datalinkx.dataserver.service.messagepush.MessagePushFactory;
import com.datalinkx.messagehub.config.annotation.MessageHub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警接收
 */
@Slf4j
@Component
public class AlarmMessageReceiver {

    @Autowired
    AlarmRuleRepository alarmRuleRepository;
    @Autowired
    AlarmRepository alarmRepository;
    @Autowired
    MessagePushFactory messagePushFactory;
    @Autowired
    JobRepository jobRepository;


    @MessageHub(
            topic = MessageHubConstants.JOB_ALARM_PUSH,
            group = MessageHubConstants.GLOBAL_COMMON_GROUP,
            type = MessageHubConstants.REDIS_STREAM_TYPE
    )
    public void alarmMessageAccept(String message) {
        this.pushMessage(message);
    }

    @MessageHub(
            topic = MessageHubConstants.JOB_ALARM_PUSH,
            group = MessageHubConstants.GLOBAL_COMMON_GROUP,
            type = MessageHubConstants.REDIS_STREAM_TYPE
    )
    public void alarmMessageAcceptLoadBalance(String message) {
        this.pushMessage(message);
    }


    public void pushMessage(String message) {
        List<String> alarmIds = new ArrayList<>();
        List<String> jobIds = new ArrayList<>();
        AlarmMessageDto alarmMessageDto = JsonUtils.toObject(message, AlarmMessageDto.class);
        List<AlarmRuleBean> alarmRuleJobs = alarmRuleRepository.findAllByJobId(alarmMessageDto.getJobId())
                .stream()
                .filter(rule -> Objects.equals(rule.getStatus(), MetaConstants.CommonConstant.KEY_ALARM_RULE_OPEN))
                .peek(rule -> {
                    alarmIds.add(rule.getAlarmId());
                    jobIds.add(rule.getJobId());
                })
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(alarmRuleJobs)) {
            return;
        }


        Map<String, AlarmComponentBean> alarmId2BeanMap = alarmRepository.findAllByAlarmIdIn(alarmIds).stream().collect(Collectors.toMap(AlarmComponentBean::getAlarmId, v -> v));
        Map<String, JobBean> jobId2BeanMap = jobRepository.findByJobIdIn(jobIds).stream().collect(Collectors.toMap(JobBean::getJobId, v -> v));

        for (AlarmRuleBean alarmRuleBean : alarmRuleJobs) {

            AlarmComponentBean alarmComponentBean = alarmId2BeanMap.get(alarmRuleBean.getAlarmId());
            if (ObjectUtils.isEmpty(alarmComponentBean)) {
                continue;
            }

            JobBean jobBean = jobId2BeanMap.get(alarmRuleBean.getJobId());
            if (ObjectUtils.isEmpty(jobBean)) {
                continue;
            }

            // 如果告警规则是失败后推送，但任务状态为成功，忽略
            if (MetaConstants.CommonConstant.ALARM_FAIL_STATUS.equals(alarmRuleBean.getType())
                    && MetaConstants.JobStatus.JOB_STATUS_SUCCESS == alarmMessageDto.getStatus()) {
                continue;
            }

            // 如果告警规则是成功后推送，但任务状态为不成功，忽略
            if (MetaConstants.CommonConstant.ALARM_SUCCESS_STATUS.equals(alarmRuleBean.getType())
                    && MetaConstants.JobStatus.JOB_STATUS_SUCCESS != alarmMessageDto.getStatus()) {
                continue;
            }

            MessagePushService messagePushService = messagePushFactory.getMessageSender(alarmComponentBean.getType());
            if (ObjectUtils.isEmpty(messagePushService)) {
                log.error("can not find message push service# {}", alarmRuleBean.getRuleId());
            }

            AlarmRuleDto.AlarmConfig alarmConfig = JsonUtils.toObject(alarmComponentBean.getConfig(), AlarmRuleDto.AlarmConfig.class);
            alarmConfig.setContent(this.buildMessageMeta(jobBean, alarmMessageDto));

            // 发送
            messagePushService.send(alarmConfig);

            alarmRuleBean.setPushTime(new Timestamp(System.currentTimeMillis()));
            alarmRuleRepository.save(alarmRuleBean);
        }
    }

    private List<String> buildMessageMeta(JobBean jobBean, AlarmMessageDto alarmMessageDto) {
        return new ArrayList<String>() {{
            add("DatalinkX Pro任务告警");
            add("流转任务名称：" + jobBean.getName());
            add("任务完成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            add("执行结果：" + MetaConstants.JobStatus.JOB_STATUS_TO_DB_NAME_MAP.get(alarmMessageDto.getStatus()));
            add("错误堆栈：" + alarmMessageDto.getErrmsg());
        }};
    }
}
