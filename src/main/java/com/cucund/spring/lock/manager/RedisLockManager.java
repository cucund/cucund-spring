package com.cucund.spring.lock.manager;

import com.cucund.spring.lock.KeyLock;
import com.cucund.spring.lock.LockEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisLockManager implements LockManager{

	@Resource(name = "redisLock")
	KeyLock redisLock ;

	@Override
	public void tryLock(LockEntity lockEntity) throws InterruptedException {
		if (lockEntity.getTime() == 0)
			redisLock.lock(lockEntity.getKey());
		else
			if(!redisLock.tryLock(lockEntity.getKey(), lockEntity.getTime(), lockEntity.getTimeUnit()))
				throw new RuntimeException("锁获取失败");
	}

	@Override
	public void unLock(LockEntity lockEntity) {
		redisLock.unlock(lockEntity.getKey());
	}
}
