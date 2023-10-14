package com.axway.yamles.utils.merge.config;

class FragmentSourceException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2312085919780990091L;
	private final String name;
	
	public FragmentSourceException(String name, String msg) {
		super(msg);
		this.name = name;
	}
	
	public FragmentSourceException(String name, Throwable cause) {
		super(cause);
		this.name = name;
	}
	
	public FragmentSourceException(FragmentSource source, Throwable cause) {
		super(cause);
		this.name = source.getName();
	}
	
	public FragmentSourceException(FragmentSource source, String msg) {
		super(msg);
		this.name = source.getName();
	}
	

	@Override
	public String getLocalizedMessage() {
		return appendSourceName(super.getLocalizedMessage());
	}

	@Override
	public String toString() {
		return appendSourceName(super.toString());
	}
	
	protected String appendSourceName(String msg) {
		StringBuilder str = new StringBuilder(msg);
		str.append(" (source: ").append(this.name).append(")");
		return str.toString();
	}
}
