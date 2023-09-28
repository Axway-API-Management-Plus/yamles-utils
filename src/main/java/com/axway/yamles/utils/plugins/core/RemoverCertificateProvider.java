package com.axway.yamles.utils.plugins.core;

import java.io.File;
import java.util.Map;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;

public class RemoverCertificateProvider extends AbstractCertificateProvider {

	public RemoverCertificateProvider() {
		super();
	}

	@Override
	public String getName() {
		return "remover";
	}

	@Override
	public String getSummary() {
		return "Removes a certificate from the entity store.";
	}

	@Override
	public String getDescription() {
		return "Removes the certificate, specified by the alias, from the entity store.";
	}

	@Override
	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		return new CertificateReplacement(aliasName);
	}

}
