package com.axway.yamles.utils;

import java.util.Iterator;

import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.spi.SecretsManager;
import com.axway.yamles.utils.spi.SecretsProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "yamlestools", description = "YAML Enity Store Tools", subcommands = {MergeCommand.class, LintCommand.class})
public class YamlEsUtils {

	public static void main(String[] args) {
		CommandLine cl = new CommandLine(new YamlEsUtils());
		
		CommandLine mergeCL = cl.getSubcommands().get("merge");
		CommandLine configCL = mergeCL.getSubcommands().get("config");
		CommandLine certCL = mergeCL.getSubcommands().get("certs");
		
		Iterator<SecretsProvider> sps = SecretsManager.getInstance().getProviders();
		while(sps.hasNext()) {
			SecretsProvider sp = sps.next();
			certCL.addMixin(sp.getName(), sp);
			configCL.addMixin(sp.getName(), sp);
		}

		int exitCode = cl.execute(args);
		System.exit(exitCode);
	}
}
