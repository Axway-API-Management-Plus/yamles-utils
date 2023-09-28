package com.axway.yamles.utils.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.axway.yamles.utils.helper.Mustache.DisabledMethodAceess;
import com.axway.yamles.utils.merge.LookupManager;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.ClassAccessException;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class CVE_2022_37767_Test {

	public static final String TEMPLATE_INVOKE_TOSTRING_METHOD = "{% set value=\"Hello\".toString() %}{{ value }}";

	@Test
	void reproduce_CVE_2022_37767() throws Exception {
		PebbleEngine pe = new PebbleEngine.Builder() //
				.extension(LookupManager.getInstance()) //
				.autoEscaping(false) //
				.strictVariables(true) //
				// .methodAccessValidator(null) //
				.build();

		// Without access validator, Java method can be called
		assertEquals("Hello", evaluate(pe, TEMPLATE_INVOKE_TOSTRING_METHOD));
	}

	@Test
	void fixed_CVE_2022_37767() throws Exception {
		PebbleEngine pe = new PebbleEngine.Builder() //
				.extension(LookupManager.getInstance()) //
				.autoEscaping(false) //
				.strictVariables(true) //
				.methodAccessValidator(new DisabledMethodAceess()) //
				.build();

		// With access validator, the method can't be invoked
		assertThrows(ClassAccessException.class, () -> evaluate(pe, TEMPLATE_INVOKE_TOSTRING_METHOD));
	}

	private static String evaluate(PebbleEngine pe, String template) throws IOException {
		PebbleTemplate pt = pe.getLiteralTemplate(template);
		StringWriter result = new StringWriter();
		pt.evaluate(result);

		return result.toString();
	}
}
