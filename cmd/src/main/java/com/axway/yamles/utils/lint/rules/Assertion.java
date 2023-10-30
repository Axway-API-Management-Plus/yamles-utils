package com.axway.yamles.utils.lint.rules;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class Assertion {
	public static enum AssertionType {
		environmentalized, exists, not_exists, regex;
	}

	static {
		// configure JSON Path library for using Jackson
		Configuration.setDefaults(new Configuration.Defaults() {
			private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				Set<Option> options = new HashSet<>();
				options.add(Option.SUPPRESS_EXCEPTIONS);
				return options;
			}
		});
	}

	private final JsonPath path;
	private final AssertionType type;
	private final String message;
	private final String param;

	@JsonCreator
	public Assertion(@JsonProperty("path") String path, @JsonProperty("type") AssertionType type, @JsonProperty("param") String param,
			@JsonProperty("message") String message) {
		this.path = JsonPath.compile(Objects.requireNonNull(path, "path requried"));
		this.type = Objects.requireNonNull(type, "assertion type required");
		this.message = message;
		this.param = param;
		
		if (this.type == AssertionType.regex && (this.param == null || this.param.isEmpty())) {
			throw new IllegalArgumentException(AssertionType.regex.name() + " assertion requires 'param' property");
		}
	}

	public JsonNode path(ObjectNode yaml) {
		return this.path.read(yaml);
	}

	public AssertionType getAssertion() {
		return this.type;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean check(ObjectNode yaml) {
		JsonNode result = path(yaml);

		switch (this.type) {
		case environmentalized:
			if (result == null)
				return false;
			if (result.isArray()) {
				for (JsonNode node : (ArrayNode) result) {
					if (!node.isTextual() || !node.asText().startsWith("{{")) {
						return false;
					}
				}
			} else if (!result.asText().startsWith("{{")) {
				return false;
			}

			break;

		case exists:
			if (result == null) {
				return false;
			} else if (result.isArray()) {
				if (((ArrayNode) result).size() == 0) {
					return false;
				}
			}
			break;

		case not_exists:
			if (result != null) {
				if (result.isArray()) {
					if (((ArrayNode) result).size() > 0) {
						return false;
					}
				} else {
					return false;
				}
			}
			break;
		case regex:
			if (result == null || !result.isValueNode()) {
				return false;
			}
			String value = result.asText();
			if (!value.matches(this.param)) {
				return false;
			}
			break;
		default:
			new IllegalStateException("unsupported assertion: " + this.type);
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("assert ").append(this.path.getPath()).append(": ").append(this.type);
		if (this.message != null) {
			str.append(" (").append(this.message).append(")");
		}
		return str.toString();
	}
}
