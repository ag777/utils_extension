package com.ag777.util.lang.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * KeyLock类，用于对特定的key进行并发控制。
 * 使用方法：
 * 1. 创建KeyLock实例：KeyLock<String> keyLock = new KeyLock<>();
 * 2. 锁定key：keyLock.lock("key1");
 * 3. 解锁key：keyLock.unlock("key1");
 * 注意：每次lock调用必须对应一个unlock调用，否则会抛出IllegalStateException。
 *
 * @param <K> 锁定的key的类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/10/19 10:12
 */
public class KeyLock<K> {
    // 存储key和对应的Semaphore的映射
    private final ConcurrentMap<K, Semaphore> map = new ConcurrentHashMap<>();
    // 存储当前线程锁定的key和对应的LockInfo的映射
    private final ThreadLocal<Map<K, LockInfo>> local = ThreadLocal.withInitial(HashMap::new);

    /**
     * 锁定指定的key。
     * 如果key已经被当前线程锁定，则增加锁定计数。
     * 如果key已经被其他线程锁定，则阻塞直到其他线程解锁。
     *
     * @param key 需要锁定的key
     * @throws InterruptedException 如果当前线程在等待锁定时被中断
     */
    public void lock(K key) throws InterruptedException {
        if (key == null) {
            return;
        }
        LockInfo info = local.get().get(key);
        if (info == null) {
            Semaphore current = new Semaphore(1);
            current.acquire();
            Semaphore previous = map.putIfAbsent(key, current);
            if (previous != null) {
                previous.acquire();
            }
            local.get().put(key, new LockInfo(current));
        } else {
            info.lockCount++;
        }
    }

    /**
     * 解锁指定的key。
     * 如果key已经被当前线程锁定多次，则减少锁定计数。
     * 如果key只被当前线程锁定一次，则解锁key。
     * 如果key没有被当前线程锁定，不进行操作。
     *
     * @param key 需要解锁的key
     */
    public void unlock(K key) {
        if (key == null) {
            return;
        }
        LockInfo info = local.get().get(key);
        if (info != null) {
            if (--info.lockCount == 0) {
                info.current.release();
                map.remove(key, info.current);
                local.get().remove(key);
            }
        }
    }

    // 内部类，用于存储Semaphore和锁定计数
    private static class LockInfo {
        private final Semaphore current; // 当前的Semaphore
        private int lockCount; // 锁定计数

        private LockInfo(Semaphore current) {
            this.current = current;
            this.lockCount = 1;
        }
    }
}
