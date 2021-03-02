package com.cucund.spring.lock.impl;

import com.cucund.spring.lock.KeyLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisLock implements KeyLock {

	private static final String LOCK_SUCCESS = "OK";
	private static final String SET_IF_NOT_EXIST = "NX";
	private static final String SET_WITH_EXPIRE_TIME = "PX";
	@Resource
	RedisTemplate<String, String> redisTemplate;

	private static final Long RELEASE_SUCCESS = 1L;

	private String scriptStr = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
	RedisScript<Long> script = new DefaultRedisScript(scriptStr,Long.class);
	ThreadRedisLockValue threadRedisLockValue = new ThreadRedisLockValue();
	@Override
	public boolean tryLock(String key, Integer expireTime, TimeUnit unit) throws InterruptedException {
		Long start = System.currentTimeMillis();
		long timeOut = unit.toMillis(expireTime);
		String value = UUID.randomUUID().toString();
		try{
			for(;;){
				//SET命令返回OK ，则证明获取锁成功
				Boolean ret = redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, unit);
				if(ret) {
					threadRedisLockValue.set(value);
					return true;
				}
				Thread.sleep(50);
				//否则循环等待，在timeout时间内仍未获取到锁，则获取失败
				long end = System.currentTimeMillis() - start;
				if (end >= timeOut) return false;
			}
		}finally {

		}

	}

	@Override
	public void lock(String key) {
		String value = UUID.randomUUID().toString();
		try {
			for (; ; ) {
				//SET命令返回OK ，则证明获取锁成功
				Boolean ret = redisTemplate.opsForValue().setIfAbsent(key, value, 30, TimeUnit.SECONDS);
				if (ret) {
					threadRedisLockValue.set(value);
					return;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}finally {
		}
	}

	@Override
	public void unlock(String key) {
		String value = threadRedisLockValue.get();
		try{
			for(;;){
				//SET命令返回OK ，则证明获取锁成功
				Object result = redisTemplate.execute(script, Collections.singletonList(key),value);
				if(RELEASE_SUCCESS.equals(result))
					return ;
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {

		}
	}
}

class ThreadRedisLockValue extends ThreadLocal<String>{}
