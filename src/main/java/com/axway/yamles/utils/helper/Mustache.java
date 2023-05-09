package com.axway.yamles.utils.helper;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.LookupManager;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class Mustache {

	private static final Logger log = LogManager.getLogger(Mustache.class);

	private final PebbleEngine pe;

	private static final Mustache instance = new Mustache();

	public static Mustache getInstance() {
		return instance;
	}

	public static String eval(String template) {
		try {
			return getInstance().evaluate(template);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Mustache() {
		this.pe = new PebbleEngine.Builder() //
				.extension(LookupManager.getInstance()) //
				.autoEscaping(false) //
				.strictVariables(true) //
				.build();
		log.debug("Pebble template engine initialized");
	}

	public String evaluate(String template) throws IOException {
		PebbleTemplate pt = pe.getLiteralTemplate(template);
		StringWriter result = new StringWriter();
		pt.evaluate(result);
		
		return result.toString();
	}
}
