package com.axway.yamles.utils.merge;

import com.axway.yamles.utils.merge.certs.MergeCertificatesCommand;
import com.axway.yamles.utils.merge.config.MergeConfigCommand;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "merge", description = "Merge configuration from various sources.", subcommands = { HelpCommand.class,
		MergeConfigCommand.class, MergeCertificatesCommand.class, DescribeCommand.class }, mixinStandardHelpOptions = true)
public class MergeCommand {
}
