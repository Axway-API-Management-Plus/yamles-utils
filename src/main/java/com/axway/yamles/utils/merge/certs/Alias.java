package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class Alias {

	private String name = "";
	private File configSource = null;
	private final String provider;
	private final Map<String, String> config;

	@JsonCreator
	public Alias(@JsonProperty("provider") String provider, @JsonProperty("config") Map<String, String> config) {
		this.provider = Objects.requireNonNull(provider);
		this.config = Objects.requireNonNull(config);

	}

	void setName(String name) {
		this.name = Objects.requireNonNull(name);
	}

	void setConfigSource(File source) {
		this.configSource = Objects.requireNonNull(source);
	}

	public String getName() {
		return this.name;
	}

	public File getConfigSource() {
		return this.configSource;
	}

	public String getProvider() {
		return this.provider;
	}

	public Map<String, String> getConfig() {
		return this.config;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alias other = (Alias) obj;
		return Objects.equals(name, other.name);
	}
}
