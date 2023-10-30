package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.ExecutionMode;
import com.axway.yamles.utils.test.MapLookupProvider;
import com.fasterxml.jackson.databind.JsonNode;

class YamlEsConfigTest {
	private static final MapLookupProvider mlp = new MapLookupProvider();

	@BeforeAll
	static void initLookupManager() {
		ProviderManager pm = ProviderManager.initialize(ExecutionMode.CONFIG);
		pm.removeProvider(mlp.getName());
		pm.addProvider(mlp);
		pm.configureBuiltInFunction();
	}

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
		final String MAP_QUOTE_NAME = "QUOTE";
		final String MAP_QUOTE_VALUE = "\"Hello\"";

		final String MAP_ML_NAME = "MULTI_LINE";
		final String MAP_ML_VALUE = "First Line\nSecond Line";

		final String MAP_YAML_STYLE_NAME = "YAML_STYLE";
		final String MAP_YAML_STYLE_VALUE = "{{env TEST }}";

		mlp.getMap().put(MAP_QUOTE_NAME, MAP_QUOTE_VALUE);
		mlp.getMap().put(MAP_ML_NAME, MAP_ML_VALUE);
		mlp.getMap().put(MAP_YAML_STYLE_NAME, MAP_YAML_STYLE_VALUE);

		// prepare config source
		String yamlConfigSource = "---\n" //
				+ "simple: Hello\n" //
				+ "quote: '{{ _map(\"" + MAP_QUOTE_NAME + "\") }}'\n" //
				+ "multi-line: '{{ _map(\"" + MAP_ML_NAME + "\") }}'\n" //
				+ "yaml-style-fix: \"{{ '{{env TEST }}' }}\"\n" //
				+ "yaml-style: '{{ _map(\"" + MAP_YAML_STYLE_NAME + "\") }}'\n";

		FragmentSource cs = new FragmentSource("test", Yaml.read(yamlConfigSource));
		List<FragmentSource> csl = new ArrayList<>();
		csl.add(cs);

		// merge config sources and evaluate values
		YamlEsConfig yec = new YamlEsConfig();
		yec.merge(csl);
		yec.evalValues();

		EnvironmentVariables.reset();

		String yamlDoc = yec.toYaml();

		JsonNode yaml = Yaml.read(yamlDoc);

		assertEquals("Hello", yaml.get("simple").asText());
		assertEquals(MAP_QUOTE_VALUE, yaml.get("quote").asText());
		assertEquals(MAP_ML_VALUE, yaml.get("multi-line").asText());
		assertEquals(MAP_YAML_STYLE_VALUE, yaml.get("yaml-style-fix").asText());
		assertEquals(MAP_YAML_STYLE_VALUE, yaml.get("yaml-style").asText());
	}
}
