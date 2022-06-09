package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;

import com.axway.yamles.utils.helper.Mustache;
import com.axway.yamles.utils.spi.Cert;
import com.axway.yamles.utils.spi.CertificateProviderException;

public class KeystoreCertificateProvider extends AbstractCertificateProvider {

	public static final String CFG_PATH = "path";
	public static final String CFG_PASSPHRASE = "pass";
	public static final String CFG_ALIAS = "alias";

	@Override
	public String getName() {
		return "keystore";
	}

	@Override
	public Cert getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		String path = getRequiredConfig(config, CFG_PATH);
		File keystoreFile = buildFile(configSource, path);
		if (!keystoreFile.exists()) {
			throw new CertificateProviderException("keystore file not found: " + keystoreFile.getAbsolutePath());
		}

		String passphrase = config.get(CFG_PASSPHRASE);
		char[] password = null;
		if (passphrase != null) {
			passphrase = Mustache.eval(passphrase);
			password = passphrase.toCharArray();
		}

		String altAlias = config.get(CFG_ALIAS);
		if (altAlias != null) {
			aliasName = altAlias;
		}

		String type = determineType(keystoreFile);

		try {
			KeyStore ks = KeyStore.getInstance(type);
			ks.load(new FileInputStream(keystoreFile), password);

			Certificate cert = ks.getCertificate(aliasName);
			if (cert == null) {
				throw new CertificateProviderException("certificate not found for alias: " + aliasName);
			}

			Key key = ks.getKey(aliasName, password);

			return new Cert(aliasName, cert, key);

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
