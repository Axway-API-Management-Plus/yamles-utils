package com.axway.yamles.utils.plugins;

import org.pf4j.ExtensionPoint;

public interface Provider extends ExtensionPoint {
	
	public void onInit(ExecutionMode mode);
	
	public ExecutionMode getMode();

	/**
	 * Returns the name of the lookup provider.
	 * 
	 * @return name of lookup provider
	 */
	public String getName();
	
	public String getSummary();
	
	public String getDescription();
}
