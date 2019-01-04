package com.game.framework.component.collections.map.lru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.game.framework.utils.collections.MapUtils;



/**
 * 自动清除HashMap(带线程同步)
 * LRULockCache.java
 * @author JiangBangMing
 * 2019年1月3日下午1:29:31
 */
public class LRULockCache<K, V> extends LRULockCache0<K, V> {
	public LRULockCache(int cacheSize) {
		super(cacheSize);
	}

	/**
	 * @param k
	 * @param v
	 * @return V
	 * @see HashMap#put(Object, Object)
	 */
	public V put(K k, V v) {
		return super.put0(k, v);
	}

	/**
	 * @param k
	 * @param v
	 * @return V
	 * @see ConcurrentMap#putIfAbsent(Object, Object)
	 */
	public V putIfAbsent(K k, V v) {
		return super.putIfAbsent0(k, v);
	}

	/**
	 * @param k
	 * @return V
	 * @see LinkedHashMap#get(Object)
	 */
	public V get(K k) {
		return super.get0(k);
	}

	/**
	 * @see List
	 * @see ArrayList
	 * @return List<V>
	 */
	public List<V> getAll() {
		return super.getAll0();
	}

	/**
	 * @param k
	 * @return V
	 * @see HashMap#remove(Object)
	 */
	public V remove(K k) {
		return super.remove0(k);
	}

	public void clear() {
		super.clear0();
	}

	public int action(MapUtils.IAction<? super K, ? super V> action, int maxCount) {
		return super.action0(action, maxCount);
	}

	/**
	 * 遍历执行(只读)
	 * 
	 * @param action
	 * @param maxCount
	 * @return
	 */
	public int action0(MapUtils.IAction<? super K, ? super V> action, int maxCount) {
		return super.action1(action, maxCount);
	}

	/**
	 * 设置移除监听器
	 * 
	 * @param listener
	 */
	public void setListener(LRURemoveListener<K, V> listener) {
		super.setListener0(listener);
	}

	/**
	 * 获取移除监听
	 * 
	 * @return
	 */
	public LRURemoveListener<K, V> getListener() {
		return super.getListener0();
	}

}
