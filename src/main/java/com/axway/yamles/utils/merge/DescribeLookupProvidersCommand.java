package com.axway.yamles.utils.merge;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Option;

@Command(name = "lookup-providers", description = "Describe the available lookup providers.", mixinStandardHelpOptions = true)
public class DescribeLookupProvidersCommand implements Callable<Integer> {

	@Option(names = { "--full" }, description = "Display full description")
	private boolean full = false;

	@Override
	public Integer call() throws Exception {
		PrintStream out = System.out;

		int width = 80;

		Help.ColorScheme scheme = Help.defaultColorScheme(Ansi.AUTO);

		out.println(scheme.text("@|bold,underline Available Lookup Providers|@"));
		out.println();

		final Column colOne = new Column(10, 0, Overflow.SPAN);
		final Column colTwo = new Column(width - colOne.width, 0, Overflow.WRAP);

		Collection<LookupProvider> providers = LookupManager.getInstance().getProviders();

		providers.forEach(p -> {
			TextTable table;

			table = TextTable.forColumns(scheme, colOne, colTwo);
			table.addRowValues("@|bold " + p.getName() + "|@", p.getSummary());

			if (this.full) {
				table.addRowValues("", "@|italic " + p.getDescription() + "|@");
				List<ConfigParameter> cps = p.getConfigParameters();
				if (!cps.isEmpty()) {
					table.addEmptyRow();
					table.addRowValues("", "@|underline Configuration Parameters|@");

					cps.forEach(cp -> {
						table.addRowValues("",
								(cp.isRequired() ? "*" : "") + "@|bold " + cp.getName() + ":|@ " + cp.getDescription() + " [" + cp.getType() + (cp.hasMustacheSupport() ? "; mustache supported" : "") + "]");
					});
				}

				List<FunctionArgument> fas = p.getFunctionArguments();
				if (!fas.isEmpty()) {
					table.addEmptyRow();
					table.addRowValues("", "@|underline Lookup Function Arguments|@");

					fas.forEach(fa -> {
						table.addRowValues("", "@|bold " + fa.getName() + ":|@ " + fa.getDescription());
					});
				}
			}
			out.println(table);
		});

		return 0;
	}

}
