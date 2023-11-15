package com.datalinkx.messagehub.demo;

import com.datalinkx.messagehub.config.annotation.MessageHub;
import lombok.extern.slf4j.Slf4j;


@Slf4j
//@Service
public class RedisConsumerDemo {

    @MessageHub(topic = "${uptown.topic}", type = "REDIS_PUBSUB")
    public void consumer(Object message) {

        log.info("pubsub info {} ", message);
    }

    @MessageHub(topic = "${uptown.topic}", type = "REDIS_PUBSUB")
    public void consumer2(Object message) {

        log.info("pubsub info {} ", message);
    }

    @MessageHub(topic = "uptown_queue", type = "REDIS_QUEUE")
    public void redisQueue(String message) {
        log.info("queue info {}", message);
    }

    @MessageHub(topic = "uptown_queue", type = "REDIS_QUEUE")
    public void redisQueue2(String message) {
        log.info("queue22222222 info {}", message);
    }


    @MessageHub(topic = "uptown_stream", group = "uptown-group", type = "REDIS_STREAM")
    public void redisStream(String message) {
        log.info("uptown-group stream info {}", message);
    }

    @MessageHub(topic = "uptown_stream", group = "uptown-group2", type = "REDIS_STREAM")
    public void redisStream2(String message) {
        log.info("uptown-group2 stream info {}", message);
    }
}
