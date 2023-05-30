package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.helper.Mustache;
import com.axway.yamles.utils.spi.CertificateProviderException;
import com.axway.yamles.utils.spi.CertificateReplacement;
import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;

public class KeystoreCertificateProvider extends AbstractCertificateProvider {

	private static final Logger log = LogManager.getLogger(KeystoreCertificateProvider.class);

	public static final ConfigParameter CFG_PATH = new ConfigParameter("path", true, "Path to keystore file", Type.file,
			false);
	public static final ConfigParameter CFG_PASSPHRASE = new ConfigParameter("pass", false, "Passphrase for keystore",
			Type.string, false);
	public static final ConfigParameter CFG_ALIAS = new ConfigParameter("alias", false,
			"Alias of the certificate within the keystore. If not specified, the alias of the Entity Store certificate is used.",
			Type.string, false);
	public static final ConfigParameter CFG_TYPE = new ConfigParameter("type", false,
			"Type of the keystore (JKS, P12): If not specified will be determined by the file extension.", Type.string,
			false);

	public KeystoreCertificateProvider() {
		super(CFG_PATH, CFG_PASSPHRASE, CFG_ALIAS);
	}

	@Override
	public String getName() {
		return "keystore";
	}

	@Override
	public String getSummary() {
		return "Provides certificates from keystore file.";
	}

	@Override
	public String getDescription() {
		return "Provides certificates from keystore file (JKS or PKCS#12 format).";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		String path = getConfig(config, CFG_PATH, "");
		File keystoreFile = buildFile(configSource, path);
		if (!keystoreFile.exists()) {
			throw new CertificateProviderException("keystore file not found: " + keystoreFile.getAbsolutePath());
		}

		String passphrase = getConfig(config, CFG_PASSPHRASE, null);
		char[] password = null;
		if (passphrase != null) {
			passphrase = Mustache.eval(passphrase);
			password = passphrase.toCharArray();
		}

		String altAlias = getConfig(config, CFG_ALIAS, aliasName);
		if (altAlias != null) {
			aliasName = altAlias;
		}

		String type = getConfig(config, CFG_TYPE, "");
		if (type.isEmpty())
			type = determineType(keystoreFile);

		log.debug("searching for certificate alias '{}' in keystore '{}' of type '{}'", aliasName, path, type);

		try {
			KeyStore ks = KeyStore.getInstance(type);
			ks.load(new FileInputStream(keystoreFile), password);

			Certificate cert = ks.getCertificate(aliasName);
			if (cert == null) {
				throw new CertificateProviderException("certificate not found for alias: " + aliasName);
			}

			log.debug("certificate with alias '{}' found", aliasName);

			Key key = ks.getKey(aliasName, password);
			log.debug("key with alias '{}' {}found", aliasName, (key == null) ? "not " : "");

			return new CertificateReplacement(aliasName, cert, key);

		} catch (Exception e) {
			throw new CertificateProviderException("error on loading keystore: " + keystoreFile.getAbsolutePath(), e);
		}
	}

	private String determineType(File keystoreFile) throws CertificateProviderException {
		String type = "JKS";

		String name = keystoreFile.getName();
		int extIdx = name.lastIndexOf(".");
		if (extIdx > 0) {
			String ext = name.substring(extIdx + 1);
			if ("P12".equalsIgnoreCase(ext)) {
				type = "PKCS12";
			} else if ("JKS".equalsIgnoreCase(ext)) {
				type = "JKS";
			} else {
				throw new CertificateProviderException("unsupported keystore type: " + ext);
			}
		}

		return type;
	}
}
