package com.axway.yamles.utils.merge.files;

import java.io.File;

public class FilesConfigException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8370110304030786679L;
	private final File source;

	public FilesConfigException(File source, String msg) {
		super(msg);
		this.source = source;
	}

	public FilesConfigException(File source, Throwable cause) {
		super(cause);
		this.source = source;
	}

	public FilesConfigException(File source, String msg, Throwable cause) {
		super(msg, cause);
		this.source = source;
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[").append(this.source.getAbsolutePath()).append("] ");
		str.append(super.getMessage());
		return str.toString();
	}
}
