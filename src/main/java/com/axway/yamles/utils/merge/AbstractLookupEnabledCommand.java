package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.LookupManager;

import picocli.CommandLine.Option;

public abstract class AbstractLookupEnabledCommand implements Callable<Integer> {

	@Option(names = {
			"--lookup-functions" }, description = "Configure lookup functions.", paramLabel = "FILE", required = false)
	private List<File> lookupFunctionsConfigs;
	
	protected List<File> getLookupFunctionsConfigs() {
		if (this.lookupFunctionsConfigs == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(this.lookupFunctionsConfigs);
	}
	
	protected void initLookupProviders() {
		LookupManager.getInstance().configureFunctions(getLookupFunctionsConfigs());
	}
}
