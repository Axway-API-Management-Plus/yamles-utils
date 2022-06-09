package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.helper.YamlEs;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "config", description = "Merge configuration files for YAML entiry store.")
public class MergeConfigCommand implements Callable<Integer> {
	private static final Logger log = LogManager.getLogger(MergeConfigCommand.class);

	static class Target {
		@Option(names = { "-o",
				"--output" }, description = "File to write generated YAML configuration", paramLabel = "FILE")
		File file = null;
		@Option(names = {
				"--project" }, description = "Path to the YAML entity store project.", paramLabel = "DIR", required = true)
		File projectDir;
	}

	@ArgGroup(exclusive = true, multiplicity = "1")
	Target target;

	@Option(names = { "-d",
			"--dir" }, description = "Directory to scan for YAML configuration sources", paramLabel = "DIR", required = true)
	private List<File> directories;

	@Override
	public Integer call() {
		try {
			ConfigSourceScanner scanner = new ConfigSourceScanner();
			scanner.addDirectories(this.directories);
			scanner.scan();

			File out;
			if (this.target.projectDir != null) {
				YamlEs es = new YamlEs(this.target.projectDir);
				out = es.getValuesFile();
			} else {
				out = this.target.file;
			}

			YamlEsConfig esConfig = new YamlEsConfig();
			esConfig.merge(scanner.getSources());

			if (out.getName().equals("-")) {
				System.out.println(esConfig.toYaml());
			} else {
				Yaml.write(out, esConfig.getConfig());
				log.info("configuration written to {}", out.getAbsoluteFile());
			}

			return 0;
		} catch (Exception e) {
			log.error(e);
			return 1;
		}
	}
}
