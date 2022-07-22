package com.axway.yamles.utils.spi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Json;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class EnvJsonLookupProvider extends AbstractLookupProvider {

	static class EnvJsonDoc {
		private final String name;
		private final JsonNode doc;

		public EnvJsonDoc(String name) throws JacksonException {
			this.name = Objects.requireNonNull(name);
			String json = System.getenv(name);
			if (json == null) {
				throw new RuntimeException("environment variable not found: " + name);
			}

			this.doc = Json.read(json);
		}

		public String getName() {
			return this.name;
		}

		public JsonNode at(String key) {
			return this.doc.at(key);
		}
	}

	private static final Logger log = LogManager.getLogger(EnvJsonLookupProvider.class);

	@Option(names = {
			"--lookup-envjson" }, description = "environment variable containing a JSON document", paramLabel = "NAME")
	private List<String> names;
	private List<EnvJsonDoc> docs;

	@Override
	public String getName() {
		return "envjson";
	}

	@Override
	public boolean isEnabled() {
		return this.names != null && !this.names.isEmpty();
	}

	@Override
	public void onRegistered() {
		synchronized (this) {
			if (this.docs == null) {
				this.docs = new ArrayList<EnvJsonDoc>();
				for (String name : this.names) {
					try {
						EnvJsonDoc doc = new EnvJsonDoc(name);
						this.docs.add(doc);
						log.info("JSON lookup from environment var registered: {}", doc.getName());
					} catch (Exception e) {
						throw new LookupProviderException(this,
								"error on initialize JSON lookup from environment variable: " + name, e);
					}
				}
			}
		}
	}

	@Override
	public Optional<String> lookup(String key) {
		if (!isEnabled() || key == null || key.trim().isEmpty())
			return Optional.empty();

		JsonNode result = null;

		for (EnvJsonDoc doc : this.docs) {
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
