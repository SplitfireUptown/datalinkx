package com.datalinkx.messagehub.config.consumer;


import java.util.concurrent.BlockingQueue;

import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ConsumerInitRetryTask implements Runnable {

    MessageHubService messageHubService;

    BlockingQueue<ConsumerAdapterForm> resetBlockingQueue;

    public ConsumerInitRetryTask(MessageHubService messageHubService, BlockingQueue<ConsumerAdapterForm> resetBlockingQueue) {
        this.messageHubService = messageHubService;
        this.resetBlockingQueue = resetBlockingQueue;
    }

    @Override
    public void run() {
        // 启动时检查，如果队列空就没必要监听了
        while (!resetBlockingQueue.isEmpty()) {
            ConsumerAdapterForm headForm = resetBlockingQueue.poll();
            int retryTime = 100;
            while (retryTime > 0) {
                try {
                    this.messageHubService.consume(headForm);
                    log.info("messagehub consumer init retry successful: type {}, topic {} , class {}#{}", headForm.getType(), headForm.getTopic(), headForm.getBean().getClass().getName(), headForm.getInvokeMethod().getName());
                    break;
                } catch (Exception e) {
                    retryTime--;
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            if (retryTime == 0) {
                log.warn("messagehub consumer init retry failed: type {}, topic {} , class {}#{}", headForm.getType(), headForm.getTopic(), headForm.getBean().getClass().getName(), headForm.getInvokeMethod().getName());
            }
        }
    }
}


