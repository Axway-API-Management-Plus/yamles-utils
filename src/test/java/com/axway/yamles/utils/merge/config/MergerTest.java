package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.node.ObjectNode;

class MergerTest {

	@Test
	void mergeEmpty() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		ConfigSource cs = ConfigSourceFactory.create("cs", "---");
		Merger merger = new Merger(audit, target, cs);

		assertTrue(target.isEmpty());

		merger.merge();

		assertTrue(target.isEmpty());
	}

	@Test
	void mergeEmptyWithSimpleObject() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		ConfigSource cs = ConfigSourceFactory.create("cs", "str1: \"value\"");
		Merger merger = new Merger(audit, target, cs);

		assertTrue(target.isEmpty());

		merger.merge();

		assertEquals(1, target.size());
		assertEquals("value", target.get("str1").asText());

		assertEquals(1, audit.getFields().length);
		assertEquals("/str1", audit.getFields()[0].getLocation().toString());
		assertEquals("cs", audit.getFields()[0].getSource());
	}

	@Test
	void mergeMultiMergeSimpleObjectNoOverwrite() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("cs1", "str1: \"value1\"");
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("str1").asText());

		cs = ConfigSourceFactory.create("cs2", "str2: \"value2\"");
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(2, target.size());
		assertEquals("value1", target.get("str1").asText());
		assertEquals("value2", target.get("str2").asText());

		// check audit
		assertEquals(2, audit.getFields().length);
		assertEquals("/str1", audit.getFields()[0].getLocation().toString());
		assertEquals("cs1", audit.getFields()[0].getSource());
		assertEquals("/str2", audit.getFields()[1].getLocation().toString());
		assertEquals("cs2", audit.getFields()[1].getSource());
	}

	@Test
	void mergeMultiMergeSimpleObjectOverwrite() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("cs1", "str: \"value\"");
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value", target.get("str").asText());

		cs = ConfigSourceFactory.create("cs2", "str: \"value_new\"");
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value_new", target.get("str").asText());

		// check audit
		assertEquals(1, audit.getFields().length);
		assertEquals("/str", audit.getFields()[0].getLocation().toString());
		assertEquals("cs2", audit.getFields()[0].getSource());
	}

	@Test
	void mergeMultiMergeObjectNoOverwrite() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("cs1", "---\n" //
				+ "obj1:\n" //
				+ "  str1: \"value1\"" //
		);
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("obj1").get("str1").asText());

		cs = ConfigSourceFactory.create("cs2", "---\n" //
				+ "obj2:\n" //
				+ "  str2: \"value2\"\n" //
		);
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(2, target.size());
		assertEquals("value1", target.get("obj1").get("str1").asText());
		assertEquals("value2", target.get("obj2").get("str2").asText());

		// check audit
		assertEquals(2, audit.getFields().length);
		assertEquals("/obj1/str1", audit.getFields()[0].getLocation().toString());
		assertEquals("cs1", audit.getFields()[0].getSource());
		assertEquals("/obj2/str2", audit.getFields()[1].getLocation().toString());
		assertEquals("cs2", audit.getFields()[1].getSource());

	}

	@Test
	void mergeMultiMergeObjectAddAndOverwrite() {
		FieldAudit audit = new FieldAudit();
		ObjectNode target = Yaml.createObjectNode();
		assertTrue(target.isEmpty());

		ConfigSource cs;
		Merger merger;

		cs = ConfigSourceFactory.create("cs1", "---\n" //
				+ "obj:\n" //
				+ "  str1: \"value1\"\n" //
		);
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value1", target.get("obj").get("str1").asText());

		cs = ConfigSourceFactory.create("cs2", "---\n" //
				+ "obj:\n" //
				+ "  str1: \"value2\"\n" //
				+ "  str2: \"value2\"\n" //
		);
		merger = new Merger(audit, target, cs);
		merger.merge();
		assertEquals(1, target.size());
		assertEquals("value2", target.get("obj").get("str1").asText());
		assertEquals("value2", target.get("obj").get("str2").asText());

		// check audit
		assertEquals(2, audit.getFields().length);
		assertEquals("/obj/str1", audit.getFields()[0].getLocation().toString());
		assertEquals("cs2", audit.getFields()[0].getSource());
		assertEquals("/obj/str2", audit.getFields()[1].getLocation().toString());
		assertEquals("cs2", audit.getFields()[1].getSource());
	}
}
