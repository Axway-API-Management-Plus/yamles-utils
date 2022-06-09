package com.axway.yamles.utils.spi;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class LookupManager implements Helper<String> {
	private static final Logger log = LogManager.getLogger(LookupManager.class);

	private static final LookupManager instance = new LookupManager();
	
	private final ServiceLoader<LookupProvider> lookupProviders;
	
	public static LookupManager getInstance() {
		return instance;
	}
	
	private LookupManager() {
		this.lookupProviders = ServiceLoader.load(LookupProvider.class);
		if (log.isDebugEnabled()) {
			this.lookupProviders.forEach(p -> {
				log.debug("lookup provider found: {}", p.getName());
			});
		}
	}
	
	public Iterator<LookupProvider> getProviders() {
		return this.lookupProviders.iterator();
	}

	public String lookup(String key) {
		Optional<String> secret = Optional.empty();
		Iterator<LookupProvider> iter = lookupProviders.iterator();
		while (iter.hasNext()) {
			LookupProvider sp = iter.next(); 
			secret = sp.lookup(key);
			if (secret.isPresent()) {
				log.info("lookup '{}' provided by '{}'", key, sp.getName());
				break;
			}
		}
		return secret.orElseThrow(() -> new LookupManagerException("secret not found"));
	}
	

	@Override
	public Object apply(String context, Options options) throws IOException {
		return new Handlebars.SafeString(lookup(context));
	}
}
