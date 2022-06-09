package com.axway.yamles.utils.spi;

public class CertificateProviderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5701448841137253156L;

	public CertificateProviderException(String msg) {
		super(msg);
	}

	public CertificateProviderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
