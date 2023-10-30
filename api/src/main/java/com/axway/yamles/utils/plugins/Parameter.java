package com.axway.yamles.utils.plugins;

import java.util.Objects;

public class Parameter {
	private final String name;
	private final boolean required;
	private final String description;

	public Parameter(String name) {
		this(name, false);
	}

	public Parameter(String name, boolean required) {
		this(name, required, null);
	}

	public Parameter(String name, boolean required, String description) {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("parameter name is null or empty");
		this.name = name;
		this.required = required;
		this.description = (description != null) ? description : "";
	}

	public String getName() {
		return this.name;
	}

	public boolean isRequired() {
		return required;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		return Objects.equals(name, other.name);
	}
}
