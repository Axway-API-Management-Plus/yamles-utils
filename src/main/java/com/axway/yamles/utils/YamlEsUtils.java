package com.axway.yamles.utils;

import java.io.File;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

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

	@Option(names = { "-v", "--verbose" }, description = "Increase logging verbosity.")
	boolean[] verbosity;

	@Option(names = { "-q", "--quiet" }, description = "Disable log message to the console.")
	boolean quiet = false;

	@Option(names = { "-a", "--audit" }, description = "Audit file.", paramLabel = "FILE")
	File audit;

	private int executionStrategy(ParseResult parseResult) {
		final String loggerName = getClass().getPackage().getName();

		if (this.quiet) {
			LoggerContext context = LoggerContext.getContext(false);
			Configuration configuration = context.getConfiguration();
			LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		    loggerConfig.removeAppender("Console");
			context.updateLoggers();
		} else {
			Optional<Level> level = determineLogLevel();
			if (level.isPresent()) {
				Configurator.setLevel(loggerName, level.get());
			}
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
