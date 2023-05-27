package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Optional;

public class LookupProviderConfigException extends RuntimeException {

	private static final long serialVersionUID = 6416878660922754744L;

	private final String source;

	public LookupProviderConfigException(Optional<File> source, String msg) {
		this(source.isPresent() ? source.get() : null, msg);
	}

	public LookupProviderConfigException(File source, String msg) {
		this(source != null ? source.getAbsolutePath() : null, msg, null);
	}

	public LookupProviderConfigException(String source, String msg) {
		this(source, msg, null);
	}

	public LookupProviderConfigException(String source, String msg, Throwable cause) {
		super(msg, cause);
		this.source = (source != null) ? source : "<undefined>";
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[");
		str.append(this.source);
		str.append("] ");
		str.append(super.getMessage());
		return str.toString();
	}
}
