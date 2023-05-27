package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "certs", description = "Merge certificates for YAML entiry store.", mixinStandardHelpOptions = true)
public class MergeCertificatesCommand extends AbstractLookupEnabledCommand {
	private static final Logger log = LogManager.getLogger(MergeCertificatesCommand.class);

	@Option(names = {
			"--project" }, description = "Project directory (YAML entity store)", paramLabel = "DIR", required = true)
	private File projectDir = null;

	@Option(names = { "-c", "--config" }, description = "Certificate config file", paramLabel = "FILE", required = true)
	private List<File> configs;

	@Option(names = { "--audit" }, description = "Audit certificate sources", paramLabel = "FILE", required = false)
	private File auditFile = null;

	private final AliasSet aliases = new AliasSet();

	@Override
	public Integer call() throws Exception {
		initLookupProviders();
		
		log.info("merge certificates");
		YamlEs es = new YamlEs(projectDir);

		loadAliases();

		this.aliases.writeAliases(es);

		writeAudit();

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

	private void writeAudit() throws IOException {
		if (this.auditFile == null)
			return;

		try (Writer out = new FileWriter(this.auditFile)) {
			writeAudit(out);
		}
		log.info("Alias audit written to {}", this.auditFile.getAbsolutePath());
	}

	private void writeAudit(Writer out) throws IOException {
		out.write("ALIAS\tPROVIDER\tSOURCE");
		out.write(System.lineSeparator());
		Iterator<Alias> iter = this.aliases.getAliases();
		while (iter.hasNext()) {
			Alias alias = iter.next();
			out.write(alias.getName());
			out.write('\t');
			out.write(alias.getProvider());
			out.write('\t');
			out.write(alias.getConfigSource().getAbsolutePath());
			out.write(System.lineSeparator());
		}
	}
}
