package com.axway.yamles.utils.spi;

import java.util.Optional;

import io.pebbletemplates.pebble.extension.Function;

public interface LookupProvider extends Function {

	public String getName();
	
	public boolean isEnabled();
	
	public Optional<String> lookup(String key);
	
	public void onRegistered();	
}
