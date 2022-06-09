package com.axway.yamles.utils.spi.impl;

import java.io.File;
import java.util.Map;

import com.axway.yamles.utils.spi.CertificateProvider;
import com.axway.yamles.utils.spi.CertificateProviderException;

public abstract class AbstractCertificateProvider implements CertificateProvider {

	protected String getRequiredConfig(Map<String, String> config, String name) throws CertificateProviderException {
		String value = config.get(name);
		if (value == null) {
			throw new CertificateProviderException("missing configuration parameter: " + name);
		}
		return value;
	}

	protected File buildFile(File configSource, String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(configSource.getParentFile(), file.getPath());
		}
		return file;
	}
}
