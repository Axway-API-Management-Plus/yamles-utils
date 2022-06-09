package com.axway.yamles.utils.spi;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.Objects;

public class Cert {

	public static enum Type {
		PEM, P12
	}

	private final String alias;
	private final Certificate cert;
	private final Key key;

	public Cert(String alias, Certificate cert) {
		this(alias, cert, null);
	}

	public Cert(String alias, Certificate cert, Key key) {
		this.alias = Objects.requireNonNull(alias, "alias must not be null");
		this.cert = Objects.requireNonNull(cert, "certificate must not be null");
		this.key = key;
	}

	public String getAlias() {
		return this.alias;
	}

	public Type getType() {
		return (this.key != null) ? Type.P12 : Type.PEM;
	}

	public Certificate getCert() {
		return this.cert;
	}

	public Key getKey() {
		return this.key;
	}

	@Override
	public int hashCode() {
		return Objects.hash(alias);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cert other = (Cert) obj;
		return Objects.equals(alias, other.alias);
	}
}
