package com.axway.yamles.utils.spi;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class LookupSourceTest {

	@Test
	void construct_without_parameters_and_without_setting_alias() {
		LookupSource src = new LookupSource("provider", null, null, null);
		assertEquals("provider", src.getProvider());

		assertNotNull(src.getRawValueParams());
		assertTrue(src.getRawValueParams().isEmpty());

		assertNotNull(src.getRawEnvironmentParams());
		assertTrue(src.getRawEnvironmentParams().isEmpty());

		assertNotNull(src.getRawSysPropsParams());
		assertTrue(src.getRawSysPropsParams().isEmpty());

		assertNotNull(src.getParams());
		assertTrue(src.getParams().isEmpty());

		assertThrows(IllegalStateException.class, () -> src.getAlias());
	}

	@Test
	void construct_with_value_parameters_and_alias() {
		Map<String, String> paramsValue = new HashMap<>();
		paramsValue.put("param", "value");

		LookupSource src = new LookupSource("provider", paramsValue, null, null);
		src.setAlias("alias");

		assertEquals("provider", src.getProvider());
		assertEquals("alias", src.getAlias());

		assertNotNull(src.getRawValueParams());
		assertEquals(1, src.getRawValueParams().size());
		assertEquals("value", src.getRawValueParams().get("param"));

		assertNotNull(src.getRawEnvironmentParams());
		assertTrue(src.getRawEnvironmentParams().isEmpty());

		assertNotNull(src.getRawSysPropsParams());
		assertTrue(src.getRawSysPropsParams().isEmpty());

		assertNotNull(src.getParams());
		assertEquals(1, src.getParams().size());
		assertEquals("value", src.getParams().get("param"));
	}
	
	@Test
	void set_empty_or_null_alias() {
		LookupSource src = new LookupSource("provider", null, null, null);
		assertThrows(IllegalArgumentException.class, () -> src.setAlias(null));
		assertThrows(IllegalArgumentException.class, () -> src.setAlias(""));
	}

	@Test
	void construct_with_duplicate_parameters() {
		final String SYS_PROP = "A_SYSTEM_PROPERTY";
		
		Map<String, String> paramsVal = new HashMap<>();
		paramsVal.put("param", "value");

		Map<String, String> paramsEnv = new HashMap<>();
		paramsEnv.put("param", "PATH");
		
		Map<String, String> paramsSys = new HashMap<>();
		paramsSys.put("param", SYS_PROP);

		assertThrows(LookupProviderConfigException.class,
				() -> new LookupSource("provider", paramsVal, paramsEnv, null).getParams());
		
		System.getProperties().setProperty(SYS_PROP, "dummy");		
		assertThrows(LookupProviderConfigException.class,
				() -> new LookupSource("provider", paramsVal, null, paramsSys).getParams());
		assertThrows(LookupProviderConfigException.class,
				() -> new LookupSource("provider", null, paramsEnv, paramsSys).getParams());
		System.getProperties().remove(SYS_PROP);
	}
	
	@Test
	void resolve_parameters_from_system_properties() {
		final String SYS_PROP_NAME = "lookup_source_param";
		
		System.getProperties().setProperty(SYS_PROP_NAME, "value");

		Map<String, String> params = new HashMap<>();
		params.put("param", SYS_PROP_NAME);
		
		LookupSource src = new LookupSource("provider", null, null, params);
		assertEquals(SYS_PROP_NAME, src.getRawSysPropsParams().get("param"));
		assertEquals("value", src.getParams().get("param"));
	}
	
	@Test
	void resolve_parameters_from_invalid_system_properties() {
		final String SYS_PROP_NAME = "non_existing_prop";

		Map<String, String> params = new HashMap<>();
		params.put("param", SYS_PROP_NAME);
		
		LookupSource src = new LookupSource("provider", null, null, params);
		assertEquals(SYS_PROP_NAME, src.getRawSysPropsParams().get("param"));
		assertThrows(LookupProviderConfigException.class, () -> src.getParams());
	}

	
	@Test
	void resolve_parameters_from_env_var() {
		final String ENV_VAR = "PATH";

		String value = System.getenv(ENV_VAR);
		if (value == null) {
			throw new IllegalStateException(ENV_VAR + " environment variable not found; test can't be executed");
		}

		Map<String, String> params = new HashMap<>();
		params.put("param", ENV_VAR);
		
		LookupSource src = new LookupSource("provider", null, params, null);
		assertEquals(ENV_VAR, src.getRawEnvironmentParams().get("param"));
		assertEquals(value, src.getParams().get("param"));
	}
	
	@Test
	void resolve_parameters_from_invalid_env_var() {
		final String ENV_VAR = "non_existing_var";

		Map<String, String> params = new HashMap<>();
		params.put("param", ENV_VAR);
		
		LookupSource src = new LookupSource("provider", null, params, null);
		assertEquals(ENV_VAR, src.getRawEnvironmentParams().get("param"));
		assertThrows(LookupProviderConfigException.class, () -> src.getParams().get("param"));
	}
	
	@Test
	void set_invalid_config_source() {
		LookupSource src = new LookupSource("provider", null, null, null);
		
		File rootDir = new File(File.pathSeparator);
		assertThrows(IllegalArgumentException.class, () -> src.setConfigSource(rootDir));
	}
}
