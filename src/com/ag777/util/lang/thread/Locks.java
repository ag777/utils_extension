package com.ag777.util.lang.thread;

import java.util.concurrent.locks.StampedLock;

public class Locks {
	private Locks() {}
	
	/**
	 * 读写分离锁
	 * <p>
	 * 示例:
	 * [写操作]
	 * long stamp = sl.writeLock();	//获取写锁
		try {
			x += deltaX;
			y += deltaY;
		} finally {
			// 释放写锁
			sl.unlockWrite(stamp);
		}
		
		[读操作]
		long stamp = sl.tryOptimisticRead();	//获取读锁,这个值为0，代表有线程正在进行写操作
		这里是一系列赋值操作
		if (!sl.validate(stamp)) {	//判断写锁被占用
			stamp = sl.readLock();	//转化为悲观锁，等待写操作结束
			try{
			这里再次进行赋值操作
			} finally {
				// 释放读锁
				sl.unlockRead(stamp);
			}
		}
	 * </p>
	 * @return
	 */
	public static StampedLock getStampedLock() {
		return new StampedLock();
	}
}
