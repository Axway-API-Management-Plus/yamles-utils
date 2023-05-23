package com.axway.yamles.utils.merge.certs;

import java.security.cert.Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.spi.CertificateReplacement;
import com.axway.yamles.utils.spi.CertificateManager;
import com.axway.yamles.utils.spi.CertificateProvider;

class AliasSet {
	private static final Logger log = LogManager.getLogger(AliasSet.class);

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
			CertificateReplacement cert = cp.getCertificate(alias.getConfigSource(), alias.getName(),
					alias.getConfig());
			if (cert.isEmpty()) {
				project.removeCertificate(alias.getName());
				log.info("certificate removed: alias={}", alias.getName());
			} else {
				project.writeCertificate(alias.getName(), cert.getCert().get(), cert.getKey());
				log.info("certificate created: alias={}; config-source={}; provider={}", alias.getName(), alias.getConfigSource().getAbsolutePath(), alias.getProvider());
				
				if (!cert.getChain().isEmpty()) {
					int i = 0;
					for(Certificate c : cert.getChain()) {
						String chainAlias = alias.getName() + "_chain_" + i;
						project.writeCertificate(chainAlias, c, Optional.empty());
						log.info("chain certificate created: alias={}; config-source={}; provider={}", chainAlias, alias.getConfigSource().getAbsolutePath(), alias.getProvider());
						i++;
					}
				}
			}
		} catch (Exception e) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"certificate failed for alias '" + alias.getName() + "'", e);
		}
	}
}
