package com.axway.yamles.utils.merge;

import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.LookupManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "lookup-providers", description = "Describe the available lookup providers.", mixinStandardHelpOptions = true)
public class DescribeLookupProvidersCommand implements Callable<Integer> {

	@Option(names = { "--full" }, description = "Display full description.")
	private boolean full = false;

	@Override
	public Integer call() throws Exception {
		new Describer(System.out, "Available Lookup Providers")
				.lookupProviders(LookupManager.getInstance().getProviders(), this.full);
		;
		return 0;
	}

}
