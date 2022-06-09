package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.Objects;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class ConfigSourceFactory {
	public static ConfigSource create(String yaml) {
		String name = "string";
		try {
			JsonNode config = Yaml.read(yaml);
			return new ConfigSource(name, config);
		} catch (JsonProcessingException e) {
			throw new ConfigSourceException(name, e);
		}
	}

	public static ConfigSource load(File file) {
		String name = Objects.requireNonNull(file).getAbsolutePath();
		JsonNode config = Yaml.load(file);
		return new ConfigSource(name, config);
	}
}
