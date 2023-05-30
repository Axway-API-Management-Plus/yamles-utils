package com.axway.yamles.utils.spi.impl;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.axway.yamles.utils.spi.ConfigParameter;
import com.axway.yamles.utils.spi.ConfigParameter.Type;
import com.axway.yamles.utils.spi.LookupDoc;
import com.axway.yamles.utils.spi.LookupProviderException;
import com.axway.yamles.utils.spi.LookupSource;

public class YamlLookupProvider extends AbstractLookupDocLookupProvider {
	public static final ConfigParameter CFG_PARAM_FILE = new ConfigParameter("file", true,
			"Path to YAML file containing lookup values.", Type.file, false);

	private static final Logger log = LogManager.getLogger(YamlLookupProvider.class);

	public YamlLookupProvider() {
		super(DESCR_KEY_JSONPOINTER, EMPTY_FUNC_ARGS, new ConfigParameter[] { CFG_PARAM_FILE }, log);
	}

	@Override
	public String getName() {
		return "yaml";
	}

	@Override
	public String getSummary() {
		return "Lookup values from YAML document files.";
	}

	@Override
	public String getDescription() {
		return "The key represents the JSON Pointer to the property containing the value (e.g. '/root/sub/key')";
	}

	@Override
	public void addSource(LookupSource source) throws LookupProviderException {
		File file = source.getFileFromRequiredParam(CFG_PARAM_FILE.getName());

		try {
			LookupDoc doc = LookupDoc.fromYamlFile(source.getAlias(), file);
			add(doc);
		} catch (Exception e) {
			throw new LookupProviderException(this, "error on loading lookup JSON file: " + file.getAbsolutePath(), e);
		}
	}
}
