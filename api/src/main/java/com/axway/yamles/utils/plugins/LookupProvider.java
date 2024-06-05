package com.axway.yamles.utils.plugins;

import java.util.List;
import java.util.Optional;

/**
 * Provides a Pebble function to lookup values from data sources.
 * 
 * @author mlook
 */
public interface LookupProvider extends Provider {
	
	public default boolean isBuiltIn() { return false; } 

	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException;

	public List<FunctionArgument> getFunctionArguments();
	
	public Optional<FunctionArgument> getFunctionArgumentByName(String name);

	public List<ConfigParameter> getConfigParameters();
}
