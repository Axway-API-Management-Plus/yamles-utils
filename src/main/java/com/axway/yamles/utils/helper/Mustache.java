package com.axway.yamles.utils.helper;

import java.io.IOException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class Mustache {

	private static final Logger log = LogManager.getLogger(Mustache.class);

	private final Handlebars hb;

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
		this.hb = new Handlebars();

		Iterator<LookupProvider> iter = LookupManager.getInstance().getProviders();
		while (iter.hasNext()) {
			LookupProvider sp = iter.next();
			if (sp.isEnabled()) {
				this.hb.registerHelper(sp.getName(), sp);
				log.debug("lookup provider registered: {}", sp.getName());
			}
		}
	}

	public String evaluate(String template) throws IOException {
		Template t = this.hb.compileInline(template);
		return t.apply(null);
	}
}
