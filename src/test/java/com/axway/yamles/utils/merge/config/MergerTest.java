package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.node.ObjectNode;

class MergerTest {

	@Test
	void mergeEmpty() {
		ObjectNode target = Yaml.createObjectNode();
		ConfigSource cs = ConfigSourceFactory.create("---");
		Merger merger = new Merger(target, cs);

		assertTrue(target.isEmpty());

		merger.merge();

		assertTrue(target.isEmpty());
	}

	@Test
	void mergeEmptyWithSimpleObject() {
		ObjectNode target = Yaml.createObjectNode();
		ConfigSource cs = ConfigSourceFactory.create("str1: \"value\"");
		Merger merger = new Merger(target, cs);

		assertTrue(target.isEmpty());

		merger.merge();

		assertEquals(1, target.size());
		assertEquals("value", target.get("str1").asText());
	}

	@Test
	void mergeMultiMergeSimpleObjectNoOverwrite() {
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("str1: \"value1\"");
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("str1").asText());

		cs = ConfigSourceFactory.create("str2: \"value2\"");
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(2, target.size());
		assertEquals("value1", target.get("str1").asText());
		assertEquals("value2", target.get("str2").asText());
	}

	@Test
	void mergeMultiMergeSimpleObjectOverwrite() {
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("str: \"value\"");
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value", target.get("str").asText());

		cs = ConfigSourceFactory.create("str: \"value_new\"");
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value_new", target.get("str").asText());
	}

	@Test
	void mergeMultiMergeObjectNoOverwrite() {
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("---\n" //
				+ "obj1:\n" //
				+ "  str1: \"value1\"" //
		);
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("obj1").get("str1").asText());

		cs = ConfigSourceFactory.create("---\n" //
				+ "obj2:\n" //
				+ "  str2: \"value2\"\n" //
		);
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(2, target.size());
		assertEquals("value1", target.get("obj1").get("str1").asText());		
		assertEquals("value2", target.get("obj2").get("str2").asText());
	}

	@Test
	void mergeMultiMergeObjectAddAndOverwrite() {
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("---\n" //
				+ "obj:\n" //
				+ "  str1: \"value1\"\n" //
		);
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("obj").get("str1").asText());

		cs = ConfigSourceFactory.create("---\n" //
				+ "obj:\n" //
				+ "  str1: \"value2\"\n" //
				+ "  str2: \"value2\"\n" //
		);
		merger = new Merger(target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value2", target.get("obj").get("str1").asText());		
		assertEquals("value2", target.get("obj").get("str2").asText());
	}
}
