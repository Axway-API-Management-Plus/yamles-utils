package com.axway.yamles.utils.plugins;

import java.util.Objects;

public abstract class AbstractProvider implements Provider {

	private ExecutionMode mode = ExecutionMode.CONFIG;

	@Override
	public void onInit(ExecutionMode mode) {
		this.mode = Objects.requireNonNull(mode, "exeuction mode required");
	}

	@Override
	public ExecutionMode getMode() {
		return this.mode;
	}
}
