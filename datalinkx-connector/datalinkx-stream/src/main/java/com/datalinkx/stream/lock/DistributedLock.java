package com.datalinkx.stream.lock;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;


/**
 * redis分布式锁
 */
@Service
public class DistributedLock {

	private static final String LOCK_LUA = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then redis.call('expire', KEYS[1], ARGV[2]) return 'true' else return 'false' end";
	private static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) end return 'true' ";

	private RedisScript lockRedisScript;
	private RedisScript unLockRedisScript;

	private RedisSerializer<String> argsSerializer;
	private RedisSerializer<String> resultSerializer;
	public static Integer LOCK_TIME = 15;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 初始化lua 脚本
	 */
	@PostConstruct
	public void init() {
		argsSerializer = new StringRedisSerializer();
		resultSerializer = new StringRedisSerializer();
		lockRedisScript = RedisScript.of(LOCK_LUA, String.class);
		unLockRedisScript = RedisScript.of(UNLOCK_LUA, String.class);
	}

	public boolean lock(String lock, String val, int second) {
		List<String> keys = Collections.singletonList(lock);
		String flag = redisTemplate.execute(lockRedisScript, argsSerializer, resultSerializer, keys, val, String.valueOf(second));
		return Boolean.parseBoolean(flag);
	}

	public void unlock(String lock, String val) {
		List<String> keys = Collections.singletonList(lock);
		redisTemplate.execute(unLockRedisScript, argsSerializer, resultSerializer, keys, val);
	}
}
