package com.axway.yamles.utils.plugins.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ConfigParameter;
import com.axway.yamles.utils.plugins.ConfigParameter.Type;

public class FileCertificateProvider extends AbstractCertificateProvider {

	public static final ConfigParameter CFG_PATH = new ConfigParameter("path", false, "Path to certificate file",
			Type.file, false);

	public FileCertificateProvider() {
		super(CFG_PATH);
	}

	@Override
	public String getName() {
		return "file";
	}

	@Override
	public String getSummary() {
		return "Provides certificate from file.";
	}

	@Override
	public String getDescription() {
		return "Provides certificate from specified file on disk.";
	}

	@Override
	public List<CertificateReplacement> getCertificates(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {

		String path = getConfig(CFG_PATH, config, "");
		if (path.isEmpty()) {
			throw new CertificateProviderException("missing configuration: " + CFG_PATH.getName());
		}
		File certFile = buildFile(configSource, path);

		try {
			InputStream in = new FileInputStream(certFile);

			CertificateFactory cf = CertificateFactory.getInstance("X509");
			Certificate c = cf.generateCertificate(in);
			return Arrays.asList(new  CertificateReplacement(Optional.empty(), c, null));
		} catch (CertificateException e) {
			throw new CertificateProviderException("error on creating certificate", e);
		} catch (FileNotFoundException e) {
			throw new CertificateProviderException("certificate file not found: " + certFile.getAbsolutePath());
		}
	}
}
