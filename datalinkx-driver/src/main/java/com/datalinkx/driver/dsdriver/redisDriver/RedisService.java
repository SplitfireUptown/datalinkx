package com.datalinkx.driver.dsdriver.redisDriver;

import com.datalinkx.common.utils.TelnetUtil;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisService {

    private RedisSetupInfo redisSetupInfo;
    private static JedisPool jedisPool;

    public RedisService(RedisSetupInfo redisSetupInfo) {
        this.redisSetupInfo = redisSetupInfo;
    }


    @SneakyThrows
    public Jedis getClient() {
        if (jedisPool == null){
            String host = this.redisSetupInfo.getHost();
            Integer port = this.redisSetupInfo.getPort();

            TelnetUtil.telnet(host, port);

            String password = this.redisSetupInfo.getPwd();
            int db = this.redisSetupInfo.getDatabase();

            jedisPool = new JedisPool(getConfig(), host, port, 3000, password, db);
        }
        return jedisPool.getResource();
    }

    private static JedisPoolConfig getConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setMaxTotal(500);
        jedisPoolConfig.setMinIdle(0);
        jedisPoolConfig.setMaxWaitMillis(2000);
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }
}
