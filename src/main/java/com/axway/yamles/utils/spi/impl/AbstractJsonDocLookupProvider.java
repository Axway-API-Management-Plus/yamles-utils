package com.axway.yamles.utils.spi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.JsonDoc;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractJsonDocLookupProvider extends AbstractLookupProvider {

	private final Logger log;
	private final List<JsonDoc> docs = new ArrayList<>();

	protected AbstractJsonDocLookupProvider(Logger logger) {
		this.log = Objects.requireNonNull(logger);
	}

	protected void add(JsonDoc doc) {
		this.docs.add(Objects.requireNonNull(doc));
	}
	
	protected boolean isEmpty() {
		return this.docs.isEmpty();
	}

	@Override
	public Optional<String> lookup(String key) {
		if (!isEnabled() || key == null || key.trim().isEmpty())
			return Optional.empty();

		JsonNode result = null;

		for (JsonDoc doc : this.docs) {
			JsonNode value = doc.at(key);
			if (value != null) {
				if (result == null) {
					log.debug("found lookup key '{}' in {}", key, doc.getName());
				} else {
					log.debug("overwrite lookup key '{}' by {}", key, doc.getName());
				}
				result = value;
			}
		}

		if (result == null || result.isNull() || result.isMissingNode())
			return Optional.empty();
		if (!result.isValueNode())
			throw new LookupProviderException(this, "key is not a value node: " + key);

		return Optional.of(result.asText());
	}

}
