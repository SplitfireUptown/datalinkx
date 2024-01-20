package com.datalinkx.messagehub.bean.form;

import java.lang.reflect.Method;

import com.datalinkx.common.constants.MessageHubConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class ConsumerAdapterForm extends BaseMessageForm {

    // 消费者名称，配合消费者组
    private String consumerName = "datalinkx-consumer";

    // 消费者回调函数名
    private Method invokeMethod;

    // 特殊类型需要传递spring bean对象
    private Object bean;

    public String getTopic() {
        return MessageHubConstants.getExternalTopicName(super.getType(), super.getTopic());
    }
}
