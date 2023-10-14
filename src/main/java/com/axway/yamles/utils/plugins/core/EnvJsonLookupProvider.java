package com.axway.yamles.utils.plugins.core;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.plugins.AbstractLookupDocLookupProvider;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.axway.yamles.utils.plugins.LookupDoc;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupProviderException;
import com.axway.yamles.utils.plugins.LookupSource;

public class EnvJsonLookupProvider extends AbstractLookupDocLookupProvider {

	public static final ConfigParameter CFG_PARAM_ENV = new ConfigParameter("env", true,
			"Name of enviornment varibale containing JSON document", Type.string, false);

	private static final Logger log = LogManager.getLogger(EnvJsonLookupProvider.class);

	public EnvJsonLookupProvider() {
		super();
		add(CFG_PARAM_ENV);
	}

	@Override
	public String getName() {
		return "envjson";
	}

	@Override
	public String getSummary() {
		return "Lookup values from JSON documents provided by environment variables.";
	}

	@Override
	public String getDescription() {
		return "The key represents the JSON Pointer to the property containing the value (e.g. '/root/sub/key')";
	}

	@Override
	public LookupFunction buildFunction(LookupSource source) throws LookupProviderException {
		String envvar = source.getConfig(CFG_PARAM_ENV, "");
		
		Optional<LookupFunction> clf = checkOnlyLookupFunction(source);
		if (clf.isPresent())
			return clf.get();

		try {
			String json = EnvironmentVariables.get(envvar);
			if (json == null) {
				throw new LookupProviderException(this, "environment variable not found: " + envvar);
			}
			LookupDoc doc = LookupDoc.fromJsonString(json);
			LookupFunction func = new LF(source.getAlias(), this, source.getConfigSource(), doc, log);
			return func;
		} catch (Exception e) {
			throw new LookupProviderException(this,
					"error on initialize JSON lookup from environment variable: " + envvar, e);
		}
	}
}
