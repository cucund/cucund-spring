package com.cucund.spring.lock;

import java.util.concurrent.TimeUnit;

public interface KeyLock {

    public boolean tryLock(String key,Integer i, TimeUnit unit) throws InterruptedException;

    public void lock(String key);

    public void unlock(String key);
}
