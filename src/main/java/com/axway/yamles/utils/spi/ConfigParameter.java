package com.axway.yamles.utils.spi;

import java.util.Objects;

public class ConfigParameter extends Parameter {

	public enum Type {
		string, file, bool
	};

	private final boolean mustache;
	private final Type type;

	/**
	 * Constructs a configuration parameter.
	 * 
	 * @param name        name of the parameter
	 * @param required    <i>true</i> if parameter is required, <i>false</i>
	 *                    otherwise
	 * @param description description of the parameter
	 * @param type        type of the parameter
	 * @param mustache    <i>true</i> if parameter supports Mustache template,
	 *                    <i>false</i> otherwise
	 */
	public ConfigParameter(String name, boolean required, String description, Type type) {
		this(name, required, description, type, false);
	}

	/**
	 * Constructs a configuration parameter.
	 * 
	 * @param name        name of the parameter
	 * @param required    <i>true</i> if parameter is required, <i>false</i>
	 *                    otherwise
	 * @param description description of the parameter
	 * @param type        type of the parameter
	 * @param mustache    <i>true</i> if parameter supports Mustache template,
	 *                    <i>false</i> otherwise
	 */
	public ConfigParameter(String name, boolean required, String description, Type type, boolean mustache) {
		super(name, required, description);
		this.type = Objects.requireNonNull(type);
		this.mustache = mustache;
	}

	public Type getType() {
		return this.type;
	}

	public boolean hasMustacheSupport() {
		return this.mustache;
	}	
}
