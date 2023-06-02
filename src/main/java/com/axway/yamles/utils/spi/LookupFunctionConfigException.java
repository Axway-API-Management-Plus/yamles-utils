package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Optional;

public class LookupFunctionConfigException extends RuntimeException {

	private static final long serialVersionUID = 6416878660922754744L;

	private final String source;

	public LookupFunctionConfigException(Optional<File> source, String msg) {
		this(source.isPresent() ? source.get() : null, msg);
	}

	public LookupFunctionConfigException(File source, String msg) {
		this(source != null ? source.getAbsolutePath() : null, msg, null);
	}

	public LookupFunctionConfigException(String source, String msg) {
		this(source, msg, null);
	}

	public LookupFunctionConfigException(String source, String msg, Throwable cause) {
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
