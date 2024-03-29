package com.axway.yamles.utils.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Yaml {
	private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

	private Yaml() {
	}

	public static JsonNode read(String yaml) throws JsonMappingException, JsonProcessingException {
		return mapper.readTree(yaml);
	}

	public static <T> T readValue(String yaml, Class<T> valueType)
			throws JsonMappingException, JsonProcessingException {
		return mapper.readValue(yaml, valueType);
	}

	public static ObjectNode createObjectNode() {
		return mapper.createObjectNode();
	}

	public static JsonNode load(File file) {
		try {
			return mapper.readTree(file);
		} catch (IOException e) {
			throw new RuntimeException("error on parsing YAML file: " + file.getAbsolutePath(), e);
		}
	}

	public static <T> T loadValue(File file, Class<T> valueType)
			throws StreamReadException, DatabindException, IOException {
		return mapper.readValue(file, valueType);
	}

	public static JsonNode load(URL url) {
		try {
			return mapper.readTree(url);
		} catch (IOException e) {
			throw new RuntimeException("error on parsing YAML: " + url, e);
		}
	}

	public static String writeAsString(ObjectNode node) throws JsonProcessingException {
		return mapper.writeValueAsString(node);
	}

	public static void write(File file, ObjectNode node) throws IOException {
		mapper.writeValue(file, node);
	}
}
