package com.axway.yamles.utils;

import java.util.Iterator;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(name = "yamlestools", description = "YAML Enity Store Tools", subcommands = { MergeCommand.class,
		LintCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class YamlEsUtils implements IExecutionExceptionHandler {

	private static final Logger log = LogManager.getLogger(YamlEsUtils.class);

	@Option(names = { "-v", "--verbose" }, description = "increase logging verbosity")
	boolean[] verbosity;

	private int executionStrategy(ParseResult parseResult) {
		Optional<Level> level = determineLogLevel();
		if (level.isPresent()) {
			Configurator.setLevel("com.axway.yamles.utils", level.get());
		}
		return new CommandLine.RunLast().execute(parseResult);
	}

	private Optional<Level> determineLogLevel() {
		Optional<Level> level = Optional.empty();
		if (this.verbosity != null) {
			if (this.verbosity.length > 1)
				level = Optional.of(Level.TRACE);
			else if (this.verbosity.length == 1)
				level = Optional.of(Level.DEBUG);
		}
		return level;
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
				.setExecutionExceptionHandler(app) //
				.execute(args);
		System.exit(exitCode);
	}

	@Override
	public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult)
			throws Exception {
		Optional<Level> level = determineLogLevel();
		if (level.isPresent() && level.get().isLessSpecificThan(Level.INFO)) {
			log.error("execution failed", ex);
		} else {
			log.error("'{}' command failed: {}", commandLine.getCommandName(), ex.getMessage());
			Throwable cause = ex;
			Throwable next = null;
			while ((next = cause.getCause()) != null) {
				cause = next;
				log.error("caused by: {}", cause.getMessage());
			}

		}
		return 1;
	}
}
