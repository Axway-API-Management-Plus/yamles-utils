package com.axway.yamles.utils.spi;

public class SecretsProviderException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9032681363655032251L;
	private final String secretsProviderName;
	
	public SecretsProviderException(SecretsProvider sp, String msg) {
		super(msg);
		this.secretsProviderName = sp.getName();
	}
	
	public SecretsProviderException(SecretsProvider sp, String msg, Throwable cause) {
		super(msg, cause);
		this.secretsProviderName = sp.getName();
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[").append(this.secretsProviderName).append("] ");
		str.append(super.getMessage());
		return str.toString();
	}

	@Override
	public String getLocalizedMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[").append(this.secretsProviderName).append("] ");
		str.append(super.getLocalizedMessage());
		return str.toString();
	}

}
