package com.axway.yamles.utils.spi.impl;

import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupFunctionException;
import com.axway.yamles.utils.spi.LookupProviderException;

/**
 * Lookup provider for environment variables.
 * 
 * 
 * @author mlook
 */
public class EnvLookupProvider extends AbstractBuiltinLookupProvider {
	protected static FunctionArgument ARG_KEY = new FunctionArgument("key", true, "Name of environment variable");
	
	protected static class LF extends LookupFunction {
		public LF(String alias, AbstractBuiltinLookupProvider provider) {
			super(alias, provider, AbstractBuiltinLookupProvider.SOURCE);
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			String key = getArg(ARG_KEY, args, "");
			if (key.isEmpty()) {
				return Optional.empty();
			}

			String value = EnvironmentVariables.get(key);
			if (value == null) {
				return Optional.empty();
			}
			return Optional.of(value);
		}
	}

	public EnvLookupProvider() {
		super();
		add(ARG_KEY);
	}

	@Override
	public String getName() {
		return "env";
	}

	@Override
	public String getSummary() {
		return "Lookup values from environment variables.";
	}

	@Override
	public String getDescription() {
		return "The key represents the name of the environment variable.";
	}

	@Override
	protected LookupFunction buildFunction() throws LookupProviderException {
		return new LF(getName(), this);
	}
}
