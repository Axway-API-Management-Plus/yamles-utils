package com.axway.yamles.utils.merge;

import picocli.CommandLine.Command;

@Command(name = "functions", description = "Describe available functions.", mixinStandardHelpOptions = true)
public class DescribeFunctionsCommand extends AbstractLookupEnabledCommand {

	@Override
	public Integer call() throws Exception {
		super.call();

		new Describer(System.out, "Available Lookup Functions")
				.lookupFunctions(LookupManager.getInstance().getLookupFunctions());

		return 0;
	}

}
