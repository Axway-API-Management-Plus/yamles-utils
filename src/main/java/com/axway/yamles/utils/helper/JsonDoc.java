package com.axway.yamles.utils.helper;

import java.io.File;
import java.util.Objects;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonDoc {

	private final String name;
	private final JsonNode doc;

	public JsonDoc(File file) throws JacksonException {
		this(Objects.requireNonNull(file).getAbsolutePath(), Json.load(file));
	}

	public JsonDoc(String name, String json) throws JacksonException {
		this(name, Json.read(json));
	}

	public JsonDoc(String name, JsonNode doc) throws JacksonException {
		this.name = Objects.requireNonNull(name);
		this.doc = Objects.requireNonNull(doc);
	}

	public String getName() {
		return this.name;
	}

	public JsonNode at(String key) {
		return this.doc.at(key);
	}
}
