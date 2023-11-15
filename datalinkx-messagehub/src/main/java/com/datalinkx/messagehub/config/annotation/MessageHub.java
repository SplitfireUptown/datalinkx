package com.datalinkx.messagehub.config.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface MessageHub {
    // topic
    String topic() default "";

    // 消费者组，一条消息只能被同组的一个消费者消费
    String group() default "";

    // 消费类型
    String type() default "";
}
