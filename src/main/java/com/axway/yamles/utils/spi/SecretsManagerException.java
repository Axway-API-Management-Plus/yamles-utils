package com.axway.yamles.utils.spi;

public class SecretsManagerException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842908289826322212L;

	public SecretsManagerException(String msg) {
		super(msg);
	}

	public SecretsManagerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
