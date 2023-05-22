package com.axway.yamles.utils.merge.config;

import java.util.ArrayDeque;
import java.util.Deque;

class YamlLocation {
	
	private final Deque<String> location = new ArrayDeque<>();
	
	
	public YamlLocation() {
	}
	
	public void clear() {
		this.location.clear();
	}

	public void push(String fieldName) {
		this.location.push(fieldName);
	}
	
	public void pop() {
		this.location.pop();
	}
	
	@Override
	public String toString() {
		if (this.location.isEmpty()) {
			return "/";
		}
		StringBuilder str = new StringBuilder();		
		this.location.descendingIterator().forEachRemaining((s) -> {
			str.append('/').append(s);
		});
		return str.toString();
	}
}
