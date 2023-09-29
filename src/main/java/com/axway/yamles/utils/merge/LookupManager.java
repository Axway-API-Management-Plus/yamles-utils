package com.axway.yamles.utils.merge;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionConfig;
import com.axway.yamles.utils.plugins.LookupFunctionConfigException;
import com.axway.yamles.utils.plugins.LookupProvider;
import com.axway.yamles.utils.plugins.LookupSource;

import io.pebbletemplates.pebble.extension.AbstractExtension;

public class LookupManager extends AbstractExtension {
	private static final Logger log = LogManager.getLogger(LookupManager.class);

	private static final LookupManager instance = new LookupManager();

	private final Map<String, LookupProvider> lookupProviders = new TreeMap<>();

	public static LookupManager getInstance() {
		return instance;
	}

	private LookupManager() {
		ServiceLoader<LookupProvider> sl = ServiceLoader.load(LookupProvider.class);

		Iterator<LookupProvider> iter = sl.iterator();
		while (iter.hasNext()) {
			LookupProvider lp = iter.next();
			addProvider(lp);
		}
	}

	protected void addProvider(LookupProvider lp) {
		LookupProvider registered = this.lookupProviders.putIfAbsent(lp.getName(), lp);
		if (registered != null) {
			throw new IllegalStateException("duplicate lookup provider name '" + lp.getName() + "'; used by "
					+ lp.getClass().getCanonicalName() + " and " + registered.getClass().getCanonicalName());
		}
		log.debug("lookup provider registered: provider={}", lp.getName());

		// add built-in functions
		if (lp.isBuiltIn()) {
			addFunction(lp.buildFunction(null));
		}
	}

	private void addFunction(LookupFunction lf) {
		Mustache.getInstance().addFunction(lf);
	}

	public Collection<LookupProvider> getProviders() {
		return this.lookupProviders.values();
	}

	public Collection<LookupFunction> getLookupFunctions() {
		return Mustache.getInstance().getLookupFunctions();
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
				LookupFunction lf = lp.buildFunction(ls);
				addFunction(lf);
			}
		}
	}
}