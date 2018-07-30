package com.ag777.util.other;

import com.carrotsearch.sizeof.RamUsageEstimator;

/**
 * 对象占用空间大小获取类
 * <p>
 * 依赖java-sizeof-x.x.x.jar
 * </p>
 * @author ag777
 * @version create on 2018年07月30日,last modify at 2018年07月30日
 */
public class RamUtils {

	private RamUtils() {}
	
	/**
	 * 获取一个对象占用内存大小
	 * @param obj
	 * @return
	 */
	public static long sizeOf(Object obj) {
		return RamUsageEstimator.sizeOf(obj);
	}
}
