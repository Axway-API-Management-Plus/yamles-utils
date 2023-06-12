package com.axway.yamles.utils.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.LookupManager;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class Mustache {

	/**
	 * Method access validator to disable all method access.
	 * 
	 * <p>
	 * A vulnerability report
	 * <a href="https://nvd.nist.gov/vuln/detail/CVE-2022-37767">CVE-2022-37767</a>
	 * exists for the Pebble engine. According to this CVE any method can be called
	 * by default which could execute code on the server running the Pebble
	 * Template.
	 * </p>
	 * <p>
	 * This is a security issue as the configuration may not provided by the same
	 * team running the YAML-ES utility. In the context of the YAML-ES utility, this
	 * feature is not required. Therefore, to avoid this issue, the execution of
	 * Java methods will be disabled.
	 * <p>
	 * The Pebble Template engine supports method access validation, so that methods
	 * can be checked before execution.
	 * </p>
	 * <p>
	 * This validator rejects the access to all methods.
	 * </p>
	 */
	public static class DisabledMethodAceess implements MethodAccessValidator {
		@Override
		public boolean isMethodAccessAllowed(Object object, Method method) {
			return false;
		}

	}

	private static final Logger log = LogManager.getLogger(Mustache.class);

	private final PebbleEngine pe;

	private static Mustache instance;

	public static Mustache getInstance() {
		synchronized (Mustache.class) {
			if (instance == null) {
				instance = new Mustache();
			}
		}
		return instance;
	}

	public static void refresh() {
		synchronized (Mustache.class) {
			instance = null;
		}
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
				.methodAccessValidator(new DisabledMethodAceess()) //
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
