package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.util.Objects;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class FragmentSourceFactory {
	public static FragmentSource create(String name, String yaml) {
		try {
			JsonNode config = Yaml.read(yaml);
			return new FragmentSource(name, config);
		} catch (JsonProcessingException e) {
			throw new FragmentSourceException(name, e);
		}
	}

	public static FragmentSource load(File file) {
		String name = Objects.requireNonNull(file).getAbsolutePath();
		JsonNode config = Yaml.load(file);
		return new FragmentSource(name, config);
	}
}
