package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.JsonNode;

class YamlEsConfigTest {

	@Test
	void testToStringInitial() {
		String str = new YamlEsConfig().toString();
		assertEquals("{}", str);
	}

	@Test
	void testToYamlInitial() throws Exception {
		String str = new YamlEsConfig().toYaml();
		assertEquals("--- {}\n", str);
	}

	@Test
	void values_with_special_characters() throws Exception {
		final String ENV_QUOTE_NAME = "QUOTE";
		final String ENV_QUOTE_VALUE = "\"Hello\"";

		final String ENV_ML_NAME = "MULTI_LINE";
		final String ENV_ML_VALUE = "First Line\nSecond Line";

		final String ENV_YAML_STYLE_NAME = "YAML_STYLE";
		final String ENV_YAML_STYLE_VALUE = "{{env TEST }}";

		EnvironmentVariables.reset();
		EnvironmentVariables.put(ENV_QUOTE_NAME, ENV_QUOTE_VALUE);
		EnvironmentVariables.put(ENV_ML_NAME, ENV_ML_VALUE);
		EnvironmentVariables.put(ENV_YAML_STYLE_NAME, ENV_YAML_STYLE_VALUE);

		// prepare config source
		String yamlConfigSource = "---\n" //
				+ "simple: Hello\n" //
				+ "quote: '{{ _env(\"" + ENV_QUOTE_NAME + "\") }}'\n" //
				+ "multi-line: '{{ _env(\"" + ENV_ML_NAME + "\") }}'\n" //
				+ "yaml-style-fix: \"{{ '{{env TEST }}' }}\"\n" //
				+ "yaml-style: '{{ _env(\"" + ENV_YAML_STYLE_NAME + "\") }}'\n";

		ConfigSource cs = new ConfigSource("test", Yaml.read(yamlConfigSource));
		List<ConfigSource> csl = new ArrayList<>();
		csl.add(cs);

		// merge config sources and evaluate values
		YamlEsConfig yec = new YamlEsConfig();
		yec.merge(csl);
		yec.evalValues();

		EnvironmentVariables.reset();

		String yamlDoc = yec.toYaml();

		JsonNode yaml = Yaml.read(yamlDoc);

		assertEquals("Hello", yaml.get("simple").asText());
		assertEquals(ENV_QUOTE_VALUE, yaml.get("quote").asText());
		assertEquals(ENV_ML_VALUE, yaml.get("multi-line").asText());
		assertEquals(ENV_YAML_STYLE_VALUE, yaml.get("yaml-style-fix").asText());
		assertEquals(ENV_YAML_STYLE_VALUE, yaml.get("yaml-style").asText());
	}
}
