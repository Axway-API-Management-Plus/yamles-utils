package com.axway.yamles.utils.plugins;

public class FunctionArgument extends Parameter {
	private final boolean secret;

	public FunctionArgument(String name, boolean required, String description) {
		this(name, required, description, false);
	}

	public FunctionArgument(String name, boolean required, String description, boolean secret) {
		super(name, required, description);
		this.secret = secret;
	}

	public boolean isSecret() {
		return this.secret;
	}
}
