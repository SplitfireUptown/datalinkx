package com.datalinkx.dataserver.service.messagepush;

import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.stream.Collectors;

public interface MessagePushService<T extends AlarmRuleDto.AlarmConfig> extends InitializingBean {

    Integer getType();

    String send(T pushMessage);

    default String buildMessageMeta(List<String> messageMetas) {
        return messageMetas.stream().collect(Collectors.joining("\n\n"));
    }

    @Override
    default void afterPropertiesSet() {
        MessagePushFactory.registrySender(getType(), this);
    }
}
