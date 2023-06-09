package com.axway.yamles.utils.spi.impl;

import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

/**
 * Lookup provider for environment variables.
 * 
 * 
 * @author mlook
 */
public class EnvLookupProvider extends AbstractLookupProvider {

	public EnvLookupProvider() {
		super("name of environment variable", EMPTY_FUNC_ARGS, EMPTY_CONFIG_PARAMS);
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
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}

	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
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
