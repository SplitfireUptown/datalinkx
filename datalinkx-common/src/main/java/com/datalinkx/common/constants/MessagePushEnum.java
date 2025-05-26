package com.datalinkx.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum MessagePushEnum {
    EMAIL("邮件", 0),
    DINGTALK("钉钉", 1);
    private String name;
    private Integer type;

    public static Integer getType(String name) {
        for (MessagePushEnum noticeMethodEnum : MessagePushEnum.values()) {
            if (name.equals(noticeMethodEnum.name())) {
                return noticeMethodEnum.getType();
            }
        }
        return null;
    }
}
