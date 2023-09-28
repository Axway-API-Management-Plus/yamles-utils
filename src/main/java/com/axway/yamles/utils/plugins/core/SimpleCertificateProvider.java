package com.axway.yamles.utils.plugins.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;

public class SimpleCertificateProvider extends AbstractCertificateProvider {
	public static final ConfigParameter CFG_CERT = new ConfigParameter("cert", true,
			"PEM encoded certificate (single line)", Type.string, true);
	public static final ConfigParameter CFG_KEY = new ConfigParameter("key", false,
			"PEM encoded private key (single line)", Type.string, true);

	public SimpleCertificateProvider() {
		super(CFG_CERT, CFG_KEY);
	}

	@Override
	public String getName() {
		return "simple";
	}

	@Override
	public String getSummary() {
		return "Provides certificates from configuration file.";
	}

	@Override
	public String getDescription() {
		return "Provides certificates directly from the configuration file of the certificate provider. Certificates and keys are specified in the Base64 encoded PEM format.";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		try {
			Certificate c = null;
			PrivateKey k = null;

			CertificateFactory cf = CertificateFactory.getInstance("X509");
			byte[] data = getDecodedConfig(CFG_CERT, config);
			if (data == null) {
				throw new CertificateProviderException("missing configuration: " + CFG_CERT);
			}
			c = cf.generateCertificate(new ByteArrayInputStream(data));

			byte[] keyData = getDecodedConfig(CFG_KEY, config);
			if (keyData != null) {
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyData);
				k = kf.generatePrivate(ks);
			}

			return new CertificateReplacement(aliasName, c, k);
		} catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CertificateProviderException("error on creating certificate", e);
		}
	}

	private byte[] getDecodedConfig(ConfigParameter param, Map<String, String> config)
			throws CertificateProviderException {
		byte[] data = null;
		String value = getConfig(param, config, null);
		if (value != null) {
			data = Base64.getDecoder().decode(value);
		}
		return data;
	}
}
