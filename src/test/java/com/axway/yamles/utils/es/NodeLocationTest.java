package com.axway.yamles.utils.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeLocationTest {

	@Test
	void rootLocation() {
		NodeLocation yl = NodeLocation.root();
		assertTrue(yl.isRoot());
		assertEquals("/", yl.toString());
	}

	@Test
	void firstLevel() {
		NodeLocation yl = NodeLocation.root().child("level1");
		assertFalse(yl.isRoot());
		assertEquals("/level1", yl.toString());
	}

	@Test
	void secondLevel() {
		NodeLocation yl = NodeLocation.root().child("level1").child("level2");
		assertEquals("/level1/level2", yl.toString());
	}
}
