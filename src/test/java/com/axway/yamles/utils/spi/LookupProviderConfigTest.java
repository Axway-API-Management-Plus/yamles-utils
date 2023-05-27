package com.axway.yamles.utils.spi;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LookupProviderConfigTest {
	
	@Test
	void loadValidLookupProviderConfig() throws Exception {
		URL url = LookupProviderConfigTest.class.getResource("/lookup-providers.yaml");
		File file = new File(url.toURI());
		
		LookupProviderConfig lpc = LookupProviderConfig.loadYAML(file);

		assertEquals(file, lpc.getConfigSource());
		assertEquals(2, lpc.getSources().size());
		
		LookupSource ls;

		// first provider (without parameters)
		ls = lpc.getSources().get("alias1");
		assertNotNull(ls);
		assertEquals("alias1", ls.getAlias());
		assertEquals("provider1", ls.getProvider());
		assertNotNull(ls.getRawValueParams());
		assertTrue(ls.getRawValueParams().isEmpty());
		assertNotNull(ls.getRawEnvironmentParams());
		assertTrue(ls.getRawEnvironmentParams().isEmpty());
		assertNotNull(ls.getRawSysPropsParams());
		assertTrue(ls.getRawSysPropsParams().isEmpty());
		
		// second provider (with parameters)
		ls = lpc.getSources().get("alias2");
		assertNotNull(ls);
		assertEquals("alias2", ls.getAlias());
		assertEquals("provider2", ls.getProvider());
		
		Map<String, String> params;
		
		params = ls.getRawValueParams();
		assertNotNull(params);
		assertEquals(1, params.size());
		assertEquals("value1", params.get("param1"));
		
		params = ls.getRawEnvironmentParams();
		assertNotNull(params);
		assertEquals(1, params.size());
		assertEquals("name_of_env_var", params.get("param2"));

		params = ls.getRawSysPropsParams();
		assertNotNull(params);
		assertEquals(1, params.size());
		assertEquals("name_of_sys_prop", params.get("param3"));
	}
}
