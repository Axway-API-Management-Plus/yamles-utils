package com.axway.yamles.utils.merge;

import java.util.concurrent.Callable;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cert-providers", description = "Describe the available certificate providers.", mixinStandardHelpOptions = true)
public class DescribeCertificateProvidersCommand implements Callable<Integer> {

	@Option(names = { "--full" }, description = "Display full description.")
	private boolean full = false;

	@Override
	public Integer call() throws Exception {
		Audit.errorsOnly();		
		ProviderManager.initialize(ExecutionMode.SYNTAX_CHECK);
		new Describer(System.out, "Available Certificate Providers").cerificateProvider(this.full);
		return 0;
	}

}
