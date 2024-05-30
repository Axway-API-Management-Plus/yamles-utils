package com.axway.yamles.utils.plugins.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.core.AnmPasswordHashLookupProvider.PasswordHashGenerator;

public class AnmPasswordHashLookupProviderTest {

	@Test
	void testGenerator() throws Exception {
		String passwordHash = PasswordHashGenerator.generate("changeme");
		assertTrue(passwordHash.startsWith("$" + AnmPasswordHashLookupProvider.PasswordHashGenerator.VERSION + "$"));
	}

	@Test
	void testGeneratePasswordHash() throws Exception {
		AnmPasswordHashLookupProvider lp = new AnmPasswordHashLookupProvider();
		LookupFunction lf = lp.buildFunction();

		Map<String, Object> args = new HashMap<>();
		args.put(AnmPasswordHashLookupProvider.ARG_PWD.getName(), "changeme");

		String passwordHash = lf.lookup(args).get();

		assertTrue(passwordHash.startsWith("$" + AnmPasswordHashLookupProvider.PasswordHashGenerator.VERSION + "$"));
	}
}
