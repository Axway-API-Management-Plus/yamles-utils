package com.axway.yamles.utils.merge.certs;

import java.util.Date;

public class CertificateExpiredException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7870925937480311012L;

	private final String alias;
	private final String dn;
	private final Date expirationDate;
	
	public CertificateExpiredException(String alias, String dn, Date expirationDate) {
		this.alias = alias;
		this.dn = dn;
		this.expirationDate = expirationDate;
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		str.append("certificate expired");
		str.append(": alias=").append(this.alias);
		str.append("; dn=").append(this.dn);
		str.append("; expire=").append(this.expirationDate.toString());
		return str.toString();
	}
}
