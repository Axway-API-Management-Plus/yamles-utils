package com.axway.yamles.utils.spi.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupDoc;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractLookupDocLookupProvider extends AbstractLookupProvider {
	public static final String DESCR_KEY_JSONPOINTER = "JSON Pointer to key node (see RFC6901)";

	private final Logger log;
	private final Map<String, LookupDoc> docs = new HashMap<>();

	protected AbstractLookupDocLookupProvider(String keyDescription, FunctionArgument[] funcArgs, ConfigParameter[] configParams, Logger logger) {
		super(keyDescription, funcArgs, configParams);
		this.log = Objects.requireNonNull(logger);
	}

	@Override
	public boolean isEnabled() {
		return !this.docs.isEmpty();
	}

	@Override
	public Optional<String> lookup(String alias, Map<String, Object> args) {
		Optional<String> result = Optional.empty();
		LookupDoc doc = this.docs.get(alias);
		if (doc == null) {
			log.error("alias not found by provider: provider={}; alias={}", getName(), alias);
			return result;
		}

		String key = getArg(ARG_KEY, args, null);

		JsonNode value = doc.at(key);
		if (value == null || value.isNull() || value.isMissingNode() || !value.isValueNode()) {
			log.error("key is not a value node: provider={}; alias={}; key={}", getName(), alias, key);
			return result;
		}

		log.debug("found lookup key: provider={}; alias={}; source={}; key={}", getName(), doc.getAlias(),
				doc.getSourceID(), key);

		result = Optional.of(value.asText());

		return result;
	}

	protected void add(LookupDoc doc) {
		Objects.requireNonNull(doc);
		if (this.docs.put(doc.getAlias(), doc) != null) {
			throw new LookupProviderException(this,
					"lookup document already registered: provider=" + getName() + "; alias=" + doc.getAlias());
		}
		log.debug("lookup document registered: provider={}; alias={}; source={}", getName(), doc.getAlias(),
				doc.getSourceID());
	}

	protected boolean isEmpty() {
		return this.docs.isEmpty();
	}
}
