package com.ag777.util.lang.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 随机工具类。
 * <p>
 * 		采用JDK8带来的SecurityRandomUtils来生成随机数
 * </p>
 * 
 * @author ag777
 * @version create on 2018年06月29日,last modify at 2018年07月18日
 */
public class SecureRandomUtils {

	/**
	 * 默认算法
	 * 内置两种随机数算法，NativePRNG和SHA1PRNG，看实例化的方法了。通过new来初始化，默认来说会使用NativePRNG算法生成随机数，但是也可以配置-Djava.security参数来修改调用的算法。如果是/dev/[u]random两者之一就是NativePRNG，否则就是SHA1PRNG。
	 * 当然我们使用这个类去生成随机数的时候，一样只需要生成一个实例每次去生成随机数就好了，也没必要每次都重新生成对象。另外，这个类生成随机数，首次调用性能比较差，如果条件允许最好服务启动后先调用一下nextInt()。
		另外，实际上SHA1PRNG的性能将近要比NativePRNG的性能好一倍，synchronized的代码少了一半，所以没有特别重的安全需要，尽量使用SHA1PRNG算法生成随机数
	 */
	private static final String DEFAULT_ALGORITHM = "SHA1PRNG";
	private static SecureRandom secureRandom;
	
	public static SecureRandom getSecureRandom() {
		if(secureRandom == null) {
			synchronized (SecureRandomUtils.class) {
				if(secureRandom == null) {
					try {
						secureRandom = SecureRandom.getInstance(DEFAULT_ALGORITHM);
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
					
				}
			}
		}
		return secureRandom;
	}
	
	/**
	 * 生成指定区间[min,max)的int型数据,支持正数和负数
	 * <p>
	 * 最小值必须不比最大值大
	 * </p>
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int rInt(int min, int max) {
		if(min>max) {
			throw new IllegalArgumentException("最小值"+min+"不能大于最大值"+max);
		}
		if(min == max) {	//两个数相同，顺便可以避免两个数同时为0的情况
			return min;
		} else if(min>=0) {	//两个数均为正数
			return getSecureRandom().nextInt(max)%(max-min+1)+min;
		} else if(max<=0) {	//两个数均为负数
			int temp = max;
			max = -min;
			min = -temp;
			return -(getSecureRandom().nextInt(max)%(max-min+1)+min)-1;	//-1是为了满足区间左闭右开
		} else {	//最小值比0小，最大值比0大
			int total = max-min;
			return getSecureRandom().nextInt(total)+min;
		}
	}
	
}
