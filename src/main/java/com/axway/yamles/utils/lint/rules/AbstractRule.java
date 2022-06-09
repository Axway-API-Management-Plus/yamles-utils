package com.axway.yamles.utils.lint.rules;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract class AbstractRule {

	private String id;
	private final String name;
	private final String description;

	public AbstractRule(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@JsonIgnore
	public String getId() {
		return this.id;
	}

	void setId(String id) {
		this.id = Objects.requireNonNull(id);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
