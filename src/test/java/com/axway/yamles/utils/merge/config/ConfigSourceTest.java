package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.config.ConfigSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ConfigSourceTest {

	@Test
	void testSourceConfig() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode config = om.createObjectNode();
		
		assertNotNull(new ConfigSource("foobar", config));
	}
	
	@Test
	void testSourceConfigMissingName() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode config = om.createObjectNode();
		
		Exception ex = assertThrows(NullPointerException.class, () -> {
			new ConfigSource(null, config);
		});
		
		assertEquals("missing source name", ex.getMessage());
	}
	
	@Test
	void testSourceConfigMissingConfig() {
		ConfigSource cs = new ConfigSource("foobar", null);
		
		assertTrue(cs.getConfig().isObject());
		assertTrue(cs.getConfig().isEmpty());
	}
	
	@Test
	void testGetName() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode config = om.createObjectNode();
		
		assertEquals("foobar", new ConfigSource("foobar", config).getName());
	}

	@Test
	void testGetConfig() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode config = om.createObjectNode();
		
		assertTrue(new ConfigSource("foobar", config).getConfig().isObject());
	}
}
