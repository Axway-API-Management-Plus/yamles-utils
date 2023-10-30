package com.axway.yamles.utils.plugins.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;

public class EnvJsonLookupProviderTest {

	@Test
	void buildAndTestLookupFunction() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();

		Map<String, String> params = new HashMap<>();
		params.put(EnvJsonLookupProvider.CFG_PARAM_ENV.getName(), "testJson");
		LookupSource ls = new LookupSource("test", lp.getName(), params);

		EnvironmentVariables.put("testJson", "{}");
		LookupFunction lf = lp.buildFunction(ls);
		assertNotNull(lf);
		EnvironmentVariables.reset();
	}

	@Test
	void addInvalidLookupSource() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();

		Map<String, String> params = new HashMap<>();
		params.put(EnvJsonLookupProvider.CFG_PARAM_ENV.getName(), "_non_exiting_var_");
		LookupSource ls = new LookupSource("test", lp.getName(), params);

		assertThrows(LookupProviderException.class, () -> lp.buildFunction(ls));
	}
}
