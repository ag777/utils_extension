package com.ag777.util.lang.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.ag777.util.lang.collection.MapUtils;

/**
 * 带权重的奖池，适合权重不变的情况多次抽奖使用,内部采用Alias Method概率抽奖算法实现
 * @author ag777
 * @version create on 2020年06月14日, last modify at 2020年06月14日
 * @param <T> 
 */
public class RandomPool<T> {

	private List<T> keyList;
	private AliasMethod am;
	
	public RandomPool(Map<T, Double> map) {
		init(map, null);
	}
	
	public RandomPool(Map<T, Double> map, long randomSeed) {
		init(map, randomSeed);
	}
	
	private void init(Map<T, Double> map, Long randomSeed) {
		keyList = new ArrayList<>(map.size());
		List<Double> probabilities =  new ArrayList<>(map.size());
		map.forEach((key, probability)->{
			keyList.add(key);
			probabilities.add(probability);
		});
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if(randomSeed != null) {
			random.setSeed(randomSeed);
		}
		am = new AliasMethod(probabilities, random);
	}
	
	public T draw() {
		int index = am.next();
		return keyList.get(index);
	}
	
	
	public static void main(String[] args) {
		RandomPool<String> pool = new RandomPool<>(MapUtils.of(
				String.class, Double.class,
				"ssr", 0.02d,
				"sr", 0.08d,
				"r", 0.5d,
				"n", 0.4d
				));
		for(int i=0; i<100; i++) {
			String next = pool.draw();
			if("ssr".contentEquals(next)) {
				System.out.println(next);
			}
			
		}
	}
}
