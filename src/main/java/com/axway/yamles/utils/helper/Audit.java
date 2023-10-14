package com.axway.yamles.utils.helper;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Audit {
	private static final String CONSOLE_APPENDER_NAME = "Console";

	public static final String HEADER_PREFIX = "### ";
	public static final String SUB_HEADER_PREFIX = "# ";

	public static final Logger AUDIT_LOG = LogManager.getLogger("com.axway.yamles.utils.audit");

	private static final Logger log = LogManager.getLogger(Audit.class);

	public static void errorsOnly() {
		init(null, true);
	}

	public static void init(File auditFile, boolean quiet) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();

		LoggerConfig auditLoggerConfig = config.getLoggerConfig(AUDIT_LOG.getName());
		auditLoggerConfig.removeAppender(CONSOLE_APPENDER_NAME);

		// configure logging to console
		Appender consoleAppender = config.getAppender(CONSOLE_APPENDER_NAME);
		if (consoleAppender == null) {
			throw new IllegalStateException("appender not found: " + CONSOLE_APPENDER_NAME);
		}

		Level consoleLevel = quiet ? Level.ERROR : Level.INFO;
		auditLoggerConfig.addAppender(consoleAppender, consoleLevel, null);

		// configure logging to file
		if (auditFile != null) {
			PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{yyyy-MM-dd HH:mm:ss} %-5level] %msg%n")
					.build();
			Appender appender = FileAppender.newBuilder() //
					.setName("audit") //
					.setLayout(layout) //
					.withFileName(auditFile.getAbsolutePath()) //
					.withAppend(true) //
					.build();

			auditLoggerConfig.addAppender(appender, Level.INFO, null);
			appender.start();

			log.info("Audit file configured: {}", auditFile.getAbsolutePath());

			AUDIT_LOG.info(HEADER_PREFIX + "Audit started");
		}
		ctx.updateLoggers();
	}
}
