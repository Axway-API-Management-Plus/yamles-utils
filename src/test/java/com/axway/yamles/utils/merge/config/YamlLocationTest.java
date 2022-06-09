package com.axway.yamles.utils.merge.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.merge.config.YamlLocation;

class YamlLocationTest {
	
	@Test
	void rootLocation() {
		YamlLocation yl = new YamlLocation();
		assertEquals("/", yl.toString());
	}
	
	@Test
	void firstLevel() {
		YamlLocation yl = new YamlLocation();
		yl.push("level1");
		assertEquals("/level1", yl.toString());
	}
	
	@Test
	void secondLevel() {
		YamlLocation yl = new YamlLocation();
		yl.push("level1");
		yl.push("level2");
		assertEquals("/level1/level2", yl.toString());
	}

	@Test
	void walk() {
		YamlLocation yl = new YamlLocation();
		assertEquals("/", yl.toString());
		
		yl.push("level1");
		assertEquals("/level1", yl.toString());
		
		yl.push("level2");
		assertEquals("/level1/level2", yl.toString());
		
		yl.pop();
		assertEquals("/level1", yl.toString());
		
		yl.pop();
		assertEquals("/", yl.toString());
	}

}
