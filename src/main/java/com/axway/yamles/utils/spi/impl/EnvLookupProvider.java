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
		super("name of environment variable");
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
	public void addSource(LookupSource source) throws LookupProviderException {
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
		String key = getStringArg(args, ARG_KEY.getName());
		if (key == null || key.isEmpty()) {
			return Optional.empty();
		}

		String value = EnvironmentVariables.get(key);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(value);
	}
}
