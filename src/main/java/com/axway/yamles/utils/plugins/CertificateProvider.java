package com.axway.yamles.utils.plugins;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface CertificateProvider extends Provider {

	public List<ConfigParameter> getConfigParameters();

	public List<CertificateReplacement> getCertificates(File configSource, String aliasName, Map<String, String> config)
			throws CertificateProviderException;
}
