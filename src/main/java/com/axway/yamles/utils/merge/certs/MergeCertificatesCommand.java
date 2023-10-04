package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "certs", description = "Merge certificates for YAML entiry store.", mixinStandardHelpOptions = true)
public class MergeCertificatesCommand extends AbstractLookupEnabledCommand {
	private static final Logger log = LogManager.getLogger(MergeCertificatesCommand.class);

	@Option(names = {
			"--project" }, description = "Project directory (YAML entity store).", paramLabel = "DIR", required = true)
	private File projectDir = null;

	@Option(names = { "-c", "--config" }, description = "Certificate configuration file.", paramLabel = "FILE", required = true)
	private List<File> configs;
	
	@Option(names = { "--expiration-warning"}, description = "Audit warning in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationWarningDays = 30;
	
	@Option(names = { "--expiration-error"}, description = "Audit error in case of certificate expires within the next days.", paramLabel = "DAYS", required = false)
	private int expirationErrorDays = 10;
	
	@Option(names = { "--expiration-fail" }, description = "Fail in case of certificate expires within the next days (-1 to to disable failure).", paramLabel = "DAYS", required = false)
	private int expirationFailDays = -1;
	

	private final AliasSet aliases = new AliasSet();
	
	MergeCertificatesCommand() {
		super();
	}
	
	public MergeCertificatesCommand(File projectDir, List<File> lookupConfigs, List<File> certConfigs, int expirationWarningDays, int expirationErrorDays, int expirationFailDays) {
		super(lookupConfigs);
		this.projectDir = Objects.requireNonNull(projectDir, "project directory required");
		this.configs = Objects.requireNonNull(certConfigs, "certificate configurations required");
		this.expirationWarningDays = expirationWarningDays;
		this.expirationErrorDays = expirationErrorDays;
		this.expirationFailDays = expirationFailDays;
	}

	@Override
	public Integer call() throws Exception {
		super.call();

		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Command: Configure Certificates");
		
		this.aliases.setExpirationWarning(this.expirationWarningDays);
		this.aliases.setExpirationError(this.expirationErrorDays);
		this.aliases.setExpirationFail(expirationFailDays);
		
		YamlEs es = new YamlEs(projectDir);

		loadAliases();

		writeAuditToLog();

		this.aliases.writeAliases(es);

		return 0;
	}

	private void loadAliases() {
		this.aliases.clear();

		this.configs.forEach(f -> {
			loadAliases(f);
		});
	}

	private void loadAliases(File file) {
		log.info("load certificate config: {}", file.getAbsoluteFile());
		CertificatesConfig cc = CertificatesConfig.loadConfig(file);
		this.aliases.addOrReplace(cc.getAliases().values());
	}

	private void writeAuditToLog() {
		Audit.AUDIT_LOG.info("## Certificate Aliases");
		Iterator<Alias> iter = this.aliases.getAliases();
		while (iter.hasNext()) {
			Alias alias = iter.next();
			Audit.AUDIT_LOG.info("{} (provider={}; source={})", alias.getName(), alias.getProvider(), alias.getConfigSource().getAbsolutePath());
		}
	}
}
