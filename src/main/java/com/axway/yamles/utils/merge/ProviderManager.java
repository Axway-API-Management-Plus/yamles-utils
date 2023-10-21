package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.plugins.CertificateProvider;
import com.axway.yamles.utils.plugins.ExecutionMode;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionConfig;
import com.axway.yamles.utils.plugins.LookupFunctionConfigException;
import com.axway.yamles.utils.plugins.LookupProvider;
import com.axway.yamles.utils.plugins.LookupSource;

public class ProviderManager {
	private static final Logger log = LogManager.getLogger(ProviderManager.class);

	private static ProviderManager instance = null;

	private final Map<String, CertificateProvider> providers = new TreeMap<>();
	private final Map<String, LookupProvider> lookupProviders = new TreeMap<>();

	private final ExecutionMode mode;

	public static ProviderManager initialize(ExecutionMode mode) {
		Mustache.getInstance().clearFunctions();		
		instance = new ProviderManager(mode);
		log.info("provider manager initizalied");
		return instance;
	}

	public static ProviderManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException("provider manager not initialized");
		}
		return instance;
	}

	public ProviderManager(ExecutionMode mode) {
		this.mode = Objects.requireNonNull(mode, "config mode required");
		
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Initialize Providers (execution mode: {})", mode);
		
		PluginManager pm = new DefaultPluginManager();
		
		List<LookupProvider> lps = pm.getExtensions(LookupProvider.class);
		lps.forEach(p -> {
			log.debug("loaded lookup provider: {}", p.getName());
			p.onInit(this.mode);
			addProvider(p);
		});

		List<CertificateProvider> cps = pm.getExtensions(CertificateProvider.class);
		cps.forEach(p -> {
			log.debug("loaded certificate provider: {}", p.getName());
			p.onInit(this.mode);
			this.providers.put(p.getName(), p);
		});
	}

	public ExecutionMode getConfigMode() {
		return this.mode;
	}

	public CertificateProvider getCertificateProvider(String name) {
		return this.providers.get(name);
	}

	public Collection<CertificateProvider> getCertificateProviders() {
		return this.providers.values();
	}

	public Collection<LookupProvider> getLookupProviders() {
		return this.lookupProviders.values();
	}

	public Collection<LookupFunction> getLookupFunctions() {
		return Mustache.getInstance().getLookupFunctions();
	}

	public void configureBuiltInFunction() {
		configureFunctions(Collections.emptyList());
	}

	public void configureFunctions(List<File> configFiles) {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Initialize Lookup Functions");
		Mustache.getInstance().clearFunctions();

		Audit.AUDIT_LOG.info(Audit.SUB_HEADER_PREFIX + "Built-in functions");
		this.lookupProviders.forEach((name, lp) -> {
			if (lp.isBuiltIn()) {
				addFunction(lp.buildFunction(null));
			}
		});
		
		if (configFiles == null || configFiles.isEmpty())
			return;
		
		Audit.AUDIT_LOG.info(Audit.SUB_HEADER_PREFIX + "Custom functions");
		for (File cf : configFiles) {
			LookupFunctionConfig lpc = LookupFunctionConfig.loadYAML(cf);

			for (LookupSource ls : lpc.getSources().values()) {
				Audit.AUDIT_LOG.info("register lookup source: alias={}", ls.getAlias());
				LookupProvider lp = this.lookupProviders.get(ls.getProvider());
				if (lp == null) {
					throw new LookupFunctionConfigException(lpc.getConfigSource(),
							"unknown lookup provider: " + ls.getProvider());
				}
				LookupFunction lf = lp.buildFunction(ls);
				addFunction(lf);
			}
		}
	}

	private void addProvider(LookupProvider lp) {
		LookupProvider registered = this.lookupProviders.putIfAbsent(lp.getName(), lp);
		if (registered != null) {
			throw new IllegalStateException("duplicate lookup provider name '" + lp.getName() + "'; used by "
					+ lp.getClass().getCanonicalName() + " and " + registered.getClass().getCanonicalName());
		}
		log.debug("lookup provider registered: provider={}", lp.getName());
	}
	
	private void addFunction(LookupFunction lf) {
		Mustache.getInstance().addFunction(lf);
	}
}
