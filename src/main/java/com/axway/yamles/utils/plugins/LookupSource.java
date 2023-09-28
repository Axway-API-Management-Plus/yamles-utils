package com.axway.yamles.utils.plugins;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.axway.yamles.utils.helper.Mustache;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LookupSource {
	private static final Pattern ALIAS_NAME = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");

	private final String provider;
	private final Map<String, String> config;
	private Optional<File> configSource = Optional.empty();
	private String alias = null;

	/**
	 * Checks for a valid alias name.
	 * 
	 * @param name alias name
	 * @return <true> if valid, <false> otherwise
	 */
	public static boolean isValidAlias(String name) {
		if (name == null || name.isEmpty())
			return false;
		return ALIAS_NAME.matcher(name).matches();
	}

	public LookupSource(String alias, String provider, Map<String, String> params) {
		if (!isValidAlias(alias))
			throw new IllegalArgumentException("invaliad alias: " + alias);
		this.provider = Objects.requireNonNull(provider, "provider requried");
		this.alias = alias;
		this.config = (params != null && !params.isEmpty()) ? Collections.unmodifiableMap(params)
				: Collections.emptyMap();
	}

	@JsonCreator
	LookupSource(@JsonProperty("provider") String provider, @JsonProperty("config") Map<String, String> params) {
		this.provider = Objects.requireNonNull(provider, "provider property requried");
		this.config = (params != null && !params.isEmpty()) ? Collections.unmodifiableMap(params)
				: Collections.emptyMap();
	}

	public String getProvider() {
		return this.provider;
	}

	public Map<String, String> getConfig() {
		return this.config;
	}

	public String getConfig(ConfigParameter param, String defaultValue) {
		String value = this.config.get(param.getName());
		if (value == null) {
			if (param.isRequired()) {
				throw new LookupFunctionConfigException(this.configSource,
						"required parameter not found: alias=" + this.alias + "; parameter=" + param.getName());
			}
			value = defaultValue;
		}
		if (value != null) {
			if (param.hasMustacheSupport()) {
				value = Mustache.eval(value);
			}
		}
		return value;
	}

	public Optional<File> getFileFromConfig(ConfigParameter param) {
		if (param.getType() != Type.file) {
			throw new LookupFunctionConfigException(this.configSource,
					"not a file parameter: alias=" + this.alias + "; parameter=" + param.getName());
		}
		String filePath = getConfig(param, null);
		if (filePath == null || filePath.isEmpty())
			return Optional.empty();

		Optional<File> baseDir = getBaseDirectory();
		File file = null;
		if (baseDir.isPresent()) {
			Path basePath = Paths.get(baseDir.get().toURI());
			file = basePath.resolve(filePath).toFile();
		} else {
			file = new File(filePath);
		}

		return Optional.ofNullable(file);
	}

	public Optional<File> getBaseDirFromConfig(ConfigParameter param) {
		if (param.getType() != Type.file) {
			throw new LookupFunctionConfigException(this.configSource,
					"not a file parameter: alias=" + this.alias + "; parameter=" + param.getName());
		}

		Optional<File> baseDir = getBaseDirectory();

		String base = getConfig(param, null);
		if (base == null || base.isEmpty())
			return baseDir;

		File resolvedBaseDir = null;

		if (baseDir.isPresent()) {
			Path basePath = Paths.get(baseDir.get().toURI());
			resolvedBaseDir = basePath.resolve(base).toFile();
		} else {
			resolvedBaseDir = new File(base);
		}

		if (!resolvedBaseDir.isDirectory()) {
			throw new LookupFunctionConfigException(this.configSource, "base directory not exists: alias=" + this.alias
					+ "; parameter=" + param.getName() + "; baseDir=" + resolvedBaseDir.getAbsolutePath());
		}

		return Optional.of(resolvedBaseDir);
	}

	public String getAlias() {
		if (this.alias == null) {
			throw new IllegalStateException("alias is not set for lookup source");
		}
		return this.alias;
	}

	void setAlias(String alias) {
		if (!isValidAlias(alias))
			throw new IllegalArgumentException("invalid alias: " + alias);
		this.alias = alias;
	}

	Optional<File> getBaseDirectory() {
		if (!this.configSource.isPresent()) {
			return Optional.empty();
		}
		return Optional.of(this.configSource.get().getParentFile());
	}

	public Optional<String> getConfigSource() {
		return this.configSource.isPresent() ? Optional.of(this.configSource.get().getAbsolutePath())
				: Optional.empty();
	}

	public void setConfigSource(File file) {
		Objects.requireNonNull(file, "configuration file is null");
		if (!file.isFile()) {
			throw new IllegalArgumentException("configuration source is not a regular file: " + file.getAbsolutePath());
		}
		this.configSource = Optional.of(file.getAbsoluteFile());
	}
}
