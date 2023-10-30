package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Option;

public abstract class AbstractLookupEnabledCommand implements Callable<Integer> {

	@Option(names = { "-l",
			"--lookup-functions" }, description = "Configure lookup functions.", paramLabel = "FILE", required = false)
	private List<File> lookupFunctionsConfigs;

	protected AbstractLookupEnabledCommand() {
	}

	protected void initializeProviderManager(ExecutionMode mode) {
		ProviderManager.initialize(mode);
		ProviderManager.getInstance().configureFunctions(getLookupFunctionsConfigs());
	}

	private List<File> getLookupFunctionsConfigs() {
		if (this.lookupFunctionsConfigs == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(this.lookupFunctionsConfigs);
	}

}
