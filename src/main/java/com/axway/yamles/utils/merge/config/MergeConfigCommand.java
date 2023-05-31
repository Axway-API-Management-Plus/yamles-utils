package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "config", description = "Merge configuration files for YAML entiry store.", mixinStandardHelpOptions = true)
public class MergeConfigCommand extends AbstractLookupEnabledCommand {
	private static final Logger log = LogManager.getLogger(MergeConfigCommand.class);

	static class Project {
		@Option(names = {
				"--project" }, description = "Path to the YAML entity store project.", paramLabel = "DIR", required = true)
		File projectDir;
		@Option(names = {
				"--ignore-missing-values" }, description = "Ignore missing configuration of values (only for projects)", required = false)
		private boolean ignoreMissingValues = false;
	}

	static class Target {
		@Option(names = { "-o",
				"--output" }, description = "File to write generated YAML configuration", paramLabel = "FILE")
		File file = null;

		@ArgGroup
		Project project;
	}

	@ArgGroup(exclusive = true, multiplicity = "1")
	Target target;

	@Option(names = { "-d",
			"--dir" }, description = "Directory to scan for YAML configuration sources", paramLabel = "DIR", required = false)
	private List<File> directories;

	@Option(names = { "-c", "--config" }, description = "Configuration file", paramLabel = "FILE", required = false)
	private List<File> files;

	@Override
	public Integer call() throws Exception {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Command: Configure Environmentalized Fields");

		initLookupProviders();

		// Load configuration sources
		ConfigSourceScanner scanner = new ConfigSourceScanner();
		scanner.addDirectories(this.directories);
		scanner.scan();

		YamlEs es = null;
		File out;
		if (this.target.project != null) {
			es = new YamlEs(this.target.project.projectDir);
			out = es.getValuesFile();
		} else {
			out = this.target.file;
		}

		List<ConfigSource> csl = new ArrayList<>();
		csl.addAll(scanner.getSources());
		if (this.files != null) {
			for (File f : this.files) {
				ConfigSource cs = ConfigSourceFactory.load(f);
				csl.add(cs);
			}
		}

		YamlEsConfig esConfig = new YamlEsConfig();
		esConfig.merge(csl);

		if (es != null && es.getRequiredValues().isPresent()) {
			if (!esConfig.allFieldsConfigured(es.getRequiredValues())) {
				if (this.target.project != null && !this.target.project.ignoreMissingValues) {
					throw new RuntimeException("Some required values are not configured. Check log for details!");
				}
			}
		}

		esConfig.evalValues();

		if (out.getName().equals("-")) {
			System.out.println(esConfig.toYaml());
		} else {
			Yaml.write(out, esConfig.getConfig());
			log.info("configuration written to {}", out.getAbsoluteFile());
		}

		return 0;
	}
}
