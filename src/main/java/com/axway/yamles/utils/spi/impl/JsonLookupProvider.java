package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Json;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.databind.JsonNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class JsonLookupProvider extends AbstractLookupProvider {

	static class JsonDoc {
		private final File file;
		private final JsonNode doc;

		public JsonDoc(File file) {
			this.file = Objects.requireNonNull(file);
			this.doc = Json.load(file);
		}

		public File getFile() {
			return this.file;
		}

		public JsonNode at(String key) {
			return this.doc.at(key);
		}
	}

	private static final Logger log = LogManager.getLogger(JsonLookupProvider.class);

	@Option(names = { "--lookup-json" }, description = "path to an JSON file", paramLabel = "FILE")
	private List<File> jsonFiles;
	private List<JsonDoc> docs;

	@Override
	public String getName() {
		return "json";
	}

	@Override
	public boolean isEnabled() {
		return this.jsonFiles != null && !this.jsonFiles.isEmpty();
	}

	@Override
	public void onRegistered() {
		synchronized (this) {
			if (this.docs == null) {
				this.docs = new ArrayList<JsonDoc>();
				for (File file : this.jsonFiles) {
					try {
						JsonDoc doc = new JsonDoc(file);
						this.docs.add(doc);
						log.info("JSON lookup file registered: {}", doc.getFile().getAbsolutePath());
					} catch (Exception e) {
						throw new LookupProviderException(this,
								"error on loading lookup JSON file: " + file.getAbsolutePath(), e);
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

		for (JsonDoc doc : this.docs) {
			JsonNode value = doc.at(key);
			if (value != null) {
				if (result == null) {
					log.debug("found lookup key '{}' in {}", key, doc.getFile().getAbsoluteFile());
				} else {
					log.debug("overwrite lookup key '{}' by {}", key, doc.getFile().getAbsoluteFile());
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
