package com.axway.yamles.utils.es;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ValueNodeSet {

	private SortedSet<NodeLocation> valueNodes = new TreeSet<>();

	public ValueNodeSet(JsonNode rootNode) {
		if (rootNode != null) {
			if (!rootNode.isObject())
				throw new IllegalArgumentException("root node is not of type object");
			NodeLocation location = NodeLocation.root();
			scan(location, (ObjectNode) rootNode);
		}
	}

	public boolean isEmpty() {
		return this.valueNodes.isEmpty();
	}

	public Set<NodeLocation> getValueNodes() {
		return Collections.unmodifiableSet(this.valueNodes);
	}

	public List<NodeLocation> detectMissing(ValueNodeSet required) {
		List<NodeLocation> missing = new ArrayList<>();

		if (required == null || required.valueNodes.isEmpty()) {
			return missing;
		}

		required.valueNodes.forEach((requiredValue) -> {
			if (!this.valueNodes.contains(requiredValue))
				missing.add(requiredValue);
		});

		return missing;
	}

	private void scan(NodeLocation location, ObjectNode node) {
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();

			NodeLocation fieldLocation = location.child(field.getKey());
			if (field.getValue().isObject()) {
				scan(fieldLocation, (ObjectNode) field.getValue());
			} else if (field.getValue().isArray() || field.getValue().isValueNode()) {
				this.valueNodes.add(fieldLocation);
			}
		}
	}
}
