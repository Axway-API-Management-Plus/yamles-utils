package com.axway.yamles.utils;

import java.util.Iterator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(name = "yamlestools", description = "YAML Enity Store Tools", subcommands = { MergeCommand.class,
		LintCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class YamlEsUtils {

	@Option(names = { "-v", "--verbose" }, description = "Enable verbose logging")
	boolean verbose = false;

	private int executionStrategy(ParseResult parseResult) {
		if (this.verbose) {
			Configurator.setLevel("com.axway.yamles.utils", Level.DEBUG);
		}
		return new CommandLine.RunLast().execute(parseResult);
	}

	public static void main(String[] args) {
		YamlEsUtils app = new YamlEsUtils();
		CommandLine cl = new CommandLine(app);

		CommandLine mergeCL = cl.getSubcommands().get("merge");
		CommandLine configCL = mergeCL.getSubcommands().get("config");
		CommandLine certCL = mergeCL.getSubcommands().get("certs");

		Iterator<LookupProvider> sps = LookupManager.getInstance().getProviders();
		while (sps.hasNext()) {
			LookupProvider sp = sps.next();
			certCL.addMixin(sp.getName(), sp);
			configCL.addMixin(sp.getName(), sp);
		}

		int exitCode = cl //
				.setExecutionStrategy(app::executionStrategy) //
				.execute(args);
		System.exit(exitCode);
	}
}
