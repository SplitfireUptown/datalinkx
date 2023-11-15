package com.datalinkx.messagehub.bean.form;


import com.datalinkx.common.constants.MessageHubConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class ProducerAdapterForm extends BaseMessageForm {

    private String message = "";

    public String getTopic() {
        return MessageHubConstants.getExternalTopicName(super.getType(), super.getTopic());
    }
}
