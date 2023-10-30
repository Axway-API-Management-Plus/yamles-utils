package com.axway.yamles.utils.merge;

import com.axway.yamles.utils.merge.certs.MergeCertificatesCommand;
import com.axway.yamles.utils.merge.config.MergeConfigCommand;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(name = "merge", description = "Merge configuration from various sources.", subcommands = { HelpCommand.class,
		MergeConfigCommand.class, MergeCertificatesCommand.class,
		DescribeCommand.class }, mixinStandardHelpOptions = true)
public class MergeCommand {

	@Option(names = { "-m",
			"--mode" }, description = "Execution mode; possible values are: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})", defaultValue = "CONFIG", paramLabel = "MODE", required = false)
	ExecutionMode mode;

	public ExecutionMode getMode() {
		return this.mode;
	}
}
