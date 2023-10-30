package com.axway.yamles.utils.lint.rules;

public class RulesException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7570607566950225448L;

	public RulesException(String msg) {
		super(msg);
	}
	
	public RulesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
