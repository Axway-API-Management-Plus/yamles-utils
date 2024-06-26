package com.axway.yamles.utils.plugins;

import java.io.File;
import java.util.Objects;

public class Evaluator {
	private static final TemplateEngine DEFAULT = new TemplateEngine() {
	};

	private static Evaluator evaluator = new Evaluator(DEFAULT);

	public static void setDefaultTemplateEngine() {
		setTemplateEngine(DEFAULT);
	}

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

	public String evaluate(File template) throws TemplateEngineException {
		return this.engine.evaluate(template);
	}

	public static String eval(String template) throws TemplateEngineException {
		return evaluator.evaluate(template);
	}

	public static String eval(File template) throws TemplateEngineException {
		return evaluator.evaluate(template);
	}

}
