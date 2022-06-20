package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.Map;

import com.axway.yamles.utils.spi.CertificateProvider;
import com.axway.yamles.utils.spi.CertificateProviderException;
import com.axway.yamles.utils.spi.CertificateReplacement;

public class RemoverCertificateProvider implements CertificateProvider {

	@Override
	public String getName() {
		return "remover";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		return new CertificateReplacement(aliasName);
	}

}
