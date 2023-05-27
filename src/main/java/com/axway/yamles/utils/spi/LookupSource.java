package com.axway.yamles.utils.spi;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.axway.yamles.utils.helper.EnvironmentVariables;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LookupSource {
	private static final Pattern ALIAS_NAME = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");

	private final String provider;
	private final Map<String, String> paramsVal;
	private final Map<String, String> paramsEnv;
	private final Map<String, String> paramsSys;
	private Optional<File> configSource = Optional.empty();
	private String alias = null;
	private Map<String, String> params = null;

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

	public LookupSource(String alias, String provider, Map<String, String> paramsVal, Map<String, String> paramsEnv,
			Map<String, String> paramsSys) {
		if (!isValidAlias(alias))
			throw new IllegalArgumentException("invaliad alias: " + alias);
		this.provider = Objects.requireNonNull(provider, "provider requried");
		this.alias = alias;
		this.paramsVal = (paramsVal != null) ? paramsVal : Collections.emptyMap();
		this.paramsEnv = (paramsEnv != null) ? paramsEnv : Collections.emptyMap();
		this.paramsSys = (paramsSys != null) ? paramsSys : Collections.emptyMap();
	}

	@JsonCreator
	LookupSource(@JsonProperty("provider") String provider, @JsonProperty("config") Map<String, String> paramsVal,
			@JsonProperty("config_env") Map<String, String> paramsEnv,
			@JsonProperty("config_sys") Map<String, String> paramsSys) {
		this.provider = Objects.requireNonNull(provider, "provider property requried");
		this.paramsVal = (paramsVal != null) ? paramsVal : Collections.emptyMap();
		this.paramsEnv = (paramsEnv != null) ? paramsEnv : Collections.emptyMap();
		this.paramsSys = (paramsSys != null) ? paramsSys : Collections.emptyMap();
	}

	public String getProvider() {
		return this.provider;
	}

	public synchronized Map<String, String> getParams() {
		if (this.params != null)
			return this.params;

		Map<String, String> config = new HashMap<>();

		// add parameters from value
		this.paramsVal.forEach((key, value) -> {
			config.put(key, value);
		});

		// add parameters from environment variable
		this.paramsEnv.forEach((key, env) -> {
			String value = EnvironmentVariables.get(env);
			if (value == null) {
				throw new LookupProviderConfigException(this.configSource,
						"environment variable not found: alias=" + this.alias + "; parameter=" + key + "; env=" + env);
			}
			if (config.putIfAbsent(key, value) != null) {
				throw new LookupProviderConfigException(this.configSource,
						"duplicate parameter: alias=" + this.alias + "; parameter=" + key);
			}
		});

		// add parameters from system property
		this.paramsSys.forEach((key, sys) -> {
			String value = System.getProperty(sys);
			if (value == null) {
				throw new LookupProviderConfigException(this.configSource,
						"system property not found: alias=" + this.alias + "; parameter=" + key + "; sys=" + sys);
			}
			if (config.putIfAbsent(key, value) != null) {
				throw new LookupProviderConfigException(this.configSource,
						"duplicate parameter: alias=" + this.alias + "; parameter=" + key);
			}
		});

		this.params = Collections.unmodifiableMap(config);

		return this.params;
	}

	public String getRequiredParam(String name) {
		String value = getParams().get(name);
		if (value == null) {
			throw new LookupProviderConfigException(this.configSource,
					"required parameter not found: alias=" + this.alias + "; parameter=" + name);
		}
		return value;
	}

	public Optional<String> getParam(String name) {
		return Optional.ofNullable(getParams().get(name));
	}

	public Optional<File> getFileFromParam(String name) {
		Optional<String> filePath = getParam(name);
		if (!filePath.isPresent())
			return Optional.empty();

		Optional<File> baseDir = getBaseDirectory();
		File file = null;
		if (baseDir.isPresent()) {
			Path basePath = Paths.get(baseDir.get().toURI());
			file = basePath.resolve(filePath.get()).toFile();
		} else {
			file = new File(filePath.get());
		}

		return Optional.ofNullable(file);
	}

	public File getFileFromRequiredParam(String name) {
		String filePath = getRequiredParam(name);

		Optional<File> baseDir = getBaseDirectory();
		File file = null;
		if (baseDir.isPresent()) {
			Path basePath = Paths.get(baseDir.get().toURI());
			file = basePath.resolve(filePath).toFile();
		} else {
			file = new File(filePath);
		}

		return file;
	}

	Map<String, String> getRawValueParams() {
		return Collections.unmodifiableMap(this.paramsVal);
	}

	Map<String, String> getRawEnvironmentParams() {
		return Collections.unmodifiableMap(this.paramsEnv);
	}

	Map<String, String> getRawSysPropsParams() {
		return Collections.unmodifiableMap(this.paramsSys);
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

	public Optional<File> getBaseDirectory() {
		if (!this.configSource.isPresent()) {
			return Optional.empty();
		}
		return Optional.of(this.configSource.get().getParentFile());
	}

	void setConfigSource(File file) {
		Objects.requireNonNull(file, "configuration file is null");
		if (!file.isFile()) {
			throw new IllegalArgumentException("configuration source is not a regular file: " + file.getAbsolutePath());
		}
		this.configSource = Optional.of(file);
	}
}
