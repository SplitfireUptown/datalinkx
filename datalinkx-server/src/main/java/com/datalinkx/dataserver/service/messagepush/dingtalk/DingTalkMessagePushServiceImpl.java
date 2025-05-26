package com.datalinkx.dataserver.service.messagepush.dingtalk;

import cn.hutool.http.HttpUtil;
import com.datalinkx.common.constants.MessagePushEnum;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import com.datalinkx.dataserver.service.messagepush.MessagePushService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class DingTalkMessagePushServiceImpl implements MessagePushService<AlarmRuleDto.AlarmDingTalkDto> {

    private static final Integer DING_TIME_OUT = 5000;

    @Override
    public Integer getType() {
        return MessagePushEnum.DINGTALK.getType();
    }

    @Override
    public String send(AlarmRuleDto.AlarmDingTalkDto alarmDingTalkDto) {
        String url = alarmDingTalkDto.getWebhook();
        if (!ObjectUtils.isEmpty(alarmDingTalkDto.getSecret())) {
            url = url + this.getSign(alarmDingTalkDto.getSecret());
        }

        String messageMeta = this.buildMessageMeta(alarmDingTalkDto.getContent());
        String requestBody = buildReqStr(messageMeta, true);
        return HttpUtil.post(url, requestBody);
    }

    @SneakyThrows
    private String getSign(String secret) {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        return "&timestamp=" + timestamp + "&sign=" + sign;
    }

    private static String buildReqStr(String content, boolean isAtAll) {
        //消息内容
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = new HashMap<>();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);

        return JsonUtils.toJson(reqMap);
    }
}
