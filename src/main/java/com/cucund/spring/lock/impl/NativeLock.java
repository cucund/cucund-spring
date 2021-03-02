package com.cucund.spring.lock.impl;


import com.cucund.spring.lock.KeyLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NativeLock implements KeyLock {

    volatile Map<String,Lock> lockMap = new ConcurrentHashMap<>();              //锁MAP
    volatile Map<String, AtomicStampedReference<Integer>> countMap = new ConcurrentHashMap<>();   //计数 MAP

    private final int i;
    private final TimeUnit unit;
    public NativeLock(int i, TimeUnit unit){
        this.unit = unit;
        this.i = i;
    }
    public NativeLock(){
        this(10,TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     */
    @Override
    public boolean tryLock(String key,Integer i, TimeUnit unit){
        Lock lock = getLock(key);
        try {
            if (i == null) i = this.i;
            if (unit == null) unit = this.unit;
            return lock.tryLock(i, unit);
        } catch (InterruptedException e) {
            decr(key);
            return false;
        }
    }


    /**
     * 加锁
     */
    @Override
    public void lock(String key) {
        Lock lock = getLock(key);
        lock.lock();
    }

    /**
     * 解锁
     */
    @Override
    public void unlock(String key) {
        decr(key);
    }

    private Lock getLock(String key) {
        Lock lock = lockMap.get(key);       //查询map锁
        if(lock == null){                   //锁为空
            lock = newLock(key);            //创建新锁
        }
        incr(key);                          //锁计数增加
        return lock;
    }




    /**
     * 创建新锁
     */
    private synchronized Lock newLock(String key) {
        Lock lock;
        if((lock = lockMap.get(key)) == null){                      // 重新获取锁是否被创建
            sleep();                                                // 同时 相同KEY 数据进入过多,原理不知道  求解释
            lockMap.put(key,lock = new ReentrantLock());            // 创建锁 并加入MAP集合
            countMap.put(key,new AtomicStampedReference<>(0,0)); //引用计数创建
        }
        return lock;
    }


    /**
     * 计数增加
     */
    private void incr(String key) {
        AtomicStampedReference<Integer> s = countMap.get(key);  //获取计数
        while (!s.compareAndSet(s.getReference(), s.getReference() + 1, s.getStamp(), s.getStamp() + 1)){}      //加一
    }


    /**
     * 计数减少
     */
    private synchronized void decr(String key) {
        AtomicStampedReference<Integer> s = countMap.get(key);  //获取计数
        while (!s.compareAndSet(s.getReference(), s.getReference() - 1, s.getStamp(), s.getStamp() + 1)){}      //减一
        if (s.getReference() == 0) {      //计数为 0 进入移除模式
            sleep();                            //同时过多相同key进入,睡眠后重新获取key计数0再删除
            if(s.getReference() == 0){    //计数再次读取 为0  确认进入移除模式
                countMap.remove(key);           //移除计数
                Lock lock = lockMap.remove(key);//移除LOCK 并获取锁
                lock.unlock();                  //锁 : 解锁
            }else{
                lockMap.get(key).unlock();      //锁 : 解锁
            }
        }else{
            lockMap.get(key).unlock();          //锁 : 解锁
        }
    }


    /**
     * 不知道为啥5毫秒  可以避免出错  小于5毫秒不行
     */
    private void sleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }

}
