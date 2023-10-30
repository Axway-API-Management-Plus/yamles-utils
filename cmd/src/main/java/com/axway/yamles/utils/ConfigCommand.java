package com.axway.yamles.utils;

import java.io.File;
import java.util.List;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.lint.Linter;
import com.axway.yamles.utils.lint.rules.Results;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;
import com.axway.yamles.utils.merge.certs.CertificatesConfigurator;
import com.axway.yamles.utils.merge.config.FieldConfigurator;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "config", description = "Configure YAML-ES. Combines the 'lint', 'merge config' and 'merge certs' commands.", mixinStandardHelpOptions = true)
public class ConfigCommand extends AbstractLookupEnabledCommand {

	@Option(names = { "-m",
			"--mode" }, description = "Possible values are: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})", defaultValue = "CONFIG", paramLabel = "MODE", required = false)
	private ExecutionMode mode;

	@Option(names = {
			"--project" }, description = "Project directory (YAML entity store).", paramLabel = "DIR", required = true)
	private File projectDir = null;

	@Option(names = { "-r", "--rules" }, description = "Rules file.", paramLabel = "FILE", required = false)
	private List<File> rulesFiles;

	@Option(names = {
			"--certs" }, description = "Certificate configuration file.", paramLabel = "FILE", required = false)
	private List<File> certConfigs;

	@Option(names = {
			"--expiration-warning" }, description = "Audit warning in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationWarningDays = CertificatesConfigurator.DEFAULT_EXP_WARNING_DAYS;

	@Option(names = {
			"--expiration-error" }, description = "Audit error in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationErrorDays = CertificatesConfigurator.DEFAULT_EXP_ERROR_DAYS;

	@Option(names = {
			"--expiration-fail" }, description = "Fail in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationFailDays = CertificatesConfigurator.DEFAULT_EXP_FAIL_DAYS;

	@Option(names = { "-f", "--fragment", "--config" }, description = "Configuration fragment for values.yaml file.", paramLabel = "FILE", required = false)
	private List<File> fragmentConfigs;

	@Option(names = {
			"--ignore-missing-values" }, description = "Ignore missing configuration of values.", required = false)
	private boolean ignoreMissingValues = false;

	@Override
	public Integer call() throws Exception {
		initializeProviderManager(this.mode);		
		Integer result = 0;

		YamlEs es = new YamlEs(this.projectDir);

		if (this.rulesFiles != null && !this.rulesFiles.isEmpty()) {
			Linter linter = new Linter(es, rulesFiles);
			Results results = linter.apply();
			results.auditFindings();

			if (results.hasFindings())
				return 1;
		}

		if (requiresFragmentConfig()) {
			FieldConfigurator vc = new FieldConfigurator(this.mode);
			vc.setConfigFragmentFiles(this.fragmentConfigs);
			vc.apply(es, this.ignoreMissingValues);
		}

		if (requiresCertificateConfig()) {
			CertificatesConfigurator cc = new CertificatesConfigurator(this.mode, this.expirationWarningDays,
					this.expirationErrorDays, this.expirationFailDays);
			cc.setCertificateConfigs(this.certConfigs);
			cc.apply(es);
		}

		return result;
	}

	private boolean requiresFragmentConfig() {
		return this.fragmentConfigs != null && !this.fragmentConfigs.isEmpty();
	}

	private boolean requiresCertificateConfig() {
		return this.certConfigs != null && !this.certConfigs.isEmpty();

	}
}
