package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.config.YamlEsConfig;

class YamlEsConfigTest {

	@Test
	void testToStringInitial() {
		String str = new YamlEsConfig().toString();
		assertEquals("{}", str);
	}
	
	@Test
	void testToYamlInitial() throws Exception {
		String str = new YamlEsConfig().toYaml();
		assertEquals("--- {}\n", str);
	}
}
