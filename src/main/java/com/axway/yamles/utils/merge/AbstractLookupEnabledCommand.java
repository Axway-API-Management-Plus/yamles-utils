package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.LookupManager;

import picocli.CommandLine.Option;

public abstract class AbstractLookupEnabledCommand implements Callable<Integer> {

	@Option(names = {
			"--lookup-providers" }, description = "Configure lookup providers from a YAML file.", paramLabel = "FILE", required = false)
	private List<File> lookupProviderConfigs;
	
	protected List<File> getLookupProviderConfigs() {
		if (this.lookupProviderConfigs == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(this.lookupProviderConfigs);
	}
	
	protected void initLookupProviders() {
		LookupManager.getInstance().configureProviders(getLookupProviderConfigs());
	}
}
