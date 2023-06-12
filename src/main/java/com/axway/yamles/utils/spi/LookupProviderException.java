package com.axway.yamles.utils.spi;

public class LookupProviderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9032681363655032251L;
	private final String secretsProviderName;

	public LookupProviderException(LookupProvider sp, String msg) {
		this(sp.getName(), msg);
	}

	public LookupProviderException(LookupProvider sp, String msg, Throwable cause) {
		this(sp.getName(), msg, cause);
	}

	public LookupProviderException(String name, String msg) {
		this(name, msg, null);
	}

	public LookupProviderException(String name, String msg, Throwable cause) {
		super(msg, cause);
		this.secretsProviderName = name;
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[").append(this.secretsProviderName).append("] ");
		str.append(super.getMessage());
		return str.toString();
	}
}
