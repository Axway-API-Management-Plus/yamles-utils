package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.fasterxml.jackson.databind.JsonNode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class YamlLookupProvider extends AbstractLookupProvider {

	static class YamlDoc {
		private final File file;
		private final JsonNode doc;

		public YamlDoc(File file) {
			this.file = Objects.requireNonNull(file);
			this.doc = Yaml.load(file);
		}

		public File getFile() {
			return this.file;
		}

		public JsonNode at(String key) {
			return this.doc.at(key);
		}
	}

	private static final Logger log = LogManager.getLogger(YamlLookupProvider.class);

	@Option(names = { "--lookup-yaml" }, description = "Path to an YAML file", paramLabel = "FILE")
	private List<File> yamlFiles;
	private List<YamlDoc> docs;

	@Override
	public String getName() {
		return "yaml";
	}

	@Override
	public Optional<String> lookup(String key) {
		if (this.yamlFiles == null || key == null || key.trim().isEmpty())
			return Optional.empty();

		synchronized (this) {
			if (this.docs == null) {
				this.docs = new ArrayList<YamlDoc>();
				for (File file : this.yamlFiles) {
					try {
						YamlDoc doc = new YamlDoc(file);
						this.docs.add(doc);
					} catch (Exception e) {
						throw new LookupProviderException(this,
								"error on loading lookup YAML file: " + file.getAbsolutePath(), e);
					}
				}
			}
		}

		JsonNode result = null;

		for (YamlDoc doc : this.docs) {
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

		if (result == null || result.isNull() || !result.isTextual())
			return Optional.empty();

		return Optional.of(result.asText());
	}
}
