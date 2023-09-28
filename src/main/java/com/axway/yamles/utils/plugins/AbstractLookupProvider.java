package com.axway.yamles.utils.plugins;

import java.util.List;

public abstract class AbstractLookupProvider implements LookupProvider {
	public static final ConfigParameter[] EMPTY_CONFIG_PARAMS = new ConfigParameter[0];
	public static final FunctionArgument[] EMPTY_FUNC_ARGS = new FunctionArgument[0];

	private final ParameterSet<FunctionArgument> funcArgs = new ParameterSet<>();
	private final ParameterSet<ConfigParameter> configParams = new ParameterSet<>();

	protected AbstractLookupProvider() {
	}

	protected void add(FunctionArgument... funcArgs) {
		this.funcArgs.add(funcArgs);
	}

	protected void add(ConfigParameter... configParams) {
		this.configParams.add(configParams);
	}

	@Override
	public List<FunctionArgument> getFunctionArguments() {
		return this.funcArgs.getParams();
	}

	@Override
	public List<ConfigParameter> getConfigParameters() {
		return this.configParams.getParams();
	}
}
