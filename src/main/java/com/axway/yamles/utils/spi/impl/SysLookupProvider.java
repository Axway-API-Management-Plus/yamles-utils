package com.axway.yamles.utils.spi.impl;

import java.util.Optional;

import picocli.CommandLine.Command;

/**
 * Lookup provider for system properties.
 * 
 * @author mlook
 */
@Command
public class SysLookupProvider extends AbstractLookupProvider {

	@Override
	public String getName() {
		return "sys";
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

		String value = System.getProperty(key);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(value);
	}

	@Override
	public void onRegistered() {
	}
}
