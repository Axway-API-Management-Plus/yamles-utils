package com.axway.yamles.utils.merge.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FieldAudit {
	private static final Logger log = LogManager.getLogger(FieldAudit.class);

	private static final Field[] EMPTY = new Field[0];

	public static class Field {
		private final String path;
		private String source = "";

		public Field(final String path) {
			this.path = Objects.requireNonNull(path, "missing field path");
		}

		public void setSource(String source) {
			this.source = Objects.requireNonNull(source, "missing field source");
		}

		public String getPath() {
			return this.path;
		}

		public String getSource() {
			return this.source;
		}

		@Override
		public int hashCode() {
			return Objects.hash(path);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			Field other = (Field) obj;
			return Objects.equals(path, other.path);
		}

		@Override
		public String toString() {
			return this.path + " from " + this.source;
		}
	}

	private final SortedMap<String, Field> fieldAudit = new TreeMap<>();

	public FieldAudit() {
	}

	protected void clear() {
		this.fieldAudit.clear();
	}

	protected void put(YamlLocation location, ConfigSource source) {
		String path = Objects.requireNonNull(location, "YAML location is missing").toString();
		String sourceName = Objects.requireNonNull(source, "source is missing").getName();

		Field field = this.fieldAudit.get(path);
		if (field == null) {
			field = new Field(path);
			this.fieldAudit.put(path, field);
		}
		field.setSource(sourceName);
	}

	public Field[] getFields() {
		return this.fieldAudit.values().toArray(EMPTY);
	}

	public void write(Writer out) throws IOException {
		out.write("FIELD\tSOURCE");
		out.write(System.lineSeparator());
		for (Field f : this.fieldAudit.values()) {
			out.write(f.getPath());
			out.write("\t");
			out.write(f.getSource());
			out.write(System.lineSeparator());
		}
	}

	public void write(File file) throws IOException {
		try (Writer out = new FileWriter(Objects.requireNonNull(file, "audit file is null"))) {
			write(out);
		} catch (IOException e) {
			throw new IOException("error writing field audit file: " + file.getAbsolutePath(), e);
		}
		log.info("Field audit written to {}", file.getAbsolutePath());
	}

	@Override
	public String toString() {
		try {
			StringWriter out = new StringWriter();
			write(out);
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
