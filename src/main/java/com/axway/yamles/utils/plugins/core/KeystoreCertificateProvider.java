package com.axway.yamles.utils.plugins.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.axway.yamles.utils.plugins.ExecutionMode;

public class KeystoreCertificateProvider extends AbstractCertificateProvider {

	private static final String TYPE_JKS = "JKS";
	private static final String TYPE_P12 = "PKCS12";

	private static final Logger log = LogManager.getLogger(KeystoreCertificateProvider.class);

	public static final ConfigParameter CFG_PATH = new ConfigParameter("path", false, "Path to keystore file",
			Type.file, false);
	public static final ConfigParameter CFG_DATA = new ConfigParameter("data", false, "Base64 encoded keystore",
			Type.string, true);
	public static final ConfigParameter CFG_PASSPHRASE = new ConfigParameter("pass", false, "Passphrase for keystore",
			Type.string, true);
	public static final ConfigParameter CFG_ALIAS = new ConfigParameter("alias", false,
			"Regular expression matching the alias of the certificate within the keystore. If not specified, the alias name of the Entity Store certificate is used.",
			Type.string, false);
	public static final ConfigParameter CFG_TYPE = new ConfigParameter("type", false,
			"Type of the keystore (JKS, PKCS12). If not specified, PKCS12 is assumed.", Type.string, false);
	public static final ConfigParameter CFG_CHAIN = new ConfigParameter("chain", false, "Include certificate chain.",
			Type.bool, false);
	public static final ConfigParameter CFG_NOKEY = new ConfigParameter("nokey", false, "Suppress to add private key.",
			Type.bool, false);

	public KeystoreCertificateProvider() {
		super(CFG_PATH, CFG_DATA, CFG_PASSPHRASE, CFG_TYPE, CFG_ALIAS, CFG_CHAIN, CFG_NOKEY);
	}

	@Override
	public String getName() {
		return "keystore";
	}

	@Override
	public String getSummary() {
		return "Provides certificates from a keystore.";
	}

	@Override
	public String getDescription() {
		return "Provides certificates from keystore (JKS or PKCS#12 format). The keystore can be provided as a file or as Base64 encoded data.";
	}

	@Override
	public List<CertificateReplacement> getCertificates(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		String type = getConfig(CFG_TYPE, config, TYPE_P12);
		if (!TYPE_JKS.equals(type) && !TYPE_P12.equals(type)) {
			throw new CertificateProviderException(
					"invalid type '" + type + "'; must be " + TYPE_JKS + " or " + TYPE_P12);
		}

		String path = getConfig(CFG_PATH, config, null);
		String data = getConfig(CFG_DATA, config, null);
		if (path != null && data != null) {
			throw new CertificateProviderException("'" + CFG_PATH.getName() + "' and '" + CFG_DATA.getName()
					+ "' configuration are mutually exclusive");
		} else if (path == null && data == null) {
			throw new CertificateProviderException(
					"'" + CFG_PATH.getName() + "' or '" + CFG_DATA.getName() + "' configuration is required");
		}

		String passphrase = getConfig(CFG_PASSPHRASE, config, null);
		String aliasExpr = getConfig(CFG_ALIAS, config, aliasName);
		boolean addChain = getConfig(CFG_CHAIN, config, "false").equals("true");
		boolean nokey = getConfig(CFG_NOKEY, config, "false").equals("true");

		if (getMode() == ExecutionMode.SYNTAX_CHECK) {
			return Collections.emptyList();
		}

		String source = "data";
		InputStream dataStream = null;
		if (path != null) {
			File keystoreFile = buildFile(configSource, path);
			try {
				dataStream = new FileInputStream(keystoreFile);
				source = keystoreFile.getAbsolutePath();
			} catch (FileNotFoundException e) {
				throw new CertificateProviderException("keystore file not found: " + keystoreFile.getAbsolutePath());
			}
		} else if (data != null) {
			try {
				dataStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
			} catch (IllegalArgumentException e) {
				throw new CertificateProviderException("data is not a valid Base64 scheme");
			}
		} else {
			throw new IllegalStateException("illegal state on handling 'path' and 'data' configuration parameter");
		}

		char[] password = null;
		if (passphrase != null) {
			password = passphrase.toCharArray();
		}

		log.debug("searching for certificates '{}' in keystore '{}' of type '{}'", aliasExpr, path, type);

		try {
			KeyStore ks = KeyStore.getInstance(type);
			ks.load(dataStream, password);

			List<CertificateReplacement> crs = new ArrayList<>();

			Enumeration<String> aliases = ks.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();

				if (!alias.matches(aliasExpr)) {
					log.debug("certificate with alias {} doesn't match and will be ignored", alias);
					continue;
				}

				Certificate cert = ks.getCertificate(alias);
				log.debug("certificate with alias '{}' found", alias);

				Key key = ks.getKey(alias, password);
				log.debug("key for alias '{}' {}found", alias, (key == null) ? "not " : "");
				if (key != null && nokey) {
					key = null;
					log.debug("key for alias '{}' suppressed", alias);
				}

				CertificateReplacement cr = new CertificateReplacement(Optional.of(alias), cert, key);

				if (addChain) {
					Certificate[] chain = ks.getCertificateChain(alias);
					if (chain != null) {
						for (Certificate chainCert : chain) {
							if (chainCert == cert)
								continue;
							cr.addChain(chainCert);
						}
					}
				}
				crs.add(cr);
			}

			if (crs.isEmpty()) {
				throw new CertificateProviderException("certificates not found for alias: " + aliasName);
			}

			return crs;

		} catch (Exception e) {
			throw new CertificateProviderException("error on loading keystore: " + source, e);
		}
	}
}
