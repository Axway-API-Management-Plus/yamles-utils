package com.axway.yamles.utils.merge;

import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.CertificateManager;
import com.axway.yamles.utils.spi.CertificateProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Option;

@Command(name = "cert-providers", description = "Describe the available certificate providers.")
public class DescribeCertificateProvidersCommand implements Callable<Integer> {
	
	@Option(names = { "--full" }, description = "Display full description")
	private boolean full = false;
	

	@Override
	public Integer call() throws Exception {
		PrintStream out = System.out;

		int width = 80;

		Help.ColorScheme scheme = Help.defaultColorScheme(Ansi.AUTO);

		out.println(scheme.text("@|bold,underline Available Certificate Providers|@"));
		out.println();

		final Column colOne = new Column(10, 0, Overflow.SPAN);
		final Column colTwo = new Column(width - colOne.width, 0, Overflow.WRAP);

		Collection<CertificateProvider> providers = CertificateManager.getInstance().getProviders();

		providers.forEach((p) -> {
			TextTable table;

			table = TextTable.forColumns(scheme, colOne, colTwo);
			table.addRowValues("@|bold " + p.getName() + "|@", p.getSummary());

			if (this.full) {
				table.addRowValues("", "@|italic " + p.getDescription() + "|@");
//				List<ConfigParameter> cps = p.getConfigParameters();
//				if (!cps.isEmpty()) {
//					table.addEmptyRow();					
//					table.addRowValues("", "@|underline Configuration Parameters|@");
//					
//					cps.forEach((cp) -> {
//						table.addRowValues("", "@|bold " + cp.getName() + " ["+ cp.getType() + "]:|@ " + cp.getDescription());
//					});
//				}
			}
			out.println(table);
		});

		return 0;
	}

}
