package com.axway.yamles.utils;

import java.io.File;
import java.util.List;

import com.axway.yamles.utils.lint.LintCommand;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;
import com.axway.yamles.utils.merge.certs.MergeCertificatesCommand;
import com.axway.yamles.utils.merge.config.MergeConfigCommand;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "config", description = "Configure YAML-ES. Combines the 'lint', 'merge config' and 'merge certs' commands.", mixinStandardHelpOptions = true)
public class ConfigCommand extends AbstractLookupEnabledCommand {

	@Option(names = {
			"--project" }, description = "Project directory (YAML entity store).", paramLabel = "DIR", required = true)
	private File projectDir = null;

	@Option(names = { "--rules" }, description = "Rules file.", paramLabel = "FILE", required = false)
	private List<File> rulesFiles;

	@Option(names = {
			"--certs" }, description = "Certificate configuration file.", paramLabel = "FILE", required = false)
	private List<File> certConfigs;

	@Option(names = { "--config" }, description = "Configuration fragment.", paramLabel = "FILE", required = false)
	private List<File> fragmentConfig;

	@Option(names = {
			"--ignore-missing-values" }, description = "Ignore missing configuration of values.", required = false)
	private boolean ignoreMissingValues = false;

	@Override
	public Integer call() throws Exception {
		Integer result = 0;

		if (this.rulesFiles != null && !this.rulesFiles.isEmpty()) {
			result = new LintCommand(projectDir, rulesFiles).call();
			if (result != 0)
				return result;
		}

		if (this.fragmentConfig != null && !this.fragmentConfig.isEmpty()) {
			result = new MergeConfigCommand(projectDir, getLookupFunctionsConfigs(), fragmentConfig,
					ignoreMissingValues).call();
			if (result != 0)
				return result;
		}

		if (this.certConfigs != null && !this.certConfigs.isEmpty()) {
			result = new MergeCertificatesCommand(projectDir, getLookupFunctionsConfigs(), certConfigs).call();
		}

		return result;
	}
}
