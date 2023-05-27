package com.axway.yamles.utils.spi.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

public class EnvJsonLookupProviderTest {
	
	@Test
	void constructEmptyProvider() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		assertFalse(lp.isEnabled());
	}

	@Test
	void addValidLookupSource() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		
		Map<String, String> params = new HashMap<>();
		params.put(EnvJsonLookupProvider.CFG_PARAM_ENV.getName(), "testJson");
		LookupSource ls = new LookupSource("test", lp.getName(), params, null, null);
		
		EnvironmentVariables.put("testJson", "{}");		
		lp.addSource(ls);
		EnvironmentVariables.reset();

		assertTrue(lp.isEnabled());
	}

	@Test
	void addInvalidLookupSource() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		
		Map<String, String> params = new HashMap<>();
		params.put(EnvJsonLookupProvider.CFG_PARAM_ENV.getName(), "_non_exiting_var_");
		LookupSource ls = new LookupSource("test", lp.getName(), params, null, null);
		
		assertThrows(LookupProviderException.class, () -> lp.addSource(ls));
	}
}
