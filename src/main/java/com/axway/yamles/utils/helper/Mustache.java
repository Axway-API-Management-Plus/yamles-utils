package com.axway.yamles.utils.helper;

import java.io.IOException;
import java.util.Iterator;

import com.axway.yamles.utils.spi.LookupManager;
import com.axway.yamles.utils.spi.LookupProvider;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class Mustache {

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
		
		this.hb.registerHelper("secret", LookupManager.getInstance());

		Iterator<LookupProvider> iter = LookupManager.getInstance().getProviders();
		while(iter.hasNext()) {
			LookupProvider sp = iter.next();
			this.hb.registerHelper(sp.getName(), sp);
		}
	}

	public String evaluate(String template) throws IOException {
		Template t = this.hb.compileInline(template);
		return t.apply(null);
	}
}
