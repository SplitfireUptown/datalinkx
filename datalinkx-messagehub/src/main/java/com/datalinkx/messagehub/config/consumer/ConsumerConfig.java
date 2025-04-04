package com.datalinkx.messagehub.config.consumer;

import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.config.annotation.MessageHub;
import com.datalinkx.messagehub.service.MessageHubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;


@Slf4j
@Order
@Component
public class ConsumerConfig implements BeanPostProcessor, EnvironmentAware {

    Environment environment;

    @Resource(name = "messageHubServiceImpl")
    MessageHubService messageHubService;

    @Resource(name = "resetBlockingQueue")
    BlockingQueue<ConsumerAdapterForm> resetBlockingQueue;


    @Bean
    public BlockingQueue<ConsumerAdapterForm> resetBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }



    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)  throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            MessageHub annotation = AnnotationUtils.findAnnotation(method, MessageHub.class);
            if (annotation == null) {
                continue;
            }

            log.info("discovery messagehub consumer bean:  class {}#{}", beanName, method.getName());

            // 1、解析topic是否配置在配置文件中，不存在返回原字符串
            String resolveTopic = environment.resolvePlaceholders(annotation.topic());

            ConsumerAdapterForm adapterForm = new ConsumerAdapterForm();
            adapterForm.setBean(bean);
            adapterForm.setInvokeMethod(method);
            adapterForm.setTopic(resolveTopic);
            adapterForm.setType(annotation.type());
            adapterForm.setGroup(annotation.group());
            this.consumerProxy(adapterForm);
        }
        return bean;
    }



    private void consumerProxy(ConsumerAdapterForm messageForm) {
        try {
            messageHubService.consume(messageForm);
        } catch (Exception e) {
            log.warn("messagehub consumer init failed: type {}, topic {} , class {}#{}", messageForm.getType(), messageForm.getTopic(), messageForm.getBean().getClass().getName(), messageForm.getInvokeMethod().getName());
            log.warn("we will retry some times", e);
            resetBlockingQueue.add(messageForm);
        }
    }



    @Override
    public void setEnvironment(Environment environment) {
        Thread healthThread = new Thread(new ConsumerInitRetryTask(messageHubService, resetBlockingQueue));
        healthThread.setDaemon(true);
        healthThread.start();

        this.environment = environment;
    }
}
