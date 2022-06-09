package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.databind.node.ObjectNode;

class YamlEsConfig {
	private static final Logger log = LogManager.getLogger(YamlEsConfig.class);
	private ObjectNode config = Yaml.createObjectNode();

	public YamlEsConfig() {
	}

	public void merge(List<ConfigSource> sources) throws MergeException {
		sources.forEach((cs) -> merge(cs));
	}

	protected void merge(ConfigSource cs) throws MergeException {
		log.info("merge configuration: {}", cs.getName());
		new Merger(this.config, cs).merge();
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
