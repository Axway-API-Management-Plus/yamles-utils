package com.axway.yamles.utils.merge.certs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.YamlEs;
import com.axway.yamles.utils.spi.Cert;
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
			Cert cert = cp.getCertificate(alias.getConfigSource(), alias.getName(), alias.getConfig());
			project.writeCertificate(alias.getName(), cert.getCert(), cert.getKey());
			
			log.info("write certificate (alias={}; config-source={}; provider={})", alias.getName(), alias.getConfigSource().getAbsolutePath(), alias.getProvider());
		} catch (Exception e) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"certificate failed for alias '" + alias.getName() + "'", e);
		}
	}
}
