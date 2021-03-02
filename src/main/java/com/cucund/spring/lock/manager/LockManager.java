package com.cucund.spring.lock.manager;


import com.cucund.spring.lock.LockEntity;

public interface LockManager {

	void tryLock(LockEntity lockEntity) throws InterruptedException;

	void unLock(LockEntity lockEntity);

}
