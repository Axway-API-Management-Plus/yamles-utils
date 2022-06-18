package com.axway.yamles.utils.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

public class LookupManager extends AbstractExtension {
	private static final Logger log = LogManager.getLogger(LookupManager.class);

	private static final LookupManager instance = new LookupManager();

	private final ServiceLoader<LookupProvider> lookupProviders;

	public static LookupManager getInstance() {
		return instance;
	}

	private LookupManager() {
		this.lookupProviders = ServiceLoader.load(LookupProvider.class);
	}

	public Iterator<LookupProvider> getProviders() {
		return this.lookupProviders.iterator();
	}

	@Override
	public Map<String, Function> getFunctions() {
		Map<String, Function> functions = new HashMap<>();
		Iterator<LookupProvider> iter = lookupProviders.iterator();
		while (iter.hasNext()) {
			LookupProvider sp = iter.next();
			if (sp.isEnabled()) {
				functions.put(sp.getName(), sp);
				log.debug("lookup provider registered: {}", sp.getName());
			} else {
				log.debug("lookup provider skipped: {}", sp.getName());
			}
		}
		return functions;
	}
}
