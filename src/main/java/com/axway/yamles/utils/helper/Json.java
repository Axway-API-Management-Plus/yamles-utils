package com.axway.yamles.utils.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Json {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static JsonNode read(String json) throws JsonMappingException, JsonProcessingException {
		return mapper.readTree(json);
	}

	public static ObjectNode createObjectNode() {
		return mapper.createObjectNode();
	}

	public static JsonNode load(File file) {
		try {
			return mapper.readTree(file);
		} catch (IOException e) {
			throw new RuntimeException("error on parsing JSON file: " + file.getAbsolutePath(), e);
		}
	}

	public static JsonNode load(URL url) {
		try {
			return mapper.readTree(url);
		} catch (IOException e) {
			throw new RuntimeException("error on parsing JSON: " + url, e);
		}
	}

	public static String writeAsString(ObjectNode node) throws JsonProcessingException {
		return mapper.writeValueAsString(node);
	}

	public static void write(File file, ObjectNode node) throws IOException {
		mapper.writeValue(file, node);
	}
}
