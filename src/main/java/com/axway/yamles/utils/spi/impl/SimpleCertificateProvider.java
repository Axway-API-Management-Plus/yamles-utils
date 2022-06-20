package com.axway.yamles.utils.spi.impl;

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

import com.axway.yamles.utils.helper.Mustache;
import com.axway.yamles.utils.spi.CertificateReplacement;
import com.axway.yamles.utils.spi.CertificateProvider;
import com.axway.yamles.utils.spi.CertificateProviderException;

public class SimpleCertificateProvider implements CertificateProvider {
	public static final String CFG_CERT = "cert";
	public static final String CFG_KEY = "key";

	@Override
	public String getName() {
		return "simple";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		try {
			Certificate c = null;
			PrivateKey k = null;
			
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			byte[] data = getDecodedConfig(config, CFG_CERT);
			if (data == null) {
				throw new CertificateProviderException("missing configuration: " + CFG_CERT);
			}
			c = cf.generateCertificate(new ByteArrayInputStream(data));
			
			byte[] keyData = getDecodedConfig(config, CFG_KEY);
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
	
	private byte[] getDecodedConfig(Map<String, String> config, String name) {
		byte[] data = null;
		String value = getConfig(config, name);
		if (value != null) {
			data = Base64.getDecoder().decode(value);
		}
		return data;
	}

	private String getConfig(Map<String, String> config, String name) {
		String value = config.get(name);
		if (value != null) {
			value = Mustache.eval(value);
		}
		return value;
	}
}
