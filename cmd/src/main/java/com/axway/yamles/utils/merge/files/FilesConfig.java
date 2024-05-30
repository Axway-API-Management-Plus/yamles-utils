package com.axway.yamles.utils.merge.files;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class FilesConfig {
	public static FilesConfig loadConfig(File file) {
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			FilesConfig fc = om.readValue(file, FilesConfig.class);
			fc.setConfigSource(file);

			return fc;
		} catch (Exception e) {
			throw new FilesConfigException(file, "error on loading files configuration", e);
		}
	}

	public static FilesConfig loadConfig(String yaml) {
		File source = new File("/string");
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.findAndRegisterModules();

			FilesConfig fc = om.readValue(yaml, FilesConfig.class);
			fc.setConfigSource(source);

			return fc;
		} catch (Exception e) {
			throw new FilesConfigException(source, "error on loading files configuration", e);
		}
	}

	private File configSource;
	private final List<FileConfig> fileConfigs;

	@JsonCreator
	public FilesConfig(@JsonProperty("files") List<FileConfig> files) {
		this.fileConfigs = (files != null) ? files : Collections.emptyList();
	}
	
	void setConfigSource(File source) {
		this.configSource = Objects.requireNonNull(source);
		this.fileConfigs.forEach(f -> {
			f.setConfigSource(this.configSource);
		});
	}

	public File getConfigSource() {
		return this.configSource;
	}
	
	public List<FileConfig> getFileConfigs() {
		return this.fileConfigs;
	}
}
