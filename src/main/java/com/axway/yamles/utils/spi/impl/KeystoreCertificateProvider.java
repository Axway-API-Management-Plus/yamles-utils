package com.axway.yamles.utils.spi.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.CertificateProviderException;
import com.axway.yamles.utils.spi.CertificateReplacement;
import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;

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
			"Alias of the certificate within the keystore. If not specified, the alias of the Entity Store certificate is used.",
			Type.string, false);
	public static final ConfigParameter CFG_TYPE = new ConfigParameter("type", false,
			"Type of the keystore (JKS, PKCS12). If not specified, PKCS12 is assumed.", Type.string, false);
	public static final ConfigParameter CFG_CHAIN = new ConfigParameter("chain", false, "Include certificate chain.",
			Type.bool, false);
	public static final ConfigParameter CFG_NOKEY = new ConfigParameter("nokey", false, "Supporess to add private key.",
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
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		String type = getConfig(CFG_TYPE, config, TYPE_P12);
		if (!TYPE_JKS.equals(type) && !TYPE_P12.equals(type)) {
			throw new CertificateProviderException(
					"invalid type '" + type + "'; must be " + TYPE_JKS + " or " + TYPE_P12);
		}

		String path = getConfig(CFG_PATH, config, "");
		String data = getConfig(CFG_DATA, config, "");
		String source = "data";

		InputStream dataStream = null;
		if (!path.isEmpty()) {
			if (!data.isEmpty()) {
				throw new CertificateProviderException("'" + CFG_PATH.getName() + "' and '" + CFG_DATA.getName()
						+ "' configuration are mutually exclusive");
			}

			File keystoreFile = buildFile(configSource, path);
			try {
				dataStream = new FileInputStream(keystoreFile);
				source = keystoreFile.getAbsolutePath();
			} catch (FileNotFoundException e) {
				throw new CertificateProviderException("keystore file not found: " + keystoreFile.getAbsolutePath());
			}
		} else {
			if (data.isEmpty()) {
				throw new CertificateProviderException(
						"'" + CFG_PATH.getName() + "' or '" + CFG_DATA.getName() + "' configuration is required");
			}
			try {
				dataStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
			} catch (IllegalArgumentException e) {
				throw new CertificateProviderException("data is not a valid Base64 scheme");
			}
		}

		String passphrase = getConfig(CFG_PASSPHRASE, config, null);
		char[] password = null;
		if (passphrase != null) {
			password = passphrase.toCharArray();
		}

		String altAlias = getConfig(CFG_ALIAS, config, aliasName);
		if (altAlias != null) {
			aliasName = altAlias;
		}

		boolean addChain = getConfig(CFG_CHAIN, config, "false").equals("true");

		log.debug("searching for certificate alias '{}' in keystore '{}' of type '{}'", aliasName, path, type);

		try {
			KeyStore ks = KeyStore.getInstance(type);
			ks.load(dataStream, password);

			Certificate cert = ks.getCertificate(aliasName);
			if (cert == null) {
				throw new CertificateProviderException("certificate not found for alias: " + aliasName);
			}

			log.debug("certificate with alias '{}' found", aliasName);

			boolean nokey = getConfig(CFG_NOKEY, config, "false").equals("true");
			Key key = ks.getKey(aliasName, password);
			log.debug("key for alias '{}' {}found", aliasName, (key == null) ? "not " : "");
			if (!nokey && key != null) {
				key = null;
				log.debug("key for alias '{}' suppressed", aliasName);
			}

			CertificateReplacement cr = new CertificateReplacement(aliasName, cert, key);

			if (addChain) {
				Certificate[] chain = ks.getCertificateChain(aliasName);
				if (chain != null) {
					for (Certificate chainCert : chain) {
						if (chainCert == cert)
							continue;
						cr.addChain(chainCert);
					}
				}
			}

			return cr;
		} catch (Exception e) {
			throw new CertificateProviderException("error on loading keystore: " + source, e);
		}
	}
}
