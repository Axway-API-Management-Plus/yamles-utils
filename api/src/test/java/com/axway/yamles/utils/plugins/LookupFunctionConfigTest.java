package com.axway.yamles.utils.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.plugins.ConfigParameter.Type;

public class LookupFunctionConfigTest {
	
	@Test
	void loadValidLookupProviderConfig() throws Exception {
		ConfigParameter param = new ConfigParameter("param1", true, "", Type.string, true);
		
		URL url = LookupFunctionConfigTest.class.getResource("/lookup-providers.yaml");
		File file = new File(url.toURI());
		
		LookupFunctionConfig lpc = LookupFunctionConfig.loadYAML(file);

		assertEquals(file, lpc.getConfigSource());
		assertEquals(2, lpc.getSources().size());
		
		LookupSource ls;

		// first provider (without parameters)
		ls = lpc.getSources().get("alias1");
		assertNotNull(ls);
		assertEquals("alias1", ls.getAlias());
		assertEquals("provider1", ls.getProvider());
		assertTrue(ls.getConfig().isEmpty());
		
		// second provider (with parameters)
		ls = lpc.getSources().get("alias2");
		assertNotNull(ls);
		assertEquals("alias2", ls.getAlias());
		assertEquals("provider2", ls.getProvider());

		assertEquals(1, ls.getConfig().size());
		assertEquals("value", ls.getConfig(param, ""));
	}
}
