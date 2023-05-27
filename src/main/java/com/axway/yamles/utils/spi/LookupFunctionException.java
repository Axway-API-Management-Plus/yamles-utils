package com.axway.yamles.utils.spi;

public class LookupFunctionException extends RuntimeException {
	
	private static final long serialVersionUID = -2625821418570560638L;

	private final LookupFunction func;
	
	public LookupFunctionException(LookupFunction func, String msg) {
		this(func, msg, null);
	}
	
	public LookupFunctionException(LookupFunction func, String msg, Throwable cause) {
		super(msg, cause);
		this.func = func;
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("[").append(this.func.getName()).append("] ");
		str.append(super.getMessage());
		return str.toString();
	}
}
