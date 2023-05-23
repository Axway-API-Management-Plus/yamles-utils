package com.axway.yamles.utils.spi.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.spi.LookupProviderException;

public class EnvJsonLookupProviderTest {
	
	@Test
	void checkDisabled() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		assertFalse(lp.isEnabled());
	}

	@Test
	void checkEnabled() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		lp.addVariable("test");
		assertTrue(lp.isEnabled());
	}

	@Test
	void variableNotFound() {
		EnvJsonLookupProvider lp = new EnvJsonLookupProvider();
		lp.addVariable("_non_exiting_var_");
		
		assertThrows(LookupProviderException.class, () -> lp.onRegistered());
	}
}
