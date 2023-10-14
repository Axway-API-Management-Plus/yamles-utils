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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pf4j.Extension;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;
import com.axway.yamles.utils.plugins.ExecutionMode;

@Extension
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
	public List<CertificateReplacement> getCertificates(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		try {
			String strData = getConfig(CFG_CERT, config, null);
			if (strData == null) {
				throw new CertificateProviderException("missing configuration: " + CFG_CERT);
			}
			String strKey = getConfig(CFG_KEY, config, null);
			if (getMode() == ExecutionMode.SYNTAX_CHECK) {
				return Collections.emptyList();
			}

			byte[] data = Base64.getDecoder().decode(strData);
			byte[] keyData = (strKey != null) ? Base64.getDecoder().decode(strKey) : null;

			Certificate c = null;
			PrivateKey k = null;

			CertificateFactory cf = CertificateFactory.getInstance("X509");

			c = cf.generateCertificate(new ByteArrayInputStream(data));

			if (keyData != null) {
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keyData);
				k = kf.generatePrivate(ks);
			}

			return Arrays.asList(new CertificateReplacement(Optional.empty(), c, k));
		} catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CertificateProviderException("error on creating certificate", e);
		}
	}
}
