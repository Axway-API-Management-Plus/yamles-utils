package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.util.List;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "certs", description = "Merge certificates for YAML entiry store.", mixinStandardHelpOptions = true)
public class MergeCertificatesCommand extends AbstractLookupEnabledCommand {
	@Option(names = {
			"--project" }, description = "Project directory (YAML entity store).", paramLabel = "DIR", required = true)
	private File projectDir = null;

	@Option(names = { "-c", "--certs",
			"--config" }, description = "Certificate configuration file.", paramLabel = "FILE", required = true)
	private List<File> configs;

	@Option(names = {
			"--expiration-warning" }, description = "Audit warning in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationWarningDays = CertificatesConfigurator.DEFAULT_EXP_WARNING_DAYS;

	@Option(names = {
			"--expiration-error" }, description = "Audit error in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationErrorDays = CertificatesConfigurator.DEFAULT_EXP_ERROR_DAYS;

	@Option(names = {
			"--expiration-fail" }, description = "Fail in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationFailDays = CertificatesConfigurator.DEFAULT_EXP_FAIL_DAYS;

	@ParentCommand
	private MergeCommand parentCommand;

	MergeCertificatesCommand() {
		super();
	}

	@Override
	public Integer call() throws Exception {
		initializeProviderManager(this.parentCommand.getMode());

		YamlEs es = new YamlEs(projectDir);

		ExecutionMode mode = ProviderManager.getInstance().getConfigMode();

		CertificatesConfigurator cc = new CertificatesConfigurator(mode, this.expirationWarningDays,
				this.expirationErrorDays, this.expirationFailDays);

		cc.setCertificateConfigs(this.configs);
		cc.apply(es);

		return 0;
	}
}
