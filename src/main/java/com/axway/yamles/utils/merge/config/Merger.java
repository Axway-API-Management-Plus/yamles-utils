package com.axway.yamles.utils.merge.config;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.axway.yamles.utils.helper.YamlLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Merger {

	private final ObjectNode target;
	private final ConfigSource source;
	private final YamlLocation currentPath = new YamlLocation();
	private final FieldAudit audit;

	public Merger(FieldAudit audit, ObjectNode target, ConfigSource source) {
		this.audit = Objects.requireNonNull(audit);
		this.target = Objects.requireNonNull(target);
		this.source = Objects.requireNonNull(source);
	}

	public void merge() {
		this.currentPath.clear();
		mergeObject(this.target, this.source.getConfig());
	}

	private void mergeObject(ObjectNode targetNode, ObjectNode sourceNode) {
		Iterator<Entry<String, JsonNode>> fields = sourceNode.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();

			if (field.getValue().isObject()) {
				JsonNode targetField = targetNode.get(field.getKey());
				if (targetField == null) {
					targetField = targetNode.putObject(field.getKey());
				}
				if (!targetField.isObject()) {
					throw new MergeException(this.source,
							"target field is not an object: " + this.currentPath + "/" + field.getKey());
				}
				this.currentPath.push(field.getKey());
				mergeObject((ObjectNode) targetField, (ObjectNode) field.getValue());
				this.currentPath.pop();
			} else {
				mergeField(targetNode, field);
			}
		}
	}

	private void mergeField(ObjectNode targetNode, Entry<String, JsonNode> field) {
		this.currentPath.push(field.getKey());

		JsonNode fieldValue = field.getValue();
		if (fieldValue.isObject()) {
			throw new MergeException(this.source, "illegal state, field is of type 'object'");
		}

		JsonNode targetField = targetNode.get(field.getKey());
		if (targetField != null && targetField.getNodeType() != fieldValue.getNodeType()) {
			throw new MergeException(this.source, "incompatible node types: " + this.currentPath);
		}

		targetNode.set(field.getKey(), field.getValue());
		this.audit.put(this.currentPath, this.source);

		this.currentPath.pop();
	}
}
