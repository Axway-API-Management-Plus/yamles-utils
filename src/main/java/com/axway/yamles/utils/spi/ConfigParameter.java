package com.axway.yamles.utils.spi;

import java.util.Objects;

public class ConfigParameter extends Parameter {
	
	public enum Type { string, file };

	private final Type type;
	
	public ConfigParameter(String name, boolean required, String description, Type type) {
		super(name, required, description);
		this.type = Objects.requireNonNull(type);
	}
	
	public Type getType() {
		return this.type;
	}
}
