package com.axway.yamles.utils.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParameterSet<T extends Parameter> {

	private List<T> params = new ArrayList<>();

	public ParameterSet() {
	}

	@SafeVarargs
	public ParameterSet(T... params) {
		add(params);
	}

	@SafeVarargs
	public final ParameterSet<T> add(T... params) {
		if (params == null)
			return this;

		for (T p : params) {
			if (get(p.getName()) != null) {
				throw new IllegalArgumentException("parameter '" + p.getName() + "' already added");
			}
			this.params.add(p);
		}

		return this;
	}

	public int size() {
		return this.params.size();
	}

	public T get(String name) {
		for (T p : this.params) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List<T> getParams() {
		return Collections.unmodifiableList(this.params);
	}
}
