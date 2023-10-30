package com.axway.yamles.utils.merge;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;

@Command(name = "functions", description = "Describe available functions.", mixinStandardHelpOptions = true)
public class DescribeFunctionsCommand extends AbstractLookupEnabledCommand {

	@Override
	public Integer call() throws Exception {
		Audit.errorsOnly();
		initializeProviderManager(ExecutionMode.SYNTAX_CHECK);
		new Describer(System.out, "Available Lookup Functions").lookupFunctions();
		return 0;
	}

}
