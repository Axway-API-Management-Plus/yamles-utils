package com.axway.yamles.utils.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.test.MapLookupProvider;

import io.pebbletemplates.pebble.error.ClassAccessException;
import io.pebbletemplates.pebble.error.PebbleException;

class MustacheTest {
	private static final String PROP_KEY = "mustache.test";
	private static final String PROP_VALUE = "<lookup succeded>";

	private static final String PROP_ML_KEY = "mustache.test.ml";
	private static final String PROP_ML_VALUE = "\"Escaped\nMulti Line\tText\"";

	private static final MapLookupProvider mlp = new MapLookupProvider();
	private static final LookupFunction lfMap = mlp.buildFunction(null);

	@Test
	void eval() {
		mlp.getMap().put(PROP_KEY, PROP_VALUE);
		mlp.getMap().put(PROP_ML_KEY, PROP_ML_VALUE);

		Mustache m = new Mustache();
		m.addFunction(lfMap);

		assertEquals("Hello", m.evaluate("Hello"));
		assertEquals("Hello", m.evaluate("{{ \"Hello\" }}"));
		assertEquals("{{Hello}} World", m.evaluate("{{ '{{Hello}}' }} World"));
		assertEquals(PROP_VALUE, m.evaluate("{{ _map('" + PROP_KEY + "') }}"));
		assertEquals(PROP_VALUE, m.evaluate("{{ _map(key='" + PROP_KEY + "') }}"));
		assertEquals("Result: " + PROP_VALUE + "!", m.evaluate("Result: {{ _map('" + PROP_KEY + "') }}!"));
		assertEquals(PROP_ML_VALUE, m.evaluate("{{ _map('" + PROP_ML_KEY + "') }}"));

		assertThrows(Exception.class, () -> {
			m.evaluate("{{ _map('non_existing_propery') }}");
		});

		assertThrows(LookupFunctionException.class, () -> {
			m.evaluate("{{ _map() }}");
		});

		assertThrows(PebbleException.class, () -> {
			m.evaluate("{{ _map(lookup='key') }}");
		});

		assertThrows(PebbleException.class, () -> {
			m.evaluate("{{hello}}");
		});

		System.getProperties().remove(PROP_KEY);
		System.getProperties().remove(PROP_ML_KEY);
	}

	@Test
	void fixed_CVE_2022_37767() throws Exception {
		Mustache m = new Mustache();
		assertThrows(ClassAccessException.class, () -> m.evaluate(CVE_2022_37767_Test.TEMPLATE_INVOKE_TOSTRING_METHOD));
	}
}
