package com.axway.yamles.utils.merge.config;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Merger {

	private final ObjectNode target;
	private final ConfigSource source;
	private final YamlLocation currentPath = new YamlLocation();
	
	public Merger(ObjectNode target, ConfigSource source) {
		this.target = Objects.requireNonNull(target);
		this.source = Objects.requireNonNull(source);
	}
	
	public void merge() {
		merge(this.target, this.source.getConfig());
	}

	private void merge(ObjectNode targetNode, Entry<String, JsonNode> field) {
		this.currentPath.push(field.getKey());
		JsonNode targetField = targetNode.get(field.getKey());		
		if (targetField != null) {
			if (targetField.getNodeType() != field.getValue().getNodeType()) {
				throw new MergeException(this.source, "incompatible node types: " + this.currentPath);
			}
			if (targetField.isObject()) {
				merge((ObjectNode) targetField, (ObjectNode) field.getValue());
			} else {
				targetNode.set(field.getKey(), field.getValue());
			}
		} else {
			targetNode.set(field.getKey(), field.getValue());
		}
		this.currentPath.pop();
	}
	
	private void merge(ObjectNode targetNode, ObjectNode sourceNode) {
		Iterator<Entry<String, JsonNode>> fields = sourceNode.fields();
		while(fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			merge(targetNode, field);
		}
	}
}
