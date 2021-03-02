package com.cucund.spring.lock;

import com.cucund.spring.lock.impl.NativeLock;
import com.cucund.spring.lock.impl.RedisLock;
import lombok.Getter;

@Getter
public enum LockType {

	NATIVE(NativeLock.class),
	REDIS(RedisLock.class)
	;

	private Class<? extends KeyLock> keyLock;

	LockType(Class<? extends KeyLock> keyLock) {
		this.keyLock = keyLock;
	}


}
