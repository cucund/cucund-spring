package com.cucund.spring.lock.manager;

import com.cucund.spring.lock.KeyLock;
import com.cucund.spring.lock.LockEntity;
import com.cucund.spring.lock.impl.NativeLock;
import org.springframework.stereotype.Component;


@Component
public class NativeLockManager implements LockManager{

	KeyLock keyLock = new NativeLock();

	@Override
	public void tryLock(LockEntity lockEntity) throws InterruptedException {
		if (lockEntity.getTime() == 0)
			keyLock.lock(lockEntity.getKey());
		else
			keyLock.tryLock(lockEntity.getKey(), lockEntity.getTime(), lockEntity.getTimeUnit());
	}

	@Override
	public void unLock(LockEntity lockEntity) {
		keyLock.unlock(lockEntity.getKey());
	}
}
