package com.axway.yamles.utils.merge;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.axway.yamles.utils.spi.CertificateProvider;
import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupProvider;

import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;

public class Describer {
	private static final String BUILT_IN = "+";
	private static final String OPTIONAL = "?";
	private static final String MUSTACHE = "{";

	private static final int WIDTH_TOTAL = 80;

	private static final Column COL_1 = new Column(10, 0, Overflow.SPAN);
	private static final Column COL_2 = new Column(WIDTH_TOTAL - COL_1.width, 0, Overflow.WRAP);

	private static final Column FUNC_COL_1 = new Column(20, 0, Overflow.SPAN);
	private static final Column FUNC_COL_2 = new Column(WIDTH_TOTAL - COL_1.width, 0, Overflow.WRAP);

	private static final Column LEGEND_COL_1 = new Column(5, 0, Overflow.SPAN);
	private static final Column LEGEND_COL_2 = new Column(WIDTH_TOTAL - LEGEND_COL_1.width, 0, Overflow.WRAP);

	public static final Help.ColorScheme scheme = Help.defaultColorScheme(Ansi.AUTO);

	private PrintStream out;
	private boolean usedBuiltIn = false;
	private boolean usedOptional = false;
	private boolean usedMustache = false;

	public Describer(PrintStream out, String title) {
		this.out = Objects.requireNonNull(out);

		out.println(scheme.text(formatTitle(title)));
		out.println();
	}

	public void lookupProviders(Collection<LookupProvider> providers, boolean full) {
		providers.forEach(p -> {
			TextTable table = TextTable.forColumns(scheme, COL_1, COL_2);
			table.addRowValues("@|bold " + p.getName() + "|@" + builtInTag(p.isBuiltIn()), p.getSummary());

			if (full) {
				table.addRowValues("", formatDescription(p.getDescription()));
				describeConfigParameter(table, p.getConfigParameters());
				describeFunctionArguments(table, p.getFunctionArguments());
			}
			out.println(table);
		});
		legend();
	}

	public void lookupFunctions(Collection<LookupFunction> functions) {
		functions.forEach(f -> {
			TextTable table = TextTable.forColumns(scheme, FUNC_COL_1, FUNC_COL_2);
			table.addRowValues("@|bold " + f.getName() + " [" + f.getProvider().getName()
					+ builtInTag(f.getProvider().isBuiltIn()) + "]|@", f.getProvider().getSummary());

			List<FunctionArgument> fas = f.getProvider().getFunctionArguments();
			describeFunctionArguments(table, fas);
			out.println(table);
		});

		legend();
	}

	public void cerificateProvider(Collection<CertificateProvider> providers, boolean full) {
		providers.forEach((p) -> {
			TextTable table;

			table = TextTable.forColumns(scheme, COL_1, COL_2);
			table.addRowValues("@|bold " + p.getName() + "|@", p.getSummary());

			if (full) {
				table.addRowValues("", formatDescription(p.getDescription()));
				List<ConfigParameter> cps = p.getConfigParameters();
				describeConfigParameter(table, cps);
			}
			out.println(table);
		});

		legend();
	}

	private String builtInTag(boolean yes) {
		this.usedBuiltIn |= yes;
		return yes ? BUILT_IN : "";
	}

	private String requiredTag(boolean yes) {
		this.usedOptional |= !yes;
		return !yes ? OPTIONAL : "";
	}

	private String mustacheTag(boolean yes) {
		this.usedMustache |= yes;
		return yes ? MUSTACHE : "";
	}

	private void describeConfigParameter(TextTable table, List<ConfigParameter> cps) {
		if (!cps.isEmpty()) {
			table.addEmptyRow();
			table.addRowValues("", formatSectionTitle("Configuration Parameters"));

			cps.forEach(cp -> {
				table.addRowValues("",
						"@|bold " + cp.getName() + "|@" + requiredTag(cp.isRequired())
								+ mustacheTag(cp.hasMustacheSupport()) + ": " + cp.getDescription() + " ["
								+ cp.getType() + "]");
			});
		}
	}

	private void describeFunctionArguments(TextTable table, List<FunctionArgument> fas) {
		if (!fas.isEmpty()) {
			table.addEmptyRow();
			table.addRowValues("", formatSectionTitle("Lookup Function Arguments"));

			fas.forEach(fa -> {
				table.addRowValues("", "@|bold " + fa.getName() + ":|@ " + fa.getDescription());
			});
		}
	}

	private void legend() {
		TextTable table = TextTable.forColumns(scheme, LEGEND_COL_1, LEGEND_COL_2);
		if (this.usedBuiltIn)
			table.addRowValues(BUILT_IN, "built-in lookup providers");
		if (this.usedOptional)
			table.addRowValues(OPTIONAL, "optional parameters");
		if (this.usedMustache)
			table.addRowValues(MUSTACHE, "parameter accepts Mustache templates");

		if (table.rowCount() > 0) {
			out.println(table);
		}
	}

	private String formatTitle(String title) {
		return "@|bold,underline " + title + "|@";
	}

	private String formatSectionTitle(String title) {
		return "@|underline " + title + ":|@";
	}

	private String formatDescription(String descr) {
		return "@|italic " + descr + "|@";
	}
}
