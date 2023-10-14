package com.axway.yamles.utils.lint;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.lint.rules.Results;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "lint", description = "Lint YAML entity store.", mixinStandardHelpOptions = true)
public class LintCommand implements Callable<Integer> {

	@Option(names = {
			"--project" }, description = "Path to the YAML entity store project.", paramLabel = "DIR", required = true)
	private File projectDir;

	@Option(names = { "-r", "--rules" }, description = "Rules file.", paramLabel = "FILE", required = true)
	private List<File> rulesFiles;

	LintCommand() {
	}

	public LintCommand(File projectDir, List<File> rulesFiles) {
		this.projectDir = Objects.requireNonNull(projectDir, "project directory required");
		this.rulesFiles = Objects.requireNonNull(rulesFiles, "rules files required");
	}

	@Override
	public Integer call() throws Exception {
		YamlEs project = new YamlEs(this.projectDir);

		Linter linter = new Linter(project, rulesFiles);
		Results results = linter.apply();
		results.auditFindings();

		return !results.hasFindings() ? 0 : 1;
	}
}
