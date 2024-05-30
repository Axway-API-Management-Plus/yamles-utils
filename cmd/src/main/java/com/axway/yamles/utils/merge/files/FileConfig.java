package com.axway.yamles.utils.merge.files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileConfig {
	public static final String ENCODING_BINARY = "binary";

	private File configSource = null;
	private final String path;
	private final Charset encoding;
	private final String content;
	private final File template;

	@JsonCreator
	public FileConfig(@JsonProperty("path") String path, @JsonProperty("encoding") String encoding,
			@JsonProperty("content") String content, @JsonProperty("template") String template) {
		if (path == null || path.isEmpty())
			throw new IllegalArgumentException("non-empty 'path' property required");
		this.path = path;
		if (encoding == null || encoding.isEmpty())
			throw new IllegalArgumentException("non-empty 'encoding' property required");
		this.encoding = ENCODING_BINARY.equals(encoding) ? null : Charset.forName(encoding);
		if (content == null && template == null)
			throw new IllegalArgumentException("'content' or 'template' property required");
		if (content != null && template != null)
			throw new IllegalArgumentException("'content' and 'template' property are mutual exclusive");
		this.content = content;
		this.template = (template != null) ? new File(template) : null;
	}

	void setConfigSource(File source) {
		this.configSource = Objects.requireNonNull(source);
	}

	public File getConfigSource() {
		return this.configSource;
	}

	public String getPath() {
		return this.path;
	}

	public Optional<Charset> getEncoding() {
		return Optional.ofNullable(this.encoding);
	}

	public String getEncodingName() {
		return (this.encoding == null) ? ENCODING_BINARY : this.encoding.name();
	}

	public String getContent() {
		return this.content;
	}
	
	public File getTemplate() {
		return this.template;
	}
}
