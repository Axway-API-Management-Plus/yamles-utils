package com.axway.yamles.utils.plugins;

import java.util.List;

/**
 * Provides a Pebble function to lookup values from data sources.
 * 
 * @author mlook
 */
public interface LookupProvider {

	/**
	 * Returns the name of the lookup provider.
	 * 
	 * @return name of lookup provider
	 */
	public String getName();
	
	public String getSummary();
	
	public String getDescription();
	
	public default boolean isBuiltIn() { return false; } 

	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException;

	public List<FunctionArgument> getFunctionArguments();

	public List<ConfigParameter> getConfigParameters();
}
