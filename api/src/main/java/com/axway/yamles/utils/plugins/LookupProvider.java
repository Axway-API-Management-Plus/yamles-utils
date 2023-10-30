package com.axway.yamles.utils.plugins;

import java.util.List;

/**
 * Provides a Pebble function to lookup values from data sources.
 * 
 * @author mlook
 */
public interface LookupProvider extends Provider {
	
	public default boolean isBuiltIn() { return false; } 

	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException;

	public List<FunctionArgument> getFunctionArguments();

	public List<ConfigParameter> getConfigParameters();
}
