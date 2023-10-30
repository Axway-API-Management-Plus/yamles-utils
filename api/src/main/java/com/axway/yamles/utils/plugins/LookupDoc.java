package com.axway.yamles.utils.plugins;

import java.io.File;
import java.util.Objects;

import com.axway.yamles.utils.helper.Json;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;

public class LookupDoc {

	private final JsonNode doc;
	private final String sourceID;

	public static LookupDoc fromJsonFile(File file) throws JacksonException {
		JsonNode node = Json.load(file);
		return new LookupDoc(node, file.getAbsolutePath());
	}

	public static LookupDoc fromYamlFile(File file) throws JacksonException {
		JsonNode node = Yaml.load(file);
		return new LookupDoc(node, file.getAbsolutePath());
	}

	public static LookupDoc fromJsonString(String json, String sourceID) throws JacksonException {
		JsonNode node = Json.read(json);
		return new LookupDoc(node, null);
	}

	public static LookupDoc fromJsonString(String json) throws JacksonException {
		JsonNode node = Json.read(json);
		return new LookupDoc(node, null);
	}

	private LookupDoc(JsonNode doc, String sourceID) throws JacksonException {
		this.doc = Objects.requireNonNull(doc);
		this.sourceID = sourceID != null ? sourceID : "<undefined>";
	}

	public String getSourceID() {
		return this.sourceID;
	}

	public JsonNode at(String key) {
		return this.doc.at(key);
	}
}
