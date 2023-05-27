package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Objects;

import com.axway.yamles.utils.helper.Json;
import com.axway.yamles.utils.helper.Yaml;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;

public class LookupDoc {

	private final String alias;
	private final JsonNode doc;
	private final String sourceID;

	public static LookupDoc fromJsonFile(String alias, File file) throws JacksonException {
		JsonNode node = Json.load(file);
		return new LookupDoc(alias, node, file.getAbsolutePath());
	}

	public static LookupDoc fromYamlFile(String alias, File file) throws JacksonException {
		JsonNode node = Yaml.load(file);
		return new LookupDoc(alias, node, file.getAbsolutePath());
	}

	public static LookupDoc fromJsonString(String alias, String json, String sourceID) throws JacksonException {
		JsonNode node = Json.read(json);
		return new LookupDoc(alias, node, null);
	}

	public static LookupDoc fromJsonString(String alias, String json) throws JacksonException {
		JsonNode node = Json.read(json);
		return new LookupDoc(alias, node, null);
	}

	private LookupDoc(String alias, JsonNode doc, String sourceID) throws JacksonException {
		this.alias = Objects.requireNonNull(alias);
		this.doc = Objects.requireNonNull(doc);
		this.sourceID = sourceID != null ? sourceID : "<undefined>";
	}

	public String getAlias() {
		return this.alias;
	}

	public String getSourceID() {
		return this.sourceID;
	}

	public JsonNode at(String key) {
		return this.doc.at(key);
	}
}
