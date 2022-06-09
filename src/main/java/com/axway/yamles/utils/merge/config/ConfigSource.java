package com.axway.yamles.utils.merge.config;

import java.util.Objects;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ConfigSource {
	private final String name;
	private final ObjectNode config;

	public ConfigSource(String name, JsonNode node) {
		this.name = Objects.requireNonNull(name, "missing source name");
		if (node == null || node.isNull()) {
			node = Yaml.createObjectNode();
		}
		if (!node.isObject()) {
			throw new ConfigSourceException(name, "config source is not an object");
		}
		this.config = (ObjectNode) node;
	}

	public String getName() {
		return this.name;
	}

	public ObjectNode getConfig() {
		return this.config;
	}
}
