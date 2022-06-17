package com.axway.yamles.utils.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jknack.handlebars.HandlebarsException;

class MustacheTest {
	private static final String PROP_KEY = "mustache.test";
	private static final String PROP_VALUE = "lookup succeded";

	@Test
	void eval() {
		System.getProperties().setProperty(PROP_KEY, PROP_VALUE);

		assertEquals("Hello", Mustache.eval("Hello"));
		assertEquals("{{Hello}}", Mustache.eval("\\{{Hello}}"));
		assertEquals(PROP_VALUE, Mustache.eval("{{sys '" + PROP_KEY + "'}}"));
		assertEquals("Result: " + PROP_VALUE + "!", Mustache.eval("Result: {{sys '" + PROP_KEY + "'}}!"));
		
		assertThrows(HandlebarsException.class, () -> {
			Mustache.eval("{{sys 'non_existing_propery'}}");
		});

		assertThrows(HandlebarsException.class, () -> {
			Mustache.eval("{{sys}}");
		});
		
//		assertThrows(HandlebarsException.class, () -> {
//			Mustache.eval("{{hello}}");
//		});

		System.getProperties().remove(PROP_KEY);
	}

}
