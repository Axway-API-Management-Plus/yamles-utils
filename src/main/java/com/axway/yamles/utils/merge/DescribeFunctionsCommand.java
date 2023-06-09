package com.axway.yamles.utils.merge;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;

@Command(name = "functions", description = "Describe available functions.", mixinStandardHelpOptions = true)
public class DescribeFunctionsCommand extends AbstractLookupEnabledCommand {

	@Override
	public Integer call() throws Exception {
		initLookupProviders();

		PrintStream out = System.out;

		int width = 80;

		Help.ColorScheme scheme = Help.defaultColorScheme(Ansi.AUTO);

		out.println(scheme.text("@|bold,underline Available Lookup Functions|@"));
		out.println();

		final Column colOne = new Column(20, 0, Overflow.SPAN);
		final Column colTwo = new Column(width - colOne.width, 0, Overflow.WRAP);
		
		Collection<LookupFunction> functions = LookupManager.getInstance().getLookupFunctions();
		functions.forEach( f -> {
			TextTable table = TextTable.forColumns(scheme, colOne, colTwo);			
			table.addRowValues("@|bold " + f.getName() + " [" + f.getProvider().getName() + "]|@", f.getProvider().getSummary());
			
			List<FunctionArgument> fas = f.getProvider().getFunctionArguments();
			if (!fas.isEmpty()) {
				table.addEmptyRow();
				table.addRowValues("", "@|underline Lookup Function Arguments:|@");

				fas.forEach(fa -> {
					table.addRowValues("", "@|bold " + fa.getName() + ":|@ " + fa.getDescription());
				});
			}
			out.println(table);
		});
		
		return 0;
	}

}
