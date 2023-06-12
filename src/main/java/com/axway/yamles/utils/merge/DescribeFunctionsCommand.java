package com.axway.yamles.utils.merge;

import com.axway.yamles.utils.spi.LookupManager;

import picocli.CommandLine.Command;

@Command(name = "functions", description = "Describe available functions.", mixinStandardHelpOptions = true)
public class DescribeFunctionsCommand extends AbstractLookupEnabledCommand {

	@Override
	public Integer call() throws Exception {
		initLookupProviders();

		new Describer(System.out, "Available Lookup Functions")
				.lookupFunctions(LookupManager.getInstance().getLookupFunctions());

		return 0;
	}

}
