package com.axway.yamles.utils.spi;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.Objects;
import java.util.Optional;

public class CertificateReplacement {

	private final String alias;
	private final Optional<Certificate> cert;
	private final Optional<Key> key;

	public CertificateReplacement(String alias) {
		this.alias = Objects.requireNonNull(alias, "alias must not be null");
		this.cert = Optional.empty();
		this.key = Optional.empty();

	}

	public CertificateReplacement(String alias, Certificate cert) {
		this(alias, cert, null);
	}

	public CertificateReplacement(String alias, Certificate cert, Key key) {
		this.alias = Objects.requireNonNull(alias, "alias must not be null");
		this.cert = Optional.of(Objects.requireNonNull(cert, "certificate must not be null"));
		this.key = (key != null) ? Optional.of(key) : Optional.empty();
	}

	public String getAlias() {
		return this.alias;
	}

	public boolean isEmpty() {
		return !this.cert.isPresent();
	}

	public Optional<Certificate> getCert() {
		return this.cert;
	}

	public Optional<Key> getKey() {
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
		CertificateReplacement other = (CertificateReplacement) obj;
		return Objects.equals(alias, other.alias);
	}
}
