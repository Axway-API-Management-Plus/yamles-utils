package com.axway.yamles.utils.spi.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;
import com.axway.yamles.utils.spi.LookupDoc;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

public class EnvJsonLookupProvider extends AbstractLookupDocLookupProvider {
	
	public static final ConfigParameter CFG_PARAM_ENV = new ConfigParameter("env", true, "Name of enviornment varibale containing JSON document", Type.string);

	private static final Logger log = LogManager.getLogger(EnvJsonLookupProvider.class);

	public EnvJsonLookupProvider() {
		super(DESCR_KEY_JSONPOINTER, log);
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
	public boolean isEnabled() {
		return !isEmpty();
	}
	
	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
		String envvar = source.getRequiredParam(CFG_PARAM_ENV.getName());
		try {
			String json = EnvironmentVariables.get(envvar);
			if (json == null) {
				throw new LookupProviderException(this, "environment variable not found: " + envvar);
			}
			LookupDoc doc = LookupDoc.fromJsonString(source.getAlias(), json);
			add(doc);
		} catch (Exception e) {
			throw new LookupProviderException(this,
					"error on initialize JSON lookup from environment variable: " + envvar, e);
		}
	}
}
