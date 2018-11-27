package com.ag777.util.lang;

import java.util.Map;

import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.interf.Disposable;


/**
 * 双向map
 * <p>
 * 用于正向及反向查询数据
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月27日,last modify at 2018年11月27日
 * @param <K>
 * @param <V>
 */
public class Dict<K, V> implements Disposable{

	private Map<K, V> map1;
	private Map<V, K> map2;
	
	public Dict() {
		map1 = MapUtils.newHashMap();
		map2 = MapUtils.newHashMap();
	}
	
	/**
	 * 获取正向map
	 * @return
	 */
	public Map<K, V> getMap1() {
		return map1;
	}

	/**
	 * 获取反向map
	 * @return
	 */
	public Map<V, K> getMap2() {
		return map2;
	}

	/**
	 * 添加一条对应关系
	 * <p>
	 * 如果key或value为null会抛出异常<br>
	 * 如果存在重复的key或value会抛出异常
	 * </p>
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws RuntimeException
	 */
	public Dict<K, V> put(K key, V value) throws RuntimeException {
		if(key == null || value == null) {
			throw new RuntimeException("键或值不能为空:"+key+":"+value);
		}
		if(map1.containsKey(key)) {
			throw new RuntimeException("重复的key:"+key);
		} else if(map2.containsKey(value)) {
			throw new RuntimeException("重复的key:"+value);
		}
		map1.put(key, value);
		map2.put(value, key);
		return this;
	}
	
	/**
	 * 添加多条对应关系
	 * 
	 * @see #put(Object, Object)
	 * @param map
	 * @return
	 * @throws RuntimeException
	 */
	public Dict<K, V> putAll(Map<K, V> map) throws RuntimeException {
		if(map != null) {
			map.forEach((k, v)->{
				put(k, v);
			});
		}
		return this;
	}
	
	/**
	 * 清除所有数据
	 * @return
	 */
	public Dict<K, V> clear() {
		if(map1 != null) {
			map1.clear();
		}
		if(map2 != null) {
			map2.clear();
		}
		return this;
	}
	
	/**
	 * 通过键获取值
	 * <p>
	 * 不存在key时返回null
	 * </p>
	 * @param key
	 * @return
	 */
	public V get(K key) {
		return MapUtils.get(map1, key);
	}
	
	/**
	 * 通过键获取值(带默认值)
	 * @param key
	 * @param defVal
	 * @return
	 */
	public V getOrDef(K key, V defVal) {
		return MapUtils.get(map1, key, defVal);
	}
	
	/**
	 * 是否存在key
	 * @param key
	 * @return
	 */
	public boolean containKey(K key) {
		return map1.containsKey(key);
	}
	
	/**
	 * 通过值获取键
	 * <p>
	 * 不存在key时返回null
	 * </p>
	 * @param value
	 * @return
	 */
	public K getByKey(V value) {
		return MapUtils.get(map2, value);
	}
	
	/**
	 * 通过值获取键(带默认值)
	 * @param value
	 * @param defKey
	 * @return
	 */
	public K getOrDefByKey(V value, K defKey) {
		return MapUtils.get(map2, value, defKey);
	}
	
	/**
	 * 通过值获取键
	 * @param val
	 * @return
	 */
	public boolean containValue(V val) {
		return map2.containsKey(val);
	}

	@Override
	public void dispose() {
		clear();
		map1 = null;
		map2 = null;
	}
	
}
