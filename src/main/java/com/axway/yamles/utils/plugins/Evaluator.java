package com.axway.yamles.utils.plugins;

import java.util.Objects;

public class Evaluator {

	private static Evaluator evaluator = new Evaluator(new TemplateEngine() {
	});

	public static void setTemplateEngine(TemplateEngine engine) {
		Objects.requireNonNull(engine, "template engine required");
		evaluator = new Evaluator(engine);
	}

	public static Evaluator getInstance() {
		return evaluator;
	}

	private final TemplateEngine engine;

	private Evaluator(TemplateEngine engine) {
		this.engine = Objects.requireNonNull(engine, "template engine required");
	}

	public String evaluate(String template) throws TemplateEngineException {
		return this.engine.evaluate(template);
	}

	public static String eval(String template) throws TemplateEngineException {
		return evaluator.evaluate(template);
	}
}
