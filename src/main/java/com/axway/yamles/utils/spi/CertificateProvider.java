package com.axway.yamles.utils.spi;

import java.io.File;
import java.util.Map;

public interface CertificateProvider {
	
	public String getName();
	public Cert getCertificate(File configSource, String aliasName, Map<String, String> config) throws CertificateProviderException;
}
