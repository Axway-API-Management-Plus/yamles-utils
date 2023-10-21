package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.helper.NodeLocation;
import com.axway.yamles.utils.helper.ValueNodeSet;
import com.axway.yamles.utils.helper.Yaml;
import com.axway.yamles.utils.plugins.Evaluator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class YamlEsConfig {
	private static final Logger log = LogManager.getLogger(YamlEsConfig.class);
	private ObjectNode config = Yaml.createObjectNode();
	private final FieldAudit audit = new FieldAudit();

	public YamlEsConfig() {
	}

	public void merge(List<FragmentSource> sources) throws MergeException {
		this.audit.clear();

		Audit.AUDIT_LOG.info(Audit.SUB_HEADER_PREFIX + "Merge Fragement Sources");
		sources.forEach((cs) -> merge(audit, cs));

		this.audit.writeSummaryToAudit();
	}

	public boolean allFieldsConfigured(Optional<ValueNodeSet> required) {
		boolean allConfigured = true;

		if (!Objects.requireNonNull(required).isPresent())
			return allConfigured;

		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Check Required Fields");

		ValueNodeSet req = required.get();

		List<NodeLocation> values = getUnusedValues(req);
		for (NodeLocation value : values) {
			Audit.AUDIT_LOG.warn("unused field: {}", value);
		}

		values = getMissingValues(req);
		for (NodeLocation value : values) {
			Audit.AUDIT_LOG.error("missing field: {}", value);
			allConfigured = false;
		}

		return allConfigured;
	}

	public void evalValues() {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Evaluate Values");
		evalValues(NodeLocation.root(), this.config);
	}

	private List<NodeLocation> getMissingValues(ValueNodeSet required) {
		ValueNodeSet configValues = new ValueNodeSet(this.config);
		return configValues.detectMissing(Objects.requireNonNull(required));
	}

	private List<NodeLocation> getUnusedValues(ValueNodeSet required) {
		ValueNodeSet configValues = new ValueNodeSet(this.config);
		return Objects.requireNonNull(required).detectMissing(configValues);
	}

	protected void merge(FieldAudit audit, FragmentSource cs) throws MergeException {
		log.info("merge configuration: {}", cs.getName());
		new Merger(this.audit, this.config, cs).merge();
	}

	protected void evalValues(NodeLocation objectLocation, ObjectNode node) {
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			NodeLocation fieldLocation = objectLocation.child(field.getKey());
			if (value.isObject()) {
				evalValues(fieldLocation, (ObjectNode) value);
			} else if (value.isArray()) {
				evalValues(fieldLocation, (ArrayNode) value);
			} else if (value.isTextual()) {
				String v = value.asText();
				Audit.AUDIT_LOG.info("evaluate field: {}", fieldLocation);
				v = Evaluator.eval(v);
				node.put(field.getKey(), v);
			}
		}
	}

	protected void evalValues(NodeLocation arrayLocation, ArrayNode node) {
		for (int i = 0; i < node.size(); i++) {
			JsonNode value = node.get(i);
			NodeLocation fieldLocation = arrayLocation.child("[" + i + "]");
			if (value.isObject()) {
				evalValues(fieldLocation.child("[" + i + "]"), (ObjectNode) value);
			} else if (value.isArray()) {
				evalValues(fieldLocation.child("[" + i + "]"), (ArrayNode) value);
			} else if (value.isTextual()) {
				String v = value.asText();
				Audit.AUDIT_LOG.info("evaluate field: {}", fieldLocation);
				v = Evaluator.eval(v);
				node.set(i, v);
			}
		}
	}

	public ObjectNode getConfig() {
		return this.config;
	}

	@Override
	public String toString() {
		return this.config.toString();
	}

	public String toYaml() throws Exception {
		return Yaml.writeAsString(this.config);
	}

	public void write(File file) throws IOException {
		Yaml.write(file, this.config);
	}
}
