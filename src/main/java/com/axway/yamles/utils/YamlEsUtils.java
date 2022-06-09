package com.axway.yamles.utils;

import java.util.Iterator;

import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "yamlestools", description = "YAML Enity Store Tools", subcommands = {MergeCommand.class, LintCommand.class})
public class YamlEsUtils {

	public static void main(String[] args) {
		CommandLine cl = new CommandLine(new YamlEsUtils());
		
		CommandLine mergeCL = cl.getSubcommands().get("merge");
		CommandLine configCL = mergeCL.getSubcommands().get("config");
		CommandLine certCL = mergeCL.getSubcommands().get("certs");
		
		Iterator<LookupProvider> sps = LookupManager.getInstance().getProviders();
		while(sps.hasNext()) {
			LookupProvider sp = sps.next();
			certCL.addMixin(sp.getName(), sp);
			configCL.addMixin(sp.getName(), sp);
		}

		int exitCode = cl.execute(args);
		System.exit(exitCode);
	}
}
