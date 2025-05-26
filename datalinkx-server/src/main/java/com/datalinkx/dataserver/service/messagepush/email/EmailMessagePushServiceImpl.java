package com.datalinkx.dataserver.service.messagepush.email;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.datalinkx.common.constants.MessagePushEnum;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import com.datalinkx.dataserver.service.messagepush.MessagePushService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailMessagePushServiceImpl implements MessagePushService<AlarmRuleDto.AlarmEmailDto> {


    @Override
    public Integer getType() {
        return MessagePushEnum.EMAIL.getType();
    }


    @Override
    public String send(AlarmRuleDto.AlarmEmailDto pushMessage) {
        MailAccount account = new MailAccount();
        account.setHost(pushMessage.getHost());
        account.setPort(pushMessage.getPort());
        account.setAuth(true);
        account.setFrom(pushMessage.getFrom());
        account.setUser(pushMessage.getFrom());
        account.setPass(pushMessage.getPassword());
        account.setSslEnable(true);
        String messageMeta = this.buildMessageMeta(pushMessage.getContent());

        return MailUtil.send(
                account,
                CollUtil.newArrayList(pushMessage.getTo()),
                MetaConstants.CommonConstant.GLOBAL_ALARM_SUBJECT,
                messageMeta,
                true
        );
    }

    // 转成html
    @Override
    public String buildMessageMeta(List<String> messageMetas) {
        StringBuilder meta = new StringBuilder();
        for (int i = 0; i < messageMetas.size(); i++) {
            if (i == 0) {
                meta.append(String.format("<h1>%s</h1><br>", messageMetas.get(i)));
            }
            else {
                meta.append(String.format("<b>%s</b><br>", messageMetas.get(i)));
            }
        }
        return meta.toString();
    }
}
