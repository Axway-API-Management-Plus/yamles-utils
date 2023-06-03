package com.axway.yamles.utils.lint;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.YamlEsUtils;
import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.lint.rules.Results;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "lint", description = "Lint YAML entity store.", mixinStandardHelpOptions = true)
public class LintCommand implements Callable<Integer> {
	private static final Logger log = LogManager.getLogger(LintCommand.class);

	@Option(names = {
			"--project" }, description = "Path to the YAML entity store project.", paramLabel = "DIR", required = true)
	private File projectDir;

	@Option(names = { "-r", "--rules" }, description = "Rules file.", paramLabel = "FILE", required = true)
	private List<File> rulesFiles;
	
	@ParentCommand
	YamlEsUtils utils;

	@Override
	public Integer call() throws Exception {
		log.info("check project");
		YamlEs project = new YamlEs(this.projectDir);

		Linter linter = new Linter(project, rulesFiles);
		Results results = linter.apply();

		if (results.hasFindings()) {
			results.log(Level.ERROR);
			return 1;
		}

		return 0;
	}
}
