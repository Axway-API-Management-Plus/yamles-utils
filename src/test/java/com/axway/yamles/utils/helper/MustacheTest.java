package com.axway.yamles.utils.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.spi.LookupProviderException;
import io.pebbletemplates.pebble.error.PebbleException;

class MustacheTest {
	private static final String PROP_KEY = "mustache.test";
	private static final String PROP_VALUE = "<lookup succeded>";
	
	private static final String PROP_ML_KEY = "mustache.test.ml";
	private static final String PROP_ML_VALUE ="\"Escaped\nMulti Line\tText\"";

	@Test
	void eval() {
		System.getProperties().setProperty(PROP_KEY, PROP_VALUE);
		System.getProperties().setProperty(PROP_ML_KEY, PROP_ML_VALUE);

		assertEquals("Hello", Mustache.eval("Hello"));
		assertEquals("Hello", Mustache.eval("{{ \"Hello\" }}"));
		assertEquals("{{Hello}} World", Mustache.eval("{{ '{{Hello}}' }} World"));
		assertEquals(PROP_VALUE, Mustache.eval("{{ sys('" + PROP_KEY + "') }}"));
		assertEquals(PROP_VALUE, Mustache.eval("{{ sys(key='" + PROP_KEY + "') }}"));
		assertEquals("Result: " + PROP_VALUE + "!", Mustache.eval("Result: {{ sys('" + PROP_KEY + "') }}!"));
		assertEquals("\\\"Escaped\\nMulti Line\\tText\\\"", Mustache.eval("{{ sys('" + PROP_ML_KEY + "') }}"));
		
		assertThrows(Exception.class, () -> {
			Mustache.eval("{{ sys('non_existing_propery') }}");
		});

		assertThrows(LookupProviderException.class, () -> {
			Mustache.eval("{{ sys() }}");
		});
		
		assertThrows(PebbleException.class, () -> {
			Mustache.eval("{{ sys(lookup='key') }}");
		});
		
		assertThrows(PebbleException.class, () -> {
			Mustache.eval("{{hello}}");
		});

		System.getProperties().remove(PROP_KEY);
	}

}
