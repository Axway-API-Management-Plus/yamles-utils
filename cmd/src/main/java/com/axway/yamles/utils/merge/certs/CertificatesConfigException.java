package com.axway.yamles.utils.merge.certs;

import java.io.File;

class CertificatesConfigException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8928855347550613957L;

	private final File source;

	public CertificatesConfigException(File source, String msg) {
		super(msg);
		this.source = source;
	}

	public CertificatesConfigException(File source, String msg, Throwable cause) {
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
