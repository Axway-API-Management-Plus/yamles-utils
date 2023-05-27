package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.axway.yamles.utils.helper.Json;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LookupProviderConfig {

	private File configSource = null;
	private final Map<String, LookupSource> sources;

	public static LookupProviderConfig loadYAML(File file) {
		try {
			LookupProviderConfig lpc = Yaml.loadValue(file, LookupProviderConfig.class);
			lpc.setConfigSource(file);

			return lpc;
		} catch (Exception e) {
			throw new LookupProviderConfigException(file.getAbsolutePath(), "error on loading lookup provider configuration", e);
		}
	}
	
	public static LookupProviderConfig fromYAML(String yaml) {
		try {
			return Yaml.readValue(yaml, LookupProviderConfig.class);
		} catch (Exception e) {
			throw new LookupProviderConfigException("<string>", "error on loading lookup provider configuration", e);
		}
	}
	
	public static LookupProviderConfig fromJSON(String json) {
		try {
			return Json.readValue(json, LookupProviderConfig.class);
		} catch (Exception e) {
			throw new LookupProviderConfigException("<string>", "error on loading lookup provider configuration", e);
		}
	}

	@JsonCreator
	protected LookupProviderConfig(@JsonProperty("lookups") Map<String, LookupSource> sources) {
		this.sources = Objects.requireNonNull(sources, "no sources defined");
		this.sources.forEach((k, v) -> {
			v.setAlias(k);
		});
	}

	public File getConfigSource() {
		return this.configSource;
	}

	protected void setConfigSource(File configSource) {
		Objects.requireNonNull(configSource);
		if (!configSource.isFile()) {
			throw new IllegalArgumentException(
					"source for lookup provider configuration is not a file: " + configSource.getAbsolutePath());
		}
		this.configSource = Objects.requireNonNull(configSource);
		this.sources.forEach((k, v) -> {
			v.setConfigSource(this.configSource);
		});
	}

	public Map<String, LookupSource> getSources() {
		return Collections.unmodifiableMap(this.sources);
	}
}
