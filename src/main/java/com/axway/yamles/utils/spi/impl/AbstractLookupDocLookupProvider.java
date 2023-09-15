package com.axway.yamles.utils.spi.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.FunctionArgument;
import com.axway.yamles.utils.spi.LookupDoc;
import com.axway.yamles.utils.spi.LookupFunction;
import com.axway.yamles.utils.spi.LookupFunctionException;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractLookupDocLookupProvider extends AbstractLookupProvider {
	public static final FunctionArgument ARG_KEY = new FunctionArgument("key", true,
			"JSON Pointer to key node (see RFC6901)");

	protected static class LF extends LookupFunction {
		private final LookupDoc doc;
		private final Logger log;

		public LF(String alias, AbstractLookupDocLookupProvider provider, Optional<String> source, LookupDoc doc, Logger log) {
			super(alias, provider, source);
			this.doc = Objects.requireNonNull(doc, "document required");
			this.log = Objects.requireNonNull(log, "logger required");
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			Optional<String> result = Optional.empty();

			String key = getArg(AbstractLookupDocLookupProvider.ARG_KEY, args, "");

			JsonNode value = doc.at(key);
			if (value == null || value.isNull() || value.isMissingNode() || !value.isValueNode()) {
				log.error("key is not a value node: provider={}; alias={}; key={}", getName(), getAlias(), key);
				return result;
			}

			log.debug("found lookup key: provider={}; alias={}; source={}; key={}", getProvider().getName(), getAlias(),
					doc.getSourceID(), key);

			result = Optional.of(value.asText());
			return result;
		}
	}

	protected AbstractLookupDocLookupProvider() {
		super();
		add(ARG_KEY);
	}
}
