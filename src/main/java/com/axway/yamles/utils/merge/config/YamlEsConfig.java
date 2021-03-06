package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Mustache;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class YamlEsConfig {
	private static final Logger log = LogManager.getLogger(YamlEsConfig.class);
	private ObjectNode config = Yaml.createObjectNode();

	public YamlEsConfig() {
	}

	public void merge(List<ConfigSource> sources) throws MergeException {
		sources.forEach((cs) -> merge(cs));
		evalValues();
	}

	protected void merge(ConfigSource cs) throws MergeException {
		log.info("merge configuration: {}", cs.getName());
		new Merger(this.config, cs).merge();
	}

	protected void evalValues() {
		evalValues(config);
	}

	protected void evalValues(ObjectNode node) {
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			if (value.isObject()) {
				evalValues((ObjectNode) value);
			} else if (value.isArray()) {
				evalValues((ArrayNode) value);
			} else if (value.isTextual()) {
				String v = value.asText();
				v = Mustache.eval(v);
				node.put(field.getKey(), v);
			}
		}
	}

	protected void evalValues(ArrayNode node) {
		for (JsonNode value : node) {
			if (value.isObject()) {
				evalValues((ObjectNode) value);
			} else if (value.isArray()) {
				evalValues((ArrayNode) value);
			}
		}
	}

	public ObjectNode getConfig() {
		return this.config;
	}

	@Override
	public String toString() {
		return this.config.toString();
	}

	public String toYaml() throws Exception {
		return Yaml.writeAsString(this.config);
	}

	public void write(File file) throws IOException {
		Yaml.write(file, this.config);
	}
}
