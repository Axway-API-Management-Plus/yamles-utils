package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import picocli.CommandLine.Option;

public abstract class AbstractLookupEnabledCommand implements Callable<Integer> {

	@Option(names = {
			"--lookup-functions" }, description = "Configure lookup functions.", paramLabel = "FILE", required = false)
	private List<File> lookupFunctionsConfigs;

	private static volatile boolean initialized = false;

	protected AbstractLookupEnabledCommand() {
	}

	protected AbstractLookupEnabledCommand(List<File> lookupConfigs) {
		this.lookupFunctionsConfigs = Objects.requireNonNull(lookupConfigs, "lookup functions configurations required");
	}

	protected List<File> getLookupFunctionsConfigs() {
		if (this.lookupFunctionsConfigs == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(this.lookupFunctionsConfigs);
	}

	@Override
	public Integer call() throws Exception {
		if (!initialized) {
			LookupManager.getInstance().configureFunctions(getLookupFunctionsConfigs());
			initialized = true;
		}
		return 0;
	}
}
