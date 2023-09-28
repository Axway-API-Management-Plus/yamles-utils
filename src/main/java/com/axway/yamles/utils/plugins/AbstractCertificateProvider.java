package com.axway.yamles.utils.plugins;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.axway.yamles.utils.helper.Mustache;

public abstract class AbstractCertificateProvider implements CertificateProvider {

	private final ParameterSet<ConfigParameter> configParams = new ParameterSet<>();

	protected AbstractCertificateProvider(ConfigParameter... param) {
		this.configParams.add(param);
	}

	protected String getConfig(ConfigParameter param, Map<String, String> config, String defaultValue)
			throws CertificateProviderException {
		String value = config.get(param.getName());
		if (value == null) {
			if (param.isRequired()) {
				throw new CertificateProviderException("missing configuration parameter: " + param.getName());
			}
			value = defaultValue;
		}
		if (value != null) {
			if (param.hasMustacheSupport()) {
				value = Mustache.eval(value);
			}
		}
		return value;
	}

	@Override
	public List<ConfigParameter> getConfigParameters() {
		return this.configParams.getParams();
	}

	protected File buildFile(File configSource, String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			configSource = configSource.getAbsoluteFile();
			file = new File(configSource.getParentFile(), file.getPath());
		}
		return file;
	}
}
