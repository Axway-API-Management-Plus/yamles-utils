package com.axway.yamles.utils.plugins.core;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.axway.yamles.utils.plugins.AbstractCertificateProvider;
import com.axway.yamles.utils.plugins.CertificateProviderException;
import com.axway.yamles.utils.plugins.CertificateReplacement;
import com.axway.yamles.utils.plugins.ExecutionMode;

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
	public List<CertificateReplacement> getCertificates(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException {
		if (getMode() == ExecutionMode.SYNTAX_CHECK)
			return Collections.emptyList();
		return Arrays.asList(new CertificateReplacement());
	}

}
