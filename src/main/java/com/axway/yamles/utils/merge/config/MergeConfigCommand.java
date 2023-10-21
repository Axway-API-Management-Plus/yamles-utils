package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.merge.AbstractLookupEnabledCommand;
import com.axway.yamles.utils.merge.MergeCommand;
import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.ExecutionMode;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "config", description = "Merge configuration fragments for YAML entity store.", mixinStandardHelpOptions = true)
public class MergeConfigCommand extends AbstractLookupEnabledCommand {
	static class Project {
		@Option(names = {
				"--project" }, description = "Path to the YAML entity store project.", paramLabel = "DIR", required = true)
		File projectDir;
		@Option(names = {
				"--ignore-missing-values" }, description = "Ignore missing configuration of values (only for projects).", required = false)
		private boolean ignoreMissingValues = false;
	}

	static class Target {
		@Option(names = { "-o",
				"--output" }, description = "File to write generated YAML configuration.", paramLabel = "FILE")
		File file = null;

		@ArgGroup
		Project project;
	}

	@ArgGroup(exclusive = true, multiplicity = "1")
	Target target;

	@Option(names = { "-d",
			"--dir" }, description = "Directory to scan for YAML configuration fragments.", paramLabel = "DIR", required = false)
	private List<File> directories;

	@Option(names = { "-f", "-c", "--config",
			"--fragment" }, description = "Configuration fragment for values.yaml file.", paramLabel = "FILE", required = false)
	private List<File> files;

	@ParentCommand
	private MergeCommand parentCommand;

	MergeConfigCommand() {
		super();
	}

	@Override
	public Integer call() throws Exception {
		initializeProviderManager(this.parentCommand.getMode());
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Command: Configure Environmentalized Fields");

		// Load configuration sources
		FragmentSourceScanner scanner = new FragmentSourceScanner();
		scanner.addDirectories(this.directories);
		scanner.scan();

		List<FragmentSource> csl = new ArrayList<>();
		csl.addAll(scanner.getSources());
		if (this.files != null) {
			for (File f : this.files) {
				FragmentSource cs = FragmentSourceFactory.load(f);
				csl.add(cs);
			}
		}

		ExecutionMode mode = ProviderManager.getInstance().getConfigMode();

		FieldConfigurator vc = new FieldConfigurator(mode);
		vc.setConfigFragments(csl);

		if (this.target.project != null) {
			YamlEs es = new YamlEs(this.target.project.projectDir);
			vc.apply(es, this.target.project.ignoreMissingValues);
		} else {
			vc.apply(this.target.file);
		}

		return 0;
	}
}
