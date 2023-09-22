package com.axway.yamles.utils.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.ConfigParameter.Type;

public class LookupFunctionConfigTest {
	
	@BeforeAll
	static void initLookupManager() {
		LookupManager.getInstance();
	}

	
	@Test
	void loadValidLookupProviderConfig() throws Exception {
		ConfigParameter param1 = new ConfigParameter("param1", true, "", Type.string, true);
		ConfigParameter param2 = new ConfigParameter("param2", true, "", Type.string, true);
		ConfigParameter param3 = new ConfigParameter("param3", true, "", Type.string, true);
		
		EnvironmentVariables.put("NAME_OF_ENV_VAR", "value2");
		System.setProperty("name_of_sys_prop", "value3");
		
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

		assertEquals(3, ls.getConfig().size());
		assertEquals("value1", ls.getConfig(param1, ""));
		assertEquals("value2", ls.getConfig(param2, ""));
		assertEquals("value3", ls.getConfig(param3, ""));
	}
}
