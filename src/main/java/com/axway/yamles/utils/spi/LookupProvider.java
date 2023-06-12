package com.axway.yamles.utils.spi;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	public boolean isEnabled();
	
	public default boolean isBuiltIn() { return false; } 

	/**
	 * Adds a new source to the lookup provider.
	 * 
	 * @param baseDir base directory used for relative file location
	 * @param source  lookup source
	 * @throws LookupProviderException if error occurred on adding the lookup source
	 */
	public void addSource(LookupSource source) throws LookupProviderException;

	public Optional<String> lookup(String alias, Map<String, Object> args);

	public List<FunctionArgument> getFunctionArguments();

	public List<ConfigParameter> getConfigParameters();
}
