package com.axway.yamles.utils.merge.certs;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.audit.Audit;
import com.axway.yamles.utils.es.YamlEs;
import com.axway.yamles.utils.merge.ProviderManager;
import com.axway.yamles.utils.plugins.CertificateProvider;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class CertificatesConfigurator {
	private static final Logger log = LogManager.getLogger(CertificatesConfigurator.class);

	public static final int EXPIRATION_CHECK_DISABLED = -1;

	public static final int DEFAULT_EXP_WARNING_DAYS = 30;
	public static final int DEFAULT_EXP_ERROR_DAYS = 10;
	public static final int DEFAULT_EXP_FAIL_DAYS = EXPIRATION_CHECK_DISABLED;

	private final ExecutionMode mode;

	private final Optional<Calendar> expirationWarning;
	private final Optional<Calendar> expirationError;
	private final Optional<Calendar> expirationFail;

	private final Set<Alias> aliases = new HashSet<>();

	private static Optional<Calendar> calculateExpiration(int daysBefore) {
		Optional<Calendar> calendar = Optional.empty();
		if (daysBefore > EXPIRATION_CHECK_DISABLED) {
			calendar = Optional.of(Calendar.getInstance());
			if (daysBefore > 0) {
				calendar.get().add(Calendar.DATE, daysBefore);
			}
		}
		return calendar;
	}

	public CertificatesConfigurator(ExecutionMode mode, int expirationWarningDays, int expirationErrorDays,
			int expirationFailDays) {
		this.mode = Objects.requireNonNull(mode, "configuration mode required");
		this.expirationWarning = calculateExpiration(expirationWarningDays);
		this.expirationError = calculateExpiration(expirationErrorDays);
		this.expirationFail = calculateExpiration(expirationFailDays);
	}

	public void setCertificateConfigs(List<File> configFiles) {
		this.aliases.clear();
		if (configFiles == null)
			return;

		configFiles.forEach(f -> {
			loadAliases(f);
		});
	}

	public void apply(YamlEs es) {
		Audit.AUDIT_LOG.info(Audit.HEADER_PREFIX + "Certificate Configuration");
		writeAuditToLog();
		writeAliases(es);
	}

	private void loadAliases(File file) {
		log.info("load certificate config: {}", file.getAbsoluteFile());
		CertificatesConfig cc = CertificatesConfig.loadConfig(file);
		addOrReplace(cc.getAliases().values());
	}

	private void writeAuditToLog() {
		Audit.AUDIT_LOG.info(Audit.SUB_HEADER_PREFIX + "Defined Certificate Aliases");
		this.aliases.forEach(alias -> {
			Audit.AUDIT_LOG.info("{} (provider={}; source={})", alias.getName(), alias.getProvider(),
					alias.getConfigSource().getAbsolutePath());
		});
	}

	private void addOrReplace(Collection<Alias> ca) {
		if (aliases == null)
			return;
		ca.forEach((a) -> {
			if (this.aliases.contains(a)) {
				this.aliases.remove(a);
			}
			this.aliases.add(a);
		});
	}

	private void writeAliases(YamlEs project) {
		if (project == null)
			throw new IllegalArgumentException("project is null");

		Audit.AUDIT_LOG.info(Audit.SUB_HEADER_PREFIX + "Process Certificates");

		this.aliases.forEach(a -> {
			writeAlias(project, a);
		});
	}

	private void writeAlias(YamlEs project, Alias alias) {
		ProviderManager pm = ProviderManager.getInstance();

		CertificateProvider cp = pm.getCertificateProvider(alias.getProvider());
		if (cp == null) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"unsupported certificate provider for alias '" + alias.getName() + "': " + alias.getProvider());
		}

		try {
			Audit.AUDIT_LOG.info("process alias: alias={}; config-source={}; provider={}", alias.getName(),
					alias.getConfigSource().getAbsolutePath(), alias.getProvider());
			List<CertificateReplacement> crs = cp.getCertificates(alias.getConfigSource(), alias.getName(),
					alias.getConfig());

			if (crs.size() == 1) {
				writeCertificate(project, alias.getName(), crs.get(0));
			} else {
				int i = 0;
				for (CertificateReplacement cr : crs) {
					writeCertificate(project, alias.getName() + "_" + i, cr);
					i++;
				}
			}
		} catch (Exception e) {
			throw new CertificatesConfigException(alias.getConfigSource(),
					"certificate failed for alias '" + alias.getName() + "'", e);
		}
	}

	/**
	 * Write certificate replacement to the YAML-ES.
	 * 
	 * <p>
	 * If the certificate replacement is empty, an existing certificate with the
	 * same alias will be removed.
	 * </p>
	 * <p>
	 * If the a certificate with the given alias already exists, the certificate and
	 * key will be replaced by the provided certificate replacement. If the alias
	 * doesn't exists, a new certificate is added,
	 * </p>
	 * <p>
	 * If the certificate replacement contains a certificate chain, aliases for
	 * chain certificates are added. The name of the alias is based on the provided
	 * alias name attached with a <code>_chain_<i>index</i></code>. The root of the
	 * chain has the index 0.
	 * </p>
	 * 
	 * @param project   YAML Entity Store
	 * @param aliasName alias of certificate in YAML-ES
	 * @param cert      certificate replacement
	 * 
	 * @throws IOException
	 * @throws CertificateEncodingException
	 */
	private void writeCertificate(YamlEs project, String aliasName, CertificateReplacement cert)
			throws IOException, CertificateEncodingException {
		if (cert.isEmpty()) {
			project.removeCertificate(aliasName);
			Audit.AUDIT_LOG.info("  certificate removed: alias={}", aliasName);
		} else {
			auditCertificate(aliasName, cert.getCert().get());
			if (this.mode == ExecutionMode.CONFIG) {
				project.writeCertificate(aliasName, cert.getCert().get(), cert.getKey());
				Audit.AUDIT_LOG.info("  certificate{} created: alias={}", (cert.getKey().isPresent() ? " and key" : ""),
						aliasName);
			} else {
				Audit.AUDIT_LOG.info("  certificate{} skipped (due to execution mode {}): alias={}", (cert.getKey().isPresent() ? " and key" : ""), this.mode,
						aliasName);
			}

			if (!cert.getChain().isEmpty()) {
				int i = cert.getChain().size();
				for (Certificate c : cert.getChain()) {
					i--;
					String chainAlias = aliasName + "_chain_" + i;
					auditCertificate(chainAlias, c);
					if (this.mode == ExecutionMode.CONFIG) {
						project.writeCertificate(chainAlias, c, Optional.empty());
						Audit.AUDIT_LOG.info("  chain certificate created: alias={}", chainAlias);						
					} else {
						Audit.AUDIT_LOG.info("  chain certificate skipped (due to execution mode {}): alias={}", this.mode, chainAlias);
					}
				}
			}
		}
	}

	private void auditCertificate(String alias, Certificate cert) {
		if (cert instanceof X509Certificate) {
			X509Certificate x509 = (X509Certificate) cert;
			Date expDate = x509.getNotAfter();
			String dn = x509.getSubjectX500Principal().getName();
			Audit.AUDIT_LOG.info("  X509: alias={}; dn={}; exp={}", alias, dn, expDate);

			auditExpiration(alias, dn, expDate);
		}
	}

	private void auditExpiration(String alias, String dn, Date expirationDate) {
		if (this.expirationFail.isPresent()) {
			if (expirationDate.before(this.expirationFail.get().getTime())) {
				Audit.AUDIT_LOG.fatal("  certificate '{}' expires at {}", alias, expirationDate);
				throw new CertificateExpiredException(alias, dn, expirationDate);
			}
		}
		if (this.expirationError.isPresent()) {
			if (expirationDate.before(this.expirationError.get().getTime())) {
				Audit.AUDIT_LOG.error("  certificate '{}' expires at {}", alias, expirationDate);
				return;
			}
		}
		if (this.expirationWarning.isPresent()) {
			if (expirationDate.before(this.expirationWarning.get().getTime())) {
				Audit.AUDIT_LOG.warn("  certificate '{}' expires at {}", alias, expirationDate);
				return;
			}
		}
	}
}
