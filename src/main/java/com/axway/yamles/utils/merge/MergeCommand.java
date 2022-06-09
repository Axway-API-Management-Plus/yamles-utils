package com.axway.yamles.utils.merge;

import com.axway.yamles.utils.merge.certs.MergeCertificatesCommand;
import com.axway.yamles.utils.merge.config.MergeConfigCommand;

import picocli.CommandLine.Command;

@Command(name = "merge", description = "Merge configuration from various sources.", subcommands={MergeConfigCommand.class, MergeCertificatesCommand.class})
public class MergeCommand {

}
