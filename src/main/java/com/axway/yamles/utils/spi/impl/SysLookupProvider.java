package com.axway.yamles.utils.spi.impl;

import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

/**
 * Lookup provider for system properties.
 * 
 * @author mlook
 */
public class SysLookupProvider extends AbstractLookupProvider {
	
	public SysLookupProvider() {
		super("name of system property");
	}

	@Override
	public String getName() {
		return "sys";
	}
	
	@Override
	public String getSummary() {
		return "Lookup values from system properties.";
	}
	
	@Override
	public String getDescription() {
		return "The key represents the name of the system property.";
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

		String value = System.getProperty(key);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(value);
	}
}
