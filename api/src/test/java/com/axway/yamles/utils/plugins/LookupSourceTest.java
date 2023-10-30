package com.axway.yamles.utils.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.plugins.ConfigParameter.Type;

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
	void set_invalid_config_source() {
		LookupSource src = new LookupSource("provider", null);

		File rootDir = new File(File.pathSeparator);
		assertThrows(IllegalArgumentException.class, () -> src.setConfigSource(rootDir));
	}
}
