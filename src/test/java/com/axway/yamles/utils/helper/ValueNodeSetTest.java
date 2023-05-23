package com.axway.yamles.utils.helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;

public class ValueNodeSetTest {

	@Test
	void constructFromNull() {
		ValueNodeSet vns = new ValueNodeSet(null);
		assertTrue(vns.isEmpty());
	}

	@Test
	void constructFromEmptyObject() throws Exception {
		JsonNode json = Json.read("{}");
		ValueNodeSet vns = new ValueNodeSet(json);

		assertTrue(vns.isEmpty());
		assertNotNull(vns.getValueNodes());
	}

	@Test
	void constructFromSimpleObject() throws Exception {
		JsonNode json = Json.read("{\"p1\":\"v1\", \"p2\":\"v2\"}");
		ValueNodeSet vns = new ValueNodeSet(json);

		assertFalse(vns.isEmpty());
		assertEquals(2, vns.getValueNodes().size());
		assertTrue(vns.getValueNodes().contains("/p1"));
		assertTrue(vns.getValueNodes().contains("/p2"));
	}

	@Test
	void constructFromComplexObject() throws Exception {
		JsonNode json = Json.read("{\"obj1\": {\"p1\":\"v1\", \"a1\": []}, \"obj2\": {} }");
		ValueNodeSet vns = new ValueNodeSet(json);

		assertFalse(vns.isEmpty());
		assertEquals(2, vns.getValueNodes().size());
		assertTrue(vns.getValueNodes().contains("/obj1/p1"));
		assertTrue(vns.getValueNodes().contains("/obj1/a1"));
	}

	@Test
	void exceptionOnConstructionFromNonObject() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> new ValueNodeSet(Json.read("[]")));
		assertThrows(IllegalArgumentException.class, () -> new ValueNodeSet(Json.read("{\"p\":\"v\"}").get("p")));
	}
}
