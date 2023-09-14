package com.axway.yamles.utils.spi;

import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CertificateManager {
	private static final Logger log = LogManager.getLogger(CertificateManager.class);
	
	private static final CertificateManager instance = new CertificateManager();
	
	private final Map<String, CertificateProvider> providers = new TreeMap<>();
	
	public static CertificateManager getInstance() {
		return instance;
	}
	
	private CertificateManager() {
		ServiceLoader<CertificateProvider> cps = ServiceLoader.load(CertificateProvider.class);
		cps.forEach(p -> {
			log.debug("loaded certificate provider: {}" , p.getName());
			this.providers.put(p.getName(), p);
		});
	}

	public CertificateProvider getProvider(String name) {
		return this.providers.get(name);
	}
	
	public Collection<CertificateProvider> getProviders() {
		return this.providers.values();
	}
}
