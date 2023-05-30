package com.axway.yamles.utils.spi.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupProvider;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.ParameterSet;

public abstract class AbstractLookupProvider implements LookupProvider {
	public static final ConfigParameter[] EMPTY_CONFIG_PARAMS = new ConfigParameter[0];
	public static final FunctionArgument[] EMPTY_FUNC_ARGS = new FunctionArgument[0];
	
	public final FunctionArgument ARG_KEY;

	private final ParameterSet<FunctionArgument> funcArgs = new ParameterSet<FunctionArgument>();
	private final ParameterSet<ConfigParameter> configParams = new ParameterSet<>();
	
	protected AbstractLookupProvider(String keyDescription, FunctionArgument[] funcArgs, ConfigParameter[] configParams) {
		if (keyDescription == null || keyDescription.isEmpty()) {
			throw new IllegalArgumentException("key description is null or empty");
		}
		this.ARG_KEY = new FunctionArgument("key", true, keyDescription);
		this.funcArgs.add(this.ARG_KEY);
		this.funcArgs.add(funcArgs);
		
		this.configParams.add(configParams);
	}
	
//	protected void add(FunctionArgument... param) {
//		this.funcArgs.add(param);
//	}
//
//	protected void add(ConfigParameter... param) {
//		this.configParams.add(param);
//	}


	@Override
	public List<FunctionArgument> getFunctionArguments() {
		return this.funcArgs.getParams();
	}

	@Override
	public List<ConfigParameter> getConfigParameters() {
		return this.configParams.getParams();
	}

	protected String getStringArg(Map<String, Object> args, String name) {
		Object value = Objects.requireNonNull(args).get(name);
		if (value == null)
			throw new LookupProviderException(this, "argument not passed to function: " + name);
		return value.toString();
	}

	protected Optional<String> getOptionalStringArg(Map<String, Object> args, String name) {
		Object value = Objects.requireNonNull(args).get(name);
		if (value == null)
			return Optional.empty();
		return Optional.of(value.toString());
	}
}
