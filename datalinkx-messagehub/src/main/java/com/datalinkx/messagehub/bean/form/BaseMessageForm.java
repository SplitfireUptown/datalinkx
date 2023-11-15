package com.datalinkx.messagehub.bean.form;

import lombok.Data;

@Data
public class BaseMessageForm {
    // 消息组件类型
    private String type;
    // 消费主题
    private String topic;
    // 消费者组
    private String group = "datalinkx";
}
