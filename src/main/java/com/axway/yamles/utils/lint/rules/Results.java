package com.axway.yamles.utils.lint.rules;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.logging.log4j.Level;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.helper.ListMap;

public class Results {

	public static class Finding {
		private final Rule rule;
		private final Assertion assertion;

		public Finding(Rule rule, Assertion assertion) {
			this.rule = Objects.requireNonNull(rule);
			this.assertion = Objects.requireNonNull(assertion);
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("rule '").append(this.rule.getId()).append("' failed - ").append(this.assertion);
			;
			return str.toString();
		}
	}

	private final ListMap<String, Finding> fileFindings = new ListMap<>();

	public Results() {
	}

	public void add(File file, Finding finding) {
		this.fileFindings.put(file.getAbsolutePath(), finding);
	}

	public void add(File file, List<Finding> finding) {
		this.fileFindings.put(file.getAbsolutePath(), finding);
	}

	public boolean hasFindings() {
		for (Entry<String, List<Finding>> entry : this.fileFindings.getMap().entrySet()) {
			if (entry.getValue().size() > 0)
				return true;
		}
		return false;
	}

	public void auditFindings() {
		this.fileFindings.getMap().forEach((file, findings) -> {
			if (findings.size() > 0) {
				findings.forEach(finding -> {
					Audit.AUDIT_LOG.log(Level.ERROR, "{} in {}", finding, file);
				});
			}
		});
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		this.fileFindings.getMap().forEach((file, findings) -> {
			if (findings.size() > 0) {
				str.append(file).append("\n");
				findings.forEach(finding -> {
					str.append("  ").append(finding).append("\n");
				});
			}
		});
		return str.toString();
	}

	public ListMap<String, Finding> getFileFindings() {
		return this.fileFindings;
	}
}
