package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Map;

public interface CertificateProvider {

	public String getName();

	public default String getSummary() {
		return "dummy summary";
	}

	public default String getDescription() {
		return "dummy description";
	}

	public CertificateReplacement getCertificate(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException;
}
