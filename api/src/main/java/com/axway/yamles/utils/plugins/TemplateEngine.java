package com.axway.yamles.utils.plugins;

import java.io.File;

public interface TemplateEngine {
	public default String evaluate(String template) throws TemplateEngineException {
		return template;
	}

	public default String evaluate(File template) throws TemplateEngineException {
		return template.getAbsolutePath();
	}
}