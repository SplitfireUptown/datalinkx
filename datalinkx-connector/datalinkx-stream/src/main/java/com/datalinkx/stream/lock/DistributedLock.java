package com.datalinkx.stream.lock;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


/**
 * redis分布式锁
 */
@Service
public class DistributedLock {


	private static final int DEFAULT_APPEND_TIME = 60;

	@Resource
	StringRedisTemplate stringRedisTemplate;

	/**
	 * 仅保证实例间互斥，无法保证上锁解锁为同一实例
	 * @param lockKey
	 * @return
	 */
	public boolean setDistributedLock(String lockKey) {
		return stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", DEFAULT_APPEND_TIME, TimeUnit.SECONDS);
	}

	/**
	 * value都为1，不用校验是否为统一client避免原子性问题
	 * 采用发布订阅统一销毁链接
	 * @param lockKey
	 */
	public void releaseDistributedLock(String lockKey) {
		stringRedisTemplate.delete(lockKey);
	}


	public void expandLockTime(String lockKey) {
		stringRedisTemplate.expire(lockKey, DEFAULT_APPEND_TIME, TimeUnit.SECONDS);
	}

}
