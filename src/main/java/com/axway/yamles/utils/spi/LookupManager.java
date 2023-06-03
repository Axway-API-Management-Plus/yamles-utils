package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.helper.Mustache;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;

public class LookupManager extends AbstractExtension {
	private static final Logger log = LogManager.getLogger(LookupManager.class);

	private static final LookupManager instance = new LookupManager();

	private final Map<String, LookupProvider> lookupProviders;

	private final Map<String, LookupFunction> functions = new HashMap<>();

	public static LookupManager getInstance() {
		return instance;
	}

	private LookupManager() {
		ServiceLoader<LookupProvider> sl = ServiceLoader.load(LookupProvider.class);
		this.lookupProviders = new HashMap<>();

		Iterator<LookupProvider> iter = sl.iterator();
		while (iter.hasNext()) {
			LookupProvider lp = iter.next();
			addProvider(lp);

			// add built-in functions
			if (lp.isEnabled()) {
				LookupFunction lf = new LookupFunction(lp.getName(), lp, Optional.of("<built-in>"));
				addFunction(lf);
			}
		}
	}

	public void addProvider(LookupProvider lp) {
		LookupProvider registered = this.lookupProviders.putIfAbsent(lp.getName(), lp);
		if (registered != null) {
			throw new IllegalStateException("duplicate lookup provider name '" + lp.getName() + "'; used by "
					+ lp.getClass().getCanonicalName() + " and " + registered.getClass().getCanonicalName());
		}
		log.debug("lookup provider registered: provider={}", lp.getName());
	}

	private void addFunction(LookupFunction lf) {
		LookupFunction existingFunc = this.functions.put(lf.getName(), lf);
		if (existingFunc != null)
			throw new LookupFunctionConfigException(lf.getDefintionSource(),
					"alias '" + lf.getAlias() + "' already defined in " + existingFunc.getDefintionSource());
		log.debug("lookup function added: func={}; provider={}; source={}", lf.getName(), lf.getProvider().getName(),
				lf.getDefintionSource());
	}

	public Collection<LookupProvider> getProviders() {
		return this.lookupProviders.values();
	}

	public Collection<LookupFunction> getLookupFunctions() {
		return this.functions.values();
	}

	public void configureFunctions(List<File> configFiles) {
		if (configFiles == null || configFiles.isEmpty())
			return;

		for (File cf : configFiles) {
			LookupFunctionConfig lpc = LookupFunctionConfig.loadYAML(cf);

			for (LookupSource ls : lpc.getSources().values()) {
				Audit.AUDIT_LOG.info("register lookup source: alias={}", ls.getAlias());
				LookupProvider lp = this.lookupProviders.get(ls.getProvider());
				if (lp == null) {
					throw new LookupFunctionConfigException(lpc.getConfigSource(),
							"unknown lookup provider: " + ls.getProvider());
				}
				lp.addSource(ls);

				LookupFunction lf = new LookupFunction(ls.getAlias(), lp,
						Optional.of(lpc.getConfigSource().getAbsolutePath()));
				addFunction(lf);
			}
		}

		// refresh Mustache processor to reflect new functions
		Mustache.refresh();
	}

	@Override
	public Map<String, Function> getFunctions() {
		Map<String, Function> func = new HashMap<>();
		this.functions.forEach((name, lf) -> {
			if (lf.isEnabled()) {
				func.put(lf.getName(), lf);
				Audit.AUDIT_LOG.info("lookup function registered: func={}; provider={}; source={}", lf.getName(),
						lf.getProvider().getName(), lf.getDefintionSource());
			}
		});
		return func;
	}
}
