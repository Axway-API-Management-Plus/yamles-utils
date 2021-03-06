package com.axway.yamles.utils.spi;

import java.util.Optional;

import com.mitchellbosecke.pebble.extension.Function;

public interface LookupProvider extends Function {

	public String getName();
	
	public boolean isEnabled();

	public Optional<String> lookup(String key);
}
