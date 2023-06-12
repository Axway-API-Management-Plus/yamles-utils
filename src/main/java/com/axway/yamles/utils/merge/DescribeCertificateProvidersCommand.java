package com.axway.yamles.utils.merge;

import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.CertificateManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cert-providers", description = "Describe the available certificate providers.", mixinStandardHelpOptions = true)
public class DescribeCertificateProvidersCommand implements Callable<Integer> {

	@Option(names = { "--full" }, description = "Display full description.")
	private boolean full = false;

	@Override
	public Integer call() throws Exception {
		new Describer(System.out, "Available Certificate Providers")
				.cerificateProvider(CertificateManager.getInstance().getProviders(), this.full);

		return 0;
	}

}
