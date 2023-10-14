package com.axway.yamles.utils.merge;

import java.util.concurrent.Callable;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "lookup-providers", description = "Describe the available lookup providers.", mixinStandardHelpOptions = true)
public class DescribeLookupProvidersCommand implements Callable<Integer> {

	@Option(names = { "--full" }, description = "Display full description.")
	private boolean full = false;

	@Override
	public Integer call() throws Exception {
		Audit.errorsOnly();		
		ProviderManager.initialize(ExecutionMode.SYNTAX_CHECK);		
		new Describer(System.out, "Available Lookup Providers").lookupProviders(this.full);
		return 0;
	}

}
