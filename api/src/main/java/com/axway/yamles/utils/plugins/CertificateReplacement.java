package com.axway.yamles.utils.plugins;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Replacement certificate and private key for a target certificate in the
 * YAML-ES.
 */
public class CertificateReplacement {

	/**
	 * Alias name of the certificate in the certificate source.
	 * 
	 * <p>
	 * If empty, alias is not supported by certificate source.
	 * </p>
	 */
	private final Optional<String> alias;

	/**
	 * Replacement certificate.
	 * 
	 * <p>
	 * If empty, target certificate will be replaced by nothing (removed).
	 * </p>
	 */
	private final Optional<Certificate> cert;

	/**
	 * Replacement private key.
	 * 
	 * <p>
	 * If empty, target private key will be replaced by nothing (removed).
	 * </p>
	 */
	private final Optional<Key> key;

	/**
	 * List of according chain certificates.
	 */
	private final List<Certificate> chain = new ArrayList<>();

	/**
	 * Creates an empty certificate replacement.
	 * 
	 * <p>
	 * Empty certificate replacement is used to delete the a certificate from the
	 * YAML-ES certificate store (replace existing certificate by nothing).
	 * </p>
	 */
	public CertificateReplacement() {
		this.alias = Optional.empty();
		this.cert = Optional.empty();
		this.key = Optional.empty();

	}

	public CertificateReplacement(Optional<String> alias, Certificate cert) {
		this(alias, cert, null);
	}

	public CertificateReplacement(Optional<String> alias, Certificate cert, Key key) {
		this.alias = Objects.requireNonNull(alias, "alias must not be null");
		this.cert = Optional.of(Objects.requireNonNull(cert, "certificate must not be null"));
		this.key = (key != null) ? Optional.of(key) : Optional.empty();
	}

	public Optional<String> getAlias() {
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

	public void addChain(Certificate cert) {
		this.chain.add(cert);
	}

	public void addChain(Collection<? extends Certificate> certs) {
		this.chain.addAll(certs);
	}

	/**
	 * Returns the list of associated certificate authorities.
	 * 
	 * <p>
	 * The chain is ordered with the root certificate at the last position.
	 * </p>
	 * 
	 * @return certificate chain
	 */
	public List<Certificate> getChain() {
		return this.chain;
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
