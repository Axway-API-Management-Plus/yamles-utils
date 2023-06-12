package com.axway.yamles.utils.lint.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.lint.rules.Results.Finding;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Rule extends AbstractRule {

	private final String fileType;
	private final List<FilePattern> filePatterns;
	private final List<Assertion> assertions;

	@JsonCreator
	public Rule(@JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("fileType") String fileType, @JsonProperty("filePatterns") List<FilePattern> filePatterns,
			@JsonProperty("assertions") List<Assertion> assertions) {
		super(name, description);
		this.fileType = fileType;
		this.filePatterns = filePatterns;
		this.assertions = Objects.requireNonNull(assertions, "assertions required");
	}

	public List<FilePattern> getFilePatterns() {
		return Collections.unmodifiableList(filePatterns);
	}

	public List<Assertion> getAssertions() {
		return Collections.unmodifiableList(this.assertions);
	}

	public List<Finding> apply(ObjectNode yaml) {
		if (this.fileType != null && !this.fileType.equals(YamlEs.getEnityType(yaml).orElse(""))) {
			return Collections.emptyList();
		}

		List<Finding> findings = new ArrayList<>();
		this.assertions.forEach(expr -> {
			if (!expr.check(yaml)) {
				findings.add(new Finding(this, expr));
			}
		});
		return findings;
	}

}
