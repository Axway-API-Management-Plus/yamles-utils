package com.axway.yamles.utils;

import java.io.File;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.EvalCommand;
import com.axway.yamles.utils.merge.MergeCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(name = "yamlesutils", description = "YAML Entity Store Utilities", subcommands = { HelpCommand.class,
		MergeCommand.class, LintCommand.class,
		ConfigCommand.class, EvalCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class YamlEsUtils implements IExecutionExceptionHandler {

	private static final Logger log = LogManager.getLogger(YamlEsUtils.class);

	@Option(names = { "-v", "--verbose" }, description = "Increase logging verbosity.")
	boolean[] verbosity;

	@Option(names = { "-q", "--quiet" }, description = "Disable info log message to the console.")
	boolean quiet = false;

	@Option(names = { "-a", "--audit" }, description = "Audit file.", paramLabel = "FILE")
	File audit;

	private int executionStrategy(ParseResult parseResult) {
		final String loggerName = getClass().getPackage().getName();

		Optional<Level> level = determineLogLevel();
		if (level.isPresent()) {
			Configurator.setLevel(loggerName, level.get());
		}
		Optional<Level> rootLevel = determineRootLogLevel();
		if (level.isPresent()) {
			Configurator.setRootLevel(rootLevel.get());
		}

		Audit.init(this.audit, this.quiet);
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
	
	private Optional<Level> determineRootLogLevel() {
		Optional<Level> level = Optional.empty();
		if (this.verbosity != null) {
			if (this.verbosity.length > 2)
				level = Optional.of(Level.DEBUG);
			else if (this.verbosity.length == 2)
				level = Optional.of(Level.INFO);
			else if (this.verbosity.length == 1)
				level = Optional.of(Level.ERROR);
		}
		return level;
	}

	public static void main(String[] args) {
		YamlEsUtils app = new YamlEsUtils();
		CommandLine cl = new CommandLine(app);

		int exitCode = cl //
				.setExecutionStrategy(app::executionStrategy) //
				.setExecutionExceptionHandler(app) //
				.setCaseInsensitiveEnumValuesAllowed(false) //
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
