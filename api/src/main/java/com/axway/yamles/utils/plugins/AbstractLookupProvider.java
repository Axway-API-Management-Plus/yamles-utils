package com.axway.yamles.utils.plugins;

import java.util.List;
import java.util.Optional;

public abstract class AbstractLookupProvider extends AbstractProvider implements LookupProvider {
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
	public Optional<FunctionArgument> getFunctionArgumentByName(String name) {
		FunctionArgument fa = this.funcArgs.get(name);
		return Optional.ofNullable(fa);
	}

	@Override
	public List<ConfigParameter> getConfigParameters() {
		return this.configParams.getParams();
	}

	protected Optional<LookupFunction> checkOnlyLookupFunction(LookupSource source) {
		if (getMode() == ExecutionMode.SYNTAX_CHECK) {
			return Optional.of(new EmptyValueLookupFunction(source.getAlias(), this, source.getConfigSource()));
		}
		return Optional.empty();
	}
}
