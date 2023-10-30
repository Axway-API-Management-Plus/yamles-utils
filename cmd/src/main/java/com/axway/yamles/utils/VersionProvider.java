package com.axway.yamles.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

	@Override
	public String[] getVersion() throws Exception {
		Properties props = loadProperties();
		
		return new String[]{(String) props.get("version")};
	}

	private Properties loadProperties() throws IOException {
		Properties props = new Properties();
		InputStream in = VersionProvider.class.getResourceAsStream("/project.properties");
		props.load(in);
		
		return props;
	}
}
