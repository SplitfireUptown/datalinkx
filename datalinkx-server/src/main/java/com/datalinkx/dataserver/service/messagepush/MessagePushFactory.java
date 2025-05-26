package com.datalinkx.dataserver.service.messagepush;

import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessagePushFactory {

    static ConcurrentHashMap<Integer, MessagePushService<? extends AlarmRuleDto.AlarmConfig>> SENDER_MAP = new ConcurrentHashMap<>();


    public static void registrySender(Integer type, MessagePushService<? extends AlarmRuleDto.AlarmConfig> messagePush) {
        SENDER_MAP.put(type, messagePush);
    }

    public MessagePushService getMessageSender(Integer type) {
        return SENDER_MAP.get(type);
    }
}
