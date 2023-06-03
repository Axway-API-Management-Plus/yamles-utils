package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.helper.YamlEs;
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

	private final AliasSet aliases = new AliasSet();

	@Override
	public Integer call() throws Exception {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Command: Configure Certificates");
		initLookupProviders();
		
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
