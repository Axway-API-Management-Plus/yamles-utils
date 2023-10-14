package com.axway.yamles.utils.merge;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "describe", description = "Describe features of the YAML-ES Utilities used for merging.", mixinStandardHelpOptions = true, subcommands = {
		DescribeLookupProvidersCommand.class, DescribeCertificateProvidersCommand.class, DescribeFunctionsCommand.class,
		HelpCommand.class })
public class DescribeCommand {
}
