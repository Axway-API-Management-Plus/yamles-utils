package com.axway.yamles.utils;

import java.io.File;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.MergeCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(name = "yamlesutils", description = "YAML Enity Store Utilities", subcommands = { HelpCommand.class,
		MergeCommand.class,
		LintCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class YamlEsUtils implements IExecutionExceptionHandler {

	private static final Logger log = LogManager.getLogger(YamlEsUtils.class);

	@Option(names = { "-v", "--verbose" }, description = "increase logging verbosity")
	boolean[] verbosity;

	@Option(names = { "-a", "--audit" }, description = "audit file", paramLabel = "FILE")
	File audit;

	private int executionStrategy(ParseResult parseResult) {
		Optional<Level> level = determineLogLevel();
		if (level.isPresent()) {
			Configurator.setLevel("com.axway.yamles.utils", level.get());
		}
		Audit.init(this.audit);
		return new CommandLine.RunLast().execute(parseResult);
	}

	private Optional<Level> determineLogLevel() {
		Optional<Level> level = Optional.empty();
		if (this.verbosity != null) {
			if (this.verbosity.length > 2)
				level = Optional.of(Level.TRACE);
			else if (this.verbosity.length == 2)
				level = Optional.of(Level.DEBUG);
			else if (this.verbosity.length == 1)
				level = Optional.of(Level.INFO);
		}
		return level;
	}

	public static void main(String[] args) {
		YamlEsUtils app = new YamlEsUtils();
		CommandLine cl = new CommandLine(app);

		int exitCode = cl //
				.setExecutionStrategy(app::executionStrategy) //
				.setExecutionExceptionHandler(app) //
				.execute(args);
		log.info("finished with exit code: {}", exitCode);
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
			Throwable cause = ex.getCause();
			while (cause != null) {
				log.error("caused by: {}", cause.getMessage());
				cause = cause.getCause();
			}
		}
		return 1;
	}
}
