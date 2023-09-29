package com.axway.yamles.utils.plugins;

public interface TemplateEngine {
	public default String evaluate(String template) throws TemplateEngineException {
		return template;
	}
}