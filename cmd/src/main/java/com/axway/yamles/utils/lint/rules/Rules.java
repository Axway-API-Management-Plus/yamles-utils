package com.axway.yamles.utils.lint.rules;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Rules {

	public static Rules loadRules(File source) {
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			Rules rules = om.readValue(source, Rules.class);
			rules.setConfigSource(source);
			return rules;
		} catch (Exception e) {
			throw new RulesException("error on loading rules: " + source.getAbsolutePath(), e);
		}
	}

	public static Rules loadRules(String yaml) {
		File source = new File("/string");
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			Rules rules = om.readValue(yaml, Rules.class);
			rules.setConfigSource(source);

			return rules;
		} catch (Exception e) {
			throw new RulesException("error on loading rules: " + source.getAbsolutePath(), e);
		}
	}

	private File source;
	private Map<String, Rule> rules;

	@JsonCreator
	public Rules(@JsonProperty("rules") Map<String, Rule> rules) {
		this.rules = Objects.requireNonNull(rules, "no rules defined");
		this.rules.forEach((k, v) -> {
			v.setId(k);
		});
	}

	private void setConfigSource(File source) {
		this.source = source;
	}

	public File getConfigSource() {
		return this.source;
	}

	public Map<String, Rule> getRules() {
		return this.rules;
	}

}
