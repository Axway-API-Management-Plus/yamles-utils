package com.axway.yamles.utils.lint;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.helper.ListMap;
import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.lint.rules.FilePattern;
import com.axway.yamles.utils.lint.rules.Results;
import com.axway.yamles.utils.lint.rules.Results.Finding;
import com.axway.yamles.utils.lint.rules.Rule;
import com.axway.yamles.utils.lint.rules.Rules;
import com.axway.yamles.utils.lint.rules.RulesManager;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Linter {
	
	private final YamlEs project;
	private final ListMap<FilePattern, Rule> fileRules = new ListMap<>();

	public Linter(YamlEs project, List<File> rules) {
		this.project = Objects.requireNonNull(project);
		RulesManager rm = new RulesManager(rules);
		buildFileRules(rm.getRulesList());
	}

	public Results apply() {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Lint project");		
		Results results = new Results();
		applyDir(results, this.project.getProjectDir());
		return results;
	}

	private void buildFileRules(List<Rules> rulesList) {
		rulesList.forEach(rules -> {
			buildFileRules(rules);
		});
	}

	private void buildFileRules(Rules rules) {
		rules.getRules().forEach((id, rule) -> {
			rule.getFilePatterns().forEach(fp -> {
				this.fileRules.put(fp, rule);
			});
		});
	}

	private void applyDir(Results results, File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				applyDir(results, file);
			} else {
				applyFile(results, file);
			}
		}
	}

	private void applyFile(Results results, File file) {
		if ("_parent.yaml".equals(file.getName()))
			return;
				
		String projectFilePath = projectFilePath(file);
		ObjectNode yaml = null;

		for (Entry<FilePattern, List<Rule>> entry : this.fileRules.getMap().entrySet()) {
			if (entry.getKey().match(projectFilePath)) {
				if (yaml == null) {
					yaml = (ObjectNode) Yaml.load(file);
				}
				for (Rule rule : entry.getValue()) {
					List<Finding> findings = rule.apply(yaml);
					results.add(file, findings);
				}
			}
		}
	}

	private String projectFilePath(File file) {
		String projectPath = this.project.getAbsolutePath();
		String path = file.getAbsolutePath();

		if (!path.startsWith(projectPath)) {
			throw new IllegalStateException("file is not a project file: " + path);
		}

		return path.substring(projectPath.length()).replace('\\', '/');
	}
}
