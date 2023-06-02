package com.axway.yamles.utils.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.ConfigParameter.Type;

public class LookupSourceTest {

	@Test
	void construct_without_parameters_and_without_setting_alias() {
		LookupSource src = new LookupSource("provider", null);
		assertEquals("provider", src.getProvider());
		assertThrows(IllegalStateException.class, () -> src.getAlias());
	}

	@Test
	void construct_with_value_parameter_and_alias() {
		ConfigParameter param = new ConfigParameter("param", false, "", Type.string, false);
		Map<String, String> paramsValue = new HashMap<>();
		paramsValue.put(param.getName(), "value");

		LookupSource src = new LookupSource("provider", paramsValue);
		src.setAlias("alias");

		assertEquals("provider", src.getProvider());
		assertEquals("alias", src.getAlias());
		assertEquals("value", src.getConfig(param, ""));
	}

	@Test
	void set_empty_or_null_alias() {
		LookupSource src = new LookupSource("provider", null);
		assertThrows(IllegalArgumentException.class, () -> src.setAlias(null));
		assertThrows(IllegalArgumentException.class, () -> src.setAlias(""));
	}

	@Test
	void get_required_parameter() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, false);
		ConfigParameter paramMissing = new ConfigParameter("paramMissing", true, "", Type.string, false);

		Map<String, String> paramsValue = new HashMap<>();
		paramsValue.put(param.getName(), "value");

		LookupSource src = new LookupSource("provider", paramsValue);

		assertEquals("value", src.getConfig(param, ""));
		assertThrows(LookupFunctionConfigException.class, () -> src.getConfig(paramMissing, ""));
	}

	@Test
	void eval_parameters_from_system_properties() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String SYS_PROP_NAME = "lookup_source_param";
		final String TEMPLATE = "{{ _sys('" + SYS_PROP_NAME + "') }}";
		final String VALUE = "value";

		System.getProperties().setProperty(SYS_PROP_NAME, VALUE);

		Map<String, String> params = new HashMap<>();
		params.put(param.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);

		assertEquals(VALUE, src.getConfig(param, ""));
	}

	@Test
	void eval_parameters_from_invalid_system_properties() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String SYS_PROP_NAME = "non_existing_prop";
		final String TEMPLATE = "{{ _sys('" + SYS_PROP_NAME + "') }}";

		Map<String, String> params = new HashMap<>();
		params.put("param", TEMPLATE);

		LookupSource src = new LookupSource("provider", params);
		assertThrows(LookupFunctionException.class, () -> src.getConfig(param, ""));
	}

	@Test
	void dont_eval_parameters_without_mustache_support() {
		ConfigParameter paramNoEval = new ConfigParameter("param", true, "", Type.string, false);

		final String SYS_PROP_NAME = "lookup_source_param";
		final String TEMPLATE = "{{ _sys('" + SYS_PROP_NAME + "') }}";
		final String VALUE = "value";

		System.getProperties().setProperty(SYS_PROP_NAME, VALUE);

		Map<String, String> params = new HashMap<>();
		params.put(paramNoEval.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);

		assertEquals(TEMPLATE, src.getConfig(paramNoEval, ""));
	}

	@Test
	void eval_parameters_from_env_var() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String ENV_VAR = "LOOKUP_SOURCE_TEST";
		final String TEMPLATE = "{{ _env('" + ENV_VAR + "') }}";
		final String VALUE = "value";

		EnvironmentVariables.put(ENV_VAR, VALUE);

		Map<String, String> params = new HashMap<>();
		params.put(param.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);

		assertEquals(VALUE, src.getConfig(param, ""));

		EnvironmentVariables.reset();
	}

	@Test
	void eval_parameters_from_invalid_env_var() {
		ConfigParameter param = new ConfigParameter("param", true, "", Type.string, true);

		final String ENV_VAR = "NON_EXISTING_VAR";
		final String TEMPLATE = "{{ _env('" + ENV_VAR + "') }}";

		Map<String, String> params = new HashMap<>();
		params.put(param.getName(), TEMPLATE);

		LookupSource src = new LookupSource("provider", params);
		assertThrows(LookupFunctionException.class, () -> src.getConfig(param, ""));
	}

	@Test
	void set_invalid_config_source() {
		LookupSource src = new LookupSource("provider", null);

		File rootDir = new File(File.pathSeparator);
		assertThrows(IllegalArgumentException.class, () -> src.setConfigSource(rootDir));
	}
}
