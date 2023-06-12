package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class CertificatesConfig {

	public static CertificatesConfig loadConfig(File file) {
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			CertificatesConfig cc = om.readValue(file, CertificatesConfig.class);
			cc.setConfigSource(file);

			return cc;
		} catch (Exception e) {
			throw new CertificatesConfigException(file, "error on loading certificate configuration", e);
		}
	}

	public static CertificatesConfig loadConfig(String yaml) {
		File source = new File("/string");
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			CertificatesConfig cc = om.readValue(yaml, CertificatesConfig.class);
			cc.setConfigSource(source);

			return cc;
		} catch (Exception e) {
			throw new CertificatesConfigException(source, "error on loading certificate configuration", e);
		}
	}

	private File configSource;
	private final Map<String, Alias> aliases;

	@JsonCreator
	public CertificatesConfig(@JsonProperty("certificates") Map<String, Alias> aliases) {
		this.aliases = Objects.requireNonNull(aliases, "no aliases defined");
		this.aliases.forEach((k, v) -> {
			v.setName(k);
		});
	}

	public Map<String, Alias> getAliases() {
		return Collections.unmodifiableMap(this.aliases);
	}

	private void setConfigSource(File source) {
		this.configSource = Objects.requireNonNull(source);
		this.aliases.forEach((k, v) -> {
			v.setConfigSource(this.configSource);
		});
	}

	public File getConfigSource() {
		return this.configSource;
	}
}
