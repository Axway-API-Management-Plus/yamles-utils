package com.axway.yamles.utils.merge.certs;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.helper.Audit;
import com.axway.yamles.utils.merge.CertificateManager;
import com.axway.yamles.utils.plugins.CertificateProvider;
import com.axway.yamles.utils.plugins.CertificateReplacement;

class AliasSet {
	private final Set<Alias> aliases = new HashSet<>();

	public AliasSet() {
	}

	public void clear() {
		this.aliases.clear();
	}

	public void addOrReplace(Collection<Alias> ca) {
		if (aliases == null)
			return;
		ca.forEach((a) -> {
			if (this.aliases.contains(a)) {
				this.aliases.remove(a);
			}
			this.aliases.add(a);
		});
	}

	public Iterator<Alias> getAliases() {
		return this.aliases.iterator();
	}

	public void writeAliases(YamlEs project) {
		if (project == null)
			throw new IllegalArgumentException("project is null");

		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Write Certificates");

		this.aliases.forEach(a -> {
			writeAlias(project, a);
		});
	}

	private void writeAlias(YamlEs project, Alias alias) {
		CertificateManager cm = CertificateManager.getInstance();

		CertificateProvider cp = cm.getProvider(alias.getProvider());
		if (cp == null) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"unsupported certificate provider for alias '" + alias.getName() + "': " + alias.getProvider());
		}

		try {
			Audit.AUDIT_LOG.info("process alias: alias={}; config-source={}; provider={}", alias.getName(),
					alias.getConfigSource().getAbsolutePath(), alias.getProvider());
			List<CertificateReplacement> crs = cp.getCertificate(alias.getConfigSource(), alias.getName(),
					alias.getConfig());

			for (CertificateReplacement cr : crs) {
				writeCertificate(project, alias.getName(), cr);
			}
		} catch (Exception e) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"certificate failed for alias '" + alias.getName() + "'", e);
		}
	}

	private void writeCertificate(YamlEs project, String aliasName, CertificateReplacement cert)
			throws IOException, CertificateEncodingException {
		if (cert.isEmpty()) {
			project.removeCertificate(aliasName);
			Audit.AUDIT_LOG.info("  certificate removed: alias={}", aliasName);
		} else {
			auditCertificate(aliasName, cert.getCert().get());
			project.writeCertificate(aliasName, cert.getCert().get(), cert.getKey());
			Audit.AUDIT_LOG.info("  certificate created: alias={}", aliasName);

			if (!cert.getChain().isEmpty()) {
				int i = 0;
				for (Certificate c : cert.getChain()) {
					String chainAlias = aliasName + "_chain_" + i;
					auditCertificate(chainAlias, c);
					project.writeCertificate(chainAlias, c, Optional.empty());
					Audit.AUDIT_LOG.info("  chain certificate created: alias={}", chainAlias);
					i++;
				}
			}
		}
	}

	private void auditCertificate(String alias, Certificate cert) {
		if (cert instanceof X509Certificate) {
			X509Certificate x509 = (X509Certificate) cert;
			Audit.AUDIT_LOG.info("  X509: alias={}; dn={}; exp={}", alias, x509.getSubjectX500Principal().getName(),
					x509.getNotAfter());
		}
	}
}
