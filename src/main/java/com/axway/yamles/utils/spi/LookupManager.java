package com.axway.yamles.utils.spi;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupManager {
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

	public String lookup(String key) {
		Optional<String> value = Optional.empty();
		Iterator<LookupProvider> iter = lookupProviders.iterator();
		while (iter.hasNext()) {
			LookupProvider sp = iter.next();
			if (sp.isEnabled()) {
				value = sp.lookup(key);
				if (value.isPresent()) {
					log.info("lookup '{}' provided by '{}'", key, sp.getName());
					break;
				}
			}
		}
		return value.orElseThrow(() -> new LookupManagerException("lookup key not found: " + key));
	}
}
