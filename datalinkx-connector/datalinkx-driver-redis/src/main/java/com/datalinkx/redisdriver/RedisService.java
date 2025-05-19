package com.datalinkx.redisdriver;

import com.datalinkx.driver.dsdriver.setupinfo.RedisSetupInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.SneakyThrows;

public class RedisService {

    private RedisSetupInfo redisSetupInfo;
    private RedisClient redisClient;

    public RedisService(RedisSetupInfo redisSetupInfo) {
        this.redisSetupInfo = redisSetupInfo;
    }


    @SneakyThrows
    public RedisClient getClient() {
        RedisURI redisUri = RedisURI.Builder.redis(redisSetupInfo.getHost())
                .withPort(redisSetupInfo.getPort())
                .withDatabase(redisSetupInfo.getDatabase())
                .withPassword(redisSetupInfo.getPwd())
                .build();
        redisClient = RedisClient.create(redisUri);
        return redisClient;
    }

}
