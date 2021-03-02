package com.cucund.spring.lock.manager;


import com.cucund.spring.lock.KeyLock;
import com.cucund.spring.lock.LockEntity;

public class LockImplManager implements LockManager {

	private KeyLock keyLock ;

	public LockImplManager(KeyLock keyLock) {
		this.keyLock = keyLock;
	}

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
