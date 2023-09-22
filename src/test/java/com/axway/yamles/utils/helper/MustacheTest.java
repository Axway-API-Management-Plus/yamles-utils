package com.axway.yamles.utils.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.spi.LookupFunctionException;
import com.axway.yamles.utils.spi.LookupManager;

import io.pebbletemplates.pebble.error.ClassAccessException;
import io.pebbletemplates.pebble.error.PebbleException;

class MustacheTest {
	private static final String PROP_KEY = "mustache.test";
	private static final String PROP_VALUE = "<lookup succeded>";

	private static final String PROP_ML_KEY = "mustache.test.ml";
	private static final String PROP_ML_VALUE = "\"Escaped\nMulti Line\tText\"";

	@BeforeAll
	static void initLookupManager() {
		LookupManager.getInstance();
	}

	@Test
	void eval() {
		System.getProperties().setProperty(PROP_KEY, PROP_VALUE);
		System.getProperties().setProperty(PROP_ML_KEY, PROP_ML_VALUE);

		assertEquals("Hello", Mustache.eval("Hello"));
		assertEquals("Hello", Mustache.eval("{{ \"Hello\" }}"));
		assertEquals("{{Hello}} World", Mustache.eval("{{ '{{Hello}}' }} World"));
		assertEquals(PROP_VALUE, Mustache.eval("{{ _sys('" + PROP_KEY + "') }}"));
		assertEquals(PROP_VALUE, Mustache.eval("{{ _sys(key='" + PROP_KEY + "') }}"));
		assertEquals("Result: " + PROP_VALUE + "!", Mustache.eval("Result: {{ _sys('" + PROP_KEY + "') }}!"));
		assertEquals(PROP_ML_VALUE, Mustache.eval("{{ _sys('" + PROP_ML_KEY + "') }}"));

		assertThrows(Exception.class, () -> {
			Mustache.eval("{{ _sys('non_existing_propery') }}");
		});

		assertThrows(LookupFunctionException.class, () -> {
			Mustache.eval("{{ _sys() }}");
		});

		assertThrows(PebbleException.class, () -> {
			Mustache.eval("{{ _sys(lookup='key') }}");
		});

		assertThrows(PebbleException.class, () -> {
			Mustache.eval("{{hello}}");
		});

		System.getProperties().remove(PROP_KEY);
	}

	@Test
	void fixed_CVE_2022_37767() throws Exception {
		assertThrows(ClassAccessException.class,
				() -> Mustache.eval(CVE_2022_37767_Test.TEMPLATE_INVOKE_TOSTRING_METHOD));
	}
}
