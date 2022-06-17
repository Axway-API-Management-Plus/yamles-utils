package com.axway.yamles.utils.spi.impl;

import java.util.Optional;

import picocli.CommandLine.Command;

/**
 * Lookup provider for environment variables.
 * 
 *  
 * @author mlook
 */
@Command
public class EnvLookupProvider extends AbstractLookupProvider {

	@Override
	public String getName() {
		return "env";
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Optional<String> lookup(String key) {
		if (key == null || key.isEmpty()) {
			return Optional.empty();
		}

		String value = System.getenv(key);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(value);
	}
}
