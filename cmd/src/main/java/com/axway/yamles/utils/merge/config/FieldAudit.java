package com.axway.yamles.utils.merge.config;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.es.NodeLocation;

public class FieldAudit {
	private static final Field[] EMPTY = new Field[0];

	public static class Field {
		private final NodeLocation location;
		private String source = "";

		public Field(final NodeLocation location) {
			this.location = Objects.requireNonNull(location, "missing field location");
		}

		public void setSource(String source) {
			this.source = Objects.requireNonNull(source, "missing field source");
		}

		public NodeLocation getLocation() {
			return this.location;
		}

		public String getSource() {
			return this.source;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.location);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			Field other = (Field) obj;
			return Objects.equals(this.location, other.location);
		}

		@Override
		public String toString() {
			return this.location + " from " + this.source;
		}
	}

	private final SortedMap<NodeLocation, Field> fieldAudit = new TreeMap<>();

	public FieldAudit() {
	}

	protected void clear() {
		this.fieldAudit.clear();
	}

	protected void put(NodeLocation location, FragmentSource source) {
		Objects.requireNonNull(location, "YAML location is missing");
		String sourceName = Objects.requireNonNull(source, "source is missing").getName();

		Field field = this.fieldAudit.get(location);
		if (field == null) {
			field = new Field(location);
			this.fieldAudit.put(field.getLocation(), field);
		}
		field.setSource(sourceName);
	}

	public Field[] getFields() {
		return this.fieldAudit.values().toArray(EMPTY);
	}

	public void writeSummaryToAudit() {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Environmentalized Fields (Summary)");
		for (Field f : this.fieldAudit.values()) {
			Audit.AUDIT_LOG.info("{} (source={})", f.getLocation(), f.getSource());
		}
	}
}
