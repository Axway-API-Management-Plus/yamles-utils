package com.axway.yamles.utils.merge.config;

class MergeException extends ConfigSourceException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4269634855838975965L;

	public MergeException(ConfigSource source, String msg) {
		super(source, msg);
	}

}
