package com.axway.yamles.utils.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMap<K, V> {

	private final Map<K, List<V>> ml = new HashMap<>();

	public ListMap() {

	}

	public void put(K k, List<V> v) {
		getList(k).addAll(v);
	}

	public void put(K k, V v) {
		getList(k).add(v);
	}

	public List<V> get(K k) {
		return this.ml.get(k);
	}

	public Map<K, List<V>> getMap() {
		return Collections.unmodifiableMap(this.ml);
	}

	private List<V> getList(K k) {
		List<V> list = ml.get(k);
		if (list == null) {
			synchronized (this) {
				list = ml.get(k);
				if (list == null) {
					list = new ArrayList<V>();
					this.ml.put(k, list);
				}
			}
		}
		return list;
	}
}
