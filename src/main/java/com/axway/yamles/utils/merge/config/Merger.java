package com.axway.yamles.utils.merge.config;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.axway.yamles.utils.helper.NodeLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Merger {

	private final ObjectNode target;
	private final FragmentSource source;
	private final FieldAudit audit;

	public Merger(FieldAudit audit, ObjectNode target, FragmentSource source) {
		this.audit = Objects.requireNonNull(audit);
		this.target = Objects.requireNonNull(target);
		this.source = Objects.requireNonNull(source);
	}

	public void merge() {
		mergeObject(NodeLocation.root(), this.target, this.source.getConfig());
	}

	private void mergeObject(NodeLocation currentPath, ObjectNode targetNode, ObjectNode sourceNode) {
		Iterator<Entry<String, JsonNode>> fields = sourceNode.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();

			if (field.getValue().isObject()) {
				NodeLocation targetPath = currentPath.child(field.getKey());
				JsonNode targetField = targetNode.get(field.getKey());
				if (targetField == null) {
					targetField = targetNode.putObject(field.getKey());
				}
				if (!targetField.isObject()) {
					throw new MergeException(this.source, "target field is not an object: " + targetPath);
				}
				mergeObject(targetPath, (ObjectNode) targetField, (ObjectNode) field.getValue());
			} else {
				mergeField(currentPath, targetNode, field);
			}
		}
	}

	private void mergeField(NodeLocation targetPath, ObjectNode targetNode, Entry<String, JsonNode> field) {
		NodeLocation currentPath = targetPath.child(field.getKey());

		JsonNode fieldValue = field.getValue();
		if (fieldValue.isObject()) {
			throw new MergeException(this.source, "illegal state, field is of type 'object'");
		}

		JsonNode targetField = targetNode.get(field.getKey());
		if (targetField != null && targetField.getNodeType() != fieldValue.getNodeType()) {
			throw new MergeException(this.source, "incompatible node types: " + currentPath);
		}

		targetNode.set(field.getKey(), field.getValue());
		this.audit.put(currentPath, this.source);
	}
}
